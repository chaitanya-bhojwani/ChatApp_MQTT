package com.example.affine.chatapp2;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;

import org.eclipse.paho.android.service.MqttAndroidClient;

import java.util.List;

import helper.DatabaseHelper;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;


public class FriendsActivity extends AppCompatActivity implements MqttBrokerManager.MqttBrokerCallback {
    RecyclerView friendList;
    FriendsListAdapter adapter;
    PreferenceManager preferenceManager;
    String onlinechannelId;
    String userDetailsChannelId;
    Gson gson = new Gson();
    DataManager dataManager;
    MqttBrokerManager mqttBrokerManager = new MqttBrokerManager(FriendsActivity.this,FriendsActivity.this);
    Handler customHandler = new Handler();
    boolean MQTTisConnected = false;
    boolean connectionStatus = false;
    ProgressBar spinner;
    DatabaseHelper databaseHelper = new DatabaseHelper(FriendsActivity.this);
    List<UserInfo> userInfos = Lists.newArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Friends");

        dataManager = new DataManager(new ApiService.Factory().createService());
        friendList = findViewById(R.id.friendList);
        spinner = (ProgressBar)findViewById(R.id.progressBar1);
        friendList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FriendsListAdapter();
        friendList.setAdapter(adapter);

        //mqttBrokerManager.connect();
        preferenceManager = new PreferenceManager(this);
        adapter.setOnItemClickListener(new FriendsListAdapter.RecyclerViewClickListener() {
            @Override
            public void onClick(UserInfo userInfo) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("user", userInfo);
                ActivityChangeUtil.change(FriendsActivity.this, MainActivity.class, bundle);
            }

            @Override
            public void onProfileClick(UserInfo userInfo) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("user", userInfo);
                ActivityChangeUtil.change(FriendsActivity.this, UserProfile.class, bundle);
            }
        });

        if (preferenceManager.getId() == -1) {
            showDialog();
        } else {
            int count = databaseHelper.totalUsers();
            Log.e("SQLite","Total Rows" + count);
            if(count == 0){
                count = 4;
            }
            int id = preferenceManager.getId();

            for (int i = 1; i <= (count + 1); i++) {
                if (i == id)
                    continue;
                UserInfo userInfo = new UserInfo();
                userInfo.setId(i);
                userInfo.setName("Name" + i);
                userInfo.setStatus("Hey there ! I am using PQ. Yay yay yay yay yay yay yay yay yay yay yay yay yay");
                userInfo.setDp("no Picture");
                int rows = databaseHelper.userDetailsPresent(userInfo);
                if(rows == 0){
                    Log.e("SQLite","Rows are 0");
                    //Insert into databse
                    databaseHelper.insertUser(userInfo);
                    userInfos.add(userInfo); // Add to List
                }
                else {
                    Log.e("SQLite","Rows: " + rows);
                    UserInfo userInfo1 = databaseHelper.getUser(i);
                    userInfos.add(userInfo1);
                    //Select From Database
                }
                //userInfos.add(userInfo);
            }
            adapter.setmDataset(userInfos);
        }

        registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_friends, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        search(searchView);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_refresh) {
            spinner.setVisibility(View.VISIBLE);
            dataManager.getAllUsers("xyz").subscribeWith(new UserDetailsObserver());
            return true;
        }

        if (id == R.id.action_search) {
            return true;
        }

        if (id == R.id.action_profile) {
            Intent intent = new Intent(FriendsActivity.this, profile.class);
            this.startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void search(SearchView searchView) {

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.e("EventListen", query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.e("EventSearching", "Filtering");
                adapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    private Runnable updateTimeThread = new Runnable() {

        @Override
        public void run() {
            postOnlineStatus();
            customHandler.postDelayed(this, 3000);
        }
    };

    public void postOnlineStatus() {
        if (MQTTisConnected) {
            final int id1 = preferenceManager.getId();
            onlinechannelId = createonlineChannelId(id1);
            DataModel dataModel1 = new DataModel();
            dataModel1.setMesgType("onlineStatus");
            dataModel1.setMessage("online");
            dataModel1.setMsgId(System.currentTimeMillis());
            dataModel1.setUserId(id1);
            dataModel1.setDeliveredStatus("sent");
            mqttBrokerManager.publish(onlinechannelId, gson.toJson(dataModel1));
            Log.e("Publishing", "Online Status Published");
        } else {
            Log.e("Can't Publish", "Phone is disconnected");
        }

    }

    private String createonlineChannelId(int n1) {
        return "PQ/online_" + n1;
    }

    private String createUserDetailsChannelId(int n1) {
        return "PQ/userDetails_" + n1;
    }

    private void showDialog() {
        Context context = FriendsActivity.this;
        if (context == null || FriendsActivity.this.isFinishing()) {
            return;
        }
        LayoutInflater li = LayoutInflater.from(FriendsActivity.this);
        View promptsView = li.inflate(R.layout.dialog_layout, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FriendsActivity.this);

        alertDialogBuilder.setView(promptsView);

        final EditText userId = (EditText) promptsView
                .findViewById(R.id.id);

        final EditText userName = (EditText) promptsView
                .findViewById(R.id.name);

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                int idddd = Integer.valueOf(userId.getText().toString());
                                preferenceManager.setId(idddd);
                                preferenceManager.setName(userName.getText().toString());
                                int count = databaseHelper.totalUsers();
                                Log.e("SQLite","Total Rows" + count);
                                if(count == 0){
                                    count = 4;
                                }

                                for (int i = 1; i <= (count + 1); i++) {
                                    if (i == idddd)
                                        continue;
                                    UserInfo userInfo = new UserInfo();
                                    userInfo.setId(i);
                                    userInfo.setName("Name" + i);
                                    userInfo.setStatus("Hey there ! I am using PQ. Yay yay yay yay yay yay yay yay yay yay yay yay yay");
                                    userInfo.setDp("no Picture");
                                    int rows = databaseHelper.userDetailsPresent(userInfo);
                                    if(rows == 0){
                                        Log.e("SQLite","Rows are 0");
                                        //Insert into databse
                                        databaseHelper.insertUser(userInfo);
                                        userInfos.add(userInfo); // Add to List
                                    }
                                    else {
                                        //Select From Database
                                        Log.e("SQLite","Rows: " + rows);
                                        UserInfo userInfo1 = databaseHelper.getUser(i);
                                        userInfos.add(userInfo1);
                                    }
                                    //userInfos.add(userInfo);
                                }
                                UserDataModel userDataModel = new UserDataModel();
                                userDataModel.setUserId(preferenceManager.getId());
                                userDataModel.setUserName(preferenceManager.getName());
                                userDataModel.setUserStatus(preferenceManager.getStatus());
                                userDataModel.setDpLink(preferenceManager.getDp());
                                userDetailsChannelId = createUserDetailsChannelId(preferenceManager.getId());
                                mqttBrokerManager.publish(userDetailsChannelId,gson.toJson(userDataModel));
                                adapter.setmDataset(userInfos);
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }

    public boolean isNetworkConnectionAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnected();
        if (isConnected) {
            Log.d("Network", "Connected");
            connectionStatus = true;
            mqttBrokerManager.connect();
            return true;
        } else {
            Log.d("Network", "Not Connected");
            connectionStatus = false;
            MQTTisConnected = false;
            return false;
        }
    }

    private BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            isNetworkConnectionAvailable();
        }

    };

