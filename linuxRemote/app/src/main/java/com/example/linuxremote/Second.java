package com.example.linuxremote;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.ViewConfiguration;


import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

public class Second extends AppCompatActivity {

    private static final String TAG = "Gestures";

    /**
     * Mouse vairables
     */
    private GestureDetector mDetector;  //p
    View.OnTouchListener listener;
    View view;
    float x = 0f, y = 0f;
    private static final int TAP_TIMEOUT = ViewConfiguration.getTapTimeout() + 100; //p
    private long mFirstDownTime = 0;
    private boolean mIsGestureHandled;
    private boolean drag = false;

    enum ClickType {
        RIGHT, MIDDLE
    }

    private static float MinDistanceToSendScroll = 3.0f; // 1.5f p
    private final static float StandardDpi = 240.0f;    //p
    int i;
    float x1;
    float y1;
    private float mPrevX;
    private float mPrevY;
    private float mCurrentX;
    private float mCurrentY;
    //private float mCurrentSensitivity;

    private float mCurrentSensitivity = 3.0f;

    private float displayDpiMultiplier; //p
    private int scrollDirection = 1;    //p
    private boolean isScrolling = false;
    private float accumulatedDistanceY = 0; //p
    private PointerAccelerationProfile mPointerAccelerationProfile; //p
    private PointerAccelerationProfile.MouseDelta mouseDelta;
    private ClickType doubleTapAction, tripleTapAction; //p


