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
    AesCbcWithIntegrity.SecretKeys keys;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;

        try {
            keys = AesCbcWithIntegrity.generateKeyFromPassword("1097", "quixomtechnology");
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    public void setCard(String category, String card_type, String number, String name, String expiry, String cvv) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
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
    }

    public Cursor getCard() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM CardInformation", null);
        return cursor;
    }
}
