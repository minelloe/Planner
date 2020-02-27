package com.example.planner;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.allyants.notifyme.NotifyMe;



//firebase imports
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

//Tag imports
import me.gujun.android.taggroup.TagGroup;

public class EventCreator extends AppCompatActivity {

    private static final String TAG = "EventCreator";
    //Edittexts
    private EditText NEName;
    private EditText NEDate;
    private EditText NETime;
    private EditText NENotes;
    //Buttons
    private Button NECreate;
    //Textviews
    private TextView tvTest;


    //Dialogs
    private Dialog ReminderPopup;
    private Dialog TagsPopup;

    //Taggroup
    private TagGroup NETags;
    List<String> TagList = new ArrayList<String>();

    private TagGroup.OnTagClickListener mTagClickListener = new TagGroup.OnTagClickListener() {
        @Override
        public void onTagClick(String tag) {
            TagList.remove(tag);
            NETags.setTags(TagList);
        }
    };


    //LinearLayout
    private LinearLayout TagBox;



    //Calendar stuff
    private Calendar mCurrentDate;
    private Calendar currentTime;
    private int hour, minute;
    private int day, month, year;
    private Calendar now;
    private DatePickerDialog.OnDateSetListener mDateSetListener;





    //firebase stuff
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mDatabaseReference = mDatabase.getReference();


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
        setContentView(R.layout.activity_event_creator);


        //edittexts
        NEName = (EditText) findViewById(R.id.etNEName);
        NEDate = (EditText) findViewById(R.id.etNEDate);
        NETime = (EditText) findViewById(R.id.etNETime);
        NENotes = (EditText) findViewById(R.id.etNENotes);

        //buttons
        NECreate = (Button) findViewById(R.id.btnGoToNewEvent);

        //tag stuff
        NETags = (TagGroup) findViewById(R.id.NETags);
        //sets onclicklistener for every tag in the taggroup
        NETags.setOnTagClickListener(mTagClickListener);

        //linearlayout containing the tags
        TagBox = (LinearLayout) findViewById(R.id.NETagBox);



        //calendar stuff
        mCurrentDate = Calendar.getInstance();
        currentTime = Calendar.getInstance();

        now = Calendar.getInstance();

        hour = currentTime.get(Calendar.HOUR_OF_DAY);
        minute = currentTime.get(Calendar.MINUTE);

        day = mCurrentDate.get(Calendar.DAY_OF_MONTH);
        month = mCurrentDate.get(Calendar.MONTH);
        year = mCurrentDate.get(Calendar.YEAR);


        //Dialogs
        ReminderPopup = new Dialog(this);
        ReminderPopup.setCanceledOnTouchOutside(false);

        TagsPopup = new Dialog(this);
        TagsPopup.setCanceledOnTouchOutside(false);


