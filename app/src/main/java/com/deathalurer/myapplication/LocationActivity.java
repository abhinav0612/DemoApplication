package com.deathalurer.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class LocationActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "LocationActivity";
    private GoogleMap mMap;
    private MapView mapView;
    private Button pickButton,currentButton,goTo;
    private List<Place.Field> fields;
    private String placeName;
    private LatLng placeLatLng = new LatLng(46.21, 121.33);
    private FusedLocationProviderClient fusedLocationClient;
    private Location mCurrentLocation;
    private TextView locationSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        isPermissionGranted();
        mapView = findViewById(R.id.mapView);
        goTo = findViewById(R.id.goToTimer);
        currentButton = findViewById(R.id.chooseCurrentLocation);
        pickButton = findViewById(R.id.pickLocation);
        locationSelected = findViewById(R.id.locationSelectedTextView);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();

        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        PlacesClient placesClient = Places.createClient(this);
        fields = Arrays.asList(Place.Field.ID,Place.Field.NAME,Place.Field.LAT_LNG);

        SharedPreferences sharedPreferences = getSharedPreferences("WalletPoints", Context.MODE_PRIVATE);
        if (sharedPreferences.contains("LocationSelected")){
            boolean isAvailable = sharedPreferences.getBoolean("LocationSelected",false);
            if (isAvailable){
                Log.e(TAG, "Latitude : "+ sharedPreferences.getString("Latitude","")+" Longitude: " + sharedPreferences.getString("Longitude","") );
                currentButton.setVisibility(View.INVISIBLE);
                pickButton.setVisibility(View.INVISIBLE);
                locationSelected.setVisibility(View.VISIBLE);
                Log.e(TAG,"Location already present");
                Intent intent = new Intent(this,MapsActivity.class);
                startActivity(intent);
            }
        }

        currentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(LocationActivity.this);
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(LocationActivity.this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    placeLatLng = new LatLng(location.getLatitude(),location.getLongitude());
                                    mCurrentLocation = location;
                                    mapView.getMapAsync(LocationActivity.this);
                                    Log.e(TAG,location.getLatitude()+" " +location.getLongitude());
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    String latitude,longitude;
                                    latitude = String.valueOf(mCurrentLocation.getLatitude());
                                    longitude = String.valueOf(mCurrentLocation.getLongitude());
                                    editor.putString("Latitude",latitude);
                                    editor.putString("Longitude",longitude);
                                    editor.putBoolean("LocationSelected",true);
                                    editor.apply();
                                    pickButton.setVisibility(View.INVISIBLE);
                                    currentButton.setVisibility(View.INVISIBLE);
                                    locationSelected.setText("Home Location :\n"+"Latitude: " + latitude +"\nLongitude: " +
                                            longitude);
                                    locationSelected.setVisibility(View.VISIBLE);
                                    goTo.setVisibility(View.VISIBLE);
                                    Toast.makeText(LocationActivity.this,"Location Saved",Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Log.e(TAG,"Null Location");
                                }
                            }
                        });

            }
        });
        goTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LocationActivity.this,MapsActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // to pick location...
        pickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.FULLSCREEN, fields)
                        .build(LocationActivity.this);
                startActivityForResult(intent, 1);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
            if(resultCode == RESULT_OK){
                Place place = Autocomplete.getPlaceFromIntent(data);
                placeName = place.getName();
                placeLatLng = place.getLatLng();
                mMap.addMarker(new MarkerOptions().position(placeLatLng).title(placeName));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(placeLatLng));
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(placeLatLng,15);
                mMap.animateCamera(cameraUpdate);
            }
            else if(resultCode == AutocompleteActivity.RESULT_ERROR){
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i("_______", status.getStatusMessage());
            }
            else {
                Log.e("_____","Cancelled");
            }
        }
    }
    void isPermissionGranted(){
        Log.e(TAG, "isPermissionGranted: " );
        if (ContextCompat.checkSelfPermission(LocationActivity.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(LocationActivity.this,
                    Manifest.permission.READ_CONTACTS)) {
            }
            else{
                ActivityCompat.requestPermissions(LocationActivity.this,
                        new String[]{
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.INTERNET,
                                Manifest.permission.ACCESS_WIFI_STATE,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION},1);
            }

        }
        else
        {
            Log.e(TAG, "isPermissionGranted: Already granted");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e(TAG, "onRequestPermissionsResult: " );
        if (requestCode==1){
            Log.e(TAG, "onRequestPermissionsResult: request code : " + requestCode );
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
            } else {
                isPermissionGranted();
            }
            return;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.addMarker(new MarkerOptions().position(placeLatLng).title("Marker in Current Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(placeLatLng));
    }
}

