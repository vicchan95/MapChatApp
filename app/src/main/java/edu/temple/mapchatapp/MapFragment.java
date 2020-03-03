package edu.temple.mapchatapp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap gMap;
    private MapView mapView;
    private ArrayList<Partners> partnersArrayList;

//    private MapFragmentListener mListener;

    private static final String LIST_KEY = "partnerArray";

    public MapFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static MapFragment newInstance(ArrayList<Partners> partners) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(LIST_KEY, partners);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = view.findViewById(R.id.mapView);
//        if(mapView != null){
        if(view.findViewById(R.id.mapView) != null){
            Log.d("mapView return", "MapView is not null");
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);
        }
        else{
            Log.d("mapView return", "mapView is null");
        }
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if(googleMap != null) {
            Log.d("onMapReady partnersArrayList", partnersArrayList.toString());
            gMap = googleMap;
            gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            for (int i = 0; i < partnersArrayList.size(); i++) {
                Partners partner = partnersArrayList.get(i);
                gMap.addMarker(new MarkerOptions()
                        .position(new LatLng(partner.getLatitude(), partner.getLongitude()))
                        .title(partner.getUsername()));
            }
        }
        else{
            Log.d("onMapReady parameter", "parameter passed in is null");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Log.d("getArguments return", "getArguments returns not null");
            Log.d("getArguments.get return", getArguments().toString());
            partnersArrayList = getArguments().getParcelableArrayList(LIST_KEY);
            Log.d("fragment partnersArrayList:", partnersArrayList.toString());
        }
        else{
            Log.d("getArguments return", "getArguments returns null");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof MapFragmentListener) {
//            mListener = (MapFragmentListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }

//    public interface MapFragmentListener{
//        void onMarkerClick(Marker marker);
//    }

}
