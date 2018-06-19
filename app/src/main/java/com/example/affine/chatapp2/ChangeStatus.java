package com.example.affine.chatapp2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;

import org.eclipse.paho.android.service.MqttAndroidClient;

public class ChangeStatus extends AppCompatActivity implements MqttBrokerManager.MqttBrokerCallback{
    EditText changeStatusE;
    PreferenceManager preferenceManager;
    Button button;
    String userDetailsChannelId;
    MqttBrokerManager mqttBrokerManager = new MqttBrokerManager(ChangeStatus.this,ChangeStatus.this);
    Gson gson = new Gson();
    String newStatus;
    ProgressBar spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changestatus);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Enter your status");
        preferenceManager = new PreferenceManager(ChangeStatus.this);
        changeStatusE = findViewById(R.id.changeStatusEditText);
        button = findViewById(R.id.buttonSetStatus);
        spinner = (ProgressBar)findViewById(R.id.progressBar1);
        changeStatusE.setText(preferenceManager.getStatus());
        changeStatusE.requestFocus();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.setVisibility(View.VISIBLE);
                newStatus = changeStatusE.getText().toString();
                mqttBrokerManager.connect();
                userDetailsChannelId = createUserDetailsChannelId(preferenceManager.getId());
                //finish();
            }
        });
    }

    private String createUserDetailsChannelId(int n1) {
        return "PQ/userDetails_" + n1;
    }

    @Override
    public void onConnectionEstablished(MqttAndroidClient MqttAndroidClient) {
        preferenceManager.setStatus(newStatus);
        UserDataModel userDataModel = new UserDataModel();
        userDataModel.setMesgType("userDetails");
        userDataModel.setUserId(preferenceManager.getId());
        userDataModel.setUserName(preferenceManager.getName());
        userDataModel.setUserStatus(preferenceManager.getStatus());
        userDataModel.setDpLink("xyz");
        mqttBrokerManager.publish(userDetailsChannelId,gson.toJson(userDataModel));
        mqttBrokerManager.disconnect();
        spinner.setVisibility(View.GONE);
        Toast.makeText(ChangeStatus.this,"Status Changed",Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onSubscription() {

    }

    @Override
    public void onUnSubscription() {

    }

    @Override
    public void onMessageReceived(String topic, String message) {

    }

    @Override
    public void onConnectionLost() {

    }

    @Override
    public void onError(String section, String error) {
        Toast.makeText(ChangeStatus.this,"Can't Connect to server. Try Again Later.",Toast.LENGTH_SHORT).show();
    }
}