        //if box containing tags is clicked, popup shows up to add a new tag
        TagBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddNewTag();
            }
        });

        //shows time dialog where you can pick a time
        NETime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(EventCreator.this,  R.style.TimePickerTheme, new TimePickerDialog.OnTimeSetListener() {
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
                        NETime.setText(fh + ":" + fm);
                    }
                }, hour, minute, true);
                timePickerDialog.show();
            }
        });

        //shows date dialog where you can pick a date
        NEDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(EventCreator.this,  R.style.TimePickerTheme,  new DatePickerDialog.OnDateSetListener() {
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
                        NEDate.setText(fd + "." + fm + "." + year);
                    }
                }, year, month, day);
                datePickerDialog.show();
            }
        });



        //firebase stuff
        mAuth = FirebaseAuth.getInstance();

        //when button to create event is clicked
        NECreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

        FirebaseUser currentUser = mAuth.getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();


                final String email = currentUser.getEmail().replace('.', ',');
                //makes sure that data is typed in correctly and that events with the same name doesn't exist, firebase reference
                myRef.child(email + "/events").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for(DataSnapshot uniqueKeySnapshot : dataSnapshot.getChildren()){

                            String EventName = uniqueKeySnapshot.getKey();
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference ref2 = database.getReference(email + "/events/" + EventName);

                            //goes through all events
                            ref2.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    String name = dataSnapshot.child("name").getValue().toString();
                                    //checks all info
                                    if (NEName.getText().toString().matches(name)) {
                                        //Toast.makeText(EventCreator.this, "Please choose a different name as an event with the same name already exists! :)", Toast.LENGTH_SHORT).show();
                                        return;
                                    } else {
                                        if (NEName.getText().toString().matches("")) {
                                            Toast.makeText(EventCreator.this, "Please fill out all required information before adding a notification! :)", Toast.LENGTH_SHORT).show();
                                            return;
                                        } else {
                                            if (NEDate.getText().toString().matches("date")) {
                                                Toast.makeText(EventCreator.this, "Please fill out all required information before adding a notification! :)", Toast.LENGTH_SHORT).show();
                                                return;
                                            } else {
                                                if (NETime.getText().toString().matches("time")) {
                                                    Toast.makeText(EventCreator.this, "Please fill out all required information before adding a notification! :)", Toast.LENGTH_SHORT).show();
                                                    return;
                                                } else {
                                                    //creates event
                                                    NECreate();
                                                }
                                            }
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







            }
        });
    }



    //Change UI according to user data.
    public void  updateUI(FirebaseUser account){
        if(account != null){
            String email = account.getEmail();
        }else {

            Toast.makeText(this,"You're not signed in",Toast.LENGTH_LONG).show();
        }
    }

    //creates event
    private void NECreate(){
        //gets current date, creates calendar variable
        final Calendar dateToSave = Calendar.getInstance();

        //gets date & time
        String fullDate = NEDate.getText().toString() + " " + NETime.getText().toString() + ":00.00";


        //creates date format
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");

        //converts string to calendar
        try {
            dateToSave.setTime(sdf.parse(fullDate));
        } catch (ParseException e) {              // Insert this block.
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //changes contentview to reminder
        ReminderPopup.setContentView(R.layout.addreminder_popup);


        //Spinners
        final Spinner sAmount;
        final Spinner sType;

        //buttons
        final Button confirmReminder;
        final Button refuseReminder;

        //initialises spinners
        sAmount = (Spinner) ReminderPopup.findViewById(R.id.sAmount);
        sType = (Spinner) ReminderPopup.findViewById(R.id.sType);

        //initialises buttons
        confirmReminder = (Button) ReminderPopup.findViewById(R.id.btnConfirmReminder);
        refuseReminder = (Button) ReminderPopup.findViewById(R.id.btnRefuseNotif);

        //if additional reminder is refused
        refuseReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //creates default notification
                NotifyMe notifyMeFinal = new NotifyMe.Builder((getApplicationContext()))
                        .title(NEName.getText().toString())
                        .content("Event due! " + NEDate.getText().toString() + ", " + NETime.getText().toString())
                        .color(255,0,0,255)
                        .led_color(255,255,255,255)
                        .time(dateToSave)
                        .addAction(new Intent(), "Snooze",false)
                        .key(NEDate.getText().toString())
                        .addAction(new Intent(),"Dismiss",true,false)
                        .addAction(new Intent(),"Done")
                        .large_icon(R.mipmap.ic_launcher_round)
                        .build();

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String email = user.getEmail();
                String EmailToSave = email.replace('.', ',');

                //gets all tags, converts to string so that it can be saved into the database
                String NEAllTags = TextUtils.join(", ", TagList);

                //creates event
                EventY newEvent = new EventY(NEName.getText().toString(), NEDate.getText().toString(), NENotes.getText().toString(), NETime.getText().toString(), "none", NEAllTags);
                mDatabaseReference = mDatabase.getReference().child(EmailToSave + "/events/" + NEName.getText().toString());
                mDatabaseReference.setValue(newEvent);

                //closes dialog
                ReminderPopup.dismiss();

                //goes to mainpage
                Intent myIntent = new Intent(EventCreator.this, MainPage.class);
                EventCreator.this.startActivity(myIntent);
                //animation
                overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_up);
            }
        });


        //if additional notification is added
        confirmReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String amount = sAmount.getSelectedItem().toString();
                Integer amountInt = Integer.parseInt(amount);
                amountInt *= -1;
                String type = sType.getSelectedItem().toString();

                //default, standard Notification
                NotifyMe notifyMeFinal = new NotifyMe.Builder((getApplicationContext()))
                        .title(NEName.getText().toString())
                        .content("Event due! " + NEDate.getText().toString() + ", " + NETime.getText().toString())
                        .color(255,0,0,255)
                        .led_color(255,255,255,255)
                        .time(dateToSave)
                        .addAction(new Intent(), "Snooze",false)
                        .key(NEName.getText().toString())
                        .addAction(new Intent(),"Dismiss",true,false)
                        .addAction(new Intent(),"Done")
                        .large_icon(R.mipmap.ic_launcher_round)
                        .build();

                //subtracts time to set notification time before the deadline
                if (type.matches("minutes")){
                    dateToSave.add(Calendar.MINUTE, amountInt);
                }

                if (type.matches("hours")){
                    dateToSave.add(Calendar.HOUR_OF_DAY, amountInt);
                }

                if (type.matches("days")){
                    dateToSave.add(Calendar.DAY_OF_MONTH, amountInt);
                }

                String NotificationWhen = amount + " " + type;

                //gets user
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String email = user.getEmail();
                String EmailToSave = email.replace('.', ',');

                //gets tags
                String NEAllTags = TextUtils.join(",", TagList);

                EventY newEvent = new EventY(NEName.getText().toString(), NEDate.getText().toString(), NENotes.getText().toString(), NETime.getText().toString(), NotificationWhen, NEAllTags);
                mDatabaseReference = mDatabase.getReference().child(EmailToSave + "/events/" + NEName.getText().toString());
                mDatabaseReference.setValue(newEvent);


                //additional Notification
                NotifyMe notifyMe = new NotifyMe.Builder((getApplicationContext()))
                        .title(NEName.getText().toString())
                        .content("Event due! " + NEDate.getText().toString() + ", " + NETime.getText().toString())
                        .color(255,0,0,255)
                        .led_color(255,255,255,255)
                        .time(dateToSave)
                        .addAction(new Intent(), "Snooze",false)
                        .key(NEName.getText().toString() + "_additional")
                        .addAction(new Intent(),"Dismiss",true,false)
                        .addAction(new Intent(),"Done")
                        .large_icon(R.mipmap.ic_launcher_round)
                        .build();



                //closes dialog
                ReminderPopup.dismiss();

                //goes to mainpage
                Intent myIntent = new Intent(EventCreator.this, MainPage.class);
                EventCreator.this.startActivity(myIntent);
                //animation
                overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_up);

            }


        });

        //shows dialog
        ReminderPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ReminderPopup.show();

    }

    //add tag method
    public void AddNewTag(){
        TagsPopup.setContentView(R.layout.tagspopup);

        //Buttons
        final Button CancelTag;
        final Button ConfirmTag;

        //initialises buttons
        CancelTag = (Button) TagsPopup.findViewById(R.id.btnCancelTag);
        ConfirmTag = (Button) TagsPopup.findViewById(R.id.btnConfirmTag);

        //Edittext
        final EditText NewTag;

        //initialises edittext
        NewTag = (EditText) TagsPopup.findViewById(R.id.etNewTag);

        //closes dialog
        CancelTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TagsPopup.dismiss();
            }
        });

        //adds tag
        ConfirmTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NewTag.getText().toString().matches("") != true){
                    //adds to list
                    TagList.add(NewTag.getText().toString());
                    //sets tags
                    NETags.setTags(TagList);
                    //closes dialog
                    TagsPopup.dismiss();
                } else {
                    Toast.makeText(EventCreator.this,"You did not type in anything.",Toast.LENGTH_LONG).show();
                }

            }
        });

        //shows dialog
        TagsPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TagsPopup.show();
    }

    //if back button is pressed, alternate animation
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_up);
    }
}

