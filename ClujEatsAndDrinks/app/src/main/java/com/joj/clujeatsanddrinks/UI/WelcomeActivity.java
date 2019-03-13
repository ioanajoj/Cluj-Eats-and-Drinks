package com.joj.clujeatsanddrinks.UI;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.joj.clujeatsanddrinks.Model.Choice;
import com.joj.clujeatsanddrinks.Model.Location;
import com.joj.clujeatsanddrinks.Model.OpenDayTime;
import com.joj.clujeatsanddrinks.R;
import com.joj.clujeatsanddrinks.UI.Utils.LocationsRecyclerViewAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class WelcomeActivity extends AppCompatActivity {

    // Firebase
    private DatabaseReference mRootRef;
    private DatabaseReference locationRef;
    private DatabaseReference openHoursRef;
    private DatabaseReference hoursRef;
    private DatabaseReference foodRef;

    // widgets
    private RecyclerView mRecyclerView;
    private TextView resultTextView;
    private ProgressBar progressBar;

    // variables
    private LocationsRecyclerViewAdapter locationsRecyclerViewAdapter;
    private ArrayList<String> hourKeys;
    private ArrayList<String> foodKeys;
    private ArrayList<String> locationsIDs;
    private ArrayList<Location> finalLocations;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_activity);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        locationRef = mRootRef.child("locations");
        openHoursRef = mRootRef.child("openHours");
        hoursRef = mRootRef.child("hours");
        foodRef = mRootRef.child("food");

        mRecyclerView = findViewById(R.id.locationsRecyclerView);
        resultTextView = findViewById(R.id.resultTextView);
        progressBar = findViewById(R.id.locationsProgressBar);

        hourKeys = new ArrayList<>();
        foodKeys = new ArrayList<>();
        locationsIDs = new ArrayList<>();
        finalLocations = new ArrayList<>();

        initRecyclerView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getIntent().getExtras() != null) {
            String food = Objects.requireNonNull(getIntent().getExtras().getString("Food")).toLowerCase();
            String day = Objects.requireNonNull(getIntent().getExtras().getString("Day"));
            String time = Objects.requireNonNull(getIntent().getExtras().getString("Time")).toLowerCase();
            startFilter(food, day, time);
        }
        else Log.w("WelcomeActivity", "Failed to read extras");
    }

    /**
     * Initialize recyclerView
     * + add onClick listeners to open location details
     */
    private void initRecyclerView() {
        if(locationsRecyclerViewAdapter == null){
            locationsRecyclerViewAdapter = new LocationsRecyclerViewAdapter(this, finalLocations);
        }
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(locationsRecyclerViewAdapter);

        locationsRecyclerViewAdapter.setOnItemClickListener(new LocationsRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemCLick(int position) {
                Toast.makeText(getApplicationContext(), finalLocations.get(position).toString(), Toast.LENGTH_SHORT).show();
                // This works !
                Intent mIntent = new Intent(WelcomeActivity.this, LocationDetailsActivity.class);
                Bundle mBundle = new Bundle();
                mBundle.putString("Location Name", finalLocations.get(position).getName());
                mIntent.putExtras(mBundle);
                startActivity(mIntent);
            }
        });
    }

    /**
     * Starts request from database based on user input
     * @param food String user input
     * @param day String user input
     * @param hour String user input
     */
    private void startFilter(String food, String day, String hour) {
        Choice choice = new Choice(food, day, hour);
        finalLocations.clear();
        // start loading animation
        progressBar.setVisibility(View.VISIBLE);
        getNewLocations(choice);
    }

    /**
     * Starts cascading async filtering
     * @param choice Choice containing food, day and hour inputted by user
     */
    private void getNewLocations(Choice choice) {
        getLocationsByFood(choice);
    }

    /**
     * Takes IDs of locations having the inputted type of food and asynchronously calls
     * filtering by day and hour
     * @param choice Choice
     */
    private void getLocationsByFood(final Choice choice) {
        String food = choice.getFoodType();
        if (food.equals("anything")) {
            // add all locations
            locationRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot data: dataSnapshot.getChildren())
                        foodKeys.add(data.getKey());
                    getLocationsByTime(choice);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else {
            // add only locations found at "food" in db
            mRootRef = FirebaseDatabase.getInstance().getReference();
            foodRef = mRootRef.child("food").child(food);
            foodRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    foodKeys.clear();
                    for(DataSnapshot data : dataSnapshot.getChildren()) {
                        foodKeys.add(data.getKey());
                    }
                    getLocationsByTime(choice);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    /**
     * Filters by day and hour in choice
     * Adds to final list of location IDs only valid locations
     * @param choice Choice
     */
    private void getLocationsByTime(final Choice choice) {
        mRootRef = FirebaseDatabase.getInstance().getReference();
        hoursRef = mRootRef.child("hours");
        hoursRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                hourKeys.clear();
                for(DataSnapshot data : dataSnapshot.getChildren()) {
                    OpenDayTime odt = data.getValue(OpenDayTime.class);
                    if (odt != null && odt.isOpen(choice.getDay(), choice.getHour()))
                        hourKeys.add(data.getKey());
                }
                mRootRef = FirebaseDatabase.getInstance().getReference();
                openHoursRef = mRootRef.child("openHours");
                openHoursRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        locationsIDs.clear();
                        for(DataSnapshot data : dataSnapshot.getChildren()) {
                            for(DataSnapshot data2 : data.getChildren()) {
                                if(hourKeys.contains(data2.getKey()) && foodKeys.contains(data.getKey()))
                                    locationsIDs.add(data.getKey());
                            }
                        }
                        getLocationDetails();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * Asynchronously called at the end of filtering
     * Takes all details of locations from database
     * Notifies adapter when done
     */
    private void getLocationDetails() {
        mRootRef = FirebaseDatabase.getInstance().getReference();
        locationRef = mRootRef.child("locations");
        locationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                finalLocations.clear();
                for(DataSnapshot data : dataSnapshot.getChildren())
                    if(locationsIDs.contains(data.getKey()))
                        finalLocations.add(data.getValue(Location.class));
                finishLoading();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * End progressBar animation
     * Show list of locations if not empty
     * Else show how sorry you are for not finding a decent place via textView
     */
    private void finishLoading() {
        progressBar.setVisibility(View.GONE);
        if (finalLocations.size() > 0) {
            resultTextView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            locationsRecyclerViewAdapter.notifyDataSetChanged();
        }
        else resultTextView.setVisibility(View.VISIBLE);
    }
}
