package com.quixom.cardwallet.library;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

/**
 * Created by Admin on 10-Dec-15.
 */
public class DBHelper extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "CardWallet.db";
    private static final int DATABASE_VERSION = 1;
    Context mContext;
    String TAG = "CARDWALLET";
    ProgressDialog progressDialog;
    AesCbcWithIntegrity.SecretKeys keys;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
        progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage("Please wait , we're encrypting your data.");
        progressDialog.setCancelable(false);

        try {
            keys = AesCbcWithIntegrity.generateKeyFromPassword("1097", "quixomtechnology");
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    public void setCard(String category, String card_type, String number, String name, String expiry, String cvv) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        progressDialog.show();
        try {
            AesCbcWithIntegrity.CipherTextIvMac cipherCategory = AesCbcWithIntegrity.encrypt(category, keys);
            AesCbcWithIntegrity.CipherTextIvMac cipherCardType = AesCbcWithIntegrity.encrypt(card_type, keys);
            AesCbcWithIntegrity.CipherTextIvMac cipherNumber = AesCbcWithIntegrity.encrypt(number, keys);
            AesCbcWithIntegrity.CipherTextIvMac cipherName = AesCbcWithIntegrity.encrypt(name, keys);
            AesCbcWithIntegrity.CipherTextIvMac cipherExpiry = AesCbcWithIntegrity.encrypt(expiry, keys);
            AesCbcWithIntegrity.CipherTextIvMac cipherCVV = AesCbcWithIntegrity.encrypt(cvv, keys);
            contentValues.put("category", cipherCategory.toString());
            contentValues.put("card_type", cipherCardType.toString());
            contentValues.put("number", cipherNumber.toString());
            contentValues.put("name", cipherName.toString());
            contentValues.put("expiry", cipherExpiry.toString());
            contentValues.put("cvv", cipherCVV.toString());
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        db.insert("CardInformation", null, contentValues);
        db.close();
        Log.e(TAG, "new  card information added");
        progressDialog.dismiss();
    }

    public void getCard() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM CardInformation", null);
        progressDialog.setMessage("Fetching card information");
        progressDialog.show();
        if (cursor.moveToFirst()) {
            do {
                AesCbcWithIntegrity.CipherTextIvMac category = new AesCbcWithIntegrity.CipherTextIvMac(cursor.getString(0));
                AesCbcWithIntegrity.CipherTextIvMac cardType = new AesCbcWithIntegrity.CipherTextIvMac(cursor.getString(1));
                AesCbcWithIntegrity.CipherTextIvMac number = new AesCbcWithIntegrity.CipherTextIvMac(cursor.getString(2));
                AesCbcWithIntegrity.CipherTextIvMac name = new AesCbcWithIntegrity.CipherTextIvMac(cursor.getString(3));
                AesCbcWithIntegrity.CipherTextIvMac expiry = new AesCbcWithIntegrity.CipherTextIvMac(cursor.getString(4));
                AesCbcWithIntegrity.CipherTextIvMac cvv = new AesCbcWithIntegrity.CipherTextIvMac(cursor.getString(5));

                try {
                    Log.e(TAG, " category : " + AesCbcWithIntegrity.decryptString(category, keys));
                    Log.e(TAG, " type : " + AesCbcWithIntegrity.decryptString(cardType, keys));
                    Log.e(TAG, " number : " + AesCbcWithIntegrity.decryptString(number, keys));
                    Log.e(TAG, " name : " + AesCbcWithIntegrity.decryptString(name, keys));
                    Log.e(TAG, " expiry : " + AesCbcWithIntegrity.decryptString(expiry, keys));
                    Log.e(TAG, " CVV : " + AesCbcWithIntegrity.decryptString(cvv, keys));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        progressDialog.cancel();
    }
}
