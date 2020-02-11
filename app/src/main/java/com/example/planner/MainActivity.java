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

public class MainActivity extends AppCompatActivity {

    private EditText Username;
    private EditText Password;
    private Button SignUp;
    private Button Login;
    private int counter = 5;
    private FirebaseAuth mAuth;
    private static final String TAG = "MainActivity";


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
        setContentView(R.layout.activity_main);




        Username = (EditText) findViewById(R.id.etUsername);
        Password = (EditText) findViewById(R.id.etPassword);
        Login = (Button) findViewById(R.id.btnLogin);
        SignUp = (Button) findViewById(R.id.btnSignUp);


        //firebase stuff
        mAuth = FirebaseAuth.getInstance();

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validate(Username.getText().toString(), Password.getText().toString());
            }
        });

        SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signup();
            }
        });
    }


    //Change UI according to user data.
    public void  updateUI(FirebaseUser account){
        if(account != null){
            String email = account.getEmail();
            Toast.makeText(this,"Signed in" + email,Toast.LENGTH_LONG).show();
            Intent myIntent = new Intent(MainActivity.this, MainPage.class);
            MainActivity.this.startActivity(myIntent);
        }else {
            Toast.makeText(this,"You're not signed in",Toast.LENGTH_LONG).show();
        }
    }


    private void signup(){
        Intent myIntent = new Intent(MainActivity.this, SignUp.class);
        MainActivity.this.startActivity(myIntent);
    }


    private void validate(String userUsername, String userPassword) {
        mAuth = FirebaseAuth.getInstance();
        Toast.makeText(MainActivity.this, "Reached", Toast.LENGTH_SHORT).show();
            mAuth.signInWithEmailAndPassword(userUsername, userPassword)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(MainActivity.this, "login successful", Toast.LENGTH_SHORT).show();
                                // Sign in success, update UI with the signed-in user's information
                                Log.w(TAG, "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(MainActivity.this, "fail", Toast.LENGTH_SHORT).show();
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                counter--;
                                Toast.makeText(MainActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                updateUI(null);
                            }

                            // ...
                        }
                    });



            if (counter <= 0){
                Toast.makeText(MainActivity.this, "Cannot login, amount of attempts surpassed", Toast.LENGTH_SHORT).show();
                Login.setEnabled(false);
            }

    }


}
