package com.example.affine.chatapp2;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.util.Random;

/**
 * Created by vikash on 15/2/18.
 */

public class MqttBrokerManager {
    private final String TAG = MqttBrokerManager.class.getSimpleName();
    private Context m_context;
    private MqttAndroidClient m_MqttAndroidClient;

    private boolean m_connectionStatus = false;
    private MqttBrokerCallback m_brokerCallback;

    public MqttBrokerManager(Context context, MqttBrokerCallback brokerCallback) {
        m_context = context;
        m_brokerCallback = brokerCallback;
    }

    public boolean connectionState() {
        return m_connectionStatus;
    }

    public void connect() {
        Random random = new Random();
        String clientId = "myclinet23" + random.nextInt();
        Log.i(TAG, "clinet id " + clientId);
        m_MqttAndroidClient = new MqttAndroidClient(m_context.getApplicationContext(), "tcp://broker.hivemq.com:1883", clientId);
        try {
            IMqttToken token = m_MqttAndroidClient.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i("broker", "success");
                    m_connectionStatus = true;
                    m_brokerCallback.onConnectionEstablished(m_MqttAndroidClient);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    m_connectionStatus = false;
                    Log.i("broker", "failure");
                    m_brokerCallback.onError("connect", exception.getLocalizedMessage());
                    exception.printStackTrace();
                }
            });
        } catch (MqttException e) {
            m_brokerCallback.onError("connect", e.getLocalizedMessage());
            e.printStackTrace();
        }
    }


    public void subscribe(final String topic) {
        int qos = 1;
        try {
            IMqttToken subToken = m_MqttAndroidClient.subscribe(topic, qos, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    m_brokerCallback.onSubscription();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    m_brokerCallback.onError("subscribe", exception.getLocalizedMessage());
                }
            });
        } catch (MqttException e) {
            m_brokerCallback.onError("subscribe", e.getLocalizedMessage());
        }
    }

    public void unSubscribe(final String topic) {
        try {
            IMqttToken unsubToken = m_MqttAndroidClient.unsubscribe(topic);
            unsubToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    m_brokerCallback.onUnSubscription();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    m_brokerCallback.onError("unSubscription", exception.getLocalizedMessage());
                }
            });
        } catch (MqttException e) {
            m_brokerCallback.onError("unSubscription", e.getLocalizedMessage());
        }
    }

    public void receiveMessages() {
        m_MqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                m_brokerCallback.onConnectionLost();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                try {
                    String data = new String(message.getPayload(), "UTF-8");
                    m_brokerCallback.onMessageReceived(topic, data);
                } catch (UnsupportedEncodingException e) {
                    m_brokerCallback.onError("message Received", e.getLocalizedMessage());
                }

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    public void publish(String topic, String data) {
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = data.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            message.setRetained(false);
            m_MqttAndroidClient.publish(topic, message);
        } catch (UnsupportedEncodingException | MqttException e) {
            m_brokerCallback.onError("publish", e.getLocalizedMessage());
        }
    }

    public void deleteRetained(String topic) {
        try {
            m_MqttAndroidClient.publish(topic,new byte[0], 0, true);
        } catch (MqttException e) {
            m_brokerCallback.onError("publish", e.getLocalizedMessage());
        }
    }


    public void disconnect() {
        try {
            IMqttToken disconToken = m_MqttAndroidClient.disconnect();
            disconToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    m_brokerCallback.onDisconnected();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    m_brokerCallback.onError("disconnect", exception.getLocalizedMessage());
                }
            });
        } catch (MqttException e) {
            m_brokerCallback.onError("disconnect", e.getLocalizedMessage());
        }

    }

    public interface MqttBrokerCallback {

        void onConnectionEstablished(MqttAndroidClient MqttAndroidClient);

        void onDisconnected();

        void onSubscription();

        void onUnSubscription();

        void onMessageReceived(String topic, String message);

        void onConnectionLost();

        void onError(String section, String error);
    }

}