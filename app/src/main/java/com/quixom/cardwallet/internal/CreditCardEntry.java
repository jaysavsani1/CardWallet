package com.quixom.cardwallet.internal;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.quixom.cardwallet.R;
import com.quixom.cardwallet.fields.CreditCardText;
import com.quixom.cardwallet.fields.CreditEntryFieldBase;
import com.quixom.cardwallet.library.CardType;
import com.quixom.cardwallet.library.CardValidCallback;
import com.quixom.cardwallet.library.CreditCard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressLint("ViewConstructor")
public class CreditCardEntry extends HorizontalScrollView implements
        OnTouchListener, OnGestureListener, CreditCardFieldDelegate {

    String TAG = "CARDWALLET";
    private Context context;
    // null textColor means we want to use the system default color instead of providing our own.
    private Integer textColor;
    private CreditCardText creditCardText;

    private TextView textFourDigits;
    private ImageView cardImage;
    private ImageView backCardImage;
    private Map<CreditEntryFieldBase, CreditEntryFieldBase> nextFocusField = new HashMap<>(4);
    private Map<CreditEntryFieldBase, CreditEntryFieldBase> prevFocusField = new HashMap<>(4);
    private List<CreditEntryFieldBase> includedFields = new ArrayList<>(4);
    private TextView textHelper;

    private boolean showingBack;
    private boolean scrolling = false;

    private CardValidCallback onCardValidCallback;


    public CreditCardEntry(Context context, AttributeSet attrs, @SuppressWarnings("UnusedParameters") int style) {
        super(context);


        this.context = context;

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CreditCardForm, 0, 0);
        if (!typedArray.getBoolean(R.styleable.CreditCardForm_default_text_colors, false)) {
            textColor = typedArray.getColor(R.styleable.CreditCardForm_text_color, Color.BLACK);
        } else {
            textColor = null;
        }
        typedArray.recycle();

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        int width;

        if (android.os.Build.VERSION.SDK_INT < 13) {
            width = display.getWidth(); // deprecated
        } else {
            Point size = new Point();
            display.getSize(size);
            width = size.x;
        }

        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        setLayoutParams(params);
        this.setHorizontalScrollBarEnabled(false);
        this.setOnTouchListener(this);

        LinearLayout container = new LinearLayout(context);
        container.setId(R.id.cc_entry_internal);
        container.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        container.setOrientation(LinearLayout.HORIZONTAL);

        creditCardText = new CreditCardText(context, attrs);
        creditCardText.setId(R.id.cc_card);
        creditCardText.setDelegate(this);
        creditCardText.setWidth(width);
        container.addView(creditCardText);
        includedFields.add(creditCardText);
        CreditEntryFieldBase currentField = creditCardText;

        textFourDigits = new TextView(context);
        textFourDigits.setTextSize(20);
        if (textColor != null) {
            textFourDigits.setTextColor(textColor);
        }
        container.addView(textFourDigits);

        nextFocusField.put(currentField, null);

        this.addView(container);

        // when the user taps the last 4 digits of the card, they probably want to edit it
        textFourDigits.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                focusOnField(creditCardText);
            }
        });
    }


    @Override
    public void onCardTypeChange(CardType type) {
        cardImage.setImageResource(type.frontResource);
        backCardImage.setImageResource(type.backResource);
        updateCardImage(false);
    }

    @Override
    public void onCreditCardNumberValid(String remainder) {
        nextField(this.creditCardText, remainder);

        updateLast4();
    }


    @Override
    protected void dispatchSaveInstanceState(@NonNull SparseArray<Parcelable> container) {
        dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(@NonNull SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).restoreHierarchyState(ss.childrenStates);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.childrenStates = new SparseArray();
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).saveHierarchyState(ss.childrenStates);
        }
        return ss;
    }

    @Override
    public void onBadInput(final EditText field) {
        Animation shake = AnimationUtils.loadAnimation(context, R.anim.shake);
        field.startAnimation(shake);
        field.setTextColor(Color.RED);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (textColor != null) {
                    field.setTextColor(textColor);
                }
            }
        }, 1000);
    }

    @Override
    public void setOnFocusChangeListener(OnFocusChangeListener l) {
        creditCardText.setOnFocusChangeListener(l);
    }

    public void focusOnField(final CreditEntryFieldBase field) {
        focusOnField(field, null);
    }

    public void focusOnField(final CreditEntryFieldBase field, String initialFieldValue) {
        field.requestFocus();
        if (!scrolling) {
            scrolling = true;
            scrollToTarget(field instanceof CreditCardText ? 0 : field.getLeft(), new Runnable() {
                @Override
                public void run() {
                    scrolling = false;
                    // if there was another focus before we were done.. catch up.
                    if (!field.hasFocus()) {
                        View newFocus = getFocusedChild();
                        if (newFocus instanceof CreditEntryFieldBase) {
                            focusOnField((CreditEntryFieldBase) newFocus);
                        }
                    }
                }
            });
        }

        if (initialFieldValue != null && initialFieldValue.length() > 0) {
            field.formatAndSetText(initialFieldValue);
        }

        if (this.textHelper != null) {
            this.textHelper.setText(field.helperText());
        }


        field.setSelection(field.getText().length());
    }

    private void scrollToTarget(int target, final Runnable after) {
        int scrollX = getScrollX();
        if (scrollX == target) {
            if (after != null) after.run();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                ValueAnimator realSmoothScrollAnimation = ValueAnimator.ofInt(scrollX, target).setDuration(500);
                realSmoothScrollAnimation.setInterpolator(new DecelerateInterpolator());
                realSmoothScrollAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        scrollTo((Integer) animation.getAnimatedValue(), 0);
                    }
                });

                realSmoothScrollAnimation.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (after != null) after.run();
                    }
                });
                realSmoothScrollAnimation.start();
            } else {
                smoothScrollTo(target, 0);
                if (after != null) after.run();
            }
        }
    }

    @Override
    public void focusOnPreviousField(CreditEntryFieldBase field) {
        CreditEntryFieldBase view = prevFocusField.get(field);
        if (view != null) {
            focusOnField(view);
        }
    }

    public void setCardNumberHint(String hint) {
        creditCardText.setHint(hint);
    }

    /**
     * set the card number will auto focus next field if param is true
     */
    public void setCardNumber(String cardNumber, boolean nextField) {
        setValue(this.creditCardText, cardNumber, nextField);
    }

    public void setCardImageView(ImageView image) {
        cardImage = image;
    }


    @SuppressWarnings("unused")
    public ImageView getBackCardImage() {
        return backCardImage;
    }

    public void setBackCardImage(ImageView backCardImage) {
        this.backCardImage = backCardImage;
    }

    @SuppressWarnings("unused")
    public TextView getTextHelper() {
        return textHelper;
    }

    public void setTextHelper(TextView textHelper) {
        this.textHelper = textHelper;
    }

    public boolean isCreditCardValid() {
        for (CreditEntryFieldBase includedField : includedFields) {
            if (!includedField.isValid()) {
                Log.e("CARDWALLET", "checking : " + !includedField.isValid());
                return true;
            }
        }
        return true;
    }

    private void setValue(CreditEntryFieldBase fieldToSet, String value, boolean nextField) {
        CreditCardFieldDelegate delegate = null;
        if (!nextField) {
            delegate = fieldToSet.getDelegate();
            // temp delegate that only deals with type.. this sucks.. TODO gut this delegate business
            fieldToSet.setDelegate(getDelegate(delegate));
        }

        fieldToSet.setText(value);

        if (delegate != null) {
            fieldToSet.setDelegate(delegate);
        }
    }

    private CreditCardFieldDelegate getDelegate(final CreditCardFieldDelegate delegate) {
        return new CreditCardFieldDelegate() {
            @Override
            public void onCardTypeChange(CardType type) {
                delegate.onCardTypeChange(type);
            }

            @Override
            public void onCreditCardNumberValid(String remainder) {
                updateLast4();
            }

            @Override
            public void onBadInput(EditText field) {
                delegate.onBadInput(field);
            }

            @Override
            public void focusOnField(CreditEntryFieldBase field, String initialValue) {
            }

            @Override
            public void focusOnPreviousField(CreditEntryFieldBase field) {
            }
        };
    }

    public void clearAll() {
        creditCardText.setText("");
        creditCardText.clearFocus();
        scrollTo(0, 0);
    }

    public CreditCard getCreditCard() {
        Log.e(TAG , "text : " + creditCardText.getText());
        Log.e(TAG, " type : " + creditCardText.getType());
        return new CreditCard(creditCardText.getText().toString(),
                creditCardText.getType());
    }

    /**
     * request focus for the credit card field
     */
    public void focusCreditCard() {
        focusOnField(creditCardText);
    }


    private void updateLast4() {
        String number = creditCardText.getText().toString();
        int length = number.length();
        String digits = number.substring(length - 4);
        textFourDigits.setText(digits);
    }

    private void nextField(CreditEntryFieldBase currentField, String initialFieldValue) {
        CreditEntryFieldBase next = nextFocusField.get(currentField);
        if (next == null) {
            entryComplete(currentField);
        } else {
            focusOnField(next, initialFieldValue);
        }
    }

    private void entryComplete(View clearField) {
        hideKeyboard();
        clearField.clearFocus();
        if (onCardValidCallback != null) onCardValidCallback.cardValid(getCreditCard());
    }

    private void updateCardImage(boolean back) {
        if (showingBack != back) {
            flipCardImage();
        }

        showingBack = back;
    }

    private void flipCardImage() {
        FlipAnimator animator = new FlipAnimator(cardImage, backCardImage);
        if (cardImage.getVisibility() == View.GONE) {
            animator.reverse();
        }
        cardImage.startAnimation(animator);
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void setOnCardValidCallback(CardValidCallback onCardValidCallback) {
        this.onCardValidCallback = onCardValidCallback;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR
                = ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in, ClassLoader loader) {
                return new SavedState(in, loader);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        });
        SparseArray childrenStates;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in, ClassLoader classLoader) {
            super(in);
            childrenStates = in.readSparseArray(classLoader);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeSparseArray(childrenStates);
        }
    }
}
