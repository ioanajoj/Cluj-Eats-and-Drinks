package com.example.firebasestuff.UI;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.widget.Toast;

import com.example.firebasestuff.Model.Choice;
import com.example.firebasestuff.Model.Location;
import com.example.firebasestuff.Model.OpenDayTime;
import com.example.firebasestuff.R;
import com.example.firebasestuff.UI.Utils.LocationsRecyclerViewAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class WelcomeActivity extends AppCompatActivity {

    // Firebase
    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference locationRef = mRootRef.child("locations");
    DatabaseReference openHoursRef = mRootRef.child("openHours");
    DatabaseReference hoursRef = mRootRef.child("hours");

    // widgets
    private RecyclerView mRecyclerView;

    // vars
    private ArrayList<Location> locations = new ArrayList<>();
    private LocationsRecyclerViewAdapter locationsRecyclerViewAdapter;
    private ArrayList<String> hourKeys = new ArrayList<>();
    private ArrayList<String> locationsIDs = new ArrayList<>();
    private ArrayList<Location> finalLocations = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_activity);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        mRecyclerView = findViewById(R.id.locationsRecyclerView);
        initRecyclerView();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        String email = mAuth.getCurrentUser().getEmail();

        locationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                finalLocations.clear();
                for(DataSnapshot snap : dataSnapshot.getChildren()) {
                    Location location = snap.getValue(Location.class);
                    System.out.println(location);
                    locations.add(location);
                    finalLocations.add(location);
                }
                locationsRecyclerViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("WelcomeActivity", "Failed to read value.", error.toException());
            }
        });

        String food = getIntent().getExtras().getString("Food");
        String day = getIntent().getExtras().getString("Day");
        String time = getIntent().getExtras().getString("Time");
        startFilter("", day, time);
    }

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

                // Slider method
//                Location selectedLocation = finalLocations.get(position);
//                initCustomView(selectedLocation);
//                mSweetSheet.toggle();

                // This works !
                Intent mIntent = new Intent(WelcomeActivity.this, LocationDetailsActivity.class);
                Bundle mBundle = new Bundle();
                mBundle.putString("Location Name", finalLocations.get(position).getName());
                mIntent.putExtras(mBundle);
                startActivity(mIntent);

//                View imageView = findViewById(R.id.profileImageView);
//                Intent mIntent = new Intent(WelcomeActivity.this, LocationDetailsActivity.class);
//                Pair<View, String> pair1 = new Pair<>(imageView, imageView.getTransitionName());
//                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(WelcomeActivity.this, pair1);
//                Bundle mBundle = new Bundle();
//                mBundle.putString("Location Name", finalLocations.get(position).getName());
//                mIntent.putExtras(mBundle);
//                startActivity(mIntent, options.toBundle());

                // New Fragment method
//                LocationDetailsFragment fragment = new LocationDetailsFragment();
//                fragment.setLocation(finalLocations.get(position));
//                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//                ft.replace(R.id.fragment_container, fragment);
//                ft.commit();

            }
        });
    }

    private void startFilter(String food, String day, String hour) {
        Choice choice = new Choice(food, day, hour);
        finalLocations.clear();
        getNewLocations(choice);
        locationsRecyclerViewAdapter.notifyDataSetChanged();
    }

    private void getNewLocations(Choice choice) {
        getLocationsByHour(choice);
    }

    private void getLocationsByHour(final Choice choice) {

        hoursRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                hourKeys.clear();
                for(DataSnapshot data : dataSnapshot.getChildren()) {
                    OpenDayTime odt = data.getValue(OpenDayTime.class);
                    if (odt.isOpen(choice.getDay(), choice.getHour()))
                        hourKeys.add(data.getKey());
                }
                openHoursRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        locationsIDs.clear();
                        for(DataSnapshot data : dataSnapshot.getChildren()) {
                            for(DataSnapshot data2 : data.getChildren()) {
                                if(hourKeys.contains(data2.getKey()))
                                    locationsIDs.add(data.getKey());
                            }
                        }
                        locationRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                finalLocations.clear();
                                for(DataSnapshot data : dataSnapshot.getChildren())
                                    if(locationsIDs.contains(data.getKey()))
                                        finalLocations.add(data.getValue(Location.class));
                                locationsRecyclerViewAdapter.notifyDataSetChanged();

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

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
