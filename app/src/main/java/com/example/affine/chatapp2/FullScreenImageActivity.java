package com.example.affine.chatapp2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class FullScreenImageActivity extends AppCompatActivity {

    private ImageView dp;
    private String fullScreenInd;
    String dpPath;
    Uri imgUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.fullscreendp);

        dp = findViewById(R.id.imageView);
        Intent intent = getIntent();
        dpPath = intent.getExtras().getString("path");
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
                Toast.makeText(FullScreenImageActivity.this, "Your previous DP has been deleted or moved to another location", Toast.LENGTH_LONG).show();
                imgUri= Uri.parse("android.resource://com.example.affine.chatapp2/"+R.drawable.ic_abstract_user_flat_1);
                dp.setImageURI(null);
                dp.setImageURI(imgUri);

            }
        }
        else {
            Log.e("Picure","File does't exists");
            Toast.makeText(FullScreenImageActivity.this, "DP not set yet !", Toast.LENGTH_LONG).show();
            imgUri= Uri.parse("android.resource://com.example.affine.chatapp2/"+R.drawable.ic_abstract_user_flat_1);
            dp.setImageURI(null);
            dp.setImageURI(imgUri);
        }

        //fullScreenInd = getIntent().getStringExtra("fullScreenIndicator");
        fullScreenInd = "y";
        if ("y".equals(fullScreenInd)) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            //getSupportActionBar().hide();

            dp.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            dp.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
            dp.setAdjustViewBounds(true);
            dp.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }

/*        dp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(FullScreenImageActivity.this,
                        FullScreenImageActivity.class);

                if("y".equals(fullScreenInd)){
                    intent.putExtra("fullScreenIndicator", "");
                }else{
                    intent.putExtra("fullScreenIndicator", "y");
                }
                FullScreenImageActivity.this.startActivity(intent);
            }
        });*/

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