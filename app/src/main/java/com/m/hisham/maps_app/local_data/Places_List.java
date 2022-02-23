package com.m.hisham.maps_app.local_data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.m.hisham.maps_app.R;
import com.m.hisham.maps_app.models.Place;

import java.util.ArrayList;
import java.util.List;

public class Places_List {
    private final List<Place> placeList = new ArrayList<>();

    public static List<Place> getPlaceList(Context context) {
        List<Place> placeList = new ArrayList<>();

//        Grand Kadri Hotel By Cristal Lebanon
//        33.85148430277257, 35.895525763213946

//        Germanos - Pastry
//        33.85217073479985, 35.89477838111461

//        Malak el Tawook
//        33.85334017189446, 35.89438946093824

//        Z Burger House
//        33.85454300475094, 35.894561122304474

//        Collège Oriental
//        33.85129821373707, 35.89446263654391

//        VERO MODA
//        33.85048738635312, 35.89664059012788

        placeList.add(new Place("Grand Kadri Hotel By Cristal Lebanon", new LatLng(33.85148430277257, 35.895525763213946), bitmapDescriptorFromVector(context, R.drawable.icon_location)));
        placeList.add(new Place("Germanos - Pastry", new LatLng( 33.85217073479985,35.89477838111461 ), bitmapDescriptorFromVector(context, R.drawable.icon_location)));
        placeList.add(new Place("Malak el Tawook", new LatLng(33.85334017189446, 35.89438946093824 ), bitmapDescriptorFromVector(context, R.drawable.icon_location)));
        placeList.add(new Place("Z Burger House", new LatLng( 33.85454300475094 ,35.894561122304474 ), bitmapDescriptorFromVector(context, R.drawable.icon_location)));
        placeList.add(new Place("Collège Oriental", new LatLng(33.85129821373707, 35.89446263654391 ), bitmapDescriptorFromVector(context, R.drawable.icon_location)));
        placeList.add(new Place("VERO MODA", new LatLng(33.85048738635312,35.89664059012788 ), bitmapDescriptorFromVector(context, R.drawable.icon_location)));

        return placeList;
    }

    public static BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

}
