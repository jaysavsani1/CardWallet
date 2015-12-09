package com.quixom.cardwallet;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.util.ArrayList;
import java.util.List;


public class HomePage extends AppCompatActivity implements View.OnClickListener {

    FloatingActionButton fab1, fab2;
    private List<FloatingActionMenu> menus = new ArrayList<>();
    private Handler mUiHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        fab1 = (FloatingActionButton) findViewById(R.id.fab_mini_1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab_mini_2);

        fab1.setOnClickListener(this);
        fab2.setOnClickListener(this);

        FloatingActionMenu menuLabels = (FloatingActionMenu) findViewById(R.id.menu_label);
        menus.add(menuLabels);

        menuLabels.hideMenuButton(false);

        int delay = 600;
        for (final FloatingActionMenu menu : menus) {
            mUiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    menu.showMenuButton(true);
                }
            }, delay);
        }
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
        Intent intent = new Intent(HomePage.this, AddCard.class);
        intent.putExtra("card_type", text);
        startActivity(intent);

        Toast.makeText(HomePage.this, "selected   :  " + text, Toast.LENGTH_SHORT).show();
    }
}
