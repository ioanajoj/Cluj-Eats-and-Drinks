package com.joj.clujeatsanddrinks;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.joj.clujeatsanddrinks.UI.ChooseActivity;
import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import androidx.transition.TransitionManager;

/**
 * This is it
 */
public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivityLog: ";
    private FirebaseAuth mAuth;

    // Widgets
    private EditText nameField;
    private EditText emailField;
    private EditText passwordField;
    private Button logInButton;
    private Button newHereButton;
    private Button createAccountButton;

    // Transitions
    private boolean mode = false;
    private ViewGroup transitionsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Firebase.setAndroidContext(this);
        mAuth = FirebaseAuth.getInstance();

        // Get widgets
        transitionsContainer = findViewById(R.id.transitions_container);
        nameField = findViewById(R.id.nameField);
        emailField = findViewById(R.id.fieldEmail);
        passwordField = findViewById(R.id.fieldPassword);
        logInButton = findViewById(R.id.loginButton);
        newHereButton = findViewById(R.id.newHereButton);
        createAccountButton = findViewById(R.id.createAccountButton);

        // Set Visibility
        nameField.setVisibility(View.GONE);
        createAccountButton.setVisibility(View.GONE);

        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Login", Toast.LENGTH_SHORT).show();
                String email = emailField.getText().toString().trim();
                String password = passwordField.getText().toString().trim();
                loginUser(email, password);
            }
        }) ;

        newHereButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransitionManager.beginDelayedTransition(transitionsContainer);
                mode = !mode;
                nameField.setVisibility(mode ? View.VISIBLE : View.GONE);
                if(mode) nameField.requestFocus();
                logInButton.setVisibility(mode ? View.GONE : View.VISIBLE);
                createAccountButton.setVisibility(mode ? View.VISIBLE : View.GONE);
                newHereButton.setText(mode ? "You actually have one? Click here" : "First time here? Create an account");
            }
        });

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Create new account", Toast.LENGTH_SHORT).show();
                String name = nameField.getText().toString().trim();
                String email = emailField.getText().toString().trim();
                String password = passwordField.getText().toString().trim();
                createUser(name, email, password);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            startWelcome();
        }
    }

    private void createUser(final String name, final String email, final String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            Toast.makeText(MainActivity.this, "Successfully created new account",
                                    Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            assert user != null;
                            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();
                            user.updateProfile(profile);
                            loginUser(email, password);
                        } else {
                            Log.w(TAG, "createUserWithEmail:fail");
                            Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void loginUser(String email, String password) {
        if(email.equals("") || password.equals("")) {
            Toast.makeText(MainActivity.this, "You have to enter something",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            startWelcome();
                        }
                        else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Failed to connect. \n" +
                                            "If you don't have an account, please create one!",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void startWelcome() {
        Intent intent = new Intent(MainActivity.this, ChooseActivity.class);
        startActivity(intent);
    }
}
