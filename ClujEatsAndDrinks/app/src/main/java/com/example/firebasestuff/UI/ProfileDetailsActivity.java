package com.example.firebasestuff.UI;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.firebasestuff.Model.Location;
import com.example.firebasestuff.R;
import com.example.firebasestuff.UI.Utils.LocationsRecyclerViewAdapter;
import com.example.firebasestuff.UI.Utils.SavedLocationsRecyclerViewAdapter;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ProfileDetailsActivity extends AppCompatActivity {

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference mRootRef;
    private DatabaseReference favouriteLocations;
    private DatabaseReference locations;

    // Widgets
    private TextView nameTextView;
    private ImageView profileImageView;
    private RecyclerView mRecyclerView;
    private EditText displayNameEditText;
    private ProgressBar imageProgressBar;
    private Button saveButton;

    // Vars
    private ArrayList<Location> savedLocations = new ArrayList<>();
    private ArrayList<String> locationKeys = new ArrayList<>();
    private SavedLocationsRecyclerViewAdapter locationsRecyclerViewAdapter;
    private static final int CHOOSE_IMAGE = 101;
    private Uri uriProfileImage;
    private String profileImageUrl;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_layout);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        locations = mRootRef.child("locations");

        nameTextView = findViewById(R.id.nameTextView);
        profileImageView = findViewById(R.id.profileImageView);
        mRecyclerView = findViewById(R.id.savedLocationsRecyclerView);
        imageProgressBar = findViewById(R.id.imageProgressBar);
        saveButton = findViewById(R.id.saveImageButton);

        setListeners();
        updateVisuals();
        getFavouriteLocations();
        initRecyclerView();
    }

    private void getFavouriteLocations() {
        favouriteLocations = mRootRef.child("savedLocations").child(user.getUid());
        favouriteLocations.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()) {
                    locationKeys.add(data.getKey());
                }
                getFavouriteLocationInfo();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getFavouriteLocationInfo() {
        locations.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()) {
                    if (locationKeys.contains(data.getKey()))
                        savedLocations.add(data.getValue(Location.class));
                }
                locationsRecyclerViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initRecyclerView() {
        if(locationsRecyclerViewAdapter == null){
            locationsRecyclerViewAdapter = new SavedLocationsRecyclerViewAdapter(this, savedLocations);
        }

        GridLayoutManager layoutManager = new GridLayoutManager(this,1);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(locationsRecyclerViewAdapter);
        mRecyclerView.scrollToPosition(1);

        locationsRecyclerViewAdapter.setOnItemClickListener(new LocationsRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemCLick(int position) {
                Toast.makeText(getApplicationContext(), savedLocations.get(position).toString(), Toast.LENGTH_SHORT).show();
                View imageView = findViewById(R.id.location_imageview);
                Intent mIntent = new Intent(ProfileDetailsActivity.this, LocationDetailsActivity.class);
//                Pair<View, String> pair1 = new Pair<>(imageView, imageView.getTransitionName());
//                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(ProfileDetailsActivity.this, pair1);
                Bundle mBundle = new Bundle();
                mBundle.putString("Location Name", savedLocations.get(position).getName());
                mIntent.putExtras(mBundle);
//                startActivity(mIntent, options.toBundle());
                startActivity(mIntent);
            }
        });
    }

    private void setListeners() {
        displayNameEditText = new EditText(this);
        displayNameEditText.setHint("Enter new display name");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change your display name")
                .setView(displayNameEditText)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final FirebaseUser user = mAuth.getCurrentUser();
                        String newDisplayName = displayNameEditText.getText().toString();
                        if (user != null && !newDisplayName.equals("")) {
                            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(newDisplayName)
                                    .build();
                            user.updateProfile(profile)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(getApplicationContext(), "successful update", Toast.LENGTH_SHORT).show();
                                            nameTextView.setText("Hello, " + user.getDisplayName());
                                        }
                                    });
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Discard", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        final AlertDialog ad = builder.create();

        // Open Dialog to change displayName
        nameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                ad.show();
            }
        });

        // change profile image
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageChooser();
            }
        });

        // save profile image
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInformation();
            }
        });
    }

    private void updateVisuals() {
        if (user != null) {
            nameTextView.setText("Hello, " + user.getDisplayName());
            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl().toString())
                        .apply(RequestOptions.circleCropTransform())
                        .into(profileImageView);
            }
            else profileImageView.setImageResource(R.drawable.ic_account_circle_black_48dp);
        }
    }

    private void showImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select profile image"), CHOOSE_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uriProfileImage = data.getData();
            Glide.with(this)
                    .load(uriProfileImage)
                    .apply(RequestOptions.circleCropTransform())
                    .into(profileImageView);
            uploadImageToFirebaseStorage();
        }
    }

    private void uploadImageToFirebaseStorage() {
        final StorageReference profileImageRef = FirebaseStorage.getInstance().getReference("profilepics/" + mAuth.getUid() + ".jpg");
        if (uriProfileImage != null) {
            imageProgressBar.setVisibility(View.VISIBLE);
            UploadTask uploadTask = profileImageRef.putFile(uriProfileImage);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful())
                        throw Objects.requireNonNull(task.getException());
                    // Continue with the task to get the download URL
                    return profileImageRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        Toast.makeText(getApplicationContext(), "Uploading", Toast.LENGTH_SHORT).show();
                        if (downloadUri != null) {
                            profileImageUrl = downloadUri.toString();
                            Toast.makeText(getApplicationContext(), "Successfully uploaded", Toast.LENGTH_SHORT).show();
                            saveButton.setVisibility(View.VISIBLE);
                        }
                        imageProgressBar.setVisibility(View.GONE);
                    } else {
                        // Handle failures
                        Toast.makeText(getApplicationContext(), "Fail", Toast.LENGTH_SHORT).show();
                        imageProgressBar.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    private void saveUserInformation() {
        System.out.println(user + " " + profileImageUrl);
        if (user != null && profileImageUrl != null) {
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(Uri.parse(profileImageUrl))
                    .build();
            Toast.makeText(getApplicationContext(), user + " " + profileImageUrl, Toast.LENGTH_SHORT).show();
            user.updateProfile(profile)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                saveButton.setVisibility(View.GONE);
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "Profile update fail", Toast.LENGTH_SHORT).show();
                                saveButton.setVisibility(View.GONE);
                            }
                        }
                    });
        }
    }
}
