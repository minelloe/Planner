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
            Toast.makeText(this,"Signed in" + email,Toast.LENGTH_LONG).show();

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

        lyNewPassword = (LinearLayout) findViewById(R.id.lyNewPassword);
        lyDeleteAccount = (LinearLayout) findViewById(R.id.lyDeleteAccount);
        lyClearEvents = (LinearLayout) findViewById(R.id.lyClearEvents);

        lyNewPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(Settings.this,"Set new password",Toast.LENGTH_SHORT).show();
                ShowPopup();


            }
        });

        lyDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(Settings.this,"Sad to see you leave...",Toast.LENGTH_SHORT).show();


                FirebaseDatabase database = FirebaseDatabase.getInstance();
                FirebaseUser currentUser = mAuth.getCurrentUser();
                final String email = currentUser.getEmail().replace('.', ',');
                Toast.makeText(Settings.this,"email: " + email,Toast.LENGTH_SHORT).show();
                DatabaseReference DeleteAccount = database.getReference().child(email);

                DeleteAccount.removeValue();


                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                user.delete()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Intent myIntent = new Intent(Settings.this, MainActivity.class);
                                    Settings.this.startActivity(myIntent);
                                }
                            }
                        });







            }
        });

        lyClearEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(Settings.this,"All events cleared",Toast.LENGTH_SHORT).show();



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
                                    String additionalNotification = dataSnapshot.child("additionalNotification").getValue().toString();

                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    FirebaseUser currentUser = mAuth.getCurrentUser();
                                    final String email = currentUser.getEmail().replace('.', ',');

                                    DatabaseReference DeleteEvent = database.getReference(email + "/events/").child(name);

                                    DeleteEvent.removeValue();
                                    NotifyMe.cancel(getApplicationContext(),name);

                                    if (additionalNotification.matches("none") == false){
                                        NotifyMe.cancel(getApplicationContext(),name + "_additional");
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



                DatabaseReference mDatabaseReference = database.getReference();

                EventY newEvent = new EventY("ignore", "ignore" ,"ignore", "ignore", "none");
                mDatabaseReference = database.getReference().child(email + "/events/" + "ignore");
                mDatabaseReference.setValue(newEvent);

            }
        });
    }

    public void ShowPopup(){
        PasswordPopup.setContentView(R.layout.newpassword);

        //Edittexts

        final EditText NewPassword;
        final EditText NewPasswordRepeat;


        NewPassword = (EditText) PasswordPopup.findViewById(R.id.etNewPassword);
        NewPasswordRepeat = (EditText) PasswordPopup.findViewById(R.id.etNewPasswordRepeat);

        //Buttons
        final Button btnCancelPassword;
        final Button btnConfirmPassword;

        btnCancelPassword = (Button) PasswordPopup.findViewById(R.id.btnCancelPassword);
        btnConfirmPassword = (Button) PasswordPopup.findViewById(R.id.btnConfirmPassword);

        btnCancelPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PasswordPopup.dismiss();
            }
        });

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





        PasswordPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        PasswordPopup.show();


    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}
