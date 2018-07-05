package app_utility;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.autochip.trackpro.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;
import static android.support.v4.app.NotificationCompat.PRIORITY_MAX;

public class TrackingService extends Service implements LocationListener, GoogleApiClient.ConnectionCallbacks, AsyncInterface {

    public static final String ServiceIntent = "app_utility.TrackingService"; //this will be used to stop service

    String channelId = "app_utility.TrackingService";
    String channelName = "tracking";

    /*startService(new Intent(MyService.ServiceIntent));
    stopService(new Intent((MyService.ServiceIntent));*/

    public static TrackingService refOfService;
    public static DatabaseReference ref;
    SharedPreferenceClass sharedPreferenceClass;

    NotificationManager notifyMgr;
    NotificationCompat.Builder nBuilder;
    NotificationCompat.InboxStyle inboxStyle;

    AsyncInterface asyncInterface;

    private Location location;
    private LocationManager locationManager;
    Timer timer = new Timer();
    Handler handler = new Handler();
    Location bestLocation = null;

    Location previousLocation;

    Double radius = 60.0;

    boolean hasNoGpsBug = true;
    String VOLLEY_STATUS = "NOT_RUNNING";

    long startTime = 0;
    long endTime = 0;
    long totalTime = 0;


    public TrackingService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        /*
        this will make sure service will run on background in oreo and above
        service wont run without a notification from oreo version.
        After the system has created the service, the app has five seconds to call the service's startForeground() method
        to show the new service's user-visible notification. If the app does not call startForeground() within the time limit,
        the system stops the service and declares the app to be ANR.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground();
        }

        refOfService = this;
        asyncInterface = this;
        ref = FirebaseDatabase.getInstance().getReference();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        sharedPreferenceClass = new SharedPreferenceClass(getApplicationContext());
        fireBaseNotifyListener();
        getLocation();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        //Toast.makeText(getApplicationContext(), "I am still running", Toast.LENGTH_LONG).show();
                        Log.e("Service status: ", "RUNNING");
                        //getLocation();
                    }
                });
            }
        };
        //Starts after 20 sec and will repeat on every 20 sec of time interval.
        timer.schedule(doAsynchronousTask, 0, 10000);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startForeground() {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), createNotificationChannel() );
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(PRIORITY_MAX)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(101, notification);
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(){
        NotificationChannel chan = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notifyMgr.createNotificationChannel(chan);
        return channelId;
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);

        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent in) {
        Log.e("Service is killed", "");
        super.onTaskRemoved(in);
        if (sharedPreferenceClass.getUserTraceableInfo()) {
            Intent intent = new Intent("app_utility.TrackingService.ServiceStopped");
            sendBroadcast(intent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //TrackingService.this.stopSelf();
        timer.cancel();
        timer.purge();

        //refOfService.stopForeground(true);
        refOfService.stopSelf();
        if (sharedPreferenceClass.getUserTraceableInfo()) {
            Intent intent = new Intent("app_utility.TrackingService.ServiceStopped");
            sendBroadcast(intent);
        }

        Log.i(TAG, "Service destroyed ...");
    }

    private void fireBaseNotifyListener() {
        ref.child("users").child(sharedPreferenceClass.getUserID()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //Toast.makeText(context, "added", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                try {
                    boolean value = (Boolean) dataSnapshot.getValue();
                    if (value) {
                        notifyUser();
                        //holder.ivOnline.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.circle_online));
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
                Toast.makeText(getApplicationContext(), "moved", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void notifyUser() {
        inboxStyle = new NotificationCompat.InboxStyle();
        notifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Intent acceptIntent = new Intent(TrackingService.this, TrackingBroadCastReceiver.class);
        acceptIntent.setAction("android.intent.action.ac.user.accept");
        PendingIntent acceptPI = PendingIntent.getBroadcast(TrackingService.this, 0, acceptIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);


        Intent declineIntent = new Intent(TrackingService.this, TrackingBroadCastReceiver.class);
        declineIntent.setAction("android.intent.action.ac.user.decline");
        PendingIntent declinePI = PendingIntent.getBroadcast(TrackingService.this, 0, declineIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        nBuilder = new NotificationCompat.Builder(TrackingService.
                this, channelId)
                .setSmallIcon(R.drawable.download)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle(getString(R.string.app_name))
                //.setContentText("Admin has asked permission to track you. Would you like to accept?")
                .setSubText("Admin has asked permission to track you. Would you like to accept?")
                .addAction(R.drawable.download, "Accept", acceptPI)
                .addAction(R.drawable.download, "Decline", declinePI)
                .setContentIntent(acceptPI)
                .setContentIntent(declinePI)
                .setOnlyAlertOnce(true)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_MAX);
        // Allows notification to be cancelled when user clicks it
        nBuilder.setAutoCancel(true);
        nBuilder.setStyle(inboxStyle);
        int notificationId = 515;
        notifyMgr.notify(notificationId, nBuilder.build());
    }


    public void acceptListener() {
        final String phone = sharedPreferenceClass.getUserID();
        ref.child("users").addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean isPresent = dataSnapshot.hasChild(phone);
                        if (isPresent) {
                            HashMap<String, Object> result = new HashMap<>();
                            result.put("adminPermission", true);
                            ref.child("users").child(phone).updateChildren(result);
                            Toast.makeText(getApplicationContext(), "Accepted", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        //handle databaseError
                    }
                });
    }

    public void declineListener() {
        final String phone = sharedPreferenceClass.getUserID();
        ref.child("users").addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean isPresent = dataSnapshot.hasChild(phone);
                        if (isPresent) {
                            HashMap<String, Object> result = new HashMap<>();
                            result.put("adminPermission", false);
                            ref.child("users").child(phone).updateChildren(result);
                            Toast.makeText(getApplicationContext(), "Accepted", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        //handle databaseError
                    }
                });
    }

    @Override
    public void onLocationChanged(Location location) {
        bestLocation = location;

        if (previousLocation == null && bestLocation != null) {
            endTime = System.currentTimeMillis();
            totalTime = (endTime - startTime) / 1000;
            System.out.println(totalTime + "TOTAL TIME TAKEN");
            //onLocationChanged(bestLocation);
            String sLatLng = bestLocation.getLatitude() + "," + bestLocation.getLongitude();
            VolleyTask volleyTask = new VolleyTask(getApplicationContext(), "SNAP_TO_ROAD", sLatLng);
            previousLocation = bestLocation;
            startTime = System.currentTimeMillis();
        } /*else if (bestLocation != null && checkTheDistance(bestLocation, previousLocation) && VOLLEY_STATUS.equals("NOT_RUNNING")) {
            endTime = System.currentTimeMillis();
            totalTime = (endTime - startTime) / 1000;
            System.out.println(totalTime + "TOTAL TIME TAKEN");
            //onLocationChanged(bestLocation);
            String sLatLng = bestLocation.getLatitude() + "," + bestLocation.getLongitude();
            VolleyTask volleyTask = new VolleyTask(getApplicationContext(), "SNAP_TO_ROAD", sLatLng);
            previousLocation = bestLocation;
            *//*endTime = System.currentTimeMillis();
            totalTime = (endTime - startTime) / 1000;
            System.out.println(totalTime + "TOTAL TIME TAKEN");*//*
            startTime = System.currentTimeMillis();
        }*/

