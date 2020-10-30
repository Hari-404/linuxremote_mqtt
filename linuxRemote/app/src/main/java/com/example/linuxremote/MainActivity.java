package com.example.linuxremote;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SendData{

    RecyclerView recyclerView;

    MqttHelper mqttHelper;
    MyRecycler myRecycler;

    NSDHelper nsdHelper;
    ArrayList<String> name;
    ArrayList<String> ip;

    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView textViewRefresh;

    public static final String TAG = "NsdChat";

    public static String serverUrl = null;
    private String port = "1883";
    public static String userName = "";
    public static String password = "";

    SharedPreferences sharedPreferences;
    //public String serverUrl = "tcp://192.168.43.239:1883";
    //public String serverUrl = "tcp://192.168.0.12:1883";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //refresh = findViewById(R.id.refresh);
        textViewRefresh = findViewById(R.id.textView);
        swipeRefreshLayout = findViewById(R.id.swipelayout);

        Toolbar toolbar = findViewById(R.id.toolBarMainActivity);
        toolbar.setTitle("Linux Remote");
        toolbar.setTitleTextColor(Color.WHITE);

        setSupportActionBar(toolbar);

        sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE);

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        toggle.getDrawerArrowDrawable().setColor(Color.WHITE);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                int id = menuItem.getItemId();

                switch (id){
                    case R.id.settings:

                        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(intent);

                        break;
                }

                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });


        swipeRefreshLayout.setColorSchemeResources(R.color.refresh,R.color.refresh1,R.color.refresh2);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);

                        nsdHelper.discoverServices();

                        if(name != null) {
                            if (name.size() > 0)
                                textViewRefresh.setVisibility(View.INVISIBLE);
                            else textViewRefresh.setVisibility(View.VISIBLE);
                        }

                    }
                },2000);
            }
        });
    }

    public void recycler(ArrayList<String> name, ArrayList<String> ip){
        recyclerView = findViewById(R.id.recycler);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(layoutManager);

        myRecycler = new MyRecycler(this, name, ip);
        myRecycler.notifyDataSetChanged();
        recyclerView.setAdapter(myRecycler);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public String getAddress(){
        Log.w(TAG,"getAddress" + serverUrl);
        return serverUrl;
    }

    public String getUserName(){
        return userName;
    }

    public String getPassword(){
        return password;
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "Starting.");
        nsdHelper = new NSDHelper(this);
        //nsdHelper.initializeNsd();
        super.onStart();
    }

    @Override
    protected void onResume() {
        if(nsdHelper != null){
            nsdHelper.initializeNsd();
        }
        try {
            nsdHelper.discoverServices();
        } catch (Exception e) {
            Toast.makeText(this, "Swipe down to load service.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "Pausing.");
        if (nsdHelper != null) {
            nsdHelper.stopDiscovery();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "Being stopped.");
        nsdHelper.tearDown();
        nsdHelper = null;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Being destroyed.");
        super.onDestroy();
    }

    @Override
    public void send(final ArrayList<String> s) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                int size = s.size();

                Log.d(TAG, "run: "+s);
                name = new ArrayList<>();
                ip = new ArrayList<>();

                for (int j = 0;j<size;j++){
                    String[] arr = s.get(j).split(" ");
                    name.add(j, arr[0]);
                    ip.add(j, arr[1]);
                }

                if(name.size() > 0)
                    textViewRefresh.setVisibility(View.INVISIBLE);
                else textViewRefresh.setVisibility(View.VISIBLE);

                recycler(name, ip);
            }
        });
    }

    @Override
    public void connectToService(final String ip) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Connecting to "+ip, Toast.LENGTH_SHORT).show();
                connect(ip);
            }
        });
    }


    public void connect(String ipAddress){
        Log.i(TAG, "connect: "+ipAddress);

        port = sharedPreferences.getString("port", "2511");
        userName = sharedPreferences.getString("userName", "");
        password = sharedPreferences.getString("password", "");

        serverUrl = "tcp://" + ipAddress+":"+port;
        Log.i(TAG, "connect: "+serverUrl);
        //String address = editText.getText().toString();

        //mqttHelper = new MqttHelper(getApplicationContext());

        /*Toast.makeText(MainActivity.this, "Connection success", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this, Second.class);
        startActivity(intent);*/

        mqttHelper = new MqttHelper(getBaseContext());
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI){
                Log.w("mqtt", serverURI);
                Toast.makeText(MainActivity.this, "Connection success", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, Second.class);
                startActivity(intent);
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.w("mqtt", cause);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.w("mqtt", ""+token);
            }
        });

    }

    @Override
    public void removeItem(final int position) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {

                name.remove(position);
                ip.remove(position);
                if(name.size() > 0)
                    textViewRefresh.setVisibility(View.INVISIBLE);
                else textViewRefresh.setVisibility(View.VISIBLE);
                myRecycler.notifyItemRemoved(position);
                myRecycler.notifyItemRangeChanged(position, name.size());

            }
        });
    }
}


/*
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serverUrl = "tcp://" + editText.getText().toString()+":1883";
                Log.w("mqtt", serverUrl);
                //String address = editText.getText().toString();

                mqttHelper = new MqttHelper(getApplicationContext());
                mqttHelper.setCallback(new MqttCallbackExtended() {
                    @Override
                    public void connectComplete(boolean reconnect, String serverURI) {
                        Log.w("mqtt", serverURI);
                        Intent intent = new Intent(MainActivity.this, Second.class);
                        startActivity(intent);
                    }

                    @Override
                    public void connectionLost(Throwable cause) {
                        Log.w("mqtt", cause);
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        Toast.makeText(MainActivity.this, ""+message.toString(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                        Log.w("mqtt", ""+token);
                    }
                });
            }
        });
        */
