package com.example.firebasestuff.UI;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.example.firebasestuff.MainActivity;
import com.example.firebasestuff.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class ChooseActivity extends AppCompatActivity {

    // Firebase
    private FirebaseAuth mAuth;
    FirebaseDatabase database;

    // Widgets
    private Spinner foodSpinner;
    private Spinner daysOfWeekSpinner;
    private Spinner timeOfDaySpinner;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_activity);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        // Widgets
        ImageButton profileButton = findViewById(R.id.profileButton);
        foodSpinner = findViewById(R.id.foodSpinner);
        daysOfWeekSpinner = findViewById(R.id.daysSpinner);
        timeOfDaySpinner = findViewById(R.id.timeSpinner);

        // Set spinner adapters
        ArrayAdapter<CharSequence> days_adapter = ArrayAdapter.createFromResource(this,
                R.array.days_of_week, android.R.layout.simple_spinner_item);
        days_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daysOfWeekSpinner.setAdapter(days_adapter);
        ArrayAdapter<CharSequence> time_adapter = ArrayAdapter.createFromResource(this,
                R.array.time_of_day, android.R.layout.simple_spinner_item);
        time_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeOfDaySpinner.setAdapter(time_adapter);

        // Set button events
        ImageButton settingButton = findViewById(R.id.settingsButton);
        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(ChooseActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        Button noFilterButton = findViewById(R.id.noFilterButton);
        noFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent(ChooseActivity.this, WelcomeActivity.class);
                Bundle mBundle = new Bundle();
                mBundle.putString("Food", "");
                mBundle.putString("Day", "");
                mBundle.putString("Time", "");

                mIntent.putExtras(mBundle);
                startActivity(mIntent);
            }
        });

        Button filterButton = findViewById(R.id.magicButton);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get values
//                String food = foodSpinner.getSelectedItem().toString();
                String day = daysOfWeekSpinner.getSelectedItem().toString();
                String time = timeOfDaySpinner.getSelectedItem().toString();

                Intent mIntent = new Intent(ChooseActivity.this, WelcomeActivity.class);
                Bundle mBundle = new Bundle();
                mBundle.putString("Food", "");
                mBundle.putString("Day", day);
                mBundle.putString("Time", time);

                mIntent.putExtras(mBundle);
                startActivity(mIntent);
            }
        });

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent intent = new Intent(ChooseActivity.this, ProfileDetailsActivity.class);
               startActivity(intent);
            }
        });
    }
}
