package com.example.affine.chatapp2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.List;

import helper.DatabaseHelper;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;

public class UserProfile extends AppCompatActivity {
    UserInfo userInfo;
    TextView name;
    TextView status;
    TextView id;
    DatabaseHelper databaseHelper = new DatabaseHelper(UserProfile.this);
    de.hdodenhof.circleimageview.CircleImageView dp;
    DataManager dataManager;
    Gson gson = new Gson();
    ProgressBar spinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userprofile);

        Bundle extras = getIntent().getBundleExtra("data");
        userInfo = (UserInfo) extras.getSerializable("user");
        spinner = (ProgressBar)findViewById(R.id.progressBar1);
        dataManager = new DataManager(new ApiService.Factory().createService());
        spinner.setVisibility(View.VISIBLE);
        dataManager.getUserDetails("PQ/userDetails_" + userInfo.getId()).subscribeWith(new UserDetailsObserver());
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(userInfo.getName());
        name = findViewById(R.id.name);
        status = findViewById(R.id.status);
        id = findViewById(R.id.id);
        dp = findViewById(R.id.circleImageViewdp);
        name.setText(userInfo.getName());
        status.setText(userInfo.getStatus());
        id.setText("Using id number " + userInfo.getId());
        status.setMovementMethod(new ScrollingMovementMethod());


    }

    class UserDetailsObserver implements SingleObserver<Response<List<HistoryResponseItem>>> {

        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onSuccess(Response<List<HistoryResponseItem>> listResponse) {

            List<HistoryResponseItem> data = listResponse.getData();
            if (data == null || data.size()== 0){
                spinner.setVisibility(View.GONE);
                Toast.makeText(UserProfile.this,"Details Not Present in DB",Toast.LENGTH_LONG).show();
                return;
            }
            for (HistoryResponseItem item : data) {
                String json = item.getMessage();
//                Log.i("data ", json);
                try {
                    UserDataModel userDataModel = gson.fromJson(json, UserDataModel.class);
                    if (userDataModel.getMesgType().equals("userDetails")) {
                        Log.e("userDetails","User Details Message Received");
                        name.setText(userDataModel.getUserName());
                        status.setText(userDataModel.getUserStatus());
                        databaseHelper.updateUser(userDataModel);
                        spinner.setVisibility(View.GONE);
                    }
//                    Collections.reverse(msgList);
                } catch (Throwable throwable) {
                    spinner.setVisibility(View.GONE);
                    Toast.makeText(UserProfile.this,"Something Went Wrong",Toast.LENGTH_LONG).show();
                }
            }
        }
        @Override
        public void onError(Throwable e) {
            spinner.setVisibility(View.GONE);
            Toast.makeText(UserProfile.this,"Can't Update. No Net Connection or Can't Connect to Server !!",Toast.LENGTH_LONG).show();
        }
    }
}
