package com.ahmadnawaz.i160020_150069;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;

import static android.os.Looper.prepare;
import static com.ahmadnawaz.i160020_150069.Fullimage_.Caption;
import static com.ahmadnawaz.i160020_150069.Fullimage_.Image_data_uri1;
import static com.ahmadnawaz.i160020_150069.MainActivity.mAuth;

public class Chat extends AppCompatActivity implements Message_Adapter._ON_ClickListener {

    RecyclerView rv1;
    List<MessageModel> ls;
    Message_Adapter adapter;

    private DatabaseReference myRef;

    Button send;
    ImageView send_img;

    TextView message;
    String Reciever_Phone,Reciever_name;
    String Sender_phone;
    public static Uri Image_data_uri;
    String url=null;

    String chat_id_ph="";
    String Reciever_phone="";

    private int GALLERY = 2, CAMERA = 3,F_IMAGE=4;

    public static Bitmap myBitmap = null;
    public static ImageView s_imageview = null;

    ByteArrayOutputStream bs=null;

    private StorageReference storageReference;
    String messg;
    int send_value=0;
    Button Record_audio;
    private MediaRecorder recorder;
    private String mfilename=null;
    private StorageReference audio_storageReference;
    Uri uri_audio;
    String audio_url;
    static public String reciever_profile_url=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        send=findViewById(R.id.m_send_btn);
        send_img=findViewById(R.id.send_img);
        message=findViewById(R.id.message_tv);
        rv1=findViewById(R.id.rv1);
        Record_audio=findViewById(R.id.m_record_btn);

        Reciever_name = getIntent().getStringExtra("Receiver_name");

        mfilename= Environment.getExternalStorageDirectory().getAbsolutePath();
        mfilename+="/RECORDED_audio.3gb";



        final ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setTitle(Reciever_name);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.show();
        Sender_phone = mAuth.getCurrentUser().getPhoneNumber();
//        if (row.contains("GROUP->")){
//            ContactModel row1 = dataSnapshot.getValue(ContactModel.class); // adding the group to chat menu
//            ls.add(row1);
//            adapter.notifyDataSetChanged();
//        }
        Reciever_Phone=getIntent().getStringExtra("Receiver_phone");

        if(getIntent().hasExtra("Group_Created") || Reciever_Phone.contains("GROUP->")){ // means groupchat is opened
            chat_id_ph = Reciever_Phone;  // creating a sorted chat id with the group name
        }
        else { // means particular contact is added

            reciever_profile_url=getIntent().getStringExtra("Receiver_profile");

            Reciever_Phone = getIntent().getStringExtra("Receiver_phone");

            List<String> numbers = new ArrayList<>();
            numbers.add(Sender_phone);
            numbers.add(Reciever_Phone);

            Collections.sort(numbers);
            chat_id_ph = numbers.get(0) + " : " + numbers.get(1);  // creating a sorted chat id

        }

        myRef = FirebaseDatabase.getInstance().getReference("Chats");
        myRef=myRef.child(chat_id_ph);

        storageReference = FirebaseStorage.getInstance().getReference();
        audio_storageReference=FirebaseStorage.getInstance().getReference();

        ls=new ArrayList<>();
        adapter=new Message_Adapter(ls,Chat.this,this);
        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(Chat.this);
        rv1.setLayoutManager(layoutManager);
        rv1.setAdapter(adapter);

        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                MessageModel row=dataSnapshot.getValue(MessageModel.class);
                ls.add(row);

                adapter.notifyDataSetChanged();
                rv1.scrollToPosition(ls.size()-1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                MessageModel row=dataSnapshot.getValue(MessageModel.class);
                ls.add(row);
                adapter.notifyDataSetChanged();
                rv1.smoothScrollToPosition(ls.size() - 1);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }


            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload_work();

                if((!getIntent().hasExtra("Group_Created")) && getIntent().hasExtra("Contact_Menu") &&  send_value==1){
                    myRef.getRoot().child("FriendList").child(Sender_phone).child(Reciever_Phone).setValue(Reciever_Phone);

                    myRef.getRoot().child("FriendList").child(Reciever_Phone).child(Sender_phone).setValue(Sender_phone);
                    send_value=send_value+1;
                }

                if(send_value==2){
                    myRef=myRef.getRoot().child("Chats").child(chat_id_ph);
                }


            }
        });

        send_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestMultiplePermissions();
                showpictureDialog();
            }
        });




        Record_audio.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    startRecording();
                }
                else if(event.getAction()==MotionEvent.ACTION_UP){
                    stopRecording();
                }
                return false;
            }
        });








    }

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(mfilename);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
            Toast.makeText(this, "Recording started ", Toast.LENGTH_SHORT).show();
            recorder.start();
        } catch (IOException e) {
        }

    }

    private void stopRecording() {
        try {
            recorder.stop();
            recorder.release();
            recorder = null;
            uploadAudio();
        } catch(Exception e) {}
    }
    private void  uploadAudio(){

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
        String format = simpleDateFormat.format(new Date());

//        final StorageReference Audioref =  storageReference.child("Audios").child("app_image"+Image_data_uri.getLastPathSegment()+"new_audio.3gb");
        uri_audio=Uri.fromFile(new File((mfilename)));

        final StorageReference Audioref =  storageReference.child("Audios").child(format+uri_audio.getLastPathSegment());
        Toast.makeText(this, "Uplaod audio "+uri_audio.getLastPathSegment(), Toast.LENGTH_SHORT).show();

        Audioref.putFile(uri_audio).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(Chat.this, "Recorded and saved successfully", Toast.LENGTH_SHORT).show();
                Audioref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
                        String format = simpleDateFormat.format(new Date());

                        audio_url = uri.toString();
                        MessageModel message_s=new MessageModel(
                                messg,
                                null,
                                Sender_phone,
                                null,
                                audio_url,
                                format
                        );
                        myRef.push().setValue(message_s);
                        send_value=send_value+1;
                        url=null; // updating the url in case if their is next image


                    }
                });
            }
        });

        // show the button on the image
        adapter.notifyDataSetChanged();
    }

    public void playAudio(){
        MediaPlayer mediaPlayer=new MediaPlayer();
        try{
            mediaPlayer.setDataSource(audio_url);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
            mediaPlayer.prepare();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }




    @Override
    public void onItemClick(int position) {

        if(ls.get(position).getVoiceurl()!=null){
            audio_url=ls.get(position).getVoiceurl();
            playAudio();
        }
        else if(ls.get(position).getImage_url()!=null){
            Intent intent=new Intent(Chat.this,Full_Image_display.class);
            intent.putExtra("Image_url",ls.get(position).getImage_url());
            startActivity(intent);
        }
    }

    @Override
    public void onItemLongClick(int position) {
    }


    private void upload_work(){

        if(Image_data_uri!=null){
            final StorageReference Imageref =  storageReference.child("my_Folder").child("app_image"+Image_data_uri.getLastPathSegment());

            Imageref.putFile(Image_data_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    messg=message.getText().toString();
                    String temp=Caption.getText().toString();

                    if((temp!=null && !temp.equals(""))){
                        messg=temp;
                        Caption.setText("");
                    }
                    else if((messg==null || messg.equals(""))){
                        messg=null;
                    }

                    Toast.makeText(Chat.this, "uploaded", Toast.LENGTH_SHORT).show();



                    Imageref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss");
                            String format = simpleDateFormat.format(new Date());

                            url=uri.toString();
                            MessageModel message_s=new MessageModel(
                                    messg,
                                    url,
                                    Sender_phone,
                                    null,
                                    null,
                                    format
                            );
                            myRef.push().setValue(message_s);
                            send_value=send_value+1;
                            url=null; // updating the url in case if their is next image

                        }
                    });

                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Chat.this, "image loading failed", Toast.LENGTH_SHORT).show();
                        }
                    });


        }
        else{ /// if image is not need to be uploaded
//            Toast.makeText(Chat.this, "message send", Toast.LENGTH_SHORT).show();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm");
            String format = simpleDateFormat.format(new Date());
            MessageModel message_s=new MessageModel(
                    message.getText().toString(),
                    null,
                    Sender_phone,
                    null,
                    null,
                    format
            );
            myRef.push().setValue(message_s);
            url=null; // updating the url in case if their is next image
            Toast.makeText(Chat.this, "message sent", Toast.LENGTH_SHORT).show();
            send_value=send_value+1;
        }
//        Toast.makeText(this, "OUT of sending", Toast.LENGTH_SHORT).show();
        Image_data_uri=null; // in case if upload work is not done
        message.setText("");
    }

    public void showpictureDialog(){
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {
                "Select photo from gallery",
                "Capture photo from camera" };
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                choosePhotoFromGallary();
                                break;
                            case 1:
                                takePhotoFromCamera();
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }
    public void choosePhotoFromGallary() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(galleryIntent, GALLERY);
    }
    public void takePhotoFromCamera() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {
            return;
        }
        Image_data_uri = null;
        if (requestCode == GALLERY) {
            if (data != null) {
                Image_data_uri=data.getData();
                try {
                    myBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Image_data_uri);
                    Toast.makeText(Chat.this, "Image Saved!", Toast.LENGTH_SHORT).show();
                    Bitmap myBitmap1 = Bitmap.createScaledBitmap (myBitmap, 1000, 1000, false);
                    myBitmap=saveImage(myBitmap1);

                    Intent intent=new Intent(Chat.this,Fullimage_.class);  // for image caption
                    startActivityForResult(intent,F_IMAGE);


                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(Chat.this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            }

        }
        else if (requestCode == CAMERA) {
            if (data != null) {
                myBitmap = (Bitmap) data.getExtras().get("data");  // taking a camera photo
                Bitmap myBitmap1 = Bitmap.createScaledBitmap(myBitmap, 1000, 1000, false);
                Image_data_uri=getImageUri(getApplicationContext(),myBitmap1);

                Toast.makeText(Chat.this, "Image Saved!"+Image_data_uri.getLastPathSegment(), Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(Chat.this, Fullimage_.class);  // for image caption
                startActivityForResult(intent, F_IMAGE);
            }
        }
        else if(requestCode==F_IMAGE){

            Image_data_uri=Image_data_uri1;
            message.setText(Caption.getText());

            upload_work(); // loading the picture after taking it
        }


    }
    private void  requestMultiplePermissions(){
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            Toast.makeText(getApplicationContext(), "All permissions are granted by user!", Toast.LENGTH_SHORT).show();
                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
                            //openSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), "Some Error! ", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }
    public Bitmap saveImage(Bitmap myBitmap) throws IOException {
        bs = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 50, bs);
        return myBitmap;
    }

    private Uri getImageUri(Context applicationContext, Bitmap photo) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(Chat.this.getContentResolver(), photo, "Title", null);
        return Uri.parse(path);
    }

}
