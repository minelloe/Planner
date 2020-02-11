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

public class SignUp extends AppCompatActivity {

    private EditText Email;
    private EditText Password;
    private Button SignUp;
    private FirebaseAuth mAuth;
    private static final String TAG = "SignUp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Email = (EditText) findViewById(R.id.etEmail);
        Password = (EditText) findViewById(R.id.etPassword);
        SignUp = (Button) findViewById(R.id.btnSignUp);

        //firebase stuff
        mAuth = FirebaseAuth.getInstance();

        SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               createnewuser(Email.getText().toString(), Password.getText().toString());
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
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUp.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        DatabaseReference mDatabaseReference = mDatabase.getReference();
        String EmailToSave = Email.getText().toString().replace('.', ',');
        mDatabaseReference = mDatabase.getReference().child(EmailToSave + "/events/");
        mDatabaseReference.setValue("new account");
    }

}
