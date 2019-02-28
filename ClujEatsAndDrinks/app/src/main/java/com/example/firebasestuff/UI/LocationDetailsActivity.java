package com.example.firebasestuff.UI;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.firebasestuff.Model.DaysOfWeek;
import com.example.firebasestuff.Model.Location;
import com.example.firebasestuff.Model.OpenDayTime;
import com.example.firebasestuff.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

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
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference locationRef = mRootRef.child("locations");
    private DatabaseReference openHoursRef = mRootRef.child("openHours");
    private DatabaseReference hoursRef = mRootRef.child("hours");
    private DatabaseReference savedLocationsRef = mRootRef.child("savedLocations");
    private DatabaseReference locationOpenHoursRef;

    // Open Hours data
    private ArrayList<String> openHoursIDs = new ArrayList<>();
    private ArrayList<OpenDayTime> openDayTimes = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_details_fragment);

        // Get info from Bundle
        String locationName = getIntent().getExtras().getString("Location Name");

        // Get Firebase Stuff
        mAuth = FirebaseAuth.getInstance();
        locationRef.orderByChild("name")
                .equalTo(locationName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot data : dataSnapshot.getChildren()) {
                            final String locationKey = data.getKey();
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
                                    addLocationToFavourites(locationKey);
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

                            if (locationKey != null) {
                                locationOpenHoursRef = openHoursRef.child(locationKey);
                                getOpenHoursIDs();
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "Cum doamne ai ajuns aici", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void addLocationToFavourites(final String locationKey) {
        final String uid = mAuth.getUid();
        DatabaseReference userSavedLocations = savedLocationsRef.child(uid);
        userSavedLocations.child(locationKey).setValue(true, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Bitmap errorIcon = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                            R.drawable.ic_error_black_48dp);
                    circularProgressButton.doneLoadingAnimation(1, errorIcon);
                }
                else {
                    Bitmap doneIcon = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                            R.drawable.ic_done_black_48dp);
                    circularProgressButton.doneLoadingAnimation(1, doneIcon);
                }
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
