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

public class ChangeName extends AppCompatActivity implements MqttBrokerManager.MqttBrokerCallback{
    EditText changeNameE;
    PreferenceManager preferenceManager;
    Button button;
    Gson gson = new Gson();
    String userDetailsChannelId;
    ProgressBar spinner;
    String newName;
    MqttBrokerManager mqttBrokerManager = new MqttBrokerManager(ChangeName.this,ChangeName.this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changename);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Enter your name");
        preferenceManager = new PreferenceManager(ChangeName.this);
        changeNameE = findViewById(R.id.changeNameEditText);
        button = findViewById(R.id.buttonSetName);
        spinner = (ProgressBar)findViewById(R.id.progressBar1);
        changeNameE.setText(preferenceManager.getName());
        changeNameE.requestFocus();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.setVisibility(View.VISIBLE);
                newName = changeNameE.getText().toString();
                userDetailsChannelId = createUserDetailsChannelId(preferenceManager.getId());
                mqttBrokerManager.connect();
                //finish();
            }
        });
    }

    private String createUserDetailsChannelId(int n1) {
        return "PQ/userDetails_" + n1;
    }

    @Override
    public void onConnectionEstablished(MqttAndroidClient MqttAndroidClient) {
        preferenceManager.setName(newName);
        UserDataModel userDataModel = new UserDataModel();
        userDataModel.setMesgType("userDetails");
        userDataModel.setUserId(preferenceManager.getId());
        userDataModel.setUserName(preferenceManager.getName());
        userDataModel.setUserStatus(preferenceManager.getStatus());
        userDataModel.setDpLink("xyz");
        mqttBrokerManager.publish(userDetailsChannelId,gson.toJson(userDataModel));
        mqttBrokerManager.disconnect();
        spinner.setVisibility(View.GONE);
        Toast.makeText(ChangeName.this,"Name Changed",Toast.LENGTH_SHORT).show();
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
        Toast.makeText(ChangeName.this,"Can't Connect to server. Try Again Later",Toast.LENGTH_SHORT).show();
    }
}

