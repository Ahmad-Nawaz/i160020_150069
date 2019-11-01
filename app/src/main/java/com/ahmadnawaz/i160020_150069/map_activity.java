package com.ahmadnawaz.i160020_150069;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class map_activity extends FragmentActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    private Location mlocation=null;
    private static final int Request_Code=101;
    FusedLocationProviderClient fusedLocationProviderClient;
    String dest_add;
    static double dist_covered;
    private Object activity_second;
    LatLng dest_loc;

    Marker activeMarker = null;
    Button nav_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        nav_btn=findViewById(R.id.nav_b);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(map_activity.this);
        Getlastlocation();


        nav_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dest_loc!=null) {
                    loadNavigationView(String.valueOf(dest_loc.latitude), String.valueOf(dest_loc.longitude));
                }
            }
        });


    }




    private void Getlastlocation() {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(map_activity.this,
            new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
            Request_Code);
            return;
        }
        Task<Location> task=fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location!=null){
                    mlocation=location;
                    Toast.makeText(getApplicationContext(),mlocation.getLatitude()+" "+mlocation.getLongitude(),
                            Toast.LENGTH_LONG).show();

                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
                    mapFragment.getMapAsync(map_activity.this);

                }

            }
        });
        Toast.makeText(this, "last location is "+mlocation+" =-> "+mlocation, Toast.LENGTH_SHORT).show();
    }


    @SuppressLint("LongLogTag")
    @Override
    public void onMapReady(GoogleMap googleMap) {

        Toast.makeText(getApplicationContext(),"Map called",
                Toast.LENGTH_LONG).show();


        mMap = googleMap;

        mMap.setMyLocationEnabled(true);

        LatLng src_loc = new LatLng(mlocation.getLatitude(),mlocation.getLongitude());

        String src_loc1=src_loc.latitude+","+src_loc.longitude;

        dest_add=getIntent().getStringExtra("Address");  // destination address

        StringTokenizer st3 = new StringTokenizer(dest_add,";", false);
        double lat=0;
        double lon=0;
        dest_loc=null;
        String dest_loc1=null;
        if(st3.hasMoreTokens()) {

            lat=Double.parseDouble(st3.nextToken());
            lon=Double.parseDouble(st3.nextToken());
            dest_loc = new LatLng(lat,lon);
            dest_loc1=dest_loc.latitude+","+dest_loc.longitude;
        }


        MarkerOptions marker = new MarkerOptions().position(src_loc).title("i am here");
        marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        activeMarker = mMap.addMarker(marker);        // creating the reference of source_location
        mMap.moveCamera(CameraUpdateFactory.newLatLng(src_loc));

         // no need of changing or maintaing the record of destination marker
        mMap.addMarker(new MarkerOptions().position(dest_loc).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title("destination"));

        List<LatLng> path1= get_pathpolyline_Array(src_loc1, dest_loc1);
        //Draw the polyline
        if (path1.size() > 0) {
            Log.i("---------------->", String.valueOf(path1.size()));
            PolylineOptions opts = new PolylineOptions().addAll(path1).color(Color.RED).width(6);
            mMap.addPolyline(opts);
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(src_loc, 6));
    }







    public List<LatLng> get_pathpolyline_Array(String src_loc1, String dest_loc1){

        List<LatLng> path = new ArrayList(); // filling the array list of path with all polylines

        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey("AIzaSyCCmzBL51FyMCXVLkVG1KJOz8JR9yuXbeM")
                .build();
        DirectionsApiRequest req = DirectionsApi.getDirections(context,src_loc1, dest_loc1);
        try {
            DirectionsResult res = req.await();

            //Loop through legs and steps to get encoded polylines of each step
            if (res.routes != null && res.routes.length > 0) {
                DirectionsRoute route = res.routes[0];

                if (route.legs !=null) {
                    for(int i=0; i<route.legs.length; i++) {
                        DirectionsLeg leg = route.legs[i];
                        if (leg.steps != null) {
                            for (int j=0; j<leg.steps.length;j++){
                                DirectionsStep step = leg.steps[j];
                                if (step.steps != null && step.steps.length >0) {
                                    for (int k=0; k<step.steps.length;k++){
                                        DirectionsStep step1 = step.steps[k];
                                        EncodedPolyline points1 = step1.polyline;
                                        if (points1 != null) {
                                            //Decode polyline and add points to list of route coordinates
                                            List<com.google.maps.model.LatLng> coords1 = points1.decodePath();
                                            for (com.google.maps.model.LatLng coord1 : coords1) {
                                                path.add(new LatLng(coord1.lat, coord1.lng));
                                            }
                                        }
                                    }
                                } else {
                                    EncodedPolyline points = step.polyline;
                                    if (points != null) {
                                        //Decode polyline and add points to list of route coordinates
                                        List<com.google.maps.model.LatLng> coords = points.decodePath();
                                        for (com.google.maps.model.LatLng coord : coords) {
                                            path.add(new LatLng(coord.lat, coord.lng));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch(Exception ex) {
            Log.e("adkk", ex.getLocalizedMessage());
        }
        return path;
    }

    public void loadNavigationView(String lat,String lng){
        Uri navigation = Uri.parse("google.navigation:q="+lat+","+lng+"");
        Intent navigationIntent = new Intent(Intent.ACTION_VIEW, navigation);
        navigationIntent.setPackage("com.google.android.apps.maps");
        startActivity(navigationIntent);
    }

//    @Override
//    public void onLocationChanged(Location location) {
//            Toast.makeText(getApplicationContext(),"location changed",
//                    Toast.LENGTH_LONG).show();
//            mlocation=location;
//            LatLng src_loc = new LatLng(mlocation.getLatitude(),mlocation.getLongitude()); // updated source location
//
//            activeMarker.setPosition(src_loc);
//            activeMarker.setTitle(" updated ");
//
//            String dest_loc1=dest_loc.latitude+","+dest_loc.longitude;
//            String src_loc1=src_loc.latitude+","+src_loc.longitude;
//
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(src_loc));
//            mMap.animateCamera(CameraUpdateFactory.zoomTo(16f));
//
//
//            List<LatLng> path1= get_pathpolyline_Array(src_loc1, dest_loc1);
//            //Draw the polyline
//            if (path1.size() > 0) {
//                Log.i("---------------->", String.valueOf(path1.size()));
//                PolylineOptions opts = new PolylineOptions().addAll(path1).color(Color.RED).width(6);
//                mMap.addPolyline(opts);
//            }
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(src_loc, 6));
//    }

}

