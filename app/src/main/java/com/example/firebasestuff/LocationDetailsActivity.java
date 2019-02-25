package com.example.firebasestuff;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

public class LocationDetailsActivity extends AppCompatActivity {

    // Widgets
    private Location mLocation;
    private TextView locationNameTextView;
    private ImageView locationImageView;
    private TextView addressTextView;
    private CircularProgressButton circularProgressButton;


    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference locationRef = mRootRef.child("locations");
    private DatabaseReference openHoursRef = mRootRef.child("openHours");
    private DatabaseReference hoursRef = mRootRef.child("hours");
    private DatabaseReference locationOpenHoursRef;

    // Open Hours data
    private ArrayList<String> openHoursIDs = new ArrayList<>();
    private ArrayList<OpenDayTime> openDayTimes = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.location_details_fragment);

        // Get info from Bundle
        String locationName = getIntent().getExtras().getString("Location Name");

        // Get Firebase Stuff
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        locationRef.orderByChild("name")
                .equalTo(locationName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String locationKey = "";
                        for(DataSnapshot data : dataSnapshot.getChildren()) {
                            locationKey = data.getKey();
                            mLocation = data.getValue(Location.class);

                            locationNameTextView = findViewById(R.id.locationNameTextView);
                            addressTextView = findViewById(R.id.addressTextView);
                            locationImageView = findViewById(R.id.location_imageview);
                            circularProgressButton = findViewById(R.id.circular_progress_image_button);
                            circularProgressButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    circularProgressButton.startMorphAnimation();
                                    circularProgressButton.startAnimation();

                                    // DO STUFF

                                    Bitmap doneIcon = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                                            R.drawable.ic_done_black_48dp);
                                    circularProgressButton.doneLoadingAnimation(1, doneIcon);
                                }
                            });

                            View locationView = findViewById(R.id.location_imageview);
                            locationView.setTransitionName(getString(R.string.locationImageTransition));


                            locationNameTextView.setText(mLocation.getName());
                            addressTextView.setText(mLocation.getAddress());
                            RequestOptions requestOptions = new RequestOptions()
                                    .placeholder(R.drawable.ic_launcher_background);
                            Glide.with(getApplicationContext())
                                    .load(mLocation.getPhoto())
                                    .apply(requestOptions)
                                    .into(locationImageView);
                        }
                        locationOpenHoursRef = openHoursRef.child(locationKey);
                        getOpenHoursIDs();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void getOpenHoursIDs() {
        locationOpenHoursRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()) {
                    String id = data.getKey();
                    openHoursIDs.add(id);
                }
                getOpenHours();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getOpenHours() {
        hoursRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()) {
                    if (openHoursIDs.contains(data.getKey())) {
                        OpenDayTime odt = data.getValue(OpenDayTime.class);
                        openDayTimes.add(odt);
                    }
                }
                initOpenHoursTable();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public ArrayList<String> getHoursPerDay(String day) {
        ArrayList<String> result = new ArrayList<>();
        for(OpenDayTime odt : openDayTimes) {
            if (odt.getDay().equalsIgnoreCase(day))
                result.add(odt.getOpen() + " to " + odt.getClose());
        }
        return result;
    }

    public void initOpenHoursTable() {
        TextView textView = null;
        for(DaysOfWeek day : DaysOfWeek.values()) {
            String name = day.name();
            if (name.equals("Monday"))
                textView = findViewById(R.id.MondayOpenHours);
            if (name.equals("Tuesday"))
                textView = findViewById(R.id.TuesdayOpenHours);
            if (name.equals("Wednesday"))
                textView = findViewById(R.id.WednesdayOpenHours);
            if (name.equals("Thursday"))
                textView = findViewById(R.id.ThursdayOpenHours);
            if (name.equals("Friday"))
                textView = findViewById(R.id.FridayOpenHours);
            if (name.equals("Saturday"))
                textView = findViewById(R.id.SaturdayOpenHours);
            if (name.equals("Sunday"))
                textView = findViewById(R.id.SundayOpenHours);
            ArrayList<String> result = getHoursPerDay(name);
            if (result.size() > 1) {
                String hours = TextUtils.join(" and ", result);
                textView.setText(hours);
            }
            else if (result.size() == 1){
                textView.setText(result.get(0));
            }
            else textView.setText("Closed");

        }
    }
}
