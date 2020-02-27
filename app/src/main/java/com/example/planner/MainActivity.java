package com.example.planner;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

//firebase imports
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

//actually the login page
public class MainActivity extends AppCompatActivity {

    //Edittexts
    private EditText Username;
    private EditText Password;

    //buttons
    private Button SignUp;
    private Button Login;

    //counter for the amount of fails
    private int counter = 5;

    //firebase stuff
    private FirebaseAuth mAuth;

    //tag
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

        //edittexts
        Username = (EditText) findViewById(R.id.etUsername);
        Password = (EditText) findViewById(R.id.etPassword);

        //buttons
        Login = (Button) findViewById(R.id.btnLogin);
        SignUp = (Button) findViewById(R.id.btnSignUp);

        //firebase stuff
        mAuth = FirebaseAuth.getInstance();

        //logs user in
        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validate(Username.getText().toString(), Password.getText().toString());
            }
        });

        //goes to sign up page
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
            Intent myIntent = new Intent(MainActivity.this, MainPage.class);
            MainActivity.this.startActivity(myIntent);
        }else {
            Toast.makeText(this,"You're not signed in ",Toast.LENGTH_LONG).show();
        }
    }


    //goes to sign up page
    private void signup(){
        Intent myIntent = new Intent(MainActivity.this, SignUp.class);
        MainActivity.this.startActivity(myIntent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }


    //validates user
    private void validate(String userUsername, String userPassword) {
        mAuth = FirebaseAuth.getInstance();
            mAuth.signInWithEmailAndPassword(userUsername, userPassword)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
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

                        }
                    });



            //if user fails 5 times, you won't be able to log in anymore
            if (counter <= 0){
                Toast.makeText(MainActivity.this, "Cannot login, amount of attempts surpassed", Toast.LENGTH_SHORT).show();
                Login.setEnabled(false);
            }

    }


}
