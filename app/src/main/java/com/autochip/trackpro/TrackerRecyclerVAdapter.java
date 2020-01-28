package com.autochip.trackpro;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class TrackerRecyclerVAdapter extends RecyclerView.Adapter<TrackerRecyclerVAdapter.TrackerTabHolder> {

    private Context context;
    ArrayList<String> alID;
    ArrayList<String> alName;

    TrackerRecyclerVAdapter(Context context, ArrayList<String> alID, ArrayList<String> alName) {
        this.context = context;
        this.alID = alID;
        this.alName = alName;
    }


    @NonNull
    @Override
    public TrackerTabHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_trackers, parent, false);

        return new TrackerTabHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackerTabHolder holder, final int position) {
    }

    @Override
    public int getItemCount() {
        return alID.size(); //alBeaconInfo.size()
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class TrackerTabHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvNumber;

        TrackerTabHolder(View itemView) {
            super(itemView);
            //tvRVEmail = (TextView) itemView.findViewById(R.id.recent_rc_email);
            tvName = itemView.findViewById(R.id.tv_rc_name);
            tvNumber = itemView.findViewById(R.id.tv_rc_number);

            //mImageView = (ImageView) itemView.findViewById(R.id.recent_rc_avatar);

        }
    }

}
