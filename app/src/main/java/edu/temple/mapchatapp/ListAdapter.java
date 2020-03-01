package edu.temple.mapchatapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.MyViewHolder> {
    Context context;
    private ArrayList<Partners> partnersList;

    public ListAdapter(Context c, ArrayList<Partners> list){
        context = c;
        partnersList = list;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView partnername;
        public MyViewHolder(TextView v) {
            super(v);
            partnername = v;
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView v = (TextView) LayoutInflater.from(parent.getContext()).inflate(
                R.layout.fragment_list, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position){
        holder.partnername.setText(partnersList.get(position).toString());
    }

    @Override
    public int getItemCount(){
        return partnersList.size();
    }
}
