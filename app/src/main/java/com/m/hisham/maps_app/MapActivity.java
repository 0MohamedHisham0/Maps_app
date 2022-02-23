package com.m.hisham.maps_app;

import static com.m.hisham.maps_app.BuildConfig.GOOGLE_MAPS_API_KEY;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.m.hisham.maps_app.directionhelpers.FetchURL;
import com.m.hisham.maps_app.directionhelpers.TaskLoadedCallback;
import com.m.hisham.maps_app.local_data.Places_List;
import com.m.hisham.maps_app.models.Place;

import java.util.List;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback {


    private GoogleMap mMap;
    private LatLng place1, place2;
    Button getDirection;
    private Polyline currentPolyline;
    private Boolean isFirstButtonClicked = false;
    private Boolean isSecondButtonClicked = false;
    private FusedLocationProviderClient fusedLocationClient;

    //Views
    private Button btnFirst;
    private Button btnSecond;
    private Button btnMyLocation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);
        initViews();

        //Get Current Location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.mapNearBy);
        mapFragment.getMapAsync(this);
    }

    private void initViews() {
        btnFirst = findViewById(R.id.btnPlaceOne);
        btnSecond = findViewById(R.id.btnPlaceTwo);
        btnMyLocation = findViewById(R.id.btnMyLocation);
        getDirection = findViewById(R.id.btnGetDirection);

        getDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (place1 != null && place2 != null) {
                    new FetchURL(MapActivity.this).execute(getUrl(place1, place2, "driving"), "driving");
                } else {
                    Toast.makeText(MapActivity.this, "Please choose first and second location", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isFirstButtonClicked = true;

            }
        });
        btnSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isSecondButtonClicked = true;
            }
        });
        btnMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCurrentLocation();
                if (place1 != null)
                    btnFirst.setText(place1.toString());

            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        addMarker(mMap);


        mMap.setOnMapClickListener(latLng -> {
            if (isFirstButtonClicked) {
                isFirstButtonClicked = false;
                btnFirst.setText(latLng.toString());
                place1 = latLng;
            }

            if (isSecondButtonClicked) {
                isSecondButtonClicked = false;
                btnSecond.setText(latLng.toString());
                place2 = latLng;
            }
        });


        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {

                if (isFirstButtonClicked) {
                    isFirstButtonClicked = false;
                    btnFirst.setText(marker.getTitle().toString());
                    place1 = marker.getPosition();
                }

                if (isSecondButtonClicked) {
                    isSecondButtonClicked = false;
                    btnSecond.setText(marker.getTitle().toString());
                    place2 = marker.getPosition();
                }

                return true;
            }
        });
    }

    private void addMarker(GoogleMap mMap) {
        List<Place> placeList = Places_List.getPlaceList(this);
        for (int i = 0; i < placeList.size(); i++) {
            Place currentPlace = placeList.get(i);
            mMap.addMarker(new MarkerOptions().position(currentPlace.getLatLng()).title(currentPlace.getName()).icon(currentPlace.getIcon()));
        }
    }

    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + GOOGLE_MAPS_API_KEY;
        return url;
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
    }

    public void getCurrentLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            place1 = new LatLng(location.getLatitude(), location.getLongitude());
                        }
                    }
                });
    }
}
