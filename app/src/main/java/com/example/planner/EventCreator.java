package com.example.planner;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

//firebase imports
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class EventCreator extends AppCompatActivity {

    private static final String TAG = "EventCreator";
    private EditText NEName;
    private EditText NEDate;
    private EditText NETime;
    private EditText NENotes;
    private Button NECreate;
    private TextView tvTest;
    private Calendar mCurrentDate;
    private Calendar currentTime;
    private int hour, minute;
    private int day, month, year;
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
        NEName = (EditText) findViewById(R.id.etNEName);
        NEDate = (EditText) findViewById(R.id.etNEDate);
        NETime = (EditText) findViewById(R.id.etNETime);
        NENotes = (EditText) findViewById(R.id.etNENotes);
        NECreate = (Button) findViewById(R.id.btnGoToNewEvent);

        mCurrentDate = Calendar.getInstance();
        currentTime = Calendar.getInstance();

        hour = currentTime.get(Calendar.HOUR_OF_DAY);
        minute = currentTime.get(Calendar.MINUTE);

        day = mCurrentDate.get(Calendar.DAY_OF_MONTH);
        month = mCurrentDate.get(Calendar.MONTH);
        year = mCurrentDate.get(Calendar.YEAR);

        NEDate.setText("date");
        NETime.setText("time");

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
                NECreate();
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
        Toast.makeText(this,"EmailToSave: " + EmailToSave,Toast.LENGTH_LONG).show();

        Event newEvent = new Event(NEName.getText().toString(), NEDate.getText().toString(), NENotes.getText().toString(), NETime.getText().toString());
        Toast.makeText(this,"Event created" + EmailToSave,Toast.LENGTH_LONG).show();
        mDatabaseReference = mDatabase.getReference().child(EmailToSave + "/events/" + NEName.getText().toString());
        mDatabaseReference.setValue(newEvent);

        Toast.makeText(this,"Saved succesfully",Toast.LENGTH_LONG).show();
		startActivity(new Intent(this,MainPage.class));
    }
}

