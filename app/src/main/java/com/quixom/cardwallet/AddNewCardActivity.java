package com.quixom.cardwallet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.quixom.cardwallet.library.CardValidCallback;
import com.quixom.cardwallet.library.CreditCard;
import com.quixom.cardwallet.library.CreditCardForm;
import com.quixom.cardwallet.library.DBHelper;
import com.quixom.cardwallet.library.TwoDigitsCardTextWatcher;


public class AddNewCardActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnSave;
    String TAG = "CARDWALLET";
    EditText etExpirtyDate, etName, etCVV;
    TwoDigitsCardTextWatcher twoDigitsCardTextWatcher;
    String cardNumber, cardType, cipherTextString, card_category;
    CardValidCallback cardValidCallback = new CardValidCallback() {
        @Override
        public void cardValid(CreditCard card) {
            cardNumber = card.getCardNumber();
            cardType = card.getCardType().toString();
            Toast.makeText(AddNewCardActivity.this, "Card valid and complete", Toast.LENGTH_SHORT).show();
        }
    };
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);

        // declaring all edit text forms
        etExpirtyDate = (EditText) findViewById(R.id.edit_text_expiry_date);
        etName = (EditText) findViewById(R.id.edit_text_name);
        etCVV = (EditText) findViewById(R.id.edit_text_cvv);
        btnSave = (Button) findViewById(R.id.btn_save_card);
        btnSave.setOnClickListener(this);

        Intent intent = getIntent();
        card_category = intent.getStringExtra("card_category");

        twoDigitsCardTextWatcher = new TwoDigitsCardTextWatcher(etExpirtyDate);
        etExpirtyDate.addTextChangedListener(twoDigitsCardTextWatcher);

        final CreditCardForm creditCardForm = (CreditCardForm) findViewById(R.id.credit_card_form);
        creditCardForm.setOnCardValidCallback(cardValidCallback);

        dbHelper = new DBHelper(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_save_card: {

                if (etCVV.getText().length() <= 2)
                    etCVV.setError("Error");
                if (etExpirtyDate.length() <= 4)
                    etExpirtyDate.setError("Error");

                dbHelper.setCard(card_category, cardType, cardNumber, etName.getText().toString(), etExpirtyDate.getText().toString(), etCVV.getText().toString());
                Intent intent = new Intent(AddNewCardActivity.this, ListOfCardsActivity.class);
                startActivity(intent);
                break;
            }
        }
    }
}
