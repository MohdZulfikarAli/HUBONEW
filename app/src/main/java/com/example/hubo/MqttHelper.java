package com.example.hubo;

import android.content.Context;
import android.util.Log;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttHelper {
    private static final String BROKER_URI = "mqtt://sonic.domainenroll.com:1883";
    private static final String CLIENT_ID = "r2adtc3425az";

    private MqttAndroidClient mqttAndroidClient;

    public MqttHelper(Context context, MqttCallback mqttCallback) {
        mqttAndroidClient = new MqttAndroidClient(context, BROKER_URI, CLIENT_ID);

        mqttAndroidClient.setCallback(mqttCallback);
    }

    public MqttHelper(Context context) {
        mqttAndroidClient = new MqttAndroidClient(context, BROKER_URI, CLIENT_ID);

        mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                // Handle connection lost
                Log.d("MQTT", "Connection Lost");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                // Handle incoming message
                String payload = new String(message.getPayload());
                // Process the received data as needed
                Log.d("MQTT", "Received message on topic: " + topic + ", Message: " + payload);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // Handle message delivery complete
                Log.d("MQTT", "Delivery Complete");
            }
        });
    }

    public void connect(IMqttActionListener listener) {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName("domainenroll");
        options.setPassword("de120467".toCharArray());

        try {
            mqttAndroidClient.connect(options, null, listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void subscribe(String topic, int qos) {
        try {
            mqttAndroidClient.subscribe(topic, qos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            mqttAndroidClient.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

