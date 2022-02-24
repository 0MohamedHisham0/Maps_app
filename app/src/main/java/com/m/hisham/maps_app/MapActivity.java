package com.m.hisham.maps_app;

import static com.m.hisham.maps_app.BuildConfig.Ad_unit_id;
import static com.m.hisham.maps_app.BuildConfig.GOOGLE_MAPS_API_KEY;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
import com.m.hisham.maps_app.models.Restaurant_Model;
import com.m.hisham.maps_app.network.remote.Retrofit_Client;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback {
    private GoogleMap mMap;
    private LatLng place1, place2;
    private Marker Marker1, Marker2, MarkerMyLocation;
    private String currentPlace;
    private Button getDirection;
    private Polyline currentPolyline;
    private Boolean isFirstButtonClicked = false;
    private Boolean isSecondButtonClicked = false;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationManager locationManager;
    private String provider;
    private Restaurant_Model restaurant_model;

    //AdMob
    private InterstitialAd mInterstitialAd;

    //Views
    private Button btnFirst;
    private Button btnSecond;
    private Button btnMyLocation;
    private Button btnShowRestaurants;
    private ImageButton btnShowMyLocation;
    Bitmap bmp = null;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);

        initViews();
        initAds();
        checkLocationPermission();

        //Get Current Location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.mapNearBy);
        mapFragment.getMapAsync(this);
    }

    private void initAds() {
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this, "ca-app-pub-3940256099942544/1033173712", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Log.i("AD", "onAdLoaded");

                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when fullscreen content is dismissed.
                                Log.d("TAG", "The ad was dismissed.");
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when fullscreen content failed to show.
                                Log.d("TAG", "The ad failed to show.");
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when fullscreen content is shown.
                                // Make sure to set your reference to null so you don't
                                // show it a second time.
                                mInterstitialAd = null;
                                Log.d("TAG", "The ad was shown.");
                            }
                        });

                        mInterstitialAd.show(MapActivity.this);
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.i("AD", loadAdError.getMessage());
                        mInterstitialAd = null;
                    }
                });


    }

    private void initViews() {
        btnFirst = findViewById(R.id.btnPlaceOne);
        btnSecond = findViewById(R.id.btnPlaceTwo);
        btnMyLocation = findViewById(R.id.btnMyLocation);
        getDirection = findViewById(R.id.btnGetDirection);
        btnShowRestaurants = findViewById(R.id.btnShowRestaurants);
        btnShowMyLocation = findViewById(R.id.btnShowMyLocation);

        if (currentPlace != null)
            btnShowRestaurants.setVisibility(View.VISIBLE);
        else
            btnShowRestaurants.setVisibility(View.GONE);

        btnShowMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCurrentLocation();
                if (currentPlace != null)
                    btnShowRestaurants.setVisibility(View.VISIBLE);
                else
                    btnShowRestaurants.setVisibility(View.GONE);
            }
        });

        btnShowRestaurants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (currentPlace != null) {
                    Call<Restaurant_Model> call = Retrofit_Client.getInstance().getRestaurants("cruise", currentPlace, "5000", "restaurant");
                    call.enqueue(new Callback<Restaurant_Model>() {
                        @Override
                        public void onResponse(Call<Restaurant_Model> call, Response<Restaurant_Model> response) {

                            restaurant_model = response.body();
                            for (int i = 0; i < restaurant_model.getResults().size(); i++) {
                                Restaurant_Model.restaurantItem restaurantItem = restaurant_model.getResults().get(i);

                                LatLng restaurantLocation = new LatLng(Double.parseDouble(restaurantItem.getGeometry().getLocation().getLat()),
                                        Double.parseDouble(restaurantItem.getGeometry().getLocation().getLng()));

                                Thread thread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {

                                        URL url;
                                        try {
                                            url = new URL(restaurantItem.getIcon());
                                            bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {

                                                if (bmp != null) {
                                                    mMap.addMarker(
                                                            new MarkerOptions()
                                                                    .position(restaurantLocation)
                                                                    .title(restaurantItem.getName())
                                                                    .icon(BitmapDescriptorFactory
                                                                            .fromBitmap(bmp)));

                                                }
                                            }
                                        });
                                    }
                                });
                                thread.start();


                            }
                        }

                        @Override
                        public void onFailure(Call<Restaurant_Model> call, Throwable t) {
                            Log.i("btnShowRestaurants", "onClick: btnShowRes" + t.getMessage());

                        }
                    });
                } else {
                    Toast.makeText(MapActivity.this, "Please Show your location first!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        getDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (place1 != null && place2 != null) {
                    new FetchURL(MapActivity.this).execute(getUrl(place1, place2, "DRIVING"), "DRIVING");
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
                if (Marker1 != null)
                    Marker1.remove();
                getCurrentLocationToFirstPlace();
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

                if (MarkerMyLocation != null)
                    MarkerMyLocation.remove();

                if (Marker1 != null)
                    Marker1.remove();

                Marker1 = mMap.addMarker(new MarkerOptions().position(latLng).title("First Location").icon(bitmapDescriptorFromVector(MapActivity.this, R.drawable.icon_location)));

                btnFirst.setText(latLng.toString());
                place1 = latLng;
            }

            if (isSecondButtonClicked) {
                isSecondButtonClicked = false;
                btnSecond.setText(latLng.toString());

                if (Marker2 != null)
                    Marker2.remove();
                Marker2 = mMap.addMarker(new MarkerOptions().position(latLng).title("Second Location").icon(bitmapDescriptorFromVector(MapActivity.this, R.drawable.icon_location)));

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
        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + GOOGLE_MAPS_API_KEY;
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
    }

    public void getCurrentLocationToFirstPlace() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            place1 = new LatLng(location.getLatitude(), location.getLongitude());
                            MarkerMyLocation = mMap.addMarker(new MarkerOptions().position(place1).title("Your Location").icon(bitmapDescriptorFromVector(MapActivity.this, R.drawable.icons_my_location)));
                            btnFirst.setText(place1.toString());
                        }
                    }
                });
    }

    public void getCurrentLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            currentPlace = location.getLatitude() + "," + location.getLongitude();

                            MarkerMyLocation = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Your Location").icon(bitmapDescriptorFromVector(MapActivity.this, R.drawable.icons_my_location)));
                        }
                    }
                });
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

        }
    }

    public static BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void SetImageIntoPicasso(String ImageUrl, ImageView imageView, int ErrorImage) {
        String ImageURL = "https://image.tmdb.org/t/p/w200/" + ImageUrl;
        if (!ImageUrl.isEmpty()) {
            Picasso.get()
                    .load(ImageURL)
                    .into(imageView);
        } else {
            imageView.setImageResource(ErrorImage);
        }
    }
}
