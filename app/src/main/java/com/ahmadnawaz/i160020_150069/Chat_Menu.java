package com.ahmadnawaz.i160020_150069;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.ahmadnawaz.i160020_150069.MainActivity.mAuth;

public class Chat_Menu extends AppCompatActivity implements RVAdapter._ON_ClickListener, SearchView.OnQueryTextListener{
    RecyclerView rv;
    List<ContactModel> ls;
    RVAdapter adapter;
    private DatabaseReference myRef = null;
    public String sender_phone;


    CircleImageView btn;
    TextView input;
    AlertDialog alert;
    SearchView searchView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat__menu);

        DatabaseReference myRef = null;

        rv = findViewById(R.id.rv_chatmenu);
        btn=findViewById(R.id.cont_btn);

        input = new TextView(Chat_Menu.this);


        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Chat_Menu.this);
        alertDialog.setTitle("Options ");
//        alertDialog.setMessage("");

        input.setText("Meet a Friend? ");
        input.setTextSize(20);

        LinearLayout lp = new LinearLayout (this);
        lp.setOrientation(LinearLayout.VERTICAL);
        lp.addView(input);
        alertDialog.setView(lp);
        alert = alertDialog.create();




        if(mAuth!=null){

            sender_phone=mAuth.getCurrentUser().getPhoneNumber();
        }

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setTitle("Chit chat ");
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.show();




        ls = new ArrayList<>(); // ls is friend list
        myRef = FirebaseDatabase.getInstance().getReference("FriendList");
        myRef=myRef.child(sender_phone);

        adapter = new RVAdapter(ls, this, this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(Chat_Menu.this);
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(adapter);



        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String row=dataSnapshot.getKey();

                if (row.contains("GROUP->")){
                    ContactModel row1 = dataSnapshot.getValue(ContactModel.class); // adding the group to chat menu
                    ls.add(row1);
                    adapter.notifyDataSetChanged();
                }
                else{
                    DatabaseReference myRef1 = null;
                    myRef1 = FirebaseDatabase.getInstance().getReference("Users");
                    myRef1.child(row).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override

                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            ContactModel row = dataSnapshot.getValue(ContactModel.class);
                            ls.add(row);
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
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




        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Chat_Menu.this,Contact_Menu.class);
                startActivity(intent);
            }
        });


    }



    @Override
    public void onItemClick(int position) {
        alert.dismiss();

        String Reciver_phone=ls.get(position).getPhno();


        Intent intent = new Intent(Chat_Menu.this, Chat.class);

        String key=ls.get(position).getName();
        String s2="GROUP";

        if(key.length() > 2 && s2.length() > 2 && (key.indexOf (s2.substring (0, 5)) == 0)){
            intent.putExtra("Group_Created","Group_Created");
        }


        intent.putExtra("Receiver_phone",Reciver_phone );

        intent.putExtra("Receiver_profile",ls.get(position).getImage_url());

        intent.putExtra("Receiver_name", ls.get(position).getName());

        startActivity(intent);
        searchView.clearFocus();
        searchView.destroyDrawingCache();
        searchView.onActionViewCollapsed();
        searchView.onFinishTemporaryDetach();
    }

    @Override
    public void onItemLongClick(final int position) {

        alert.show();

        input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String add_coord=ls.get(position).getCoordinates();
                if (add_coord.equals(" ")) {
                    Toast.makeText(getApplicationContext(), " Address is not given by your friend so cant meet your friend",
                            Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(Chat_Menu.this, map_activity.class);
                    intent.putExtra("Address", add_coord);
                    startActivity(intent);
                    alert.dismiss();
                }

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);


        MenuItem menuitem1 = menu.findItem(R.id.search_t);
        searchView = (SearchView) menuitem1.getActionView();

        searchView.setOnQueryTextListener(this);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        alert.dismiss();

        if (item.getTitle().equals("Profile")) {
            Intent Profile = new Intent(Chat_Menu.this, ProfileActivity.class);
            startActivity(Profile);

        }
        else if (item.getTitle().equals("Log Out")) {
            AlertDialog.Builder myDialog
                    = new AlertDialog.Builder(Chat_Menu.this);
            myDialog.setTitle("Log out/No?");
            myDialog.setMessage("Are you sure you want to log out");

            myDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                // do something when the button is clicked
                public void onClick(DialogInterface arg0, int arg1) {
                    if(myRef==null){
                        myRef = FirebaseDatabase.getInstance().getReference("Users");
                    }
                    myRef.getRoot().child("Users").child(mAuth.getCurrentUser().getPhoneNumber().toString()).child("active_status").setValue("false");

                    mAuth.signOut(); // logging out of the current account
                    finish();
                }
            });
            myDialog.setNegativeButton(" No ", new DialogInterface.OnClickListener() {
                // do something when the button is clicked
                public void onClick(DialogInterface arg0, int arg1) {
                }
            });
            myDialog.show();
        }

        else if (item.getTitle().equals("Create New Group")){
            Intent intent = new Intent(Chat_Menu.this, Contact_Menu.class);
            intent.putExtra("Group_Request","Group_request");
            startActivity(intent);
        }


            return true;
    }
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }


    @Override
    public boolean onQueryTextChange(String newText) {
        String user_input;
        user_input=newText.toLowerCase();

        List<ContactModel> filteredList = new ArrayList<>();

        for (ContactModel row : ls) {
            // name match condition. this might differ depending on your requirement
            // here we are looking for name or phone number match
            if (row.getName().toLowerCase().contains(user_input) || row.getPhno().contains(user_input)) {
                filteredList.add(row);
            }
        }
        adapter.updatelist(filteredList);

        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            searchView.clearFocus();
            searchView.destroyDrawingCache();
            searchView.onActionViewCollapsed();
            searchView.setIconified(true);

            if(myRef==null){
                myRef = FirebaseDatabase.getInstance().getReference("Users");
            }
            // logging off
            myRef.getRoot().child("Users").child(mAuth.getCurrentUser().getPhoneNumber().toString()).child("active_status").setValue("false");

        }
        return super.onKeyDown(keyCode, event);
    }


}