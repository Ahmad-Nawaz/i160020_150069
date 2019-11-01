package com.ahmadnawaz.i160020_150069;
import static android.Manifest.permission.READ_CONTACTS;
import static com.ahmadnawaz.i160020_150069.MainActivity.mAuth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Contact_Menu extends AppCompatActivity implements RVAdapter._ON_ClickListener, SearchView.OnQueryTextListener {
    RecyclerView rv;
    List<ContactModel> ls;
    RVAdapter adapter;
    private DatabaseReference myRef = null;
    Map<String, String> contacts;

    Map<String, Integer> group_memebers;
    Button groupcreate_btn;
    SearchView searchView;

    public String sender_phone;
    public String Reciever_phone;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact__menu);

        groupcreate_btn=findViewById(R.id.Group_create);

        rv = findViewById(R.id.rv);






        group_memebers = new HashMap<String,Integer>();
        group_memebers.put(mAuth.getCurrentUser().getPhoneNumber(),1);// adding the admin to the group first
        sender_phone=mAuth.getCurrentUser().getPhoneNumber();

        contacts = new HashMap<String, String>();


        final ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setTitle("Contacts ");
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.show();


        ls = new ArrayList<>();
        myRef = FirebaseDatabase.getInstance().getReference("Users");
        getPhoneNumbers();  // getting the users from the phonebook and loading our app users's contacts in the ls

        adapter = new RVAdapter(ls, this, this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(Contact_Menu.this);
        rv.setLayoutManager(layoutManager);

        if(getIntent().hasExtra("Group_Request")){  // i-e activating the selected contact background color on selection while creating group
            adapter.Active_group_status();
        }




        rv.setAdapter(adapter);

        displaying_contacts();



        groupcreate_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(group_memebers.size()>1){
                    final EditText input1;
                    input1 = new EditText(Contact_Menu.this);

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(Contact_Menu.this);
                    alertDialog.setTitle("Group name");
                    alertDialog.setMessage("Enter group name");
                    LinearLayout lp = new LinearLayout (Contact_Menu.this);
                    lp.setOrientation(LinearLayout.VERTICAL);
                    lp.addView(input1);

                    alertDialog.setView(lp);

                    alertDialog.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    final String group_name = input1.getText().toString();
                                    if(group_name.isEmpty() || group_name==null){
                                        Toast.makeText(Contact_Menu.this, "Invalid group name ", Toast.LENGTH_SHORT).show();
                                    }
                                    else{  // valid group name
                                        final String group_identifier="GROUP->"+group_name+sender_phone;
                                        myRef.getRoot().child("FriendList").child(sender_phone).child(group_identifier).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.getValue()==null) { // i-e if the group doesnot exist
                                                    Create_group(group_name,group_identifier);
                                                }
                                                else{
                                                    Toast.makeText(Contact_Menu.this, "Group "+group_name+" already exists ", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });

                                    }
                                }
                            });

                    alertDialog.setNegativeButton("NO",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                    alertDialog.show();

                }
                else{
                    Toast.makeText(Contact_Menu.this, "No member is selected to create group", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void displaying_contacts(){
        myRef=myRef.getRoot().child("Users");
        for (Map.Entry<String, String> entry : contacts.entrySet()) {
            String key = entry.getKey(); // key is the phone number
            myRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue()!=null) {
                        ContactModel row=dataSnapshot.getValue(ContactModel.class);
                        ls.add(row);
                        adapter.notifyDataSetChanged();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }


    public void Create_group(String group_name, String group_identifier){

        ContactModel group=new ContactModel(group_name,null,group_identifier,null,null,"true");    // dealing a contact as a group
        if(myRef==null){
            myRef = FirebaseDatabase.getInstance().getReference("FriendList");
        }
        else {
            myRef=myRef.getRoot().child("FriendList");
        }


        //adding the group to the member's chat list
        for (Map.Entry<String, Integer> entry : group_memebers.entrySet()) {
            String key = entry.getKey();
            int value = entry.getValue();
            if(value==1){ // just for double checking
                myRef.child(key).child(group_identifier).setValue(group);
            }
        }
        Intent intent = new Intent(Contact_Menu.this, Chat.class);
        intent.putExtra("Contact_Menu", "Contact_Menu");
        intent.putExtra("Group_Created","Group_Created");
        intent.putExtra("Receiver_phone", group_identifier);
        intent.putExtra("Receiver_name", group_name);

        startActivity(intent);
    }

    @Override
    public void onItemClick(int position) {

        if(getIntent().hasExtra("Group_Request")){  // group has to be created
            if(group_memebers.containsKey(ls.get(position).getPhno())){ // if already has means row is clicked twice so remove it i-e make click equal to 0 or simply remove the entry from map
                group_memebers.remove(ls.get(position).getPhno());
                adapter.notifyDataSetChanged();
            }
            else{
                group_memebers.put(ls.get(position).getPhno(),1);// adding the clicked members to the group
                adapter.notifyDataSetChanged();
            }
            if(group_memebers.size()>1) {
                groupcreate_btn.setVisibility(View.VISIBLE);
            }
            else{
                groupcreate_btn.setVisibility(View.GONE);
            }

        }

        else {

            // intent is coming from contacts else coming from the group options
            String Reciver_phone = ls.get(position).getPhno();

            Reciever_phone = ls.get(position).getPhno();

            Intent intent = new Intent(Contact_Menu.this, Chat.class);
            intent.putExtra("Receiver_phone", Reciver_phone);
            intent.putExtra("Receiver_name", ls.get(position).getName());
            intent.putExtra("Contact_Menu", "Contact_Menu");
            intent.putExtra("Receiver_profile",ls.get(position).getImage_url());
            startActivity(intent);
            searchView.clearFocus();
            searchView.destroyDrawingCache();
            searchView.onActionViewCollapsed();
            searchView.onFinishTemporaryDetach();
        }
    }

    @Override
    public void onItemLongClick(final int position) {

    }

    private void getPhoneNumbers() {  // move it in background


        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        // Loop Through All The Numbers
        while (phones.moveToNext()) {
//            Log.d(">>>",phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER)));
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            // Cleanup the phone number
            phoneNumber = phoneNumber.replaceAll("[()\\s-]+", "");

            // formatting the format number
            phoneNumber = phoneNumber.replaceAll(" ", ""); // replacing spaces

            if (phoneNumber.charAt(0) == '0') {
                phoneNumber = phoneNumber.replaceFirst("0", "+92");

            }

            if (phoneNumber.charAt(0) == '+' && !phoneNumber.equals(sender_phone) && phoneNumber.length()>11) {
                contacts.put(phoneNumber, phoneNumber);
            }
        }

        phones.close();
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
        if (item.getTitle().equals("Profile")) {
            Intent Profile = new Intent(Contact_Menu.this, ProfileActivity.class);
            startActivity(Profile);
//            finish();

        } else if (item.getTitle().equals("Log Out")) {
            AlertDialog.Builder myDialog
                    = new AlertDialog.Builder(Contact_Menu.this);
            myDialog.setTitle("Log out/No?");
            myDialog.setMessage("Are you sure you want to log out");

            myDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                // do something when the button is clicked
                public void onClick(DialogInterface arg0, int arg1) {
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
            Intent intent = new Intent(Contact_Menu.this, Contact_Menu.class);
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

        }
        return super.onKeyDown(keyCode, event);
    }


}