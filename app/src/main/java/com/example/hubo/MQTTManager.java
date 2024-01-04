package com.example.hubo;

import android.content.Context;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

public class MQTTManager {

    private static final String BROKER_URI = "mqtt://sonic.domainenroll.com:1883";
    private static final String CLIENT_ID = "r6zp7thdk34nd";

    private MqttAndroidClient mqttAndroidClient;
    private MQTTListener mqttListener;

    public interface MQTTListener {
        void onMessageReceived(String topic, String message);
    }
    public void setMQTTListener(MQTTListener listener) {
        mqttListener = listener;
    }

    public MQTTManager(Context context) {
        mqttAndroidClient = new MqttAndroidClient(context, BROKER_URI, CLIENT_ID);

        mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                // Handle connection loss
            }

            @Override
            public void messageArrived(String topic, org.eclipse.paho.client.mqttv3.MqttMessage message) throws Exception {
                // Handle incoming messages
                String payload = new String(message.getPayload());
                // Do something with the payload
                if (mqttListener != null) {
                    mqttListener.onMessageReceived(topic, payload);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // Handle message delivery completion
            }
        });
    }

    public void connectAndSubscribe(String topic) {
        try {
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setCleanSession(true);
            mqttConnectOptions.setUserName("domainenroll");
            mqttConnectOptions.setPassword("de120467".toCharArray());

            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // Connection successful, subscribe to the topic
                    subscribeToTopic(topic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Handle connection failure
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void subscribeToTopic(String topic) {
        try {
            mqttAndroidClient.subscribe(topic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // Subscription successful
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Handle subscription failure
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            mqttAndroidClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}

