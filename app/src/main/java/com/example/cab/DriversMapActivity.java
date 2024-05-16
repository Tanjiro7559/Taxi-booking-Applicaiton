package com.example.cab;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.cab.databinding.ActivityDriversMapBinding;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class DriversMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    private ActivityDriversMapBinding binding;

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private Marker pickUpMarker;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference assignedCustomerRef, assignedCustomerPickUpRef;
    private String driverID, customerID = "";
    private ValueEventListener assignedCustomerPickUpRefListener;
    private boolean currentLogOutDriverStatus = false;

    private Button logOutDriverBtn;
    private Button settingsDriverBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            driverID = currentUser.getUid();
        }

        binding = ActivityDriversMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        logOutDriverBtn = findViewById(R.id.Driver_logout_btn);
        settingsDriverBtn = findViewById(R.id.Driver_setting_btn);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        logOutDriverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentLogOutDriverStatus = true;
                disconnectTheDriver();
                mAuth.signOut();
                logOutDriver();
            }
        });

        getAssignedCustomerRequest();
    }

    private void getAssignedCustomerRequest() {
        assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("User").child("Drivers").child(driverID).child("CustomerRiderID");
        assignedCustomerPickUpRefListener = assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    customerID = snapshot.getValue().toString();
                    getAssignedCustomerPickUpLocation();
                } else {
                    customerID = "";
                    if (pickUpMarker != null) {
                        pickUpMarker.remove();
                    }
                    if (assignedCustomerPickUpRefListener != null) {
                        assignedCustomerRef.removeEventListener(assignedCustomerPickUpRefListener);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void getAssignedCustomerPickUpLocation() {
        assignedCustomerPickUpRef = FirebaseDatabase.getInstance().getReference().child("Customer Request").child(customerID).child("l");
        assignedCustomerPickUpRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<Object> customerLocationMap = (List<Object>) snapshot.getValue();
                    double locationLat = 0, locationLng = 0;
                    if (customerLocationMap != null && customerLocationMap.size() >= 2) {
                        locationLng = Double.parseDouble(customerLocationMap.get(0).toString());
                        locationLat = Double.parseDouble(customerLocationMap.get(1).toString());
                        LatLng driverLatLng = new LatLng(locationLat, locationLng);
                        if (mMap != null) {
                            if (pickUpMarker != null) {
                                pickUpMarker.remove();
                            }
                            pickUpMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Customer Pick Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.user)));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull com.google.android.gms.common.ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        lastLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));

        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference driverAvailabilityRef = FirebaseDatabase.getInstance().getReference().child("Drivers Available");
        GeoFire geoFireAvailability = new GeoFire(driverAvailabilityRef);
        DatabaseReference driverWorkingRef = FirebaseDatabase.getInstance().getReference().child("Drivers Working");
        GeoFire geoFireWorking = new GeoFire(driverWorkingRef);

        switch (customerID) {
            case "":
                geoFireWorking.removeLocation(userID);
                geoFireAvailability.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                break;
            default:
                geoFireAvailability.removeLocation(userID);
                geoFireWorking.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                break;
        }
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!currentLogOutDriverStatus) {
            disconnectTheDriver();
        }
    }

    private void disconnectTheDriver() {
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference driverAvailabilityRef = FirebaseDatabase.getInstance().getReference().child("Drivers Available");
        GeoFire geoFire = new GeoFire(driverAvailabilityRef);
        geoFire.removeLocation(userID);
    }

    private void logOutDriver() {
        Intent welcomeIntent = new Intent(DriversMapActivity.this, WelcomeActivity.class);
        welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(welcomeIntent);
        finish();
    }
}
