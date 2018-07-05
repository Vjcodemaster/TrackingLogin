package com.autochip.trackpro;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

import app_utility.CircularProgressBar;
import app_utility.OnAdapterInteractionListener;
import app_utility.OnFragmentInteractionListener;
import app_utility.SharedPreferenceClass;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link app_utility.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ManageUserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ManageUserFragment extends Fragment implements OnAdapterInteractionListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public DatabaseReference ref;

    private RecyclerView recyclerView;
    public AdminTrackingRVAdapter recyclerVAdapter;
    private ArrayList<String> alID = new ArrayList<>();
    private ArrayList<String> alName = new ArrayList<>();
    public ArrayList<Integer> alAdminPermissionFlag = new ArrayList<>(); //0 = offline/Red, 1 = online/green, 2 = permission pending/orange

    public OnAdapterInteractionListener mAdapterListener;
    private OnFragmentInteractionListener mListener;
    private ExpandableLayout expandableLayout;
    private TextView tvExpand, tvWhoToTrack;
    private FloatingActionButton fab;
    private EditText etID, etName;
    private Button btnAdd;
    int adminPermissionFlag;
    int nScrollIndex = 0;

    CircularProgressBar circularProgressBar;

    private Thread threadFireBaseDBFetch;

    SharedPreferenceClass sharedPreferenceClass;

    public ManageUserFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ManageUserFragment.
     */
    public static ManageUserFragment newInstance(String param1, String param2) {
        ManageUserFragment fragment = new ManageUserFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        sharedPreferenceClass = new SharedPreferenceClass(getActivity());
        ref = FirebaseDatabase.getInstance().getReference();
        //Intent bi = new Intent(getActivity(), TrackingService.class);
        //bi.setPackage(StaticReferenceClass.ServiceIntent);
        //getActivity().startService(bi);
        //mAdapterListener =this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_manage_user, container, false);

        mAdapterListener =this;
        init(view);
        /*String build = android.os.Build.PRODUCT;
        String deviceName = android.os.Build.MODEL;
        String deviceMan = android.os.Build.MANUFACTURER;
        int version = Build.VERSION.SDK_INT;
        String versionRelease = Build.VERSION.RELEASE;*/

        tvExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleExpandableLayout();
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sID = etID.getText().toString().trim();
                String sName = etName.getText().toString().trim();
                if(isUserIDValid(sID) && isNameValid(sName)){
                    showProgressBar();
                    //new MyTask(ManageUserFragment.this, sID, sName).execute();
                    runThread(sID, sName);
                    /*alID.add(sID);
                    alName.add(sName);
                    recyclerVAdapter.notifyDataSetChanged();
                    etID.setText("");
                    etName.setText("");
                    etName.requestFocus();*/
                }
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //showProgressBar();
                //SharedPreferenceClass sharedPreferenceClass = new SharedPreferenceClass(getActivity());
                /*ArrayList<String> alFlag = new ArrayList<>(alAdminPermissionFlag.size());
                for (Integer myInt : alAdminPermissionFlag) {
                    alFlag.add(String.valueOf(myInt));
                }

                sharedPreferenceClass.setUserList(alID, alName, alFlag);*/

                Set<String> setID = sharedPreferenceClass.getUserList();
                if(setID!=null && setID.size()>0) {
                    Intent mapIntent = new Intent(getActivity(), MapsActivity.class);
                    getActivity().startActivity(mapIntent);
                } else {
                    Toast.makeText(getActivity(), "Atleast one user is required for tracking", Toast.LENGTH_SHORT).show();
                }
                //stopProgressBar();

            }
        });

        return view;
    }

    private void init(View view){
        circularProgressBar = new CircularProgressBar(getActivity());

        tvExpand = view.findViewById(R.id.tv_expand);
        expandableLayout = view.findViewById(R.id.expandable_layout);
        tvWhoToTrack = view.findViewById(R.id.tv_who_to_track);
        fab = view.findViewById(R.id.fab);

        etID = expandableLayout.findViewById(R.id.et_ID);
        etName = expandableLayout.findViewById(R.id.et_name);
        btnAdd = expandableLayout.findViewById(R.id.btn_add);

        recyclerView = view.findViewById(R.id.rv_tracking_list);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(mLinearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recyclerView.setHasFixedSize(true);

        try {
            Set<String> setID = sharedPreferenceClass.getUserList();
            Set<String> setNames = sharedPreferenceClass.getNamesList();
            ArrayList<String> setAdminPermissionList = sharedPreferenceClass.getAdminPermissionList();


            if(setID!=null && setID.size()>0) {
                showProgressBar();
                ArrayList<String> tmp = new ArrayList<>(setAdminPermissionList);
                ArrayList<Integer> alFlag = new ArrayList<>(tmp.size());
                for (String myInt : tmp) {
                    alFlag.add(Integer.valueOf(myInt));
                }

                alID.addAll(setID);
                alName.addAll(setNames);
                alAdminPermissionFlag.addAll(alFlag);
                //alAdminPermissionFlag.addAll(setAdminPermissionList);
            }
        }catch (Exception e){
            e.printStackTrace();
        }


        /*else {
            stopProgressBar();
        }*/

        recyclerVAdapter = new AdminTrackingRVAdapter(getActivity(), recyclerView, alID, alName, alAdminPermissionFlag, mAdapterListener);
        recyclerView.setAdapter(recyclerVAdapter);

        /*recyclerView.addOnItemTouchListener(new AdminTrackingRVAdapter.RecyclerTouchListener(getActivity(),
                recyclerView, new AdminTrackingRVAdapter.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                //Values are passing to activity & to fragment as well
                nScrollIndex = position;
                //Toast.makeText(getActivity(), "Single Click on position        :" + position, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClick(View view, int position) {
                nScrollIndex = position;
                //Toast.makeText(getActivity(), "Long press on position :" + position, Toast.LENGTH_SHORT).show();
            }
        }));*/
        //stopProgressBar();
    }

    private void handleExpandableLayout(){
        if(expandableLayout.isExpanded()){
            expandableLayout.setVisibility(View.GONE);
            expandableLayout.collapse();
            tvExpand.setCompoundDrawablesWithIntrinsicBounds(R.drawable.circle_plus, 0, 0, 0);
        } else {
            expandableLayout.expand();
            expandableLayout.setVisibility(View.VISIBLE);
            tvExpand.setCompoundDrawablesWithIntrinsicBounds(R.drawable.circle_minus, 0, 0, 0);
        }
    }

    private boolean isUserIDValid(String ID) {
        return ID.length() == 10;
    }

    private boolean isNameValid(String name) {
        return name.length() > 2 && !TextUtils.isEmpty(name);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onAdapterChange(String TAG, int position) {
        switch (TAG){
            case "REMOVE_POSITION":
                alID.remove(position);
                alName.remove(position);
                alAdminPermissionFlag.remove(position);
                recyclerVAdapter.notifyItemRemoved(position);

                ArrayList<String> alFlag = new ArrayList<>(alAdminPermissionFlag.size());
                for (Integer myInt : alAdminPermissionFlag) {
                    alFlag.add(String.valueOf(myInt));
                }

                sharedPreferenceClass.setUserList(alID, alName, alFlag);
                break;
            case "STOP_PROGRESS_BAR":
                stopProgressBar();
                break;
        }
    }

    /*static class MyTask extends AsyncTask<Void, Void, String> {

        private Context context;
        private WeakReference<ManageUserFragment> activityReference;
        private String phone;
        private String name;
        private int flag;

        // only retain a weak reference to the activity
        MyTask(ManageUserFragment context, String phone, String name) {
            activityReference = new WeakReference<>(context);
            this.phone = phone;
            this.name = name;
        }

        @Override
        protected String doInBackground(Void... params) {
            // do some long running task...
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");
            ref.addListenerForSingleValueEvent(
                    new ValueEventListener() {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            boolean isPresent = dataSnapshot.hasChild(phone);
                            isPresent = dataSnapshot.hasChild(phone);
                            if(isPresent) {
                                //Get map of users in dataSnapshot
                                boolean value = (Boolean) dataSnapshot.child(phone).child("adminPermission").getValue();
                                if(value){
                                    flag = 1;
                                } else {
                                    flag = 2;
                                }
                                //WHO_IS_USER = Integer.valueOf(value);
                                //sharedPreferenceClass.setUserType(WHO_IS_USER);
                                //sharedPreferenceClass.setUserLogStatus(true, name, phone);

                            } else {
                                flag = 0;
                            }
                            //collectPhoneNumbers((Map<String,Object>) dataSnapshot.getValue());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            //handle databaseError
                        }
                    });
            return String.valueOf(flag);
        }

        @Override
        protected void onPostExecute(String result) {
            // get a reference to the activity if it is still there
            ManageUserFragment activity = activityReference.get();
            if (activity == null || activity.isDetached()) return;

            // modify the activity's UI
            activity.alAdminPermissionFlag.add(Integer.valueOf(result));
            activity.alID.add(phone);
            activity.alName.add(name);
            activity.recyclerVAdapter.notifyDataSetChanged();
            activity.etID.setText("");
            activity.etName.setText("");
            activity.etName.requestFocus();
            Toast.makeText(activity.getActivity(), result, Toast.LENGTH_SHORT).show();
            // access Activity member variables
            //activity.mSomeMemberVariable = 321;
        }
    }*/

    private void runThread(final String phone, final String name) {


        new Thread() {
            public void run() {
                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        //DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");
                        ref.child("users").addListenerForSingleValueEvent(
                                new ValueEventListener() {

                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        boolean isPresent = dataSnapshot.hasChild(phone);
                                        if(isPresent) {
                                            //Get map of users in dataSnapshot
                                            boolean value = (Boolean) dataSnapshot.child(phone).child("adminPermission").getValue();
                                            if(value){
                                                adminPermissionFlag = 1;
                                            } else {
                                                adminPermissionFlag = 2;
                                            }
                                            notifyRecycler(phone, name);
                                            //WHO_IS_USER = Integer.valueOf(value);
                                            //sharedPreferenceClass.setUserType(WHO_IS_USER);
                                            //sharedPreferenceClass.setUserLogStatus(true, name, phone);

                                        } else {
                                            /*ref.child("users").addListenerForSingleValueEvent(
                                                    new ValueEventListener() {

                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            boolean isPresent = dataSnapshot.hasChild(phone);
                                                            if(isPresent) {
                                                                //Get map of users in dataSnapshot
                                                                boolean value = (Boolean) dataSnapshot.child(phone).child("adminPermission").getValue();
                                                                if(value){
                                                                    adminPermissionFlag = 1;
                                                                } else {
                                                                    adminPermissionFlag = 2;
                                                                }
                                                                //WHO_IS_USER = Integer.valueOf(value);
                                                                //sharedPreferenceClass.setUserType(WHO_IS_USER);
                                                                //sharedPreferenceClass.setUserLogStatus(true, name, phone);

                                                            } else {
                                                                adminPermissionFlag = 0;
                                                            }
                                                            //collectPhoneNumbers((Map<String,Object>) dataSnapshot.getValue());
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                                            //handle databaseError
                                                        }
                                                    });*/
                                            adminPermissionFlag = 0;
                                            Toast.makeText(getActivity(), "User doesn't exist", Toast.LENGTH_LONG).show();
                                            //notifyRecycler(phone, name);
                                        }
                                        stopProgressBar();
                                        //collectPhoneNumbers((Map<String,Object>) dataSnapshot.getValue());
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        stopProgressBar();
                                        //handle databaseError
                                    }
                                });

                    }
                });
                //Thread.sleep(300);
            }
        }.start();
    }

    private void notifyRecycler(String phone, String name){
        alAdminPermissionFlag.add(adminPermissionFlag);
        alID.add(phone);
        alName.add(name);
        recyclerVAdapter.notifyDataSetChanged();
        etID.setText("");
        etName.setText("");
        etName.requestFocus();

        ArrayList<String> alFlag = new ArrayList<>(alAdminPermissionFlag.size());
        for (Integer myInt : alAdminPermissionFlag) {
            alFlag.add(String.valueOf(myInt));
        }

        sharedPreferenceClass.setUserList(alID, alName, alFlag);
    }

    private void showProgressBar() {
        circularProgressBar.setCanceledOnTouchOutside(false);
        circularProgressBar.setCancelable(false);
        circularProgressBar.show();
    }

    private void stopProgressBar() {
        if (circularProgressBar != null && circularProgressBar.isShowing())
            circularProgressBar.dismiss();
    }
}
