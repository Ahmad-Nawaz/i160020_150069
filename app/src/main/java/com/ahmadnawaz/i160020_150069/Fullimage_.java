package com.ahmadnawaz.i160020_150069;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import static com.ahmadnawaz.i160020_150069.Chat.Image_data_uri;
import static com.ahmadnawaz.i160020_150069.Chat.myBitmap;
import static com.ahmadnawaz.i160020_150069.Chat.s_imageview;

public class Fullimage_ extends AppCompatActivity {


    public static TextView  Caption;
    Button send_btn;
    public static Uri Image_data_uri1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullimage_);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);  // hiding the status bar
        getSupportActionBar().hide(); // hiding the action bar

        s_imageview=findViewById(R.id.fimage);
        Caption=findViewById(R.id.fcaption);
        send_btn=findViewById(R.id.fSend);

        Image_data_uri1=Image_data_uri;

        s_imageview.setImageBitmap(myBitmap);
        if(Image_data_uri!=null) {
            Toast.makeText(this, "image data uri is " + Image_data_uri.getLastPathSegment(), Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "caption is " + Caption.getText(), Toast.LENGTH_SHORT).show();
        }

        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Fullimage_.this, Chat.class);

//                intent.putExtras(new Bundle());
//
//                intent.putExtra("name",et1.getText().toString());
//                intent.putExtra("image",myBitmap);

                setResult(4,intent);

                finish();
            }
        });

    }
}
