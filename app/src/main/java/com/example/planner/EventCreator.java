package com.example.planner;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
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
import com.skyhope.eventcalenderlibrary.CalenderEvent;
import com.skyhope.eventcalenderlibrary.listener.CalenderDayClickListener;
import com.skyhope.eventcalenderlibrary.model.DayContainerModel;
import com.skyhope.eventcalenderlibrary.model.Event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

        //createNotificationChannel();

        NEName = (EditText) findViewById(R.id.etNEName);
        NEDate = (EditText) findViewById(R.id.etNEDate);
        NETime = (EditText) findViewById(R.id.etNETime);
        NENotes = (EditText) findViewById(R.id.etNENotes);

        NECreate = (Button) findViewById(R.id.btnGoToNewEvent);

        NETags = (TagGroup) findViewById(R.id.NETags);
        NETags.setOnTagClickListener(mTagClickListener);


        TagBox = (LinearLayout) findViewById(R.id.NETagBox);




        mCurrentDate = Calendar.getInstance();
        currentTime = Calendar.getInstance();

        now = Calendar.getInstance();

        hour = currentTime.get(Calendar.HOUR_OF_DAY);
        minute = currentTime.get(Calendar.MINUTE);

        day = mCurrentDate.get(Calendar.DAY_OF_MONTH);
        month = mCurrentDate.get(Calendar.MONTH);
        year = mCurrentDate.get(Calendar.YEAR);

        NEDate.setText("date");
        NETime.setText("time");

        ReminderPopup = new Dialog(this);
        ReminderPopup.setCanceledOnTouchOutside(false);

        TagsPopup = new Dialog(this);
        TagsPopup.setCanceledOnTouchOutside(false);



        TagBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddNewTag();
            }
        });


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
        NECreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FirebaseUser currentUser = mAuth.getCurrentUser();
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference();
                DatabaseReference refCounter = database.getReference();


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
                                    if (NEName.getText().toString().matches(name)) {
                                        Toast.makeText(EventCreator.this, "Please choose a different name as an event with the same name already exists! :)", Toast.LENGTH_SHORT).show();
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
                                                    NECreate();
                                                }
                                            }
                                        }
                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    System.out.println("The read failed: " + databaseError.getCode());
                                }
                            });
                            //Toast.makeText(MainPage.this,"EventName: " + EventName,Toast.LENGTH_LONG).show();
                        }
                        //Toast.makeText(MainPage.this,"Reached hiYA",Toast.LENGTH_LONG).show();
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
            Toast.makeText(this,"Signed in" + email,Toast.LENGTH_LONG).show();

        }else {

            Toast.makeText(this,"You're not signed in",Toast.LENGTH_LONG).show();

            //startActivity(new Intent(this,MainActivity.class));

        }
    }

    private void NECreate(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String email = user.getEmail();
        String EmailToSave = email.replace('.', ',');

        final Calendar dateToSave = Calendar.getInstance();



        String fullDate = NEDate.getText().toString() + " " + NETime.getText().toString() + ":00.00";



        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");


        try {
            dateToSave.setTime(sdf.parse(fullDate));
            Toast.makeText(this,"tried: " + dateToSave.toString(),Toast.LENGTH_SHORT).show();
        } catch (ParseException e) {              // Insert this block.
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Toast.makeText(this,"Date: " + dateToSave.getTime(),Toast.LENGTH_SHORT).show();






        ReminderPopup.setContentView(R.layout.addreminder_popup);


        //Dialog Reminder stuff
        final Spinner sAmount;
        final Spinner sType;
        final Button confirmReminder;
        final Button refuseReminder;

        sAmount = (Spinner) ReminderPopup.findViewById(R.id.sAmount);
        sType = (Spinner) ReminderPopup.findViewById(R.id.sType);

        confirmReminder = (Button) ReminderPopup.findViewById(R.id.btnConfirmReminder);

        refuseReminder = (Button) ReminderPopup.findViewById(R.id.btnRefuseNotif);

        refuseReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //initialize notification
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
                EventY newEvent = new EventY(NEName.getText().toString(), NEDate.getText().toString(), NENotes.getText().toString(), NETime.getText().toString(), "none");
                mDatabaseReference = mDatabase.getReference().child(EmailToSave + "/events/" + NEName.getText().toString());
                mDatabaseReference.setValue(newEvent);

                ReminderPopup.dismiss();

                Intent myIntent = new Intent(EventCreator.this, MainPage.class);
                EventCreator.this.startActivity(myIntent);
                overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_up);
            }
        });



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

                if (type.matches("minutes")){
                    dateToSave.add(Calendar.MINUTE, amountInt);
                }

                if (type.matches("hours")){
                    dateToSave.add(Calendar.HOUR_OF_DAY, amountInt);
                }

                if (type.matches("days")){
                    dateToSave.add(Calendar.MONTH, amountInt);
                }

                Toast.makeText(EventCreator.this,"Amount: " + amountInt.toString(),Toast.LENGTH_LONG).show();
                Toast.makeText(EventCreator.this,"Type: " + type,Toast.LENGTH_LONG).show();
                Toast.makeText(EventCreator.this,"Notification-Time: " + dateToSave.getTime(),Toast.LENGTH_LONG).show();

                String NotificationWhen = amount + " " + type;
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String email = user.getEmail();
                String EmailToSave = email.replace('.', ',');
                EventY newEvent = new EventY(NEName.getText().toString(), NEDate.getText().toString(), NENotes.getText().toString(), NETime.getText().toString(), NotificationWhen);
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




                ReminderPopup.dismiss();

                Intent myIntent = new Intent(EventCreator.this, MainPage.class);
                EventCreator.this.startActivity(myIntent);
                overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_up);

            }


        });


        ReminderPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ReminderPopup.show();

    }

    public void AddNewTag(){
        TagsPopup.setContentView(R.layout.tagspopup);

        //Buttons
        final Button CancelTag;
        final Button ConfirmTag;

        CancelTag = (Button) TagsPopup.findViewById(R.id.btnCancelTag);
        ConfirmTag = (Button) TagsPopup.findViewById(R.id.btnConfirmTag);

        //Edittext
        final EditText NewTag;

        NewTag = (EditText) TagsPopup.findViewById(R.id.etNewTag);

        CancelTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TagsPopup.dismiss();
            }
        });


        ConfirmTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NewTag.getText().toString().matches("") != true){
                    TagList.add(NewTag.getText().toString());
                    //String[] newtags = new String[TagList.size()];
                    //newtags = TagList.toArray(newtags);
                    NETags.setTags(TagList);
                    TagsPopup.dismiss();
                }

            }
        });

        TagsPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TagsPopup.show();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_up);
    }
}

