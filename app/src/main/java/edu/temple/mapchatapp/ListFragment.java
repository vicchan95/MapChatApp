package edu.temple.mapchatapp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class ListFragment extends Fragment {
    RecyclerView recyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager layoutManager;

    private static final String LIST_KEY = "partnerList";
    private ArrayList<Partners> partnerList;

    private UserLocationInterface parentActivity;

    public ListFragment() {}

    public static ListFragment newInstance(ArrayList<Partners> partners) {
        ListFragment fragment = new ListFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(LIST_KEY, partners);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            partnerList = getArguments().getParcelableArrayList(LIST_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager((Context) parentActivity);
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new ListAdapter((Context) parentActivity, partnerList);
        recyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof UserLocationInterface) {
            parentActivity = (UserLocationInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        parentActivity = null;
    }

    public ArrayList<Partners> getPartners(){
        return partnerList;
    }

    public interface UserLocationInterface {
        void getUserLocation(ArrayList<Partners> list);
    }
}
