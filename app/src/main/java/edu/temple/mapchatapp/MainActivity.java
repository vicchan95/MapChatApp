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
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements ListFragment.UserLocationInterface {
    private final String getURL = "https://kamorris.com/lab/get_locations.php";
    private final String postURL = "https://kamorris.com/lab/register_location.php";

    private String username = "victor";

    LocationManager locationManager;
    LocationListener locationListener;
    Location mlocation;
    RequestQueue queue;
    public ArrayList<Partners> partnersArrayList;

    private FusedLocationProviderClient fusedLocationProviderClient;
    int minTime = 0, minDistance = 10;

    FragmentManager fm;
    Fragment listFrag, mapFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fm = getSupportFragmentManager();
        partnersArrayList = new ArrayList<>();
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

        // Changing name of user
        findViewById(R.id.saveButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newUsername = ((EditText)findViewById(R.id.nameBox)).getText().toString();
                if(!newUsername.isEmpty() && newUsername != null){
                    setUsername(newUsername);
//                    postLocation();
                    Log.d("username changed", username);
                }
                else{
                    Toast.makeText(MainActivity.this, "Username cannot be empty",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        listFrag = fm.findFragmentById(R.id.listContainer);
        // check if fragment is null (first startup)
        if(listFrag == null){
            requestPartners();
            Log.d("partnersList after method call", partnersArrayList.toString());
        }
        // Not startup, need to update listFrag
        else{
            // TODO: Finish writing getPartners() to replace requestPartners()
            //  to get partners from previous state by putting update listFrag in separate method.
            requestPartners();
            fm.beginTransaction()
                    .remove(listFrag)
                    .add(R.id.listContainer, ListFragment.newInstance(partnersArrayList))
                    .commitAllowingStateLoss();
        }

        mapFrag = fm.findFragmentById(R.id.mapContainer);
        // Startup
        if(mapFrag == null){
            Log.d("before new instance partnersArrayList", partnersArrayList.toString());
            mapFrag = MapFragment.newInstance(partnersArrayList);
            fm.beginTransaction()
                    .add(R.id.mapContainer, mapFrag)
                    .commitAllowingStateLoss();
        }
        // Not startup, replace mapfrag
        else{
            // TODO: add method for updating
            fm.beginTransaction()
                    .replace(R.id.mapContainer, MapFragment.newInstance(partnersArrayList))
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
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            minTime, minDistance, locationListener);
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.d("Permissions Result", "Permission denied");
                }
                return;
            }

        }
    }

    private void postLocation() {
        StringRequest postRequest = new StringRequest(Request.Method.POST, postURL,new Response.Listener<String>() {
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

    public class requestPartners extends TimerTask
    {
        @Override
        public void run() {
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
                    // TODO: remove first value which is user
                    Log.d("Sorted Array List", "----------------");
                    for(Partners p : partnersArrayList){
                        Log.d("Partner Object: ", p.getUsername());
                    }
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
//                            Log.d("Location after assignment", mlocation.toString());
                            // TODO: Add code to automatically report location update every 30 seconds
                            Timer timer = new Timer();
                            TimerTask getRequest = new requestPartners();
                            timer.schedule(getRequest, 0, 30000);

                        } else {
                            Log.d("getLastLocation Response", "Location is null");
                            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                /* Request location update RIGHT NOW because last location was null
                                meaning this was initial startup.
                                */
                                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                        minTime, 0, locationListener);
                            }
                            else{
                                Log.d("getLastKnownLocation Permission Error", "Permission not granted");
                            }
                        }
                    }
                });
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                minTime, minDistance, locationListener);
    }

    private void fragmentDisplay(public ArrayList()){

    }

    private void setUsername(String username){
        this.username = username;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
    }

    @Override
    public void getUserLocation(ArrayList<Partners> list) {
        // TODO: finish writing method.
    }
}
