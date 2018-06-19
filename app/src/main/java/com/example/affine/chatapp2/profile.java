package com.example.affine.chatapp2;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


public class profile extends AppCompatActivity {
    TextView name;
    TextView status;
    TextView id;
    android.support.design.widget.FloatingActionButton editdp;
    de.hdodenhof.circleimageview.CircleImageView dp;
    PreferenceManager preferenceManager;
    String dpPath;
    Uri imgUri;
    ProgressBar spinner;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    protected static final int REQUEST_CODE_MANUAL = 5;
    //private static final int SELECT_PICTURE = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile");
        preferenceManager = new PreferenceManager(profile.this);
        name = findViewById(R.id.name);
        status = findViewById(R.id.status);
        id = findViewById(R.id.id);
        dp = findViewById(R.id.circleImageViewdp);
        editdp = findViewById(R.id.editdp);
        spinner = (ProgressBar)findViewById(R.id.progressBar1);
        dpPath = preferenceManager.getDp();
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        if(!dpPath.equals("no Picture")){
            Log.e("Picure","Taking picture from Storage");
            File imgFile = new  File (dpPath);
            if(imgFile.exists()){
                Uri imageUri = Uri.fromFile(imgFile);
                InputStream imageStream = null;
                try {
                    imageStream = getContentResolver().openInputStream(imageUri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                selectedImage = getResizedBitmap(selectedImage, 1024);// 400 is for example, replace with desired size
                dp.setImageBitmap(selectedImage);
//                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
//                dp.setImageBitmap(null);
//                dp.setImageBitmap(myBitmap);
                Log.e("Picure","Picture Set to DP");
            }
            else {
                Log.e("Picure","File does't exists");
                Toast.makeText(profile.this, "Your previous DP has been deleted or moved to another location", Toast.LENGTH_LONG).show();
                preferenceManager.setDp("no Picture");
                imgUri=Uri.parse("android.resource://com.example.affine.chatapp2/"+R.drawable.ic_abstract_user_flat_1);
                dp.setImageURI(null);
                dp.setImageURI(imgUri);

            }
        }
        else {
            imgUri=Uri.parse("android.resource://com.example.affine.chatapp2/"+R.drawable.ic_abstract_user_flat_1);
            dp.setImageURI(null);
            dp.setImageURI(imgUri);
        }
        name.setText(preferenceManager.getName());
        status.setText(preferenceManager.getStatus());
        id.setText("Using id number " + preferenceManager.getId());
        status.setMovementMethod(new ScrollingMovementMethod());
        name.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(profile.this, ChangeName.class);
                startActivity(intent);
            }
        });
        status.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(profile.this, ChangeStatus.class);
                startActivity(intent);
            }
        });
        dp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent(profile.this, FullScreenImageActivity.class);
                intent.putExtra("path",dpPath);
                startActivity(intent);
                Log.e("Image View","Pressed");*/
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                File imgFile = new  File (dpPath);
                if(imgFile.exists()){
                    Uri imageUri = Uri.fromFile(imgFile);
                    intent.setDataAndType(imageUri, "image/*"); }
                    startActivity(intent);
            }
        });
        editdp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                galleryPermissionDialog();
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        recreate();
    }

/*
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                // Get the url from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // Get the path from the Uri
                    Log.e("Image Picker", "URI is " + selectedImageUri);
                    String path = getRealPathFromURI(profile.this, selectedImageUri);
                    Log.i("Image Picker", "Image Path : " + path);
                    // Set the image in ImageView
//                    dp.setImageURI(null);
//                    dp.setImageURI(selectedImageUri);
//                    dp.invalidate();
                    File imgFile = new  File (path);
                    if(imgFile.exists()){
                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        dp.setImageBitmap(myBitmap);
                        Log.e("Picure","Picture Set to DP");
                    }
                    //Store ImageUri to preference manager
                }
            }
        }
    }
*/

     //Get the real path from the URI

    void galleryPermissionDialog() {

        int hasWriteContactsPermission = ContextCompat.checkSelfPermission(profile.this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(profile.this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_ASK_PERMISSIONS);
            return;

        } else {
            openGallry();
        }
    }

    void openGallry() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<String, Integer>();
                // Initial
                perms.put(android.Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for READ_EXTERNAL_STORAGE

                boolean showRationale = false;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    if (perms.get(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        // All Permissions Granted
                        galleryPermissionDialog();
                    } else {
                        showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
                        if (showRationale) {
                            showMessageOKCancel("Read Storage Permission required for this app ",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            galleryPermissionDialog();

                                        }
                                    });
                        } else {
                            showMessageOKCancel("Read Storage Permission required for this app ",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Toast.makeText(profile.this, "Please Enable the Read Storage permission in permission", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                                            intent.setData(uri);
                                            startActivityForResult(intent, REQUEST_CODE_MANUAL);
                                        }
                                    });

                            //proceed with logic by disabling the related features or quit the app.
                        }


                    }


                } else {
                    galleryPermissionDialog();

                }

            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    try {
                        final Uri imageUri = imageReturnedIntent.getData();
                   /* final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    img_profile.setImageBitmap(selectedImage);*/
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        Cursor cursor = getContentResolver().query(imageUri, filePathColumn, null, null, null);
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String picturePath = cursor.getString(columnIndex);
                        cursor.close();

                        File imgFile = new  File (picturePath);
                        if(imgFile.exists()){
                            Log.e("Picure","Picture Uri " + imageUri);
                            Log.e("Picure","Picture Path " + picturePath);
                            InputStream imageStream = getContentResolver().openInputStream(imageUri);
                            Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                            selectedImage = getResizedBitmap(selectedImage, 1024);// 400 is for example, replace with desired size

                            dp.setImageBitmap(selectedImage);
                            spinner.setVisibility(View.GONE);
//                            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
//                            dp.setImageBitmap(null);
//                            dp.setImageBitmap(myBitmap);
                            preferenceManager.setDp(picturePath);
                            Log.e("Picure","Picture Set to DP");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        }
    }

    public void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(profile.this)
                .setTitle(R.string.app_name)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

}