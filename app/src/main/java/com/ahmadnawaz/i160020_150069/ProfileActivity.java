package com.ahmadnawaz.i160020_150069;

import androidx.annotation.IntegerRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.ahmadnawaz.i160020_150069.MainActivity.mAuth;

public class ProfileActivity extends AppCompatActivity  {

    Button save;
    EditText name;

    TextView address,ph_no;

    String coord_add;
    Button image_button;
    ImageView imageview = null;
    ByteArrayOutputStream bs=null;
    private int GALLERY = 2, CAMERA = 3;
    Bitmap myBitmap = null;
    int AUTOCOMPLETE_REQUEST_CODE = 4;
    Place place;
    Uri Image_data_uri;
    String url;
    ProgressDialog progressDialog;

    String prev_url=null;

    String sender_activity;

    private StorageReference storageReference;
    private DatabaseReference myRef;
    String phone;
    String prev_address;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        save = findViewById(R.id.save);
        name = findViewById(R.id.name);
        ph_no = findViewById(R.id.phone);
        address = findViewById(R.id.address);

        progressDialog = new ProgressDialog(ProfileActivity.this);
//        if(mAuth!=null) {
        phone = mAuth.getCurrentUser().getPhoneNumber();
//        }
        ph_no.setText(phone); // setting the text with current user phone number

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setTitle(" Profile ");
        actionBar.show();


        if(getIntent().hasExtra("Signup_signall")){
            sender_activity = getIntent().getStringExtra("Signup_signall");
        }



        myRef=  FirebaseDatabase.getInstance().getReference("Users");
        myRef=myRef.child(phone);


        name.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        image_button = findViewById(R.id.img_btn);
        imageview=findViewById(R.id.IV1);

        storageReference = FirebaseStorage.getInstance().getReference();

        phone=ph_no.getText().toString();


        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//

                if(dataSnapshot.getKey().equals("address")) {
                    prev_address = dataSnapshot.getValue().toString();
                    address.setText(prev_address);
                }
                else if(dataSnapshot.getKey().equals("image_url")) {
                    prev_url = dataSnapshot.getValue().toString();
                    Glide.with(ProfileActivity.this).asBitmap().load(prev_url).into(imageview);
//                    Toast.makeText(ProfileActivity.this, "image url is " + prev_url, Toast.LENGTH_SHORT).show();
                }
                else if(dataSnapshot.getKey().equals("name")) {
                    String name1 = dataSnapshot.getValue().toString();
                    name.setText(name1);
//                    Toast.makeText(ProfileActivity.this, "name is " + name1, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

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










        // taking address using google map intent
        if(!Places.isInitialized()){
            Places.initialize(getApplicationContext(), getString(R.string.API_KEY));

        }
        address.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG);
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.FULLSCREEN, fields)
                        .build(ProfileActivity.this);
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);

            }

        });


        image_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestMultiplePermissions();
                showpictureDialog();
            }
        });




        save.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onClick(View view) {
//                Toast.makeText(ProfileActivity.this, "place is save is "+place, Toast.LENGTH_SHORT).show();
                if(place!=null) {
                    coord_add=(double) place.getLatLng().latitude + ";" + (double) place.getLatLng().longitude;
                }
                else{
                    coord_add=" ";
                }

                image_work();
                if(sender_activity!=null && sender_activity.equals("signup")){ //Signup has called the profile
//                    Toast.makeText(ProfileActivity.this, "Signup has called the profile", Toast.LENGTH_SHORT).show();
                    sender_activity=null;
                    Intent contact_menuI=new Intent(ProfileActivity.this,Chat_Menu.class); // first time opening the contact list
                    startActivity(contact_menuI);
                }
                else{  // it means already called from contact list and no need to make contact menu intent again
//                    Toast.makeText(ProfileActivity.this, "Contact menu has called the profile", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });

    }
    private void image_work(){
        if(Image_data_uri!=null){

            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            final StorageReference Imageref =  storageReference.child("my_Folder").child("app_image"+Image_data_uri.getLastPathSegment());

            Imageref.putFile(Image_data_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    Toast.makeText(ProfileActivity.this, " Uploaded ", Toast.LENGTH_SHORT).show();

                    Imageref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            url=uri.toString();
//                            Toast.makeText(ProfileActivity.this, " Uploaded and url is "+url, Toast.LENGTH_SHORT).show();
                            ContactModel contact=new ContactModel(
                                    name.getText().toString(),
                                    address.getText().toString(),
                                    phone,
                                    url,
                                    coord_add,
                                    "true"
                            );
                            myRef.setValue(contact);
                            if(progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            finish();
                        }
                    });
                    // Uri downloadUrl = taskSnapshot.getDownloadUrl();
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if(progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                        }
                    });
        }
        else{
            ContactModel contact=new ContactModel(
                    name.getText().toString(),
                    address.getText().toString(),
                    phone,
                    prev_url,
                    coord_add,
                    "true"
            );
            myRef.setValue(contact);
            finish();
        }

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
            address.setText(prev_address);
            place = null;
            return;
        }
        Image_data_uri = null;
        if (requestCode == GALLERY) {
            if (data != null) {
                Image_data_uri=data.getData();
                try {
                    myBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Image_data_uri);
                    Toast.makeText(ProfileActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();
                    Bitmap myBitmap1 = Bitmap.createScaledBitmap (myBitmap, 1000, 1000, false);
                    myBitmap=saveImage(myBitmap1);
                    imageview.setImageBitmap(myBitmap);


                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(ProfileActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            }

        } else if (requestCode == CAMERA) {
            Image_data_uri=data.getData();

            myBitmap = (Bitmap) data.getExtras().get("data");
            imageview.setImageBitmap(myBitmap);
            try {
                saveImage(myBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(ProfileActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();
        }
        else if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            place = null;
            address.setText(" ");
            if (resultCode == RESULT_OK) {
                place = Autocomplete.getPlaceFromIntent(data);
//                Log.i("as-------------->", "Place:-----------> " + place.getName() + ", " + place.getId() + ", " + place.getLatLng());
//                Toast.makeText(ProfileActivity.this, "place before is "+place.getName(), Toast.LENGTH_SHORT).show();
                address.setText(place.getName());
//                Toast.makeText(ProfileActivity.this, "place after is "+place.getName(), Toast.LENGTH_SHORT).show();
//                Toast.makeText(ProfileActivity.this, "place after is "+address.getText(), Toast.LENGTH_SHORT).show();

            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
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
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bs);
        return myBitmap;
    }

    @Override
    protected void onDestroy() {
        if(progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        super.onDestroy();

    }


}
