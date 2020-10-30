package com.example.linuxremote;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

public class MqttHelper {

    private MqttAndroidClient mqttAndroidClient;
    private String server = "";
    final String clientId = Build.MODEL;
    final String sub = "keyReply";
    Context c;
    MainActivity mainActivity;

    private boolean retained = false;

        public void disconnect()
                      throws MqttException {
            MqttAndroidClient client = mqttAndroidClient;
            IMqttToken mqttToken = client.disconnect();
            mqttToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    Log.d("mqtt", "Successfully disconnected");
                }
                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    Log.d("mqtt", "Failed to disconnected " + throwable.toString());
                }
            });
        }

    public MqttHelper(Context context){
        c = context;

        mainActivity = new MainActivity();

        server = mainActivity.getAddress();


        mqttAndroidClient = new MqttAndroidClient(context, server, clientId);


        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                Log.w("mqtt", serverURI);
            }

            @Override
            public void connectionLost(Throwable cause) {



                Toast.makeText(c, "MQTTHELPER connection lost cause "+cause , Toast.LENGTH_SHORT).show();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.w("Mqtt", message.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });
        connect();
    }

    public void publishMessage(@NonNull String msg, int qos, @NonNull String topic)
            throws MqttException, UnsupportedEncodingException {
        byte[] encodedPayload ;
        encodedPayload = msg.getBytes("UTF-8");
        MqttMessage message = new MqttMessage(encodedPayload);
        message.setId(5866);
        message.setRetained(retained);
        message.setQos(qos);
        mqttAndroidClient.publish(topic, message);
    }


    public void setCallback(MqttCallbackExtended callback) {
        mqttAndroidClient.setCallback(callback);
    }

    private void connect(){
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);

        String user = mainActivity.getUserName();
        String pass = mainActivity.getPassword();


        try {
            mqttConnectOptions.setUserName(user);
            mqttConnectOptions.setPassword(pass.toCharArray());
        }catch (Exception e){
            Toast.makeText(c, "UserName and Password are missing.", Toast.LENGTH_SHORT).show();
        }

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    Log.w("mqtt", "Connected");
                    subscribeToTopic();
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Failed to connect to: " + server + exception.toString());
                    Toast.makeText(c, "Can't connect to machine.\n"+exception.toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }catch (MqttException ex){
            ex.printStackTrace();
        }
    }

    private void subscribeToTopic() {
        try {
            mqttAndroidClient.subscribe(sub, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w("Mqtt","Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Subscribed fail!");
                }
            });

        } catch (MqttException ex) {
            System.err.println("Exception while subscribing");
            ex.printStackTrace();
        }
    }
}