    /**
     * KeyBoard vairables
     */
    private KeyboardView mKeyboardView; //p
    private Keyboard mKeyboard; //p
    private Keyboard symbolKeyBoard;    //p
    private Keyboard arrowKeyBoard; //p
    private boolean symbol = false; //p
    private boolean arrows = false; //p
    private Vibrator vibrator;
    private TextView shift, ctrl, alt, trackPadHelp;
    private ImageButton imageButton;
    MqttHelper mqttHelper;
    private byte caps = 0;
    private Toolbar toolbar;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.system_commands, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setCancelable(true);
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });



        if (id == R.id.power_off) {
            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(Second.this, "Power Off.", Toast.LENGTH_SHORT).show();

                    try {
                        mqttHelper.publishMessage("263", 0, "key");
                    } catch (MqttException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            });
            AlertDialog dialog = alertDialog.create();
            dialog.setTitle("Power Off");
            dialog.setMessage("Do you want to power off?");
            dialog.show();
        } else if (id == R.id.restart) {
            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(Second.this, "Restart.", Toast.LENGTH_SHORT).show();

                    try {
                        mqttHelper.publishMessage("264", 0, "key");
                    } catch (MqttException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                }
            });
            AlertDialog dialog = alertDialog.create();
            dialog.setTitle("Restart");
            dialog.setMessage("Do you want to restart?");
            dialog.show();

        }

        else if(id == R.id.mouseSensitivity){

            Dialog sensitivityDialog = new Dialog(Second.this/*, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen*/);
            sensitivityDialog.setCancelable(true);
            sensitivityDialog.setContentView(R.layout.seek_bar_layout);
            SeekBar seekBar = sensitivityDialog.findViewById(R.id.seekBar);
            Window window = sensitivityDialog.getWindow();
            window.setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);

            seekBar.setProgress((int) mCurrentSensitivity);

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    int i = seekBar.getProgress();
                    mCurrentSensitivity = i;
                }
            });

            sensitivityDialog.show();

        }

        return super.onOptionsItemSelected(item);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        toolbar = findViewById(R.id.toolBarSecondActivity);
        toolbar.setTitle("Linux Remote");
        toolbar.setTitleTextColor(Color.WHITE);
        imageButton = findViewById(R.id.toolBarImageButton);

        toolbar.setNavigationIcon(R.drawable.arrow_left_white_24dp);

        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exit();
            }
        });

        toolbar.setOverflowIcon(getDrawable(R.drawable.menu_overflow));

        shift = findViewById(R.id.shift);
        ctrl = findViewById(R.id.ctrl);
        alt = findViewById(R.id.alt);
        trackPadHelp = findViewById(R.id.track_pad_help);
        trackPadHelp.setText(getString(R.string.mouse_help));
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        i = 0;
        x1 = 0;
        y1 = 0;

        /**
         *
         *
         * Mouse function code starts
         *
         *
         * */
        view = findViewById(R.id.myView);
        mDetector = new GestureDetector(this, new GalleryGestureDetector());

        listener = new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {


                int action = event.getAction();
                switch (action & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        mIsGestureHandled = false;
                        mFirstDownTime = event.getEventTime();
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        int count = event.getPointerCount();
                        if (event.getEventTime() - mFirstDownTime <= TAP_TIMEOUT) {
                            if (count == 3) {
                                if (!mIsGestureHandled) {
                                    onTripleFingerTap(event);
                                }
                            } else if (count == 2) {
                                if (!mIsGestureHandled) {
                                    onDoubleFingerTap(event);
                                }
                            }
                        }
                        mFirstDownTime = 0;
                }

                int actionType = event.getAction();
                    if (isScrolling) {
                        if (actionType == MotionEvent.ACTION_UP) {
                            isScrolling = false;
                        } else {
                            return false;
                        }
                    }
                    switch (actionType) {
                        case MotionEvent.ACTION_DOWN:
                            mPrevX = event.getX();
                            mPrevY = event.getY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            mCurrentX = event.getX();
                            mCurrentY = event.getY();
                            float deltaX = (mCurrentX - mPrevX) * displayDpiMultiplier * mCurrentSensitivity;
                            float deltaY = (mCurrentY - mPrevY) * displayDpiMultiplier * mCurrentSensitivity;
                            mPointerAccelerationProfile.touchMoved(deltaX, deltaY, event.getEventTime());
                            mouseDelta = mPointerAccelerationProfile.commitAcceleratedMouseDelta(mouseDelta);
                            Log.d(TAG, "onTouch: x = " + mouseDelta.x + "y = " + mouseDelta.y);
                            if (drag) {

                                deltaX = (mCurrentX - mPrevX) * displayDpiMultiplier * (mCurrentSensitivity + 1.0f);
                                deltaY = (mCurrentY - mPrevY) * displayDpiMultiplier * (mCurrentSensitivity + 1.0f);

                                mPointerAccelerationProfile.touchMoved(deltaX, deltaY, event.getEventTime());
                                mouseDelta = mPointerAccelerationProfile.commitAcceleratedMouseDelta(mouseDelta);
                                if ((mouseDelta.x != 0.0) || (mouseDelta.y != 0.0)) {
                                    x = mouseDelta.x;
                                    y = mouseDelta.y;
                                    try {
                                        int n = 5;
                                        if ((mouseDelta.x != 0.0) || (mouseDelta.y != 0.0)) {
                                            if (i < n) {
                                                x1 += mouseDelta.x;
                                                y1 += mouseDelta.y;
                                                i++;
                                            } else {
                                                x1 = x1 / n;
                                                y1 = y1 / n;
                                                i = 0;
                                                mqttHelper.publishMessage("m d " + x1 + " " + y1, 0, "key");
                                                x1 = y1 = 0;
                                            }
                                        }
                                    } catch (MqttException e1) {
                                        e1.printStackTrace();
                                    } catch (UnsupportedEncodingException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            } else {
                                try {
                                    int n = 5;
                                    if ((mouseDelta.x != 0.0) || (mouseDelta.y != 0.0)) {
                                        if (i < n) {
                                            x1 += mouseDelta.x;
                                            y1 += mouseDelta.y;
                                            i++;
                                        } else {
                                            x1 = x1 / n;
                                            y1 = y1 / n;
                                            i = 0;
                                            mqttHelper.publishMessage("m x " + x1 + " " + y1, 0, "key");
                                            x1 = y1 = 0;
                                        }
                                    }
                                } catch (MqttException e1) {
                                    e1.printStackTrace();
                                } catch (UnsupportedEncodingException e1) {
                                    e1.printStackTrace();
                                }
                            }
                            mPrevX = mCurrentX;
                            mPrevY = mCurrentY;
                            break;

                    }
                    return mDetector.onTouchEvent(event);
                }
            };
        view.setOnTouchListener(listener);
            mPointerAccelerationProfile =PointerAccelerationProfileFactory.getProfileWithName("medium");
            doubleTapAction =ClickType.RIGHT;
            tripleTapAction =ClickType.MIDDLE;
            displayDpiMultiplier =StandardDpi / getResources().getDisplayMetrics().xdpi;

            /*switch("fastest"){
                case "slowest":
                    mCurrentSensitivity = 0.2f;
                    break;
                case "aboveSlowest":
                    mCurrentSensitivity = 0.5f;
                    break;
                case "default":
                    mCurrentSensitivity = 1.0f;
                    break;
                case "aboveDefault":
                    mCurrentSensitivity = 1.5f;
                    break;
                case "fastest":
                    //mCurrentSensitivity = 2.0f;
                    mCurrentSensitivity = 3.0f;
                    break;
                default:
                    mCurrentSensitivity = 1.0f;
            }*/

            /*final View decorView = getWindow().getDecorView();

        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener()

            {
                @Override
                public void onSystemUiVisibilityChange ( int visibility){
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {

                    int fullscreenType = 0;

                    fullscreenType |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

                    Second.this.getWindow().getDecorView().setSystemUiVisibility(fullscreenType);
                }
            }
            });*/

            /**
             *
             *
             * Mouse function code stops
             *
             *
             * */


            /**
             *
             *
             * KeyBoard function code starts
             *
             *
             * */



        imageButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick (View v){
                mKeyboardView.setVisibility(View.VISIBLE);
                mKeyboardView.setEnabled(true);
                if (v != null)
                    ((InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
                imageButton.setImageResource(R.drawable.keyboard_hide_white);
            }
            });

            mKeyboard =new Keyboard(this,R.xml.qwerty);

            symbolKeyBoard =new Keyboard(this,R.xml.symbols);

            arrowKeyBoard =new Keyboard(this,R.xml.arrows);

            mKeyboardView = findViewById(R.id.keyboardview);
        mKeyboardView.setKeyboard(mKeyboard);
        mKeyboardView.setPreviewEnabled(false);
        mKeyboardView.setOnKeyboardActionListener(mOnKeyboardActionListener);
        mqttHelper =new MqttHelper(this);
        mqttHelper.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete ( boolean reconnect, String serverURI){
                    Toast.makeText(Second.this, "Connection Succes.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void connectionLost (Throwable cause){
                    Second.this.finish();
                }

                @Override
                public void messageArrived (String topic, MqttMessage message) throws Exception {

                    if (topic.equals("keyReply")) {


                        String[] strings = message.toString().split(" ");
                    /*Keyboard currentKeyboard = mKeyboardView.getKeyboard();
                    List<Keyboard.Key> keys = currentKeyboard.getKeys();
                    mKeyboardView.invalidateKey(keys.size());*/

                        if (strings[0].equals("alt")) {
                            if (strings[1].equals("down")) alt.setVisibility(View.VISIBLE);

                            else alt.setVisibility(View.INVISIBLE);
                        } else if (strings[0].equals("ctrl")) {
                            if (strings[1].equals("down")) ctrl.setVisibility(View.VISIBLE);

                            else ctrl.setVisibility(View.INVISIBLE);
                        } else if (strings[0].equals("shift")) {
                            if (strings[1].equals("down")) shift.setVisibility(View.VISIBLE);

                            else shift.setVisibility(View.INVISIBLE);
                        }
                    }
                }

                @Override
                public void deliveryComplete (IMqttDeliveryToken token){ }
            });
            /**
             *
             *
             * KeyBoard function code stops
             *
             *
             *
             * */
        }


        /**
         *
         *
         * Mouse functions
         *
         *
         * */
        public boolean onTripleFingerTap (MotionEvent ev){
            switch (tripleTapAction) {
                case RIGHT:
                    sendRightClick();
                    break;
                case MIDDLE:
                    sendMiddleClick();
                    break;
                default:
            }
            return true;
        }

        public boolean onDoubleFingerTap (MotionEvent ev){
            switch (doubleTapAction) {
                case RIGHT:
                    sendRightClick();
                    break;
                case MIDDLE:
                    sendMiddleClick();
                    break;
                default:
            }
            return true;
        }

        public void sendMiddleClick () {
            Log.d(TAG, "sendMiddleClick: ");

            try {
                mqttHelper.publishMessage("m 1 1", 0, "key");
            } catch (MqttException e1) {
                e1.printStackTrace();
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
        }

        private void sendRightClick () {
            Log.d(TAG, "sendRightClick: ");
            try {
                mqttHelper.publishMessage("m 2 1", 0, "key");
            } catch (MqttException e1) {
                e1.printStackTrace();
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
        }


/**
 *
 *
 * Gesture detector interface for handling mouse starts here.
 *
 *
 * */
        class GalleryGestureDetector implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
            @Override
            public boolean onDown(MotionEvent motionEvent) {
                Log.d(TAG, "onDown: ");
                return true;
            }

            @Override
            public void onShowPress(MotionEvent motionEvent) {
                Log.d(TAG, "onShowPress: ");
            }

            @Override
            public boolean onSingleTapUp(MotionEvent motionEvent) {
                Log.d(TAG, "onSingleTapUp: ");
                drag = false;
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, final float distanceX, final float distanceY) {

                if (e2.getPointerCount() <= 1) {
                    return false;
                }
                isScrolling = true;
                accumulatedDistanceY += distanceY;
                if (accumulatedDistanceY > MinDistanceToSendScroll || accumulatedDistanceY < -MinDistanceToSendScroll) {

                    sendScroll(scrollDirection * accumulatedDistanceY);

                    accumulatedDistanceY = 0;
                }
                return true;
            }

            @Override
            public void onLongPress(MotionEvent motionEvent) {
                Log.d(TAG, "onLongPress: singlehold");
                getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                drag = true;
                vibrator.vibrate(100);
            }

            @Override
            public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
                return false;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                Log.d(TAG, "onSingleTapConfirmed: ");

                try {
                    mqttHelper.publishMessage("m 0 1", 0, "key");
                } catch (MqttException e1) {
                    e1.printStackTrace();
                } catch (UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                }
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                Log.d(TAG, "onDoubleTap: ");

                try {
                    mqttHelper.publishMessage("m 0 2", 0, "key");
                } catch (MqttException e1) {
                    e1.printStackTrace();
                } catch (UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                }
                return true;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return false;
            }

            private void sendScroll(final float y) {
                Log.d(TAG, "sendScroll: " + y);

                try {
                    mqttHelper.publishMessage("m s " + y, 0, "key");
                } catch (MqttException e1) {
                    e1.printStackTrace();
                } catch (UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                }
            }

        }
        /**
         *
         * interface implementation stops.
         *
         * */


        @Override
        public void onBackPressed () {


            if (mKeyboardView.isEnabled()) {
                mKeyboardView.setVisibility(View.GONE);
                mKeyboardView.setEnabled(false);
                imageButton.setImageResource(R.drawable.keyboard_white);
            } else {
                exit();
                super.onBackPressed();
            }


        }

        private void exit () {
            try {
                mqttHelper.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
            this.finish();
        }


        /**
         *
         *
         * KeyBoardActionListner starts.
         *
         *
         * */
        private KeyboardView.OnKeyboardActionListener mOnKeyboardActionListener = new KeyboardView.OnKeyboardActionListener() {

            @Override
            public void onKey(int primaryCode, int[] keyCodes) {

            /* Keyboard currentKeyboard = mKeyboardView.getKeyboard();
            List<Keyboard.Key> keys = currentKeyboard.getKeys();
            mKeyboardView.invalidateKey(primaryCode);
            keys.get(primaryCode).label = null;*/

                try {
                    if (primaryCode == -1) caps = (byte) (1 - caps);
                    else if ((primaryCode >= 97 && primaryCode <= 122) || (primaryCode >= 65 && primaryCode <= 90)) {
                        primaryCode = primaryCode - (32 * caps);
                    }
                    mqttHelper.publishMessage("" + primaryCode, 0, "key");
                } catch (MqttException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                Log.d("Key", "You pressed key " + primaryCode + " caps = " + caps);
            }

            @Override
            public void onPress(int arg0) {
            }

            @Override
            public void onRelease(int primaryCode) {
            }

            @Override
            public void onText(CharSequence text) {
            }

            @Override
            public void swipeDown() {
                mKeyboardView.setVisibility(View.GONE);
                mKeyboardView.setEnabled(false);
                imageButton.setImageResource(R.drawable.keyboard_white);
            }

            @Override
            public void swipeLeft() {
                if (!symbol && !arrows) {
                    symbol = true;
                    arrows = false;
                    nextKeyBoard(symbolKeyBoard);
                } else if (arrows) {
                    arrows = symbol = false;
                    nextKeyBoard(mKeyboard);
                }
            }

            @Override
            public void swipeRight() {
                if (!symbol && !arrows) {
                    symbol = false;
                    arrows = true;
                    nextKeyBoard(arrowKeyBoard);
                } else if (symbol) {
                    arrows = symbol = false;
                    nextKeyBoard(mKeyboard);
                }
            }

            @Override
            public void swipeUp() {
            }
        };

        public void nextKeyBoard (Keyboard keyboard){
            mKeyboardView.setVisibility(View.GONE);
            mKeyboardView.setEnabled(false);
            mKeyboardView.setKeyboard(keyboard);
            mKeyboardView.setVisibility(View.VISIBLE);
            mKeyboardView.setEnabled(true);
        }
    }
