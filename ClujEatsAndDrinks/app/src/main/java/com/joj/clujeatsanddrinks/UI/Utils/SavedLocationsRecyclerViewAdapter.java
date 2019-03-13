package com.joj.clujeatsanddrinks.UI.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.joj.clujeatsanddrinks.Model.Location;
import com.joj.clujeatsanddrinks.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SavedLocationsRecyclerViewAdapter extends RecyclerView.Adapter<LocationsRecyclerViewAdapter.MyViewHolder> {
    private List<Location> locations;
    // context for glide
    private Context mContext;
    private LocationsRecyclerViewAdapter.OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemCLick(int position);
    }

    public void setOnItemClickListener(LocationsRecyclerViewAdapter.OnItemClickListener listener) {
        mListener = listener;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public TextView address;
        public ImageView image;

        public MyViewHolder(@NonNull View itemView, final LocationsRecyclerViewAdapter.OnItemClickListener listener) {
            super(itemView);
            name = itemView.findViewById(R.id.location_name);
            address = itemView.findViewById(R.id.location_address);
            image = itemView.findViewById(R.id.location_imageview);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null) {
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION) {
                            listener.onItemCLick(position);
                        }
                    }
                }
            });
        }
    }

    public SavedLocationsRecyclerViewAdapter(Context context, List<Location> locations) {
        this.mContext = context;
        this.locations = locations;
    }

    @Override
    public LocationsRecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.location_cardview, parent, false);
        return new LocationsRecyclerViewAdapter.MyViewHolder(itemView, mListener);
    }

    @Override
    public void onBindViewHolder(LocationsRecyclerViewAdapter.MyViewHolder viewHolder, int position) {
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.ic_launcher_background);

        Glide.with(mContext)
                .load(locations.get(viewHolder.getAdapterPosition()).getPhoto())
                .apply(requestOptions)
                .into(viewHolder.image);

        viewHolder.name.setText(locations.get(position).getName());
        viewHolder.address.setText(locations.get(position).getAddress());
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }
}
