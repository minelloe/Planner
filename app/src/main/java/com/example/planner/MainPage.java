package com.example.planner;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import com.skyhope.eventcalenderlibrary.CalenderEvent;
import com.skyhope.eventcalenderlibrary.listener.CalenderDayClickListener;
import com.skyhope.eventcalenderlibrary.model.DayContainerModel;
import com.skyhope.eventcalenderlibrary.model.Event;
import com.allyants.notifyme.NotifyMe;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class
MainPage extends AppCompatActivity {



    private static final String TAG = "MainPage";
    private Button Logout;
    private Button NewEvent;
    private Button Settings;
    private ListView lvEvents;
    private Dialog EventInfo;

    //CALENDAR & CLOCK STUFF
    private CalenderEvent calenderEvent;

    private Calendar mCurrentDate;
    private Calendar currentTime;
    private int hour, minute;
    private int day, month, year;
    private DatePickerDialog.OnDateSetListener mDateSetListener;



    private ArrayList<String> items = new ArrayList<>();





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
        setContentView(R.layout.activity_main_page);


        Logout = (Button) findViewById(R.id.btnLogout);
        NewEvent = (Button) findViewById(R.id.btnGoToNewEvent);
        Settings = (Button) findViewById(R.id.btnSettings);

        lvEvents = (ListView) findViewById(R.id.lvEvents);


        final List<String[]> ItemsList = new LinkedList<String[]>();



        calenderEvent = (CalenderEvent) findViewById(R.id.calender_event);


        //firebase stuff
        mAuth = FirebaseAuth.getInstance();

        NewEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewEvent();
            }
        });
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

        items.clear();
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



        lvEvents.setAdapter(adapter);
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


                            fillCalendar(date, time, name);

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

        lvEvents.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String[] entry = ItemsList.get(i);
                String name = entry[0];


                Toast.makeText(MainPage.this,"Data received,, name: " + name,Toast.LENGTH_SHORT).show();


                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference refName = database.getReference(email + "/events/" + name);

                refName.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String name = dataSnapshot.child("name").getValue().toString();
                        String date = dataSnapshot.child("date").getValue().toString();
                        String time = dataSnapshot.child("time").getValue().toString();
                        String notes = dataSnapshot.child("notes").getValue().toString();
                        String additionalNotification = dataSnapshot.child("additionalNotification").getValue().toString();

                        Toast.makeText(MainPage.this,"Data received,, name: " + name + "date: " + date + "time: " + time + "notes: " + notes,Toast.LENGTH_SHORT).show();



                        ShowPopup(name, date, time, notes, additionalNotification);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("The read failed: " + databaseError.getCode());
                    }
                });






            }
        });

        //updateListView();

        Settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(MainPage.this, Settings.class);
                MainPage.this.startActivity(myIntent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });




        //Calendar
        calenderEvent.initCalderItemClickCallback(new CalenderDayClickListener() {
            @Override
            public void onGetDay(DayContainerModel dayContainerModel) {

                Log.d(TAG, dayContainerModel.getDate());

            }
        });

    }

    private void fillCalendar(final String date, final String time, final String name){
        /*
        Toast.makeText(MainPage.this,"reached method",Toast.LENGTH_SHORT).show();
        Calendar dateForCalendar = Calendar.getInstance();
        String fullDate = date + " " + time + ":00:00";


        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
        try {
            dateForCalendar.setTime(sdf.parse(fullDate));
            Toast.makeText(this,"tried: " + dateForCalendar.toString(),Toast.LENGTH_SHORT).show();
        } catch (ParseException e) {              // Insert this block.
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //Event event = new Event(dateForCalendar.getTimeInMillis(), name);
        //Toast.makeText(MainPage.this,"Event added",Toast.LENGTH_SHORT).show();
        //calenderEvent.addEvent(event);

         */
    }

    //Change UI according to user data.
    public void  updateUI(FirebaseUser account){
        if(account != null){
            String email = account.getEmail();
            Toast.makeText(this,"Signed in" + email,Toast.LENGTH_LONG).show();

        }else {

            Toast.makeText(this,"You're not signed in",Toast.LENGTH_LONG).show();

            Intent myIntent = new Intent(MainPage.this, MainActivity.class);
            MainPage.this.startActivity(myIntent);

        }
    }

    private void NewEvent(){
        Intent myIntent = new Intent(MainPage.this, EventCreator.class);
        MainPage.this.startActivity(myIntent);
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);

    }



    private void logout(){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this,MainActivity.class));
    }

    public void ShowPopup(final String name, final String date, final String time, final String notes, final String additionalNotification){
        EventInfo = new Dialog(this);
        EventInfo.requestWindowFeature(Window.FEATURE_NO_TITLE);

        final TextView tvClosePopup;
        final ImageView ivEditEvent;
        final ImageView ivDelete;


        EventInfo.setContentView(R.layout.eventpopup);


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

        //--INFO (TEXTVIEW)
        tvPopupTitle = (TextView) EventInfo.findViewById(R.id.tvPopupTitle);
        tvPopupDate = (TextView) EventInfo.findViewById(R.id.tvPopupDate);
        tvPopupTime = (TextView) EventInfo.findViewById(R.id.tvPopupTime);
        tvPopupNotes = (TextView) EventInfo.findViewById(R.id.tvPopupNotes);
        tvNotification = (TextView) EventInfo.findViewById(R.id.tvNotification);

        ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                FirebaseUser currentUser = mAuth.getCurrentUser();
                final String email = currentUser.getEmail().replace('.', ',');
                DatabaseReference DeleteEvent = database.getReference(email + "/events/").child(name);

                DeleteEvent.removeValue();
                NotifyMe.cancel(getApplicationContext(),name);

                if (additionalNotification.matches("none") == false){
                    NotifyMe.cancel(getApplicationContext(),name + "_additional");
                }


                EventInfo.dismiss();
                Intent myIntent = new Intent(MainPage.this, MainPage.class);
                MainPage.this.startActivity(myIntent);
            }
        });

        tvClosePopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventInfo.dismiss();

            }
        });
        EventInfo.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        EventInfo.show();
        tvPopupTitle.setText(name);
        tvPopupDate.setText(date);
        tvPopupTime.setText(time);
        tvPopupNotes.setText(notes);
        if (additionalNotification.matches("none") == false){
            tvNotification.setText("Notification: " + additionalNotification + " before");
        } else {
            tvNotification.setText("No additional notification was set.");
        }



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
                editEvent(name, date, time, notes);


            }
        });
    }

    public void editEvent(final String oldName, final String oldDate, final String oldTime, final String oldNotes){

        setContentView(R.layout.activity_main_page);

        //FINAL COULD BE A MISTAKE???
        final EditText etPopupTitle;
        final EditText etPopupDate;
        final EditText etPopupTime;
        final EditText etPopupNotes;

        ImageView ivConfirmChanges;

        etPopupTitle = (EditText) EventInfo.findViewById(R.id.etPopupTitle);
        etPopupDate = (EditText) EventInfo.findViewById(R.id.etPopupDate);
        etPopupTime = (EditText) EventInfo.findViewById(R.id.etPopupTime);
        etPopupNotes = (EditText) EventInfo.findViewById(R.id.etPopupNotes);


        ivConfirmChanges = (ImageView) EventInfo.findViewById(R.id.ivConfirmChanges);



        etPopupTitle.setVisibility(View.VISIBLE);
        etPopupDate.setVisibility(View.VISIBLE);
        etPopupTime.setVisibility(View.VISIBLE);
        etPopupNotes.setVisibility(View.VISIBLE);

        etPopupTitle.setText(oldName);
        etPopupDate.setText(oldDate);
        etPopupTime.setText(oldTime);
        etPopupNotes.setText(oldNotes);

        ivConfirmChanges.setVisibility(View.VISIBLE);

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

    public void confirmChanges(String oldName, String newName, String newDate, String newTime, String newNotes){

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference mDatabaseReference = database.getReference();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        final String email = currentUser.getEmail().replace('.', ',');

        DatabaseReference DeleteEvent = database.getReference(email + "/events/").child(oldName);

        DeleteEvent.removeValue();

        EventY newEvent = new EventY(newName, newDate, newNotes, newTime, "");
        mDatabaseReference = database.getReference().child(email + "/events/" + newName);
        mDatabaseReference.setValue(newEvent);


        EventInfo.dismiss();
        Intent myIntent = new Intent(MainPage.this, MainPage.class);
        MainPage.this.startActivity(myIntent);

    }


    public void updateListView(){

    }
}
