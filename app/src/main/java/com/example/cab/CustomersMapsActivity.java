package com.example.cab;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.cab.databinding.ActivityCustomersMapsBinding;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
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

import java.util.HashMap;

public class CustomersMapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        com.google.android.gms.location.LocationListener,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private ActivityCustomersMapsBinding binding;
    GoogleApiClient googleApiClient;
    Location lastlocation;
    LocationRequest locationRequest;
    private Button CustomerLogoutButton;
    private Button CallCabButton;
    private String customerID;
    private LatLng CustomerPickUpLocation;
    private int radius = 1;
    private boolean Driverfound = false, requesttype = false;
    private String DriverFoundID;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    Marker DriverMaker, PickUpMarker;
    private DatabaseReference CustomerDatabaseRef;
    private DatabaseReference DriverAvailableRef;
    private DatabaseReference DriverRef;
    private DatabaseReference DriverLocationRef;
    private ValueEventListener DriverLocationRefListner;

    GeoQuery geoQuery;
    private RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        customerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        CustomerDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Customers Request");
        DriverAvailableRef = FirebaseDatabase.getInstance().getReference().child("Drivers Available");
        DriverLocationRef = FirebaseDatabase.getInstance().getReference().child("Drivers Working");

        binding = ActivityCustomersMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CustomerLogoutButton = findViewById(R.id.Customer_logout_btn);
        CallCabButton = findViewById(R.id.call_cab);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        CustomerLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                LogoutButton();
            }
        });

        CallCabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (requesttype) {
                    requesttype = false;
                    geoQuery.removeAllListeners();
                    DriverLocationRef.removeEventListener(DriverLocationRefListner);
                    if (Driverfound) {
                        DriverRef = FirebaseDatabase.getInstance().getReference().child("User").child("Driver").child(DriverFoundID).child("CustomerRiderID");

                        DriverRef.removeValue();
                        DriverFoundID = null;
                    }
                    Driverfound = false;
                    radius = 1;
                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    GeoFire geoFire = new GeoFire(CustomerDatabaseRef);
                    if (PickUpMarker != null) {
                        PickUpMarker.remove();
                    }
                    if (DriverMaker != null) {
                        DriverMaker.remove();
                    }
                    CallCabButton.setText("Call a Cab");
                } else {
                    requesttype = true;
                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    GeoFire geoFire = new GeoFire(CustomerDatabaseRef);
                    geoFire.setLocation(customerID, new GeoLocation(lastlocation.getLatitude(), lastlocation.getLongitude()));
                    CustomerPickUpLocation = new LatLng(lastlocation.getLatitude(), lastlocation.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(CustomerPickUpLocation).title("My Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.user)));
                    CallCabButton.setText("Getting Your Driver....");
                    GetClosetDriverCab();
                }
            }
        });
    }

    private void GetClosetDriverCab() {
        GeoFire geoFire = new GeoFire(DriverAvailableRef);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(CustomerPickUpLocation.latitude, CustomerPickUpLocation.longitude), radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation geoLocation) {
                if (!Driverfound && requesttype) {
                    Driverfound = true;
                    DriverFoundID = key;
                    DatabaseReference driverWorkingRef = FirebaseDatabase.getInstance().getReference().child("Drivers Working").child(DriverFoundID);
                    driverWorkingRef.setValue(true);
                    DriverRef = FirebaseDatabase.getInstance().getReference().child("User").child("Driver").child(DriverFoundID);
                    HashMap<String, Object> driverMap = new HashMap<>();
                    driverMap.put("CustomerRiderID", customerID);
                    DriverRef.updateChildren(driverMap);
                    GettingDriverLocation();
                    CallCabButton.setText("Looking for Driver Location");
                }
            }

            @Override
            public void onKeyExited(String s) {
            }

            @Override
            public void onKeyMoved(String s, GeoLocation geoLocation) {
            }

            @Override
            public void onGeoQueryReady() {
                if (!Driverfound) {
                    radius = radius + 1;
                    GetClosetDriverCab();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError databaseError) {
            }
        });
    }

    private void GettingDriverLocation() {
        DriverLocationRefListner = DriverLocationRef.child(DriverFoundID).child("l")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && requesttype) {
                            double LocationLat = 0;
                            double LocationLng = 0;
                            for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                if (childSnapshot.getKey().equals("0")) {
                                    LocationLng = Double.parseDouble(childSnapshot.getValue().toString());
                                } else if (childSnapshot.getKey().equals("1")) {
                                    LocationLat = Double.parseDouble(childSnapshot.getValue().toString());
                                }
                            }
                            LatLng DriverLatLng = new LatLng(LocationLat, LocationLng);
                            if (DriverMaker != null) {
                                DriverMaker.remove();
                            }
                            DriverMaker = mMap.addMarker(new MarkerOptions().position(DriverLatLng).title("Your Driver is here").icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
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
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        lastlocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
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
    }

    private void LogoutButton() {
        Intent welcomeIntent = new Intent(CustomersMapsActivity.this, WelcomeActivity.class);
        welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(welcomeIntent);
        finish();
    }
}
