package com.autochip.trackpro;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import app_utility.PermissionHandler;
import app_utility.SharedPreferenceClass;
import app_utility.TrackingService;

import static app_utility.PermissionHandler.LOCATION_PERMISSION;

public class SettingsActivity extends AppCompatActivity {
    public static int REQUEST_CODE_MIUI = 3001;
    int WHO_IS_USER;
    TextView tvName;
    Switch switchTracking;
    SharedPreferenceClass sharedPreferenceClass;
    int nPermissionFlag = 0;
    private static DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_settings);
        //Intent intent = getIntent();
        //WHO_IS_USER = Objects.requireNonNull(intent.getExtras()).getInt("user");
        sharedPreferenceClass = new SharedPreferenceClass(SettingsActivity.this);
        WHO_IS_USER = sharedPreferenceClass.getUserType();
        ref = FirebaseDatabase.getInstance().getReference();

        if (WHO_IS_USER == 0) {
            Fragment newFragment;
            FragmentTransaction transaction;
            newFragment = ManageUserFragment.newInstance("", "");
            transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fl_container, newFragment, null);
            transaction.commit();
        }

        //below code will check if the OS is MIUI to get AutoStart permission from it.
        //string miui will return empty string if OS is not MIUI.
        checkIfMIUI();

        tvName = findViewById(R.id.tv_display_name);
        switchTracking = findViewById(R.id.switch_tracking);

        String sName = sharedPreferenceClass.getUserName();
        tvName.setText(sName);

        if (!isMyServiceRunning(app_utility.TrackingService.class)) {
            switchTracking.setChecked(false);
        } else {
            switchTracking.setChecked(true);
        }

        switchTracking.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if (!isMyServiceRunning(app_utility.TrackingService.class)) {
                        Intent in = new Intent(SettingsActivity.this, app_utility.TrackingService.class);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(in);
                        } else {
                            startService(in);
                        }
                    }
                    /*Intent bi = new Intent(SettingsActivity.this, TrackingService.class);
                    bi.setPackage(StaticReferenceClass.ServiceIntent);
                    startService(bi);*/
                } else {
                    turnOffServiceAndNotification();
                    //stopService(new Intent(TrackingService.ServiceIntent));
                }
            }
        });
    }

    private void turnOffServiceAndNotification() {
        final String sNotifyKey = "notify";
        final String sAdminPermission = "adminPermission";
        ref.child("users").addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean isPresent = dataSnapshot.hasChild(sharedPreferenceClass.getUserID());
                        if (isPresent) {
                            HashMap<String, Object> result = new HashMap<>();
                            result.put(sNotifyKey, false);
                            result.put(sAdminPermission, false);
                            ref.child("users").child(sharedPreferenceClass.getUserID()).updateChildren(result);
                        } else {
                            Toast.makeText(SettingsActivity.this, "Turned Off", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(SettingsActivity.this, "Db error. Please try again later", Toast.LENGTH_LONG).show();
                    }
                });

        sharedPreferenceClass.setIfUserIsTraceable(false);
        if (TrackingService.refOfService != null) {
            TrackingService.refOfService.stopSelf();
        }
    }

    private void checkIfMIUI(){
        Class<?> c;
        String miui;
        if (!sharedPreferenceClass.getUserAutoStartPermission()) {
            try {
                c = Class.forName("android.os.SystemProperties");
                Method get = c.getMethod("get", String.class);
                miui = (String) get.invoke(c, "ro.miui.ui.version.code");
                if (!miui.equals("")) {
                    showLocationPermissionExplanation();
                }
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!PermissionHandler.hasPermissions(SettingsActivity.this, LOCATION_PERMISSION)) {
            ActivityCompat.requestPermissions(SettingsActivity.this, LOCATION_PERMISSION, 2);
        } else {
            PermissionHandler permissionHandler = new PermissionHandler(SettingsActivity.this, 1);
        }
    }

    public void onSettingsClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                Fragment newFragment;
                FragmentTransaction transaction;
                newFragment = TrackersFragment.newInstance("", "");
                transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fl_container, newFragment, null);
                transaction.commit();
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int PERMISSION_ALL, String permissions[], int[] grantResults) {
        StringBuilder sMSG = new StringBuilder();
        PermissionHandler permissionHandler;
        if (PERMISSION_ALL == 2) {
            for (String sPermission : permissions) {
                switch (sPermission) {
                    case android.Manifest.permission.ACCESS_COARSE_LOCATION:
                        if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(SettingsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                                //Show permission explanation dialog...
                                //showPermissionExplanation(SignInActivity.this.getResources().getString(R.string.phone_explanation));
                                //Toast.makeText(SignInActivity.this, "not given", Toast.LENGTH_SHORT).show();
                                sMSG.append("LOCATION, ");
                                nPermissionFlag = 0;
                            } else {
                                //Never ask again selected, or device policy prohibits the app from having that permission.
                                //So, disable that feature, or fall back to another situation...
                                //@SuppressWarnings("unused") AlertDialogs alertDialogs = new AlertDialogs(HomeScreen.this, 1, mListener);
                                //Toast.makeText(SignInActivity.this, "permission never ask", Toast.LENGTH_SHORT).show();
                                //showPermissionExplanation(HomeScreenActivity.this.getResources().getString(R.string.phone_explanation));
                                sMSG.append("LOCATION, ");
                                nPermissionFlag = 0;
                            }
                        } else {
                            permissionHandler = new PermissionHandler(SettingsActivity.this, 1);
                        }
                        break;

                }
            }
            if (!sMSG.toString().equals("") && !sMSG.toString().equals(" ")) {
                permissionHandler = new PermissionHandler(SettingsActivity.this, 0, sMSG.toString(), nPermissionFlag);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 3:
                if (resultCode == Activity.RESULT_CANCELED) {
                    SettingsActivity.this.finish();
                }
                break;
            case 3001:
                if (resultCode == Activity.RESULT_OK) {
                    sharedPreferenceClass.setUserAutoStartPermission(false);
                    SettingsActivity.this.finish();

                } else if (resultCode == Activity.RESULT_CANCELED) {

                    sharedPreferenceClass.setUserAutoStartPermission(true);
                }
                break;
        }
    }

    private void showLocationPermissionExplanation() {
        AlertDialog.Builder alertBluetooth = new AlertDialog.Builder(SettingsActivity.this);
        alertBluetooth.setMessage(getResources().getString(R.string.auto_start_explanation))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
                        startActivityForResult(intent, REQUEST_CODE_MIUI);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        onBackPressed();
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }
}
