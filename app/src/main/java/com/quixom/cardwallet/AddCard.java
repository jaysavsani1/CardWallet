package com.quixom.cardwallet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.quixom.cardwallet.library.CardValidCallback;
import com.quixom.cardwallet.library.CreditCard;
import com.quixom.cardwallet.library.CreditCardForm;
import com.quixom.cardwallet.library.TwoDigitsCardTextWatcher;


public class AddCard extends AppCompatActivity {

    Button btnSave;
    String TAG = "CARDWALLET";
    EditText editText;
    TwoDigitsCardTextWatcher twoDigitsCardTextWatcher;
    String cardNumber, cardType;
    CardValidCallback cardValidCallback = new CardValidCallback() {
        @Override
        public void cardValid(CreditCard card) {

            cardNumber = card.getCardNumber();
            cardType = card.getCardType().toString();

            Toast.makeText(AddCard.this, "Card valid and complete", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);

        editText = (EditText) findViewById(R.id.edit_text_expiry_date);
        twoDigitsCardTextWatcher = new TwoDigitsCardTextWatcher(editText);
        editText.addTextChangedListener(twoDigitsCardTextWatcher);


        final CreditCardForm zipForm = (CreditCardForm) findViewById(R.id.credit_card_form);
        zipForm.setOnCardValidCallback(cardValidCallback);

        btnSave = (Button) findViewById(R.id.btn_save_card);

        Intent intent = getIntent();
        String card_type = intent.getStringExtra("card_type");
        Log.e(TAG, "card type: " + card_type);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.e(TAG, ".......................");
                Log.e(TAG, "card type : " + cardType);
                Log.e(TAG, "card number : " + cardNumber);

            }
        });
    }

}
