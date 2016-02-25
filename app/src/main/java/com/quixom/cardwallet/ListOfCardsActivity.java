package com.quixom.cardwallet;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.quixom.cardwallet.Utils.CardAdapter;
import com.quixom.cardwallet.Utils.CardInfo;
import com.quixom.cardwallet.Utils.MyRecyclerView;
import com.quixom.cardwallet.library.AesCbcWithIntegrity;
import com.quixom.cardwallet.library.DBHelper;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;


public class ListOfCardsActivity extends AppCompatActivity implements View.OnClickListener {

    FloatingActionButton fab1, fab2;
    MyRecyclerView recyclerView;
    RecyclerView.LayoutManager mLayoutManager;

    String TAG = "CARDWALLET";

    ProgressDialog progressDialog;
    ArrayList<CardInfo> cardInfoArrayList;
    private List<FloatingActionMenu> menus = new ArrayList<>();
    private Handler mUiHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait");
        progressDialog.setCancelable(false);

        mLayoutManager = new LinearLayoutManager(this);
        cardInfoArrayList = new ArrayList<CardInfo>();


        //Floating button code
        fab1 = (FloatingActionButton) findViewById(R.id.fab_mini_1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab_mini_2);
        fab1.setOnClickListener(this);
        fab2.setOnClickListener(this);
        final FloatingActionMenu menuLabels = (FloatingActionMenu) findViewById(R.id.menu_label);
        menus.add(menuLabels);
        menuLabels.hideMenuButton(false);
        int delay = 5000;
        for (final FloatingActionMenu menu : menus) {
            mUiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    menu.showMenuButton(true);
                }
            }, delay);
        }
        recyclerView = (MyRecyclerView) findViewById(R.id.ahp_recyclervew);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(mLayoutManager);
        fetchCardInformation();
    }

    private void fetchCardInformation() {
        progressDialog.show();
        DBHelper dbHelper = new DBHelper(this);
        AesCbcWithIntegrity.SecretKeys keys = null;
        try {
            keys = AesCbcWithIntegrity.generateKeyFromPassword("1097", "quixomtechnology");
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

        Cursor cursor = dbHelper.getCard();
        Log.e(TAG, "cursor count : " + cursor.getCount());
        if (cursor.getCount() > 0) {
            Log.e(TAG, "total count : " + cursor.getColumnCount());
            if (cursor.moveToFirst()) {
                do {

                    AesCbcWithIntegrity.CipherTextIvMac categoryCipherText = new AesCbcWithIntegrity.CipherTextIvMac(cursor.getString(0));
                    AesCbcWithIntegrity.CipherTextIvMac cardTypeCipherText = new AesCbcWithIntegrity.CipherTextIvMac(cursor.getString(1));
                    AesCbcWithIntegrity.CipherTextIvMac numberCipherText = new AesCbcWithIntegrity.CipherTextIvMac(cursor.getString(2));
                    AesCbcWithIntegrity.CipherTextIvMac nameCipherText = new AesCbcWithIntegrity.CipherTextIvMac(cursor.getString(3));
                    AesCbcWithIntegrity.CipherTextIvMac expiryCipherText = new AesCbcWithIntegrity.CipherTextIvMac(cursor.getString(4));
                    AesCbcWithIntegrity.CipherTextIvMac cvvCipherText = new AesCbcWithIntegrity.CipherTextIvMac(cursor.getString(5));
                    AesCbcWithIntegrity.CipherTextIvMac bankNameCipherText = new AesCbcWithIntegrity.CipherTextIvMac(cursor.getString(6));
                    try {
                        CardInfo cardInfo = new CardInfo();
                        String category = AesCbcWithIntegrity.decryptString(categoryCipherText, keys);
                        String cardType = AesCbcWithIntegrity.decryptString(cardTypeCipherText, keys);
                        String cardNumber = AesCbcWithIntegrity.decryptString(numberCipherText, keys);
                        String cardName = AesCbcWithIntegrity.decryptString(nameCipherText, keys);
                        String cardExpiry = AesCbcWithIntegrity.decryptString(expiryCipherText, keys);
                        String cvv = AesCbcWithIntegrity.decryptString(cvvCipherText, keys);
                        String bankName = AesCbcWithIntegrity.decryptString(bankNameCipherText, keys);
                        cardInfo.setBankName(bankName);
                        cardInfo.setCategory(category);
                        cardInfo.setCardType(cardType);
                        cardInfo.setCardNumber(cardNumber);
                        cardInfo.setCardName(cardName);
                        cardInfo.setCardExpiry(cardExpiry);
                        cardInfo.setCardCVV(cvv);
                        cardInfoArrayList.add(cardInfo);

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (GeneralSecurityException e) {
                        e.printStackTrace();
                    }
                } while (cursor.moveToNext());
            }
        }
        cursor.close();
        CardAdapter cardAdapter = new CardAdapter(ListOfCardsActivity.this, cardInfoArrayList);

        recyclerView.setAdapter(cardAdapter);
        progressDialog.cancel();
    }

    @Override
    public void onClick(View view) {
        String text = "";
        switch (view.getId()) {
            case R.id.fab_mini_1:
                text = fab1.getLabelText();
                break;
            case R.id.fab_mini_2:
                text = fab2.getLabelText();
                break;
        }
        Intent intent = new Intent(ListOfCardsActivity.this, AddNewCardActivity.class);
        intent.putExtra("card_category", text);
        startActivity(intent);
    }
}