//    @Override
//    public void onResume() {
//        super.onResume();
//        registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
//    }
//
//    @Override
//    public void onPause() {
//        unregisterReceiver(networkStateReceiver);
//        super.onPause();
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkStateReceiver);
        //mqttBrokerManager.deleteRetained(onlinechannelId);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //recreate();
    }

    @Override
    public void onConnectionEstablished(MqttAndroidClient MqttAndroidClient) {
        //mqttBrokerManager.deleteRetained(onlinechannelId);
        Log.e("FriendsActivity", "connected ");
        Log.d("Connection", "MQTT Connected");
        connectionStatus = true;
        MQTTisConnected = true;
        postOnlineStatus();
        customHandler.postDelayed(updateTimeThread, 500);
    }

    @Override
    public void onDisconnected() {
        Log.e("FriendsActivity", "disconnected");
    }

    @Override
    public void onSubscription() {
        Log.e("FriendsActivity", "subscription");
    }

    @Override
    public void onUnSubscription() {
        Log.e("FriendsActivity", "unsubscribe");
    }

    @Override
    public void onMessageReceived(String topic, String message) {

    }

    @Override
    public void onConnectionLost() {
        Log.e("FriendsActivity", "connectedlost");
    }

    @Override
    public void onError(String section, String error) {
        Log.e("FriendsActivity", "error");
    }

    private String createChannelId(int n1, int n2) {
        if (n1 > n2) {
            return "PQ/" + n2 + "" + n1;
        }
        return "PQ/" + n1 + "" + n2;
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
                Toast.makeText(FriendsActivity.this,"User Details Not Present in DB",Toast.LENGTH_LONG).show();
                return;
            }
            for (HistoryResponseItem item : data) {
                spinner.setVisibility(View.VISIBLE);
                boolean userUpdated = false;
                String json = item.getMessage();
//                Log.i("data ", json);
                try {
                    UserDataModel userDataModel = gson.fromJson(json, UserDataModel.class);
                    if (userDataModel.getMesgType().equals("userDetails")) {
                        for (int i = 0; i < userInfos.size(); i++) {
                            if (userInfos.get(i).getId() == userDataModel.getUserId()) {
                                String channelId;
                                channelId = createChannelId(preferenceManager.getId(), userDataModel.getUserId());
                                userInfos.get(i).setId(userDataModel.getUserId());
                                userInfos.get(i).setName(userDataModel.getUserName());
                                userInfos.get(i).setStatus(userDataModel.getUserStatus());
                                userInfos.get(i).setDp(userDataModel.getDpLink());
                                adapter.notifyItemChanged(i);
                                dataManager.getHistory(channelId).subscribeWith(new UserChatObserver());
                                int check = databaseHelper.updateUser(userInfos.get(i));
                                Log.e("SQLite","Rows: " + check);
                                userUpdated = true;
                                break; }
                        }

                        if(!userUpdated){
                            if (userDataModel.getUserId() == preferenceManager.getId()){
                                continue;
                            }
                            String channelId;
                            channelId = createChannelId(preferenceManager.getId(), userDataModel.getUserId());
                            UserInfo userInfo = new UserInfo();
                            userInfo.setId(userDataModel.getUserId());
                            userInfo.setName(userDataModel.getUserName());
                            userInfo.setStatus(userDataModel.getUserStatus());
                            userInfo.setDp(userDataModel.getDpLink());
                            userInfos.add(userInfo);
                            adapter.notifyDataSetChanged();
                            dataManager.getHistory(channelId).subscribeWith(new UserChatObserver());
                            databaseHelper.insertUser(userInfo);
                        }

                        Log.e("userDetails","User Details Message Received");
                    }
//                    Collections.reverse(msgList);
                } catch (Throwable throwable) {
                    spinner.setVisibility(View.GONE);
                    Toast.makeText(FriendsActivity.this,"Something Went Wrong",Toast.LENGTH_LONG).show();
                }
            }
            //spinner.setVisibility(View.GONE);
            //Toast.makeText(FriendsActivity.this,"Users Refreshed",Toast.LENGTH_LONG).show();
        }
        @Override
        public void onError(Throwable e) {
            spinner.setVisibility(View.GONE);
            Toast.makeText(FriendsActivity.this,"Can't Update. No Net Connection or Can't Connect to Server !!",Toast.LENGTH_LONG).show();
        }
    }

    class UserChatObserver implements SingleObserver<Response<List<HistoryResponseItem>>> {

        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onSuccess(Response<List<HistoryResponseItem>> listResponse) {

            List<HistoryResponseItem> data = listResponse.getData();
            if (data == null || data.size()== 0){
                spinner.setVisibility(View.GONE);
                //Toast.makeText(FriendsActivity.this,"Chat History Not Present in DB",Toast.LENGTH_SHORT).show();
                return;
            }
            Long readTillStatus_me = 0L;
            boolean readTillStatus_meSet = false;
            int count = 0;
            for (HistoryResponseItem item : data) {
                String json = item.getMessage();
                try {
                    DataModel dataModel = gson.fromJson(json, DataModel.class);
                    if (dataModel.getUserId() == preferenceManager.getId()){
                        if (dataModel.getMesgType().equals("readTillStatus")){
                            readTillStatus_me = dataModel.getMsgId();
                            readTillStatus_meSet = true;
                            continue;
                        }
                        continue;
                    }
                    if (dataModel.getMesgType().equals("message")) {
                        for (int i = 0; i < userInfos.size(); i++) {
                            if (userInfos.get(i).getId() == dataModel.getUserId()) {
                                userInfos.get(i).setLastMessage(dataModel.getMessage());
                                userInfos.get(i).setLastChatTime(TimeDateUtil.formatDateTime(dataModel.getMsgId()));
                                if(dataModel.getMsgId() > readTillStatus_me && readTillStatus_meSet){
                                    count++;
                                }
                                userInfos.get(i).setUnreadMessageCount(count);
                                adapter.notifyItemChanged(i);
                                int check = databaseHelper.updateUser(userInfos.get(i));
                                Log.e("SQLite","Rows: " + check);
                                break;
                            }
                        }
                        Log.e("userDetails","User Details Message Received");
                    }
//                    Collections.reverse(msgList);
                } catch (Throwable throwable) {
                    spinner.setVisibility(View.GONE);
                    Toast.makeText(FriendsActivity.this,"Something Went Wrong",Toast.LENGTH_LONG).show();
                }
            }
            spinner.setVisibility(View.GONE);
            Toast.makeText(FriendsActivity.this,"Users Refreshed",Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onError(Throwable e) {
//            spinner.setVisibility(View.GONE);
//            Toast.makeText(FriendsActivity.this,"Can't Update. No Net Connection or Can't Connect to Server !!",Toast.LENGTH_LONG).show();
        }
    }
}
