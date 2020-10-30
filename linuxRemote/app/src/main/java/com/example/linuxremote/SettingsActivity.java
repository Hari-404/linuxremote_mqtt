package com.example.linuxremote;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;


public class SettingsActivity extends AppCompatActivity {

    EditText userName;
    EditText password;
    EditText port;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.settingsToolbar);
        toolbar.setNavigationIcon(R.drawable.arrow_left_white_24dp);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsActivity.this.finish();
            }
        });

        SharedPreferences sp =  this.getSharedPreferences("Settings", MODE_PRIVATE);
        editor = sp.edit();
        userName = findViewById(R.id.userName);
        password = findViewById(R.id.password);
        port = findViewById(R.id.port);
    }

    @Override
    protected void onPause() {

        String name, psswd, p;

        name = userName.getText().toString();
        psswd = password.getText().toString();

        p = port.getText().toString();

        if(!(name.isEmpty()) && !(psswd.isEmpty())){
            editor.putString("userName", name);
            editor.putString("password", psswd);
            editor.commit();
        }

        if(!p.isEmpty()){
            editor.putString("port", p);
            editor.commit();
        }

        super.onPause();
    }
}
