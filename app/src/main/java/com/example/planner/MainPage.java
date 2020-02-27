package com.example.planner;

//a lot of imports
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.content.Intent;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

//firebase imports
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

//Notification imports
import com.allyants.notifyme.NotifyMe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

//Tags import
import me.gujun.android.taggroup.TagGroup;

public class
MainPage extends AppCompatActivity {


    //Tag
    private static final String TAG = "MainPage";

    //Buttons
    private Button Logout;
    private Button NewEvent;
    private Button ClearSearch;

    //Imageviews
    private ImageView Settings;
    private ImageView Search;

    //Listviews
    private ListView lvEvents;


    //Dialogs
    private Dialog EventInfo;
    private Dialog TagsOverview;
    private Dialog SearchTags;

    //Calendar stuff
    private Calendar mCurrentDate;
    private Calendar currentTime;
    private int hour, minute;
    private int day, month, year;

    //TAGS
    private TagGroup TagsInfo;
    private List<String> TagListInfo = new ArrayList<String>();


    //firebase stuff
    private FirebaseAuth mAuth;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //checks for internet connection
        if(connected()){
        }else{
            Toast.makeText(MainPage.this,"Please check your network connection! You are not connected :(",Toast.LENGTH_LONG).show();
        }

        setContentView(R.layout.activity_main_page);

        //Dialogs
        SearchTags = new Dialog(this);
        SearchTags.setCanceledOnTouchOutside(false);

        TagsOverview = new Dialog(MainPage.this);
        TagsOverview.setCanceledOnTouchOutside(false);

        //Initialise Buttons
        Logout = (Button) findViewById(R.id.btnLogout);
        NewEvent = (Button) findViewById(R.id.btnGoToNewEvent);
        ClearSearch = (Button) findViewById(R.id.btnClearSearch);

        //Initialise Imageviews
        Settings = (ImageView) findViewById(R.id.btnSettings);
        Search = (ImageView) findViewById(R.id.btnSearchTags);


        //Initialise Listviews
        lvEvents = (ListView) findViewById(R.id.lvEvents);


        //firebase stuff
        mAuth = FirebaseAuth.getInstance();

        //Create new Event
        NewEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewEvent();
            }
        });

        //logout
        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });

        //CALENDAR & CLOCK STUFF
        mCurrentDate = Calendar.getInstance();
        currentTime = Calendar.getInstance();

        hour = currentTime.get(Calendar.HOUR_OF_DAY);
        minute = currentTime.get(Calendar.MINUTE);

        day = mCurrentDate.get(Calendar.DAY_OF_MONTH);
        month = mCurrentDate.get(Calendar.MONTH);
        year = mCurrentDate.get(Calendar.YEAR);

        //List with events
        final List<String[]> ItemsList = new LinkedList<String[]>();

        //custom adapter so that every item in the listview has two rows (+subitem)
        final ArrayAdapter<String[]> adapter = new ArrayAdapter<String[]>(MainPage.this,android.R.layout.simple_list_item_2, android.R.id.text1, ItemsList){
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                View view = super.getView(position, convertView, parent);

                String[] entry = ItemsList.get(position);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                text1.setText(entry[0]);
                text2.setText(entry[1]);



                return view;

            }

        };


        //sets adapter
        lvEvents.setAdapter(adapter);

        //firebase stuff
        FirebaseUser currentUser = mAuth.getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();

        //gets email, emails are saved with commas instead of dots (firebase doesn't allow dots)
        final String email = currentUser.getEmail().replace('.', ',');

        //first reference, gets key (name) of every event
        myRef.child(email + "/events").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot uniqueKeySnapshot : dataSnapshot.getChildren()){

                    //gets key
                    String EventName = uniqueKeySnapshot.getKey();
                    FirebaseDatabase database = FirebaseDatabase.getInstance();

                    //second reference, gets data of event (key)
                    DatabaseReference ref2 = database.getReference(email + "/events/" + EventName);
                    ref2.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String name = dataSnapshot.child("name").getValue().toString();
                            String date = dataSnapshot.child("date").getValue().toString();
                            String time = dataSnapshot.child("time").getValue().toString();
                            String DateTime = date + ", " + time;

                            //updates adapter
                            adapter.notifyDataSetChanged();

                            //every account has an "ignore" event so that all events can be deleted, this event will not be shown in the listview
                            if (name.matches("ignore") != true){
                                //event will be added to the list
                                ItemsList.add(new String[]{name, DateTime});
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(MainPage.this,"read failed" + databaseError.getCode(),Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }


        });

        //onClickListener for every event
        lvEvents.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String[] entry = ItemsList.get(i);
                String name = entry[0];

                //firebase stuff
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference refName = database.getReference(email + "/events/" + name);

                //searches for event that was clicked on, firebase reference
                refName.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        //gets information
                        String name = dataSnapshot.child("name").getValue().toString();
                        String date = dataSnapshot.child("date").getValue().toString();
                        String time = dataSnapshot.child("time").getValue().toString();
                        String notes = dataSnapshot.child("notes").getValue().toString();
                        String additionalNotification = dataSnapshot.child("additionalNotification").getValue().toString();


                        //opens popup containing event info
                        ShowPopup(name, date, time, notes, additionalNotification);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("The read failed: " + databaseError.getCode());
                    }
                });






            }
        });

        //goes to settings
        Settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(MainPage.this, Settings.class);
                MainPage.this.startActivity(myIntent);
                //animation
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        //opens Search Popup
        Search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SearchTags();
            }
        });


        //If a filter was set, it can be cleared by clicking this button
        ClearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //clears all events and fills the listview again, similar to the one in onCreate
                lvEvents.setAdapter(null);
                final List<String[]> ItemsList = new LinkedList<String[]>();

                final ArrayAdapter<String[]> adapter = new ArrayAdapter<String[]>(MainPage.this,android.R.layout.simple_list_item_2, android.R.id.text1, ItemsList){
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent){
                        View view = super.getView(position, convertView, parent);
                        String[] entry = ItemsList.get(position);
                        TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                        TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                        text1.setText(entry[0]);
                        text2.setText(entry[1]);
                        return view;

                    }

                };

                FirebaseUser currentUser = mAuth.getCurrentUser();
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference();


                final String email = currentUser.getEmail().replace('.', ',');

                myRef.child(email + "/events").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for(DataSnapshot uniqueKeySnapshot : dataSnapshot.getChildren()){

                            String EventName = uniqueKeySnapshot.getKey();
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference ref2 = database.getReference(email + "/events/" + EventName);


                            // Attach a listener to read the data at our posts reference
                            ref2.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    String name = dataSnapshot.child("name").getValue().toString();
                                    String date = dataSnapshot.child("date").getValue().toString();
                                    String time = dataSnapshot.child("time").getValue().toString();
                                    String DateTime = date + ", " + time;
                                    adapter.notifyDataSetChanged();

                                    if (name.matches("ignore") != true){
                                        ItemsList.add(new String[]{name, DateTime});
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    System.out.println("The read failed: " + databaseError.getCode());
                                }
                            });
                        }
                    }



                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }


                });

                //hides button
                ClearSearch.setVisibility(View.GONE);
                lvEvents.setAdapter(adapter);
            }
        });


    }


    //Change UI according to user data
    public void  updateUI(FirebaseUser account){
        if(account != null){
            String email = account.getEmail();
        }else {
            //goes to Login page
            Intent myIntent = new Intent(MainPage.this, MainActivity.class);
            MainPage.this.startActivity(myIntent);

        }
    }

    //goes to new event page
    private void NewEvent(){
        Intent myIntent = new Intent(MainPage.this, EventCreator.class);
        MainPage.this.startActivity(myIntent);
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);

    }


    //logs user out
    private void logout(){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this,MainActivity.class));
    }

    //shows popup containing event info
    public void ShowPopup(final String name, final String date, final String time, final String notes, final String additionalNotification){
        //creates dialog
        EventInfo = new Dialog(this);
        //dialog can't be closed by tapping outside
        EventInfo.setCanceledOnTouchOutside(false);
        EventInfo.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //textviews
        final TextView tvClosePopup;

        //imageviews
        final ImageView ivEditEvent;
        final ImageView ivDelete;
        final ImageView ivSeeTags;

        //changes contentview to popup
        EventInfo.setContentView(R.layout.eventpopup);

        //textviews
        final TextView tvPopupTitle;
        final TextView tvPopupDate;
        final TextView tvPopupTime;
        final TextView tvPopupNotes;
        final TextView tvNotification;

        //--CLOSE-BUTTON (TEXTVIEW)
        tvClosePopup = (TextView) EventInfo.findViewById(R.id.tvClosePopup);

        //--IMAGE-BUTTONs (IMAGEVIEW)
        ivEditEvent = (ImageView) EventInfo.findViewById(R.id.ivEditEvent);
        ivDelete = (ImageView) EventInfo.findViewById(R.id.ivDelete);
        ivSeeTags = (ImageView) EventInfo.findViewById(R.id.ivSeeTags);

        //--INFO (TEXTVIEW)
        tvPopupTitle = (TextView) EventInfo.findViewById(R.id.tvPopupTitle);
        tvPopupDate = (TextView) EventInfo.findViewById(R.id.tvPopupDate);
        tvPopupTime = (TextView) EventInfo.findViewById(R.id.tvPopupTime);
        tvPopupNotes = (TextView) EventInfo.findViewById(R.id.tvPopupNotes);
        tvNotification = (TextView) EventInfo.findViewById(R.id.tvNotification);

        //tags overview
        ivSeeTags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowTagsOverview(name);
            }
        });

        //delete event
        ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //firebase stuff
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                FirebaseUser currentUser = mAuth.getCurrentUser();
                //gets user
                final String email = currentUser.getEmail().replace('.', ',');
                DatabaseReference DeleteEvent = database.getReference(email + "/events/").child(name);

                //removes data
                DeleteEvent.removeValue();

                //removes default notification
                NotifyMe.cancel(getApplicationContext(),name);

                //removes additional notification if one has been set
                if (additionalNotification.matches("none") == false){
                    NotifyMe.cancel(getApplicationContext(),name + "_additional");
                }

                //closes popup
                EventInfo.dismiss();

                //resets mainpage to avoid crashes (don't know why this is necessary)
                Intent myIntent = new Intent(MainPage.this, MainPage.class);
                MainPage.this.startActivity(myIntent);
            }
        });

        //Button to close popup
        tvClosePopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventInfo.dismiss();

            }
        });

        //shows dialog
        EventInfo.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        EventInfo.show();

        //fills in data
        tvPopupTitle.setText(name);
        tvPopupDate.setText(date);
        tvPopupTime.setText(time);
        tvPopupNotes.setText(notes);
        if (additionalNotification.matches("none") == false){
            tvNotification.setText("Notification: " + additionalNotification + " before");
        } else {
            tvNotification.setText("No additional notification was set.");
        }



        //edit event
        ivEditEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvPopupTitle.setVisibility(View.GONE);
                tvPopupDate.setVisibility(View.GONE);
                tvPopupTime.setVisibility(View.GONE);
                tvPopupNotes.setVisibility(View.GONE);
                ivEditEvent.setVisibility(View.GONE);
                tvClosePopup.setVisibility(View.GONE);
                tvNotification.setVisibility(View.GONE);
                ivSeeTags.setVisibility(View.GONE);
                editEvent(name, date, time, notes);


            }
        });
    }

    //edit event method
    public void editEvent(final String oldName, final String oldDate, final String oldTime, final String oldNotes){

        setContentView(R.layout.activity_main_page);

        //edittexts
        final EditText etPopupTitle;
        final EditText etPopupDate;
        final EditText etPopupTime;
        final EditText etPopupNotes;

        //imageviews
        ImageView ivConfirmChanges;

        //initialises edittexts
        etPopupTitle = (EditText) EventInfo.findViewById(R.id.etPopupTitle);
        etPopupDate = (EditText) EventInfo.findViewById(R.id.etPopupDate);
        etPopupTime = (EditText) EventInfo.findViewById(R.id.etPopupTime);
        etPopupNotes = (EditText) EventInfo.findViewById(R.id.etPopupNotes);

        //initialises imageviews
        ivConfirmChanges = (ImageView) EventInfo.findViewById(R.id.ivConfirmChanges);


        //shows edittexts
        etPopupTitle.setVisibility(View.VISIBLE);
        etPopupDate.setVisibility(View.VISIBLE);
        etPopupTime.setVisibility(View.VISIBLE);
        etPopupNotes.setVisibility(View.VISIBLE);

        //sets data
        etPopupTitle.setText(oldName);
        etPopupDate.setText(oldDate);
        etPopupTime.setText(oldTime);
        etPopupNotes.setText(oldNotes);

        //shows button to confirm changes
        ivConfirmChanges.setVisibility(View.VISIBLE);



        //opens calendar dialog where you can pick a date
        etPopupDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(MainPage.this,  R.style.TimePickerTheme,  new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        month = month + 1;
                        String fm=""+month;
                        String fd=""+day;
                        if(month<10){
                            fm ="0"+month;
                        }
                        if (day<10){
                            fd = "0" + day;
                        }
                        etPopupDate.setText(fd + "." + fm + "." + year);
                    }
                }, year, month, day);
                datePickerDialog.show();
            }
        });

        //shows time dialog where you can pick a time
        etPopupTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(MainPage.this,  R.style.TimePickerTheme, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                        String fh = ""+hour;
                        String fm = ""+minute;
                        if (hour <10){
                            fh = "0" + hour;
                        }
                        if (minute <10){
                            fm = "0" + minute;
                        }
                        etPopupDate.setText(fh + ":" + fm);
                    }
                }, hour, minute, true);
                timePickerDialog.show();
            }
        });


        //confirm changes button
        ivConfirmChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newName = etPopupTitle.getText().toString();
                String newDate = etPopupDate.getText().toString();
                String newTime = etPopupTime.getText().toString();
                String newNotes = etPopupNotes.getText().toString();



                confirmChanges(oldName, newName, newDate, newTime, newNotes);


            }
        });


    }

    //confirm changes method
    public void confirmChanges(String oldName, String newName, String newDate, String newTime, String newNotes){

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference mDatabaseReference = database.getReference();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        final String email = currentUser.getEmail().replace('.', ',');

        DatabaseReference DeleteEvent = database.getReference(email + "/events/").child(oldName);

        //deletes event
        DeleteEvent.removeValue();

        //replaces event
        EventY newEvent = new EventY(newName, newDate, newNotes, newTime, "", "");
        mDatabaseReference = database.getReference().child(email + "/events/" + newName);
        mDatabaseReference.setValue(newEvent);


        //closes dialog
        EventInfo.dismiss();
        //restarts mainpage to avoid crashes
        Intent myIntent = new Intent(MainPage.this, MainPage.class);
        MainPage.this.startActivity(myIntent);

    }


    //shows tags overview
    public void ShowTagsOverview(final String name){
        //changes contentview
        TagsOverview.setContentView(R.layout.tagsoverviewpopup);

        //tags
        TagsInfo = (TagGroup) TagsOverview.findViewById(R.id.InfoTags);

        //textviews
        final TextView CloseTagsPopup;

        //initialises textviews
        CloseTagsPopup = (TextView) TagsOverview.findViewById(R.id.tvCloseTagsPopup);



        //close tags overview button
        CloseTagsPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TagsOverview.dismiss();
            }
        });

        //clears list with tags to avoid duplicates
        TagListInfo.clear();


        //firebase stuff
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        final String email = currentUser.getEmail().replace('.', ',');
        DatabaseReference refName = database.getReference(email + "/events/" + name);

        //reference, gets tags
        refName.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //gets old tags
                String oldTags = dataSnapshot.child("tags").getValue().toString();
                //converts tags (string) into an arraylist
                List<String> TagListOld = new ArrayList<String>(Arrays.asList(oldTags.split(",")));

                //fills list with old tags
                for (int counter = 0; counter < TagListOld.size(); counter++) {
                    TagListInfo.add(TagListOld.get(counter));
                }

                //sets tags
                TagsInfo.setTags(TagListInfo);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        //shows dialog
        TagsOverview.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TagsOverview.show();


    }

    //search tag method
    public void SearchTags(){
        //changes contentview
        SearchTags.setContentView(R.layout.tagspopup);

        //Buttons
        final Button CancelTag;
        final Button ConfirmTag;

        //initialises buttons
        CancelTag = (Button) SearchTags.findViewById(R.id.btnCancelTag);
        ConfirmTag = (Button) SearchTags.findViewById(R.id.btnConfirmTag);

        //Edittext
        final EditText SearchTag;

        //initialises edittexts
        SearchTag = (EditText) SearchTags.findViewById(R.id.etNewTag);

        //button to close popup
        CancelTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SearchTags.dismiss();
            }
        });



        //confirm tag search
        ConfirmTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SearchTag.getText().toString().matches("") != true){
                    //sets filter: clears listview, fills it again, similar to the one in onCreate
                    lvEvents.setAdapter(null);
                    final List<String[]> ItemsList = new LinkedList<String[]>();

                    final ArrayAdapter<String[]> adapter = new ArrayAdapter<String[]>(MainPage.this,android.R.layout.simple_list_item_2, android.R.id.text1, ItemsList){
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent){
                            View view = super.getView(position, convertView, parent);

                            String[] entry = ItemsList.get(position);
                            TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                            TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                            text1.setText(entry[0]);
                            text2.setText(entry[1]);

                            return view;

                        }

                    };

                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference();


                    final String email = currentUser.getEmail().replace('.', ',');

                    myRef.child(email + "/events").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            for(DataSnapshot uniqueKeySnapshot : dataSnapshot.getChildren()){

                                String EventName = uniqueKeySnapshot.getKey();
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference ref2 = database.getReference(email + "/events/" + EventName);


                                // Attach a listener to read the data at our posts reference
                                ref2.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        String oldTags = dataSnapshot.child("tags").getValue().toString();
                                        List<String> TagListOld = new ArrayList<String>(Arrays.asList(oldTags.split(",")));

                                        //goes through oldtags and only adds these events into the list that contain the wanted tag
                                        for (int counter = 0; counter < TagListOld.size(); counter++) {
                                            if (TagListOld.get(counter).matches(SearchTag.getText().toString())){

                                                String name = dataSnapshot.child("name").getValue().toString();
                                                String date = dataSnapshot.child("date").getValue().toString();
                                                String time = dataSnapshot.child("time").getValue().toString();
                                                String DateTime = date + ", " + time;
                                                adapter.notifyDataSetChanged();
                                                ItemsList.add(new String[]{name, DateTime});
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }


                    });

                    //filter is set, shows ClearFilter button
                    ClearSearch.setVisibility(View.VISIBLE);
                    lvEvents.setAdapter(adapter);
                    SearchTags.dismiss();

                } else {
                    Toast.makeText(MainPage.this,"You did not type in anything.",Toast.LENGTH_LONG).show();
                }

            }
        });

        //shows dialog
        SearchTags.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        SearchTags.show();
    }

    //method for connection testing
    private boolean connected(){
        ConnectivityManager connectivityManager=(ConnectivityManager) getSystemService(MainPage.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo !=null && activeNetworkInfo.isConnected();
    }
}
