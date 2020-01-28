package com.autochip.trackpro;


import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.ArrayList;
import java.util.HashMap;

import app_utility.OnAdapterInteractionListener;


public class AdminTrackingRVAdapter extends RecyclerView.Adapter<AdminTrackingRVAdapter.AdminTrackingTabHolder> {

    private Context context;
    RecyclerView recyclerView;
    private static final int UNSELECTED = -1;

    private int selectedItem = UNSELECTED;
    private static DatabaseReference ref;

    private OnAdapterInteractionListener mAdapterListener;
    private ArrayList<String> alID;
    private ArrayList<String> alName;
    private ArrayList<Integer> alAdminPermissionFlag;
    ExpandableLayout expandableLayout;

    AdminTrackingRVAdapter(Context context, RecyclerView recyclerView, ArrayList<String> alID, ArrayList<String> alName,
                           ArrayList<Integer> alAdminPermissionFlag, OnAdapterInteractionListener mAdapterListener) {
        this.context = context;
        this.recyclerView = recyclerView;
        this.alID = alID;
        this.alName = alName;
        this.alAdminPermissionFlag = alAdminPermissionFlag;
        this.mAdapterListener = mAdapterListener;

    }


    @NonNull
    @Override
    public AdminTrackingTabHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_trackers_list, parent, false);
        ref = FirebaseDatabase.getInstance().getReference();
        return new AdminTrackingTabHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final AdminTrackingTabHolder holder, final int position) {
        //DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        /*ValueEventListener messageListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    for (DataSnapshot dataSnapshotChild : dataSnapshot.child("users").getChildren()) {

                    }
                    //user = dataSnapshot.getValue(User.class);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Db error", "value event listener");
                // Failed to read value
            }
        };
        ref.addValueEventListener(messageListener);*/
        /*ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Toast.makeText(context, "added", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Toast.makeText(context, "changed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Toast.makeText(context, "removed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Toast.makeText(context, "moved", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        ref.child("users").child(alID.get(position)).child("notify").addChildEventListener(childEventListener);*/

        holder.tvRvName.setText(alName.get(position));
        holder.tvRvDesignation.setText(alID.get(position));
        //if(position < alAdminPermissionFlag.size()) {
        switch (alAdminPermissionFlag.get(position)) {
            case 0:
                holder.ivOnline.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.circle_offline));
                break;
            case 1:
                holder.ivOnline.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.circle_online));
                break;
            case 2:
                holder.ivOnline.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.circle_permission_pending));
                break;
        }
        //}
        /*if (alAdminPermissionFlag.get(position) == 0) {
            holder.ivOnline.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.circle_offline));
        } else if (alAdminPermissionFlag.get(position) == 1) {
            holder.ivOnline.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.circle_online));
        } else if (alAdminPermissionFlag.get(position) == 2){
            holder.ivOnline.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.circle_permission_pending));
        }*/
        holder.bind(holder);

        //STOP loading dialog
        mAdapterListener.onAdapterChange("STOP_PROGRESS_BAR", position);
    }

    @Override
    public int getItemCount() {
        return alID.size(); //alBeaconInfo.size()
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class AdminTrackingTabHolder extends RecyclerView.ViewHolder implements View.OnClickListener, ExpandableLayout.OnExpansionUpdateListener {
        TextView tvRVEmail;
        TextView tvRVLastSeenTime;
        TextView tvRVDate;
        TextView tvRVTime;
        TextView tvRVNumber;
        ImageView mImageView;
        TextView tvRvName;
        TextView tvRvDesignation;
        CardView cvExpand;
        private LinearLayout llParentExpand;
        private ExpandableLayout expandableLayout;
        boolean isSelected;
        Button btnTrack, btnRemove;
        //TextView tvTrack;
        int position;
        ImageView ivOnline;
        //ExpandableLayout expandableLayout;
        //TextView tvExpand;

        public AdminTrackingTabHolder(View itemView) {
            super(itemView);
            //tvRVEmail = (TextView) itemView.findViewById(R.id.recent_rc_email);
            tvRVLastSeenTime = itemView.findViewById(R.id.recent_rc_last_seen);
            //tvRVNumber = (TextView) itemView.findViewById(R.id.recent_rc_number);
            tvRvName = itemView.findViewById(R.id.recent_rc_name);
            tvRvDesignation = itemView.findViewById(R.id.recent_rc_designation);
            tvRVDate = itemView.findViewById(R.id.recent_rc_date);
            tvRVTime = itemView.findViewById(R.id.recent_rc_time);
            cvExpand = itemView.findViewById(R.id.cv_expand);
            llParentExpand = itemView.findViewById(R.id.ll_parent);
            ivOnline = itemView.findViewById(R.id.iv_online);
            //mImageView = (ImageView) itemView.findViewById(R.id.recent_rc_avatar);
            expandableLayout = itemView.findViewById(R.id.rv_admin_expandable_layout);
            btnTrack = expandableLayout.findViewById(R.id.btn_track);
            btnRemove = expandableLayout.findViewById(R.id.btn_remove);
            //tvTrack = expandableLayout.findViewById(R.id.tv_track);

            //AdminTrackingRVAdapter.expandableLayout = expandableLayout;
            expandableLayout.setInterpolator(new FastOutLinearInInterpolator());
            expandableLayout.setOnExpansionUpdateListener(this);

            //tvExpand = itemView.findViewById(R.id.tv_expand);

            //tvExpand.setOnClickListener(this);
            llParentExpand.setOnClickListener(this);


        }

        public void bind(final AdminTrackingTabHolder holder) {
            position = getAdapterPosition();
            isSelected = position == selectedItem;
            btnTrack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //send tracking request
                    //final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");
                    notifyAndListen(position, holder);

                }
            });

            /*tvTrack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    isSelected = false;
                    //Toast.makeText(context, String.valueOf(position) + " Track clicked", Toast.LENGTH_SHORT).show();
                }
            });*/
            btnRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    /*alID.remove(position);
                    alName.remove(position);*/
                    mAdapterListener.onAdapterChange("REMOVE_POSITION", position);
                    //delete item from list
                }
            });

            llParentExpand.setSelected(isSelected);
            expandableLayout.setExpanded(isSelected, false);
        }

        @Override
        public void onClick(View view) {
            AdminTrackingTabHolder holder = (AdminTrackingTabHolder) recyclerView.findViewHolderForAdapterPosition(getAdapterPosition());
            if (holder != null) {
                holder.llParentExpand.setSelected(false);
                holder.expandableLayout.collapse();
            }

            int position = getAdapterPosition();
            if (position == selectedItem) {
                selectedItem = UNSELECTED;
            } else {
                llParentExpand.setSelected(true);
                expandableLayout.expand();
                selectedItem = position;
            }
        }

        @Override
        public void onExpansionUpdate(float expansionFraction, int state) {
            if (state == ExpandableLayout.State.EXPANDING) {
                recyclerView.smoothScrollToPosition(getAdapterPosition());
            }
        }
    }


    private void notifyAndListen(final int position, final AdminTrackingTabHolder holder) {
        final String sKey = "notify";
        ref.child("users").addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean isPresent = dataSnapshot.hasChild(alID.get(position));
                        if (isPresent) {
                            //User updatedUser=new User();
                            //updatedUser.notify = true;
                            //ref.setValue(updatedUser);
                            //ref.child("users").child(alID.get(position)).child("notify").setValue(true);
                            HashMap<String, Object> result = new HashMap<>();
                            result.put(sKey, true);
                            ref.child("users").child(alID.get(position)).updateChildren(result);
                            Toast.makeText(context, "Notified", Toast.LENGTH_SHORT).show();
                            //Get map of users in dataSnapshot
                                       /* boolean value = (Boolean) dataSnapshot.child(alID.get(position)).child("adminPermission").getValue();
                                        if(value){
                                            adminPermissionFlag = 1;
                                        } else {
                                            adminPermissionFlag = 2;
                                        }*/

                        } else {
                            Toast.makeText(context, "This user is not registered", Toast.LENGTH_SHORT).show();
                        }
                        //collectPhoneNumbers((Map<String,Object>) dataSnapshot.getValue());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        //handle databaseError
                    }
                });

        ref.child("users").child(alID.get(position)).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //Toast.makeText(context, "added", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                try {
                    if (dataSnapshot.getKey() != null && !dataSnapshot.getKey().contentEquals(sKey)) {
                        boolean value = (Boolean) dataSnapshot.getValue();
                        if (value) {
                            holder.ivOnline.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.circle_online));
                        } else {
                            holder.ivOnline.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.circle_permission_pending));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //Toast.makeText(context, "changed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Toast.makeText(context, "moved", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    /*interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }*/

    /*
    below RecyclerTouchListener class is created to listen to longPress and singleTap
     */

    /*static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private ClickListener clicklistener;
        private GestureDetector gestureDetector;


        RecyclerTouchListener(final Context context, final RecyclerView recycleView, final ClickListener clicklistener) {

            this.clicklistener = clicklistener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    *//*
                    below code works same as whatsApp list selection
                    below code works only when at least one onLongPress is triggered and the position is added to the hashset from onLongPress listener
                     *//*
                    //adds the view of the recyclerView
                    View child = recycleView.findChildViewUnder(e.getX(), e.getY());
                    //gets the index of recyclerView
                    int index = recycleView.getChildAdapterPosition(child);

                    child.setSelected(true);
                    //expandableLayout.findViewById(R.id.tv).setText(index + ". Tap to expand");
                    handleExpandableLayout();
                    *//*
                    below if statement checks for 2 condition.
                    below code executes when there is atleast one value in hashset and the index shouldn't be already added to the hashset
                    it sets the background to grey(which means selected) and the same will be added to hashset
                    else statement executes when the first if statement doesn't meet the conditions
                    which sets the background to white (which means unselected)
                     *//*

                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recycleView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clicklistener != null) {
                        clicklistener.onLongClick(child, recycleView.getChildAdapterPosition(child));
                        int index = recycleView.getChildAdapterPosition(child);
                        *//*
                        below if statement checks for one condition.
                        if hashset doesn't contain the position already the background is set to grey(which means selected) as well as added to the hashset
                        else
                        it removes the position from the hashset and sets the background to white(which means unselected)
                         *//*
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clicklistener != null && gestureDetector.onTouchEvent(e)) {
                clicklistener.onClick(child, rv.getChildAdapterPosition(child));
            }

            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }

        private void handleExpandableLayout(){
            if(expandableLayout.isExpanded()){
                expandableLayout.setVisibility(View.GONE);
                expandableLayout.collapse();
            } else {
                expandableLayout.expand();
                expandableLayout.setVisibility(View.VISIBLE);
            }
        }
    }*/

}
