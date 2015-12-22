package com.quixom.cardwallet;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.quixom.cardwallet.library.SecretTextView;


public class SplashActivity extends AppCompatActivity  {

//    SecretTextView secretTextView;
    String TAG = "CARDWALLET";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        /*Typeface tf = Typeface.createFromAsset(getAssets(), "cc.ttf");
        textView.setTypeface(tf);

        secretTextView = (SecretTextView) findViewById(R.id.cvl_tv_cvv);
        secretTextView.setText("CVV");
        secretTextView.setDuration(1100);
        secretTextView.setOnClickListener(this);
        textView1.setOnClickListener(this);*/


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    Intent intent = new Intent(SplashActivity.this, ListOfCardsActivity.class);
                    startActivity(intent);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

  /*  @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cvl_tv_cvv:
                secretTextView.show();
                secretTextView.setText("383");
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        secretTextView.show();
                        secretTextView.setText("CVV");
                    }
                }, 3000);
                break;
        }
    }*/


}
