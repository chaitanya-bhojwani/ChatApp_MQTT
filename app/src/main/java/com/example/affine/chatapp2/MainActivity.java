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
import android.provider.SyncStateContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import org.eclipse.paho.android.service.MqttAndroidClient;

import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity implements MqttBrokerManager.MqttBrokerCallback {
    LinearLayout serverStatus;
    RecyclerView messageList;
    MessageListAdapter messageListAdapter;
    MqttBrokerManager mqttBrokerManager = new MqttBrokerManager(MainActivity.this,MainActivity.this);
    List<DataModel> msgList = Lists.newArrayList();
    EditText chatbox;
    TextView onlineStatus;
    TextView isTypingStatus;
    FloatingActionButton sendButton;
    Gson gson;
    PreferenceManager preferenceManager;
    String channelId;
    UserInfo userInfo;
    String listenonlinechannelId;
    DataManager dataManager;
    AlertDialog alertDialog;
    Handler isTypingHandler = new Handler();
    Handler OnlineStatusHandler = new Handler();
    boolean MQTTisConnected = false;
    boolean connectionStatus = false;
    boolean dialogcreated = false;
    int checkedtill = 0;
    int readtill = 0;
    long lastSeenTimer = System.currentTimeMillis();
    boolean lastSeenUpdated = false;
    private static final int PICKFILE_REQUEST_CODE = 8778;
    private static final int CHOOSE_FILE_REQUESTCODE = 8777;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Bundle extras = getIntent().getBundleExtra("data");
        userInfo = (UserInfo) extras.getSerializable("user");
        Log.i("userinfo ", userInfo.getName() + userInfo.getId());
        gson = new Gson();
        dataManager = new DataManager(new ApiService.Factory().createService());
        preferenceManager = new PreferenceManager(this);
        final int id = preferenceManager.getId();
        String name = preferenceManager.getName();

        serverStatus = findViewById(R.id.serverStatus);
        chatbox = findViewById(R.id.chatMessage);
        chatbox.requestFocus();
        sendButton = findViewById(R.id.chatSubmit);
        messageList = findViewById(R.id.chatList);
        onlineStatus = findViewById(R.id.onlineStatus);
        isTypingStatus = findViewById(R.id.isTyping);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("user", userInfo);
                ActivityChangeUtil.change(MainActivity.this, UserProfile.class, bundle);
            }
        });
        messageList.setLayoutManager(linearLayoutManager);
        messageListAdapter = new MessageListAdapter();
        messageListAdapter.setUserId(id);
        messageList.setAdapter(messageListAdapter);

        channelId = createChannelId(id, userInfo.getId());
        listenonlinechannelId = createlistenonlinechannelId(userInfo.getId());
        getSupportActionBar().setTitle(userInfo.getName());

        dataManager.getHistory(channelId).subscribeWith(new HistoryObserver());
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = chatbox.getText().toString();
                if(message.equals(null) || message.equals("")) {return;}
                DataModel dataModel = new DataModel();
                dataModel.setMesgType("message");
                dataModel.setMessage(message);
                dataModel.setMsgId(System.currentTimeMillis());
                dataModel.setUserId(id);
                dataModel.setDeliveredStatus("sent");
                msgList.add(dataModel);
                messageListAdapter.setmDataset(msgList);
                messageList.scrollToPosition(msgList.size() - 1);
                if (MQTTisConnected) {
                    mqttBrokerManager.publish(channelId, gson.toJson(dataModel));
                } else {
                    Log.e("No Connection", "Store Message and send later");
                }
                chatbox.getText().clear();
            }
        });

        chatbox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    //send message of istyping
                    final int id = preferenceManager.getId();
                    DataModel dataModel = new DataModel();
                    dataModel.setMesgType("istypingStatus");
                    dataModel.setMessage("istyping" + id);
                    dataModel.setMsgId(System.currentTimeMillis());
                    dataModel.setUserId(id);
                    dataModel.setDeliveredStatus("sent");
                    if (MQTTisConnected) {
                        mqttBrokerManager.publish(channelId, gson.toJson(dataModel));
                    }
                    if (updateisTypingStatus != null) {
                        isTypingHandler.removeCallbacks(updateisTypingStatus);
                        isTypingHandler.postDelayed(updateisTypingStatus, 2000);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        OnlineStatusHandler.postDelayed(updateOnlineStatus, 5000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        search(searchView);
        return true;
    }

    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_GET_CONTENT);
        shareIntent.addCategory(Intent.CATEGORY_OPENABLE);
        shareIntent.setType("*/*");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "Park Quility - By Jiva Adventures");
        return shareIntent;
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
                messageListAdapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share){
            Intent shareIntent = createShareIntent();
            Intent i = Intent.createChooser(shareIntent, "File");
            startActivityForResult(shareIntent,CHOOSE_FILE_REQUESTCODE);
            return true;
        }

        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Runnable updateisTypingStatus = new Runnable() {

        @Override
        public void run() {
            postisTypingStatus();
        }
    };

    private Runnable updateOnlineStatus = new Runnable() {

        @Override
        public void run() {
            postOnlineStatus();
        }
    };


    public void postisTypingStatus() {
        final int id = preferenceManager.getId();
        DataModel dataModel = new DataModel();
        dataModel.setMesgType("istypingStatus");
        dataModel.setMessage("isnottyping"+ id);
        dataModel.setMsgId(System.currentTimeMillis());
        dataModel.setUserId(id);
        dataModel.setDeliveredStatus("sent");
        if (MQTTisConnected) {
            mqttBrokerManager.publish(channelId, gson.toJson(dataModel));
        } else {
            Log.e("Can't send", "Phone disconnected");
        }
    }

    public void postreadTillStatus(long msgId) {
        final int id = preferenceManager.getId();
        DataModel dataModel = new DataModel();
        dataModel.setMesgType("readTillStatus");
        dataModel.setMessage("readTillHereby" + id);
        dataModel.setMsgId(msgId);
        dataModel.setUserId(id);
        dataModel.setDeliveredStatus("sent");
        if (MQTTisConnected) {
            mqttBrokerManager.publish(channelId, gson.toJson(dataModel));
        } else {
            Log.e("Can't send", "Phone disconnected");
        }
    }

    public void postOnlineStatus() {
        if(!lastSeenUpdated) {
            onlineStatus.setVisibility(View.VISIBLE);
            onlineStatus.setText("CCTD Last Seen at " + TimeDateUtil.formatDateTime(lastSeenTimer)); }
        if (lastSeenUpdated) {
            onlineStatus.setVisibility(View.VISIBLE);
            onlineStatus.setText("Last Seen at " + TimeDateUtil.formatDateTime(lastSeenTimer)); }
    }

    @Override
    public void onConnectionEstablished(MqttAndroidClient MqttAndroidClient) {
        serverStatus.setVisibility(View.GONE);
        MQTTisConnected = true;
        Log.i("main", "connected " + channelId);
        mqttBrokerManager.receiveMessages();
        mqttBrokerManager.subscribe(channelId);
        mqttBrokerManager.subscribe(listenonlinechannelId);
    }

    @Override
    public void onDisconnected() {
        serverStatus.setVisibility(View.VISIBLE);
        Log.i("main", "disconnected");
    }

    @Override
    public void onSubscription() {
        Log.i("main", "subscription");

    }

    @Override
    public void onUnSubscription() {
        Log.i("main", "unsubscribe");

    }

    @Override
    public void onMessageReceived(String topic, String message) {
        Log.i("main", "message " + topic + " " + message);
        DataModel dataModel = gson.fromJson(message, DataModel.class);
        if (Strings.areEqual(dataModel.getMesgType(), "message")) {
            if(dataModel.getUserId() == userInfo.getId()) {
                msgList.add(dataModel);
                messageListAdapter.setmDataset(msgList);
                Log.e("List", "List numbering at " + msgList.size());
                messageList.scrollToPosition(msgList.size() - 1);
                Log.e("List", "List Scrolled to " + (msgList.size() - 1));
                postreadTillStatus(dataModel.getMsgId());
            }
        }
        if (Strings.areEqual(dataModel.getMesgType(), "onlineStatus")) {
            if (Strings.areEqual(dataModel.getMessage(), "online")) {
                lastSeenTimer = dataModel.getMsgId();
                if (updateOnlineStatus != null) {
                    OnlineStatusHandler.removeCallbacks(updateOnlineStatus);
                    OnlineStatusHandler.postDelayed(updateOnlineStatus, 10000);
                }

                if(checkedtill<readtill){
                    checkedtill = readtill;
                }

                for(int i = checkedtill; i<msgList.size(); i++) {
                    if(msgList.get(i).getMsgId() <= lastSeenTimer) {
                        if(msgList.get(i).getDeliveredStatus().equals("sent") && (msgList.get(i).getUserId()== preferenceManager.getId())) {
                            Log.e("Double Tick","Change to double tick of message" + msgList.get(i).getMessage());
                            msgList.get(i).setDeliveredStatus("delivered");
                            messageListAdapter.notifyItemChanged(i);
                            checkedtill++;
                        }
                    }
                    else {
                        break;
                    }
                }
                onlineStatus.setVisibility(ImageView.VISIBLE);
                onlineStatus.setText("Online");
            } else {
                onlineStatus.setVisibility(ImageView.VISIBLE);
                onlineStatus.setText("SIW Last Seen at " + TimeDateUtil.formatDateTime(lastSeenTimer));
            }
        }
        if (Strings.areEqual(dataModel.getMesgType(), "istypingStatus")) {
            if (Strings.areEqual(dataModel.getMessage(), "istyping" + userInfo.getId())) {
                isTypingStatus.setVisibility(TextView.VISIBLE);
            } else {
                isTypingStatus.setVisibility(TextView.INVISIBLE);
            }
        }
        if (Strings.areEqual(dataModel.getMesgType(), "readTillStatus")) {
            if (Strings.areEqual(dataModel.getMessage(), "readTillHereby" + userInfo.getId())) {
                //Change read ticks to read tick on time basis;
                for (int i = readtill; i < msgList.size(); i++) {
                    if (msgList.get(i).getMsgId() <= dataModel.getMsgId()) {
                        Log.e("Read Tick", "Change to read tick of message" + msgList.get(i).getMessage());
                        msgList.get(i).setDeliveredStatus("read");
                        messageListAdapter.notifyItemChanged(i);
                        readtill++;
                    }
                    else {
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onConnectionLost() {
        serverStatus.setVisibility(View.VISIBLE);
        Log.i("main", "connectedlost");

    }

    @Override
    public void onError(String section, String error) {
        Log.i("main", "error");

    }


    private String createChannelId(int n1, int n2) {
        if (n1 > n2) {
            return "PQ/" + n2 + "" + n1;
        }
        return "PQ/" + n1 + "" + n2;
    }

    private String createlistenonlinechannelId(int n1) {
        return "PQ/online_" + n1;
    }

    public void checkNetworkConnection() {
        dialogcreated = true;
        if (this.isFinishing()) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No internet Connection");
        builder.setCancelable(false);
        builder.setMessage("Please turn on internet connection to continue");
        builder.setNegativeButton("close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.this.finish();
            }
        });
        alertDialog = builder.create();
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
            serverStatus.setVisibility(View.VISIBLE);
            Log.e("Connection","MQTT is Connected");
            if (dialogcreated) {
                alertDialog.dismiss();
                dialogcreated = false;
            }
            return true;
        } else {
            connectionStatus = false;
            MQTTisConnected = false;
            if (!dialogcreated) {
                checkNetworkConnection();
            }
            Log.d("Network", "Not Connected");
            return false;
        }
    }

    private BroadcastReceiver networkStateReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            isNetworkConnectionAvailable();
        }

    };

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onPause() {
        unregisterReceiver(networkStateReceiver);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        //mqttBrokerManager.disconnect();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Log.e("TAG", "Window Exit");
        mqttBrokerManager.disconnect();
        super.onBackPressed();
    }

    @Override
    protected void onUserLeaveHint() {
        mqttBrokerManager.disconnect();
        super.onUserLeaveHint();
        Log.e("TAG", "Activity Minimized");
    }

    class HistoryObserver implements SingleObserver<Response<List<HistoryResponseItem>>> {

        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onSuccess(Response<List<HistoryResponseItem>> listResponse) {

            List<HistoryResponseItem> data = listResponse.getData();
            long msgId = 0;
            long reatTillHistory = 0;
            for (HistoryResponseItem item : data) {
                String json = item.getMessage();
//                Log.i("data ", json);
                try {
                    DataModel dataModel = gson.fromJson(json, DataModel.class);
                    if(dataModel.getMesgType().equals("message")) {
//                Log.i("data ", dataModel.getMesgType());
//                Log.i("data ", dataModel.getMessage());
//                Log.i("data ", "" + dataModel.getUserId());
                        msgList.add(dataModel);
                    msgId = dataModel.getMsgId();
                    }
                    if(dataModel.getMesgType().equals("readTillStatus")) {
                        if (Strings.areEqual(dataModel.getMessage(), "readTillHereby" + userInfo.getId())) {
                            //Change read ticks to read tick on time basis;
                            reatTillHistory = dataModel.getMsgId();
                        }
                    }
//                    Collections.reverse(msgList);
                } catch (Throwable throwable) {

                }
            }
            messageListAdapter.setmDataset(msgList);
            messageList.scrollToPosition(msgList.size() - 1);
            postreadTillStatus(msgId);
            for (int i = readtill; i < msgList.size(); i++) {
                if (msgList.get(i).getMsgId() <= reatTillHistory) {
                    Log.e("Read Tick", "Change to Read tick of message" + msgList.get(i).getMessage());
                    msgList.get(i).setDeliveredStatus("read");
                    messageListAdapter.notifyItemChanged(i);
                    readtill++;
                }
                else {
                    break;
                }
            }
            Log.e("Online History","Online History Message should come now");
            dataManager.getHistory(listenonlinechannelId).subscribeWith(new OnlineObserver());

        }

        @Override
        public void onError(Throwable e) {

        }
    }

    class OnlineObserver implements SingleObserver<Response<List<HistoryResponseItem>>> {

        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onSuccess(Response<List<HistoryResponseItem>> listResponse) {

            List<HistoryResponseItem> data = listResponse.getData();
            for (HistoryResponseItem item : data) {
                String json = item.getMessage();
//                Log.i("data ", json);
                try {
                    DataModel dataModel = gson.fromJson(json, DataModel.class);
                    if (dataModel.getMesgType().equals("onlineStatus")) {
                        Log.e("Online History","Online History Message Received");
                        lastSeenTimer = dataModel.getMsgId();
                        lastSeenUpdated = true;
                        if (updateOnlineStatus != null) {
                            OnlineStatusHandler.removeCallbacks(updateOnlineStatus);}
                        //postOnlineStatus();
                        OnlineStatusHandler.postDelayed(updateOnlineStatus,2000);
                        if (checkedtill<readtill){
                            checkedtill = readtill;
                        }
                        for(int i = checkedtill; i<msgList.size(); i++) {
                            if(msgList.get(i).getMsgId() <= lastSeenTimer) {
                                if(msgList.get(i).getUserId()== preferenceManager.getId()) {
                                    Log.e("Double Tick","Change to double tick of message" + msgList.get(i).getMessage());
                                    msgList.get(i).setDeliveredStatus("delivered");
                                    messageListAdapter.notifyItemChanged(i); }
                                checkedtill++;
                            }
                            else {
                                break;
                            }
                        }
                    }
//                    Collections.reverse(msgList);
                } catch (Throwable throwable) {

                }
            }
        }

        @Override
        public void onError(Throwable e) {

        }
    }
}

