package edu.temple.mapchatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements ListFragment.UserLocationInterface {
    private final String getURL = "https://kamorris.com/lab/get_locations.php";
    private final String postURL = "https://kamorris.com/lab/register_location.php";

    private String username = "victor";

    LocationManager locationManager;
    LocationListener locationListener;
    Location mlocation;
    RequestQueue queue;
    ArrayList<Partners> partnersArrayList = new ArrayList();

    private FusedLocationProviderClient fusedLocationProviderClient;
    int minTime = 0, minDistance = 10;

    FragmentManager fm;
    Fragment listFrag, mapFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fm = getSupportFragmentManager();

        queue = Singleton.getInstance(this.getApplicationContext()).getRequestQueue();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mlocation = location;
                postLocation();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        // Check to see if permission is granted and grab last known location for startup
        if (checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            // Permission already granted
            getLastKnownLocation();
        }

        listFrag = fm.findFragmentById(R.id.listContainer);
        // check if fragment is null (first startup)
        if(listFrag == null){
            requestPartners();
        }
        // Not startup, need to update listFrag
        else{
            fm.beginTransaction()
                    .remove(listFrag)
                    .add(R.id.listContainer, ListFragment.newInstance(partnersArrayList))
                    .commitAllowingStateLoss();
        }

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    getLastKnownLocation();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.d("Permissions Result", "Permission denied");
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime,
                        minDistance, locationListener);
                return;
            }

        }
    }

    private void postLocation() {
        StringRequest postRequest = new StringRequest(Request.Method.POST, postURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("POST Response", response);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("POST Response", error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("user", username);
                params.put("latitude", Double.toString(mlocation.getLatitude()));
                params.put("longitude", Double.toString(mlocation.getLongitude()));

                return params;
            }
        };
        queue.add(postRequest);
    }

    private void requestPartners() {
        JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, getURL,
                null, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                Log.d("Get Response", response.toString());
                Log.d("Location", mlocation.toString());
                partnersArrayList.clear();
                Partners partner;
                for(int i = 0; i < response.length(); i++){
                    try {
                        JSONObject partnerObject = response.getJSONObject(i);
                        float[] distance = new float[3];
                        if(username == partnerObject.getString("username")){
                            distance[0] = 0;
                        }
                        else {
                            Location.distanceBetween(mlocation.getLatitude(), mlocation.getLongitude(),
                                    Double.valueOf(partnerObject.getString("latitude")),
                                    Double.valueOf(partnerObject.getString("longitude")), distance);
                        }
                        partner = new Partners(partnerObject.getString("username"),
                                Double.valueOf(partnerObject.getString("latitude")),
                                Double.valueOf(partnerObject.getString("longitude")),
                                (double) distance[0]);
                        Log.d("Partner object", "Name: "+partner.getUsername()+
                                " distance: " + Double.toString(partner.getDistToUser()));
                        partnersArrayList.add(partner);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Collections.sort(partnersArrayList);
                Log.d("Sorted Array List", partnersArrayList.toString());
                listFrag = ListFragment.newInstance(partnersArrayList);
                fm.beginTransaction()
                        .add(R.id.listContainer, listFrag)
                        .commitAllowingStateLoss();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Get Response", error.toString());
            }
        });

        queue.add(getRequest);
    }

    // TODO: Finish writing getLastKnownLocation
    private void getLastKnownLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            Log.d("getLastLocation Response", "Location is not null");
                            mlocation = location;
                            postLocation();
                            // TODO: Add code to automatically report location update every 30 seconds
                        } else {
                            Log.d("getLastLocation Response", "Location is null");
                            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                        minTime, minDistance, locationListener);
                            }
                            else{
                                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        }
                    }
                });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void getUserLocation(ArrayList<Partners> list) {
        // TODO: finish writing method.
    }
}
