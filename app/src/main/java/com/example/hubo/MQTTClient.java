package com.example.hubo;

import android.content.Context;
import android.util.Log;

import info.mqtt.android.service.Ack;
import info.mqtt.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

public class MQTTClient {
    private static final String BROKER_URI = "tcp://sonic.domainenroll.com:1883"; // Update with your MQTT broker URI
    private static final String CLIENT_ID = "tr56fdsretge674fegyge";
    private static final String TOPIC = "/user_data"; // Update with your desired MQTT topic

    private MqttAndroidClient mqttAndroidClient;

    private MQTTClientListener mqttclient;

    public interface MQTTClientListener
    {
        public void onMessageReceived(String topic, String message);
    }

    public MQTTClient(Context context, MQTTClientListener callback) {
        mqttclient = callback;

        mqttAndroidClient = new MqttAndroidClient(context, BROKER_URI, CLIENT_ID, Ack.AUTO_ACK);


        mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                // Handle connection lost
            }

            @Override
            public void messageArrived(String topic, org.eclipse.paho.client.mqttv3.MqttMessage message) throws Exception {
                // Handle incoming messages
                String payload = new String(message.getPayload());
                // Send the payload back to the MainActivity
                mqttclient.onMessageReceived(topic,payload);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }

        });
    }

    public void connect() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setUserName("domainenroll");
        options.setPassword("de120467".toCharArray());
        options.setCleanSession(false);

        try {
            mqttAndroidClient.connect(options, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("Connection", "Successful");
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d("Connection", "unsuccessful"+exception);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void subscribeToTopic() {
        try {
            mqttAndroidClient.subscribe(TOPIC, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // Handle successful subscription
                    Log.d("Subscription", "subscription successful");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d("Subscription", "subscription unsuccessful");
                }
            });
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



