package com.example.planner;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Intent;
import android.app.AlertDialog;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

//firebase imports
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

public class SignUp extends AppCompatActivity {

    private EditText Email;
    private EditText Password;
    private EditText PasswordValidation;
    private Button SignUp;
    private FirebaseAuth mAuth;
    private static final String TAG = "SignUp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Email = (EditText) findViewById(R.id.etEmail);
        Password = (EditText) findViewById(R.id.etPassword);
        PasswordValidation = (EditText) findViewById(R.id.etPasswordValidation);


        SignUp = (Button) findViewById(R.id.btnSignUp);

        //firebase stuff
        mAuth = FirebaseAuth.getInstance();


        SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isValid(Email.getText().toString())){
                    if (Password.getText().toString().matches(PasswordValidation.getText().toString())){
                        createnewuser(Email.getText().toString(), Password.getText().toString());
                    } else {
                        Toast.makeText(SignUp.this,"Passwords do not match! :(",Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(SignUp.this,"E-Mail isn't valid! :(",Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    //Change UI according to user data.
    public void  updateUI(FirebaseUser account){
        if(account != null){
            String email = account.getEmail();
            Toast.makeText(this,"Signed in" + email,Toast.LENGTH_LONG).show();
            Intent myIntent = new Intent(SignUp.this, MainPage.class);
            SignUp.this.startActivity(myIntent);
        }else {
            Toast.makeText(this,"You're not signed in",Toast.LENGTH_LONG).show();
        }
    }

    private void createnewuser(String userUsername, String userPassword){
        mAuth = FirebaseAuth.getInstance();
        Toast.makeText(SignUp.this, "Reached", Toast.LENGTH_SHORT).show();
        mAuth.createUserWithEmailAndPassword(userUsername, userPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(SignUp.this, "user created", Toast.LENGTH_SHORT).show();

                            FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
                            DatabaseReference mDatabaseReference = mDatabase.getReference();
                            String EmailToSave = Email.getText().toString().replace('.', ',');

                            EventY newEvent = new EventY("ignore", "ignore" ,"ignore", "ignore", "none");
                            mDatabaseReference = mDatabase.getReference().child(EmailToSave + "/events/" + "ignore");
                            mDatabaseReference.setValue(newEvent);


                            updateUI(user);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUp.this, "Signup failed. " +  task.getException().getMessage() + " :(",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });

    }

    public static boolean isValid(String email)
    {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
