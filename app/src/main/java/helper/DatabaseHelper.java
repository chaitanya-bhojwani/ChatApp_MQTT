package helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.affine.chatapp2.UserDataModel;
import com.example.affine.chatapp2.UserInfo;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Logcat tag
    private static final String LOG = "DatabaseHelper";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "contactsManager";

    // Table Names
    private static final String TABLE_USER = "User";

    // Common column names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_STATUS = "status";
    private static final String KEY_DP = "dp";
    private static final String KEY_LAST_MESSAGE = "last_message";
    private static final String KEY_LAST_CHAT_TIME = "last_chat_time";
    private static final String KEY_UNREAD_MESSAGE_COUNT = "unread_message_count";


    // Table Create Statements
    // User table create statement
    private static final String CREATE_TABLE_USER = "CREATE TABLE "
            + TABLE_USER + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT," + KEY_STATUS + " TEXT," + KEY_DP
            + " TEXT," + KEY_LAST_MESSAGE + " TEXT," + KEY_LAST_CHAT_TIME + " TEXT," + KEY_UNREAD_MESSAGE_COUNT + " INTEGER" + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // creating required tables
        db.execSQL(CREATE_TABLE_USER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);

        // create new tables
        onCreate(db);
    }


    public int userDetailsPresent(UserInfo userInfo){
        String countQuery = "SELECT  * FROM " + TABLE_USER + " WHERE "
                + KEY_ID + " = " + userInfo.getId();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public int totalUsers(){
        String countQuery = "SELECT  * FROM " + TABLE_USER ;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public int updateUser(UserInfo userInfo) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, userInfo.getId());
        values.put(KEY_NAME, userInfo.getName());
        values.put(KEY_STATUS, userInfo.getStatus());
        values.put(KEY_DP, userInfo.getDp());
        values.put(KEY_LAST_MESSAGE, userInfo.getLastMessage());
        values.put(KEY_LAST_CHAT_TIME, userInfo.getLastChatTime());
        values.put(KEY_UNREAD_MESSAGE_COUNT, userInfo.getUnreadMessageCount());

        int check = db.update(TABLE_USER, values, KEY_ID + " = " + userInfo.getId(), null);

        return check;
    }

    public int updateUser(UserDataModel userDataModel) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, userDataModel.getUserId());
        values.put(KEY_NAME, userDataModel.getUserName());
        values.put(KEY_STATUS, userDataModel.getUserStatus());
        values.put(KEY_DP, userDataModel.getDpLink());

        int check = db.update(TABLE_USER, values, KEY_ID + " = " + userDataModel.getUserId(), null);

        return check;
    }

    public void deleteEntries(){
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("delete from "+ TABLE_USER);
    }

    public void deleteTable(){
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
    }

    public void insertUser(UserInfo userInfo) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, userInfo.getId());
        values.put(KEY_NAME, userInfo.getName());
        values.put(KEY_STATUS, userInfo.getStatus());
        values.put(KEY_DP, userInfo.getDp());
        values.put(KEY_LAST_MESSAGE, userInfo.getLastMessage());
        values.put(KEY_LAST_CHAT_TIME, userInfo.getLastChatTime());
        values.put(KEY_UNREAD_MESSAGE_COUNT, userInfo.getUnreadMessageCount());

        // insert row
        db.insert(TABLE_USER, null, values);

        return;
    }

    public UserInfo getUser(int user_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_USER + " WHERE "
                + KEY_ID + " = " + user_id;

        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null)
            c.moveToFirst();

        UserInfo userInfo = new UserInfo();
        userInfo.setId(c.getInt(c.getColumnIndex(KEY_ID)));
        userInfo.setName((c.getString(c.getColumnIndex(KEY_NAME))));
        userInfo.setStatus(c.getString(c.getColumnIndex(KEY_STATUS)));
        userInfo.setDp(c.getString(c.getColumnIndex(KEY_DP)));
        userInfo.setLastMessage(c.getString(c.getColumnIndex(KEY_LAST_MESSAGE)));
        userInfo.setLastChatTime(c.getString(c.getColumnIndex(KEY_LAST_CHAT_TIME)));
        userInfo.setUnreadMessageCount(c.getInt(c.getColumnIndex(KEY_UNREAD_MESSAGE_COUNT)));

        return userInfo;
    }
}
