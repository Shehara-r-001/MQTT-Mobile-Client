package com.example.mqttmobileclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "TAG";
    TextInputLayout publish_topic, publish_message, led_Topic, temp_Topic;
    Button publish_btn, connect, disconnect, led_ON, led_OFF, temp_Read, temp_Stop;
    TextView temp_val;

    String host = "tcp://broker.mqttdashboard.com:1883";
    String username = "strider";
    String password = "Thepurplekey01";

    MqttAndroidClient client;
    MqttConnectOptions options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        publish_topic = findViewById(R.id.pub_topic);
        publish_message = findViewById(R.id.pub_data);
        publish_btn = findViewById(R.id.publish_btn);
        led_Topic = findViewById(R.id.led_topic);
        led_ON = findViewById(R.id.led_on);
        led_OFF = findViewById(R.id.led_off);
        temp_Topic = findViewById(R.id.temp_topic);
        temp_Read = findViewById(R.id.read_start);
        temp_Stop = findViewById(R.id.read_stop);
        temp_val = findViewById(R.id.temp_val);

        connect = findViewById(R.id.connect);
        disconnect = findViewById(R.id.disconnect);

        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Disconnect();
                temp_Topic.getEditText().setText("");
                publish_topic.getEditText().setText("");
                publish_message.getEditText().setText("");
                led_Topic.getEditText().setText("");
                temp_val.setText(" _ ");
            }
        });

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Connect();
            }
        });

        temp_Stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        temp_Read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String topic_temp = temp_Topic.getEditText().getText().toString().trim();
                int qos = 0;

                Read_Temp(topic_temp, qos);

                client.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {

                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        Toast.makeText(getApplicationContext(), "Message received", Toast.LENGTH_LONG).show();
                        String msg = new String(message.getPayload());
                        String setMsg = msg + " C";
                        temp_val.setText(setMsg);

                        double temp = Double.parseDouble(msg);
                        if (temp >= 40.0 ){
                            temp_val.setTextColor(Color.parseColor("#e60000"));
                        }
                        if (temp >= 5.0 && temp < 40.0){
                            temp_val.setTextColor(Color.parseColor("#33cc00"));
                        }
                        if (temp < 5.0) {
                            temp_val.setTextColor(Color.parseColor("#007acc"));
                        }

                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {

                    }
                });
            }
        });

        led_ON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String topic_LED = led_Topic.getEditText().getText().toString().trim();
                String on = "on";
                LED_ON(topic_LED, on);
            }
        });

        led_OFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String topic_LED = led_Topic.getEditText().getText().toString().trim();
                String off = "off";
                LED_OFF(topic_LED, off);
            }
        });

        publish_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String topic_to_publish = publish_topic.getEditText().getText().toString().trim();
                String message_to_send = publish_message.getEditText().getText().toString().trim();

                Publish(topic_to_publish, message_to_send);
                Toast.makeText(getApplicationContext(), "Message has been published", Toast.LENGTH_LONG).show();
                publish_message.getEditText().setText("");
            }
        });

    }

    private void Disconnect() {
        options = new MqttConnectOptions();
        options.setUserName(username);
        options.setPassword(password.toCharArray());

        try {
            IMqttToken token = client.disconnect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(getApplicationContext(), "Disconnected..!", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(getApplicationContext(), "Failed..!", Toast.LENGTH_LONG).show();

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void Read_Temp(String topic_temp, int qos) {
        try{
            client.subscribe(topic_temp, qos);
        }catch (MqttException e){
            e.printStackTrace();
        }
    }

    private void LED_OFF(String topic_led, String off) {
        try {
            client.publish(topic_led, off.getBytes(), 0, false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void LED_ON(String topic_led, String on) {
//        try {
//            client.publish(topic_led, on.getBytes(), 0, false);
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }

    }

    private void Connect() {

        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), host, clientId);

        options = new MqttConnectOptions();
        options.setUserName(username);
        options.setPassword(password.toCharArray());

        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(getApplicationContext(), "Connected..!", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(getApplicationContext(), "Failed..!", Toast.LENGTH_LONG).show();

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void Publish(String topic_to_publish, String message_to_send){
        //Toast.makeText(getApplicationContext(), topic_to_publish+"--"+message_to_send, Toast.LENGTH_LONG).show();

        try {
            client.publish(topic_to_publish, message_to_send.getBytes(), 0, false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}