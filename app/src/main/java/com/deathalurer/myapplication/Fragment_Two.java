package com.deathalurer.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

/**
 * Created by Abhinav Singh on 26,April,2020
 */
public class Fragment_Two extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "Fragment_Two" ;
    CountDownTimer timer;
    LocationRequest locationRequest;
    TextView textView;
    private boolean requestingLocationUpdates;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationClient;
    private Location fixedLocation;
    private Double latitude,longitude;
    private boolean canValidate = false;
    private boolean isMapVisible = false;
    private Button showMap;
    private SharedPreferences sharedPreferences;
    private MapView mapView;
    private GoogleMap mMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView: ");
        return inflater.inflate(R.layout.layout_fragment_two,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showMap = view.findViewById(R.id.homeLocationButton);
        mapView = view.findViewById(R.id.showLocation);

        showMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isMapVisible){
                    isMapVisible = true;
                    mapView.onCreate(savedInstanceState);
                    mapView.onResume();
                    mapView.getMapAsync(Fragment_Two.this::onMapReady);
                    mapView.setVisibility(View.VISIBLE);
                    showMap.setText("Hide Map");

                }
                else {
                    isMapVisible = false;
                    mapView.setVisibility(View.GONE);
                    showMap.setText("Show Home Location");
                }
            }
        });

        sharedPreferences = this.getActivity().
                getSharedPreferences("WalletPoints", Context.MODE_PRIVATE);
        boolean isAvailable = sharedPreferences.getBoolean("LocationSelected",false);
        createLocationRequest();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        textView = view.findViewById(R.id.timerTextView);
        timer = new CountDownTimer(20000,1000) {
            @Override
            public void onTick(long l) {
                textView.setText("Time to recheck: " + l/1000);
            }

            @Override
            public void onFinish() {
                canValidate=true;
                reset();
            }
        };

        if(isAvailable){
            timer.start();
        }
        else{
            textView.setText("Please select your home location!");
        }

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.e(TAG," Result" + locationResult);
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    Log.e(TAG, "canValidate: " + canValidate );
                    if(canValidate){
                        Log.e(TAG, "isAvailable: " + isAvailable );
                        if (isAvailable) {
                            latitude = Double.parseDouble(sharedPreferences.getString("Latitude", ""));
                            longitude = Double.parseDouble(sharedPreferences.getString("Longitude", ""));
                            fixedLocation = new Location("");
                            fixedLocation.setLatitude(latitude);
                            fixedLocation.setLongitude(longitude);
                            Log.e(TAG, "Coordinated" + fixedLocation.getLatitude() + " " + fixedLocation.getLongitude());
                            float distance = fixedLocation.distanceTo(location);
                            boolean isWithin = distance < 1000;
                            Log.e(TAG, "isWithin: " + isWithin );
                            if(isWithin){
                                if (!sharedPreferences.contains("Coins")){
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putInt("Coins",10);
                                    editor.apply();
                                    Toast.makeText(getActivity(),"10 points added.",Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    int current = sharedPreferences.getInt("Coins",0);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putInt("Coins",current +10);
                                    editor.apply();
                                    Toast.makeText(getActivity(),"10 points added.",Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
                                if (!sharedPreferences.contains("Coins")){
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putInt("Coins",0);
                                    editor.apply();
                                }
                                else{
                                    int current = sharedPreferences.getInt("Coins",0);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putInt("Coins",current - 10);
                                    editor.apply();
                                    Toast.makeText(getActivity(),"10 points deducted.",Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                }
            }
        };


    }
    void reset(){
        timer.start();
    }


    protected void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(17000);
        //locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(getActivity());
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
                Log.e(TAG,"Location Request success");
                requestingLocationUpdates = true;
            }
        });

        task.addOnFailureListener(getActivity(), new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG,"Location Request failure");
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(getActivity(),
                                1);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (requestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        Log.e(TAG,"Method statLocationUpdates called");
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Double lati,longi;
        lati = Double.parseDouble(sharedPreferences.getString("Latitude",""));
        longi = Double.parseDouble(sharedPreferences.getString("Longitude",""));
        LatLng placeLatLng = new LatLng(lati,longi);
        mMap = googleMap;
        mMap.addMarker(new MarkerOptions().position(placeLatLng).title("Marker in Current Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(placeLatLng));
    }

}
