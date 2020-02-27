package com.example.planner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.allyants.notifyme.NotifyMe;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Settings extends AppCompatActivity {

    //linearlayouts
    private LinearLayout lyNewPassword;
    private LinearLayout lyDeleteAccount;
    private LinearLayout lyClearEvents;

    //Dialogs
    private Dialog PasswordPopup;

    //firebase stuff
    private FirebaseAuth mAuth;

    private static final String TAG = "Settings";

    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);

    }

    public void  updateUI(FirebaseUser account){
        if(account != null){
            String email = account.getEmail();

        }else {

            Toast.makeText(this,"You're not signed in",Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.this,MainActivity.class));


        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //firebase stuff
        mAuth = FirebaseAuth.getInstance();

        //Dialog stuff
        PasswordPopup = new Dialog(Settings.this);
        PasswordPopup.setCanceledOnTouchOutside(false);

        //initialises linearlayouts
        lyNewPassword = (LinearLayout) findViewById(R.id.lyNewPassword);
        lyDeleteAccount = (LinearLayout) findViewById(R.id.lyDeleteAccount);
        lyClearEvents = (LinearLayout) findViewById(R.id.lyClearEvents);

        //passwort reset
        lyNewPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowPopup();


            }
        });

        //delete account
        lyDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(Settings.this,"Sad to see you leave. :(",Toast.LENGTH_SHORT).show();

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                FirebaseUser currentUser = mAuth.getCurrentUser();
                final String email = currentUser.getEmail().replace('.', ',');
                Toast.makeText(Settings.this,"email: " + email,Toast.LENGTH_SHORT).show();
                DatabaseReference DeleteAccount = database.getReference().child(email);

                //deletes all data
                DeleteAccount.removeValue();


                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                //deletes user
                user.delete()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    //goes to login page
                                    Intent myIntent = new Intent(Settings.this, MainActivity.class);
                                    Settings.this.startActivity(myIntent);
                                }
                            }
                        });







            }
        });

        //clears events
        lyClearEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(Settings.this,"All events cleared",Toast.LENGTH_SHORT).show();

                FirebaseUser currentUser = mAuth.getCurrentUser();
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference();


                final String email = currentUser.getEmail().replace('.', ',');

                //goes through all events
                myRef.child(email + "/events").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for(DataSnapshot uniqueKeySnapshot : dataSnapshot.getChildren()){

                            String EventName = uniqueKeySnapshot.getKey();
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference ref2 = database.getReference(email + "/events/" + EventName);

                            ref2.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    String name = dataSnapshot.child("name").getValue().toString();
                                    String additionalNotification = dataSnapshot.child("additionalNotification").getValue().toString();

                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    FirebaseUser currentUser = mAuth.getCurrentUser();
                                    final String email = currentUser.getEmail().replace('.', ',');

                                    DatabaseReference DeleteEvent = database.getReference(email + "/events/").child(name);

                                    //leaves "ignore" event so that the database structure won't get deleted
                                    if (name.matches("ignore") != true){
                                        //removes event
                                        DeleteEvent.removeValue();
                                        //cancels notification
                                        NotifyMe.cancel(getApplicationContext(),name);
                                    }


                                    //deletes additional notification
                                    if (additionalNotification.matches("none") == false){
                                        NotifyMe.cancel(getApplicationContext(),name + "_additional");
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

    //shows new passwort dialog
    public void ShowPopup(){
        PasswordPopup.setContentView(R.layout.newpassword);

        //Edittexts
        final EditText NewPassword;
        final EditText NewPasswordRepeat;

        //initialises edittexts
        NewPassword = (EditText) PasswordPopup.findViewById(R.id.etNewPassword);
        NewPasswordRepeat = (EditText) PasswordPopup.findViewById(R.id.etNewPasswordRepeat);

        //Buttons
        final Button btnCancelPassword;
        final Button btnConfirmPassword;

        //initialises buttons
        btnCancelPassword = (Button) PasswordPopup.findViewById(R.id.btnCancelPassword);
        btnConfirmPassword = (Button) PasswordPopup.findViewById(R.id.btnConfirmPassword);

        //closes dialog
        btnCancelPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PasswordPopup.dismiss();
            }
        });

        //sets new password
        btnConfirmPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(Settings.this,"Got here",Toast.LENGTH_LONG).show();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (NewPassword.getText().toString().matches(NewPasswordRepeat.getText().toString())){
                    user.updatePassword(NewPassword.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(Settings.this,"Password updated! :)",Toast.LENGTH_LONG).show();
                                        PasswordPopup.dismiss();
                                    }
                                }
                            });
                } else {
                    Toast.makeText(Settings.this,"Passwords do not match! :(",Toast.LENGTH_LONG).show();
                }
            }



        });




        //shows dialog
        PasswordPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        PasswordPopup.show();


    }

    //if back-button is pressed, animation is overriden
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}
