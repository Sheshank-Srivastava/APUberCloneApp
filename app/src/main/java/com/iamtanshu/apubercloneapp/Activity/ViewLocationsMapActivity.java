package com.iamtanshu.apubercloneapp.Activity;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.iamtanshu.apubercloneapp.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class ViewLocationsMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private Button btnRide;
    Intent intent = getIntent();
    private double dLatitude;
    private double dLongitude;
    private double pLatitude;
    private double pLongitude;
    private String rUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_locations_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        intent = getIntent();
        dLatitude = intent.getDoubleExtra("dLatitude", 0);
        dLongitude = intent.getDoubleExtra("dLongitude", 0);
        pLatitude = intent.getDoubleExtra("pLatitude", 0);
        pLongitude = intent.getDoubleExtra("pLongitude", 0);
        rUserName = intent.getStringExtra("rUserName");


        btnRide = findViewById(R.id.btn_GiveRide);
        btnRide.setText("I want to give "+rUserName+" ride..");
        btnRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(ViewLocationsMapActivity.this, getIntent().getStringExtra("rUserName")+"", Toast.LENGTH_SHORT).show();

                ParseQuery<ParseObject> carRequestQuesry = ParseQuery.getQuery("RequestCar");
                carRequestQuesry.whereEqualTo("username", rUserName);
                carRequestQuesry.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (e != null || objects.size() <= 0) return;
                        for (ParseObject uberRequest : objects) {
                            uberRequest.put("driverOfMe", ParseUser.getCurrentUser().getUsername());
                            uberRequest.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e != null) return;
                                    Intent googleIntent =
                                            new Intent(Intent.ACTION_VIEW,
                                                    Uri.parse("https://maps.google.com/maps?saddr=" +
                                                            dLatitude + "," +
                                                            dLongitude + "&daddr=" +
                                                            pLatitude + "," +
                                                            pLongitude));
                                    startActivity(googleIntent);

                                }
                            });
                        }
                    }
                });
            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
//
//        Toast.makeText(this, intent.getDoubleExtra("dLatitude",0) + "", Toast.LENGTH_LONG).show();
        // Add a marker in Sydney and move the camera

        //Driver LatLng
        LatLng driverLocation = new LatLng(dLatitude, dLongitude);
//        //Passenger LatLng
        LatLng passengerLocation = new LatLng(pLatitude, pLongitude);
//        mMap.addMarker(new MarkerOptions().position(passengerLocation).title("Passanger Location"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(passengerLocation,14.0f));

        //        mMap.addMarker(new MarkerOptions().position(driverLocation).title("Driver Location"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(driverLocation,16.0f ));


        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        Marker driverMarker = mMap.addMarker(new MarkerOptions().position(driverLocation).title("Driver Location"));
        Marker passengerMarker = mMap.addMarker(new MarkerOptions().position(passengerLocation).title("Passenger Location"));

        ArrayList<Marker> myMarkers = new ArrayList<>();
        myMarkers.add(driverMarker);
        myMarkers.add(passengerMarker);
        for (Marker marker : myMarkers) {
            builder.include(marker.getPosition());
        }

        LatLngBounds bounds = builder.build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 1);
        mMap.animateCamera(cameraUpdate);
    }


}