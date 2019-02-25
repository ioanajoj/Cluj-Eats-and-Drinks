package com.example.firebasestuff;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class LocationDetailsFragment extends Fragment {

    Location mLocation;
    TextView locationNameTextView;
    TextView openHoursTextView;
    ImageView locationImageView;

    // Firebase
    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference locationRef = mRootRef.child("locations");
    DatabaseReference openHoursRef = mRootRef.child("openHours");
    DatabaseReference hoursRef = mRootRef.child("hours");
    DatabaseReference locationOpenHoursRef;

    // Open Hours data
    ArrayList<String> openHoursIDs = new ArrayList<>();
    ArrayList<OpenDayTime> openDayTimes = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.location_details_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        locationRef.orderByChild("name")
                .equalTo(mLocation.getName())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String locationKey = "";
                        for(DataSnapshot data : dataSnapshot.getChildren()) {
                            locationKey = data.getKey();
                        }
                        locationOpenHoursRef = openHoursRef.child(locationKey);
                        getOpenHoursIDs();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        locationNameTextView = view.findViewById(R.id.locationNameTextView);
        locationImageView = view.findViewById(R.id.location_imageview);

        locationNameTextView.setText(mLocation.getName());
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.ic_launcher_background);
        Glide.with(this)
                .load(mLocation.getPhoto())
                .apply(requestOptions)
                .into(locationImageView);
    }

    public void setLocation(Location location) {
        this.mLocation = location;
    }

    private void getOpenHoursIDs() {
        locationOpenHoursRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()) {
                    String id = data.getKey();
                    openHoursIDs.add(id);
                }
                openHoursTextView.setText(openHoursIDs.toString());
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
                    openHoursTextView.setText(openDayTimes.toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
