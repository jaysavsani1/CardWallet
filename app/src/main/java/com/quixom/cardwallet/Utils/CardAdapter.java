package com.quixom.cardwallet.Utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.quixom.cardwallet.R;
import com.quixom.cardwallet.library.SecretTextView;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Admin on 21-Dec-15.
 */
public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {

    static Context mContext;
    ArrayList<CardInfo> cardInfoArrayList = new ArrayList<CardInfo>();
    String TAG = "CARDWALLET";
    Resources res;
    private int lastPosition = -1;
    final Random random = new Random();

    public CardAdapter(Context context, ArrayList<CardInfo> arrayList) {
        Log.e(TAG, "in Adapter");
        this.mContext = context;
        this.cardInfoArrayList = arrayList;
        res = mContext.getResources();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        final CardInfo cardInfo = cardInfoArrayList.get(position);
        viewHolder.bankName.setText(cardInfo.getBankName());
        viewHolder.cardCategory.setText(cardInfo.getCategory());
        Typeface typeface = Typeface.createFromAsset(mContext.getAssets(), "cc.ttf");
        viewHolder.cardNumber.setTypeface(typeface);
        viewHolder.cardName.setTypeface(typeface);
        viewHolder.cardNumber.setText(cardInfo.getCardNumber());
        viewHolder.cardName.setText(cardInfo.getCardName());
        viewHolder.expiryDate.setText(cardInfo.getCardExpiry());
        setCardTypeImage(cardInfo.getCardType(), viewHolder);
        viewHolder.cvv.setText("CVV");
        viewHolder.cvv.setDuration(1100);


        viewHolder.cardNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation jumpAnimation = AnimationUtils.loadAnimation(mContext, R.anim.textview_click);
                view.startAnimation(jumpAnimation);
                Toast.makeText(mContext, "Number copied", Toast.LENGTH_SHORT).show();
            }
        });
        viewHolder.cardName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation jumpAnimation = AnimationUtils.loadAnimation(mContext, R.anim.textview_click);
                view.startAnimation(jumpAnimation);
                Toast.makeText(mContext, "Name copied", Toast.LENGTH_SHORT).show();
            }
        });
        viewHolder.cvv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewHolder.cvv.show();
                viewHolder.cvv.setText(cardInfo.getCardCVV());
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        viewHolder.cvv.show();
                        viewHolder.cvv.setText("CVV");
                    }
                }, 2000);
            }
        });
    }


    private void setCardTypeImage(String cardType, ViewHolder viewHolder) {
        Log.e(TAG, "card type : " + cardType);
        switch (cardType) {
            case "VISA":
                viewHolder.cardType.setImageResource(R.drawable.visa);
                break;
            case "MasterCard":
                viewHolder.cardType.setImageResource(R.drawable.master_card);
                break;
            case "Discover":
                viewHolder.cardType.setImageResource(R.drawable.discover);
                break;
            case "American Express":
                viewHolder.cardType.setImageResource(R.drawable.amex);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return cardInfoArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView bankName, cardCategory, cardNumber, expiryDate, cardName;
        SecretTextView cvv;
        ImageView cardType;
        CardView cardView;
        Animation openCVV;
        LinearLayout linearLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            bankName = (TextView) itemView.findViewById(R.id.cvl_tv_bankname);
            cardCategory = (TextView) itemView.findViewById(R.id.cvl_tv_card_category);
            cardNumber = (TextView) itemView.findViewById(R.id.cvl_tv_card_number);
            expiryDate = (TextView) itemView.findViewById(R.id.cvl_tv_expiry);
            cardName = (TextView) itemView.findViewById(R.id.cvl_tv_cardname);
            cvv = (SecretTextView) itemView.findViewById(R.id.cvl_tv_cvv);
            cardType = (ImageView) itemView.findViewById(R.id.cvl_iv_cardtype);
            cardView = (CardView) itemView.findViewById(R.id.cvl_cv);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.cv_ll_bg);
            openCVV = AnimationUtils.loadAnimation(mContext, R.anim.open);
            cvv.startAnimation(openCVV);
        }
    }

}
