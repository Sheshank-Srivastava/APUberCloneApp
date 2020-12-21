package com.iamtanshu.apubercloneapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.iamtanshu.apubercloneapp.R;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class TransitionActivity extends AppCompatActivity implements  AdapterView.OnItemClickListener{
    Button btnGetRequest;
    LocationManager mLocationManager;
    LocationListener mLocationListener;
    private ListView listView;
    private ArrayList<String> nearByDriveRequest;
    private ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transition);

        listView = findViewById(R.id.requestListView);
        btnGetRequest = findViewById(R.id.btnGetRequest);
        btnGetRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                mLocationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        updateRequestListView(location);
                    }

                    @Override
                    public void onStatusChanged(String s, int i, Bundle bundle) {

                    }

                    @Override
                    public void onProviderEnabled(String s) {

                    }

                    @Override
                    public void onProviderDisabled(String s) {

                    }
                };
                if (Build.VERSION.SDK_INT < 23) {
                    if (ContextCompat.checkSelfPermission(TransitionActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
                     updateLocation();
                    }
                } else if (Build.VERSION.SDK_INT >= 23) {
                    if (ContextCompat.checkSelfPermission(TransitionActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(TransitionActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
                    } else {
//                        if (!mMap.isMyLocationEnabled()) {
//                            mMap.setMyLocationEnabled(true);
//                        }
                        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);

                     updateLocation();
                    }
                }
            }
        });
        nearByDriveRequest = new ArrayList<>();
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, nearByDriveRequest);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.driver_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_logout:
                ParseUser.getCurrentUser().logOutInBackground(new LogOutCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) return;
                        startActivity(new Intent(TransitionActivity.this, MainActivity.class));
                        finish();
                    }
                });
                break;
        }
        return true;
    }


    private void updateRequestListView(Location driverLocation) {
        if (driverLocation == null) return;
        ParseGeoPoint driverCurrentLocation = new ParseGeoPoint(driverLocation.getLatitude(), driverLocation.getLongitude());
        ParseQuery<ParseObject> requestCarQuery = ParseQuery.getQuery("RequestCar");
//        requestCarQuery.whereNear("passangerLocation",driverCurrentLocation);
        requestCarQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e != null) {
                    Toast.makeText(TransitionActivity.this, "Parse Error", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (objects.size() <= 0) {
                    Toast.makeText(TransitionActivity.this, "No on want car", Toast.LENGTH_SHORT).show();
                    return;
                }
                nearByDriveRequest.clear();
                for (ParseObject nearRequest : objects) {
                    ParseGeoPoint point = (ParseGeoPoint) nearRequest.get("passangerLoaction");
                    Double milesDistanceToPassanger = driverCurrentLocation.distanceInMilesTo(point);
                    float roundedDistanceValue = Math.round(milesDistanceToPassanger * 10) / 10;
                    nearByDriveRequest.add("There are " + roundedDistanceValue + " miles  to " + nearRequest.get("username"));
                }

                adapter.notifyDataSetChanged();
            }
        });
    }

    private Location getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location:
                bestLocation = l;
            }
        }

        return bestLocation;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(TransitionActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
             updateLocation();
            }
        }
    }
    private void updateLocation(){
        Location currentDriverLocation = getLastKnownLocation();
        updateRequestListView(currentDriverLocation);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(this, "Clicked", Toast.LENGTH_LONG).show();
    }
}
