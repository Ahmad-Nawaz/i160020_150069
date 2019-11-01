package com.ahmadnawaz.i160020_150069;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import static com.ahmadnawaz.i160020_150069.Chat.Image_data_uri;
import static com.ahmadnawaz.i160020_150069.Chat.myBitmap;
import static com.ahmadnawaz.i160020_150069.Chat.s_imageview;

public class Full_Image_display extends AppCompatActivity {

    ImageView img;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full__image_display);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);  // hiding the status bar
        getSupportActionBar().hide(); // hiding the action bar

        img=findViewById(R.id.full_image_i);

        if(getIntent().hasExtra("Image_url")){
            String url = getIntent().getStringExtra("Image_url");  // creating a sorted chat id

            Toast.makeText(this, "URL is  "+url, Toast.LENGTH_LONG).show();
            Log.e("------------------url>>",url);


            Glide.with(Full_Image_display.this).asBitmap().load(url).into(img);
            Log.e("------------------url>>",url);

        }



    }
}