        else if (bestLocation != null && checkTheDistance(bestLocation, previousLocation) && VOLLEY_STATUS.equals("NOT_RUNNING")) {
            endTime = System.currentTimeMillis();
            totalTime = (endTime - startTime) / 1000;
            //System.out.println(totalTime + "TOTAL TIME TAKEN");
            //onLocationChanged(bestLocation);
            String sLatLng = bestLocation.getLatitude() + "," + bestLocation.getLongitude();
            VolleyTask volleyTask = new VolleyTask(getApplicationContext(), "SNAP_TO_ROAD", sLatLng);
            previousLocation = bestLocation;
            /*endTime = System.currentTimeMillis();
            totalTime = (endTime - startTime) / 1000;
            System.out.println(totalTime + "TOTAL TIME TAKEN");*/
            startTime = System.currentTimeMillis();
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private void getLocation() {

        //LocationRequest locationRequest = LocationRequest.create();
        //locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //locationRequest.setInterval(20000);
        bestLocation = null;
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
       /* bestLocation = null;
        boolean gps_enabled = false;
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {
            ex.printStackTrace();
        }*/
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }

            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                if (bestLocation == null || location.getAccuracy() < bestLocation.getAccuracy()) {
                    //Log.d("found best location: %s", location.getProvider());
                    bestLocation = location;
                    locationManager.requestLocationUpdates(provider, 0, 60, this);
                }
            }


        }
        /*if (previousLocation==null && bestLocation!=null) {
            //onLocationChanged(bestLocation);
            String sLatLng = bestLocation.getLatitude() + "," + bestLocation.getLongitude();
            VolleyTask volleyTask = new VolleyTask(getApplicationContext(), "SNAP_TO_ROAD", sLatLng);
            previousLocation = bestLocation;
        } else if(bestLocation!=null && checkTheDistance(bestLocation, previousLocation)){
            //onLocationChanged(bestLocation);
            String sLatLng = bestLocation.getLatitude() + "," + bestLocation.getLongitude();
            VolleyTask volleyTask = new VolleyTask(getApplicationContext(), "SNAP_TO_ROAD", sLatLng);
            previousLocation = bestLocation;
        }*/
    }

    /*public void getLocation() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(20000);
        locationRequest.setFastestInterval(5000);
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 20, 50, this);
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                double accuracy = location.getAccuracy();
                if(checkTheDistance(location, previousLocation)) {
                    onLocationChanged(location);
                    previousLocation = location;
                }
            } else {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location != null) {
                    double accuracy = location.getAccuracy();
                    if(checkTheDistance(location, previousLocation)) {
                        onLocationChanged(location);
                        previousLocation = location;
                    }
                }
            }
            if (locationManager != null) {
                double accuracy = location.getAccuracy();
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        }
    }*/

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    /*private Boolean checkTheDistance(Location location, Location previousLocation) {
        float distance = location.distanceTo(previousLocation);
        boolean b = distance >= radius;
        if (distance >= 150.0) {
            // speed = distance / time
            float speedInMetersPerSecond = (distance / totalTime);
            float speed = (float) (speedInMetersPerSecond * 3.6);
            if (speed >= 80.0) {
                VolleyTask volleyTask = new VolleyTask(getApplicationContext(), "GET_DISTANCE", String.valueOf(location.getLatitude())
                        + "," + String.valueOf(location.getLongitude()), String.valueOf(previousLocation.getLatitude()) + "," +
                        String.valueOf(previousLocation.getLongitude()), asyncInterface, "");
            }
            return hasNoGpsBug;
        }
        return b;
    }*/


    /*private Boolean checkTheDistance(Location location, Location previousLocation) {
        float distance = location.distanceTo(previousLocation);
        boolean b;
        if (distance >= 150.0 || distance<=65) {
            totalTime = (endTime - startTime) / 1000;
            // speed = distance / time
            float speedInMetersPerSecond = (distance / totalTime);

            //A meter per second is equal to 3.6 kilometers per hour, so to convert simply multiply by 3.6. For example, 5m/sec = (5 × 3.6) = 18km/h
            //speed is compared to 36km/h instead of 75km/h because distanceTo function only gives distance in radius and not actual road distance
            //here we will be run volley task to confirm if distance by road is more than 500 mts
            float speed = (float) (speedInMetersPerSecond * 3.6);
            if (speed >= 40.0) {
                VolleyTask volleyTask = new VolleyTask(getApplicationContext(), "GET_DISTANCE", String.valueOf(location.getLatitude())
                        + "," + String.valueOf(location.getLongitude()), String.valueOf(previousLocation.getLatitude()) + "," +
                        String.valueOf(previousLocation.getLongitude()), asyncInterface, "");
                VOLLEY_STATUS = "RUNNING";
            }
        }
        b = !VOLLEY_STATUS.equals("RUNNING");
        return b;
    }*/

    /*
   logic written by Vijay on 02-07-2018.
   this checks for gps bug, where in case if gps is not stable enough or might show wrong location, this reduces the margin of error where
   a marker can be at certain time and the distance it has travelled.
    */
    private Boolean checkTheDistance(Location location, Location previousLocation) {
        float distance = location.distanceTo(previousLocation);
        boolean b;
        if (distance >= 60.0) {
            VolleyTask volleyTask = new VolleyTask(getApplicationContext(), "GET_DISTANCE", String.valueOf(location.getLatitude())
                    + "," + String.valueOf(location.getLongitude()), String.valueOf(previousLocation.getLatitude()) + "," +
                    String.valueOf(previousLocation.getLongitude()), asyncInterface, "");
            VOLLEY_STATUS = "RUNNING";
        }
        b = !VOLLEY_STATUS.equals("RUNNING");
        return b;
    }
    @Override
    public void onResultReceived(String sMessage, int nCase, String sResult) {

    }

    @Override
    public void onResultReceived(String sMessage, int nCase, double dLatitudeStart, double dLongitudeStart, double dLatitudeEnd, double dLongitudeEnd) {

    }

    @Override
    public void onDistanceReceived(String sMessage, int nCase, String sDistance, String sTime, String ID) {
        switch (sMessage){
            case "UPDATE_DISTANCE":
                String[] distanceArray = sDistance.split(" ");
                Double distance = Double.valueOf(distanceArray[0]);
                endTime = System.currentTimeMillis();
                totalTime = (endTime - startTime) / 1000;
                // speed = distance / time
                double speedInMetersPerSecond = (distance*1000 / totalTime);

                float speed = (float) (speedInMetersPerSecond * 3.6);
                if (speed <= 78.0) {
                    String sLatLng = bestLocation.getLatitude() + "," + bestLocation.getLongitude();
                    VolleyTask volleyTask = new VolleyTask(getApplicationContext(), "SNAP_TO_ROAD", sLatLng);
                    previousLocation = bestLocation;
                    endTime = 0;
                    startTime = System.currentTimeMillis();
                }
                //hasNoGpsBug = !(distance >= .5);
                VOLLEY_STATUS = "NOT_RUNNING";
                break;
        }
    }
}
