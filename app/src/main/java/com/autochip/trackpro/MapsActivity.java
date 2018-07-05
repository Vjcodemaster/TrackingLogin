package com.autochip.trackpro;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import app_utility.AsyncInterface;
import app_utility.MarkerAnimation;
import app_utility.SharedPreferenceClass;
import app_utility.TrackingService;
import app_utility.VolleyTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, AsyncInterface {

    public static boolean isAppInFg = false;
    public static boolean isScrInFg = false;
    public static boolean isChangeScrFg = false;

    private GoogleMap mMap;

    public DatabaseReference ref;

    Polyline polyline;

    private Location location;
    private LocationManager locationManager;

    Location previousLocation;

    AsyncInterface asyncInterface;

    Double radius = 2000.0;

    String sMarkerIDInRange;

    Button btnYes, btnClear;

    boolean isFocusedOnMarker;

    BottomSheetBehavior sheetBehavior;

    LinearLayout llBottomSheet;
    private ArrayList<Marker> alMarker = new ArrayList<>();
    private ArrayList<String> alUsersList = new ArrayList<>();
    ArrayList<LatLng> alPathLatLng = new ArrayList<>();
    LinkedHashMap<String, ArrayList<LatLng>> linkedHMLatLng = new LinkedHashMap<>();

    LinkedHashMap<String, Marker> linkedHMMarkers = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_maps);
        ref = FirebaseDatabase.getInstance().getReference();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        asyncInterface = this;

        SharedPreferenceClass sharedPreferenceClass = new SharedPreferenceClass(MapsActivity.this);
        Set<String> set = sharedPreferenceClass.getUserList();
        alUsersList.addAll(set);

        /*Intent in = new Intent(MapsActivity.this, TrackingService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(in);
        } else {
            startService(in);
        }*/
        //startService(in);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        llBottomSheet = findViewById(R.id.bottom_sheet);
        btnYes = llBottomSheet.findViewById(R.id.btn_yes);
        btnClear = llBottomSheet.findViewById(R.id.btn_clear);

        sheetBehavior = BottomSheetBehavior.from(llBottomSheet);

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isFocusedOnMarker = false;
                polyline.remove();
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });


        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isFocusedOnMarker = true;
                ArrayList<LatLng> alTmp = new ArrayList<>();
                LatLng sLatLng = new LatLng(previousLocation.getLatitude(), previousLocation.getLongitude());
                alTmp.add(linkedHMLatLng.get(sMarkerIDInRange).get(linkedHMLatLng.get(sMarkerIDInRange).size() - 1));
                alTmp.add(sLatLng);
                fetchPath(alTmp, 111222, sMarkerIDInRange);
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                //Toast.makeText(MapsActivity.this, "triggered", Toast.LENGTH_SHORT).show();
            }
        });

        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        //sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED: {
                        //btnBottomSheet.setText("Close Sheet");
                    }
                    break;
                    case BottomSheetBehavior.STATE_COLLAPSED: {
                        //btnBottomSheet.setText("Expand Sheet");
                    }
                    break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    @Override
    protected void onStart() {
        if (!isAppInFg) {
            isAppInFg = true;
            isChangeScrFg = false;
            onAppStart();
        } else {
            isChangeScrFg = true;
        }
        isScrInFg = true;

        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (!isScrInFg || !isChangeScrFg) {
            isAppInFg = false;
            onAppPause();
        }
        isScrInFg = false;

    }

    public void onAppStart() {
        //remove this toast
        Toast.makeText(getApplicationContext(), "App in foreground", Toast.LENGTH_LONG).show();
        // your code
    }

    public void onAppPause() {
        //remove this toast
        Toast.makeText(getApplicationContext(), "App in background", Toast.LENGTH_LONG).show();
        // your code
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setMinZoomPreference(11f);
        mMap.setMaxZoomPreference(15.5f);
        getLocation();
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
        mMap.setMyLocationEnabled(true);
        addLocationChangeListener();
        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        //mMap.addMarker(new MarkerOptions().position(currentLocation).title("You are here"));

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15.5f));
        previousLocation = location;
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

    private void addLocationChangeListener() {

        for (int i = 0; i < alUsersList.size(); i++) {
            final String sPhone = alUsersList.get(i);
            final int finalI = i;
            ref.child("users").child(sPhone).addChildEventListener(new ChildEventListener() {

                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    //Toast.makeText(context, "added", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    //boolean value = (Boolean) dataSnapshot.getValue();
                    String sLocation = null;
                    LatLng latLng = null;
                    try {
                        sLocation = (String) dataSnapshot.getValue();
                        Toast.makeText(MapsActivity.this, "moved" + sLocation, Toast.LENGTH_SHORT).show();
                        String[] saLocation = sLocation.split(",");
                        latLng = new LatLng(Double.valueOf(saLocation[0]), Double.valueOf(saLocation[1]));
                        alPathLatLng = new ArrayList<>();

                        if (linkedHMLatLng.size() >= 1 && linkedHMLatLng.containsKey(sPhone)) {
                            alPathLatLng.addAll(linkedHMLatLng.get(sPhone));
                        }
                        alPathLatLng.add(latLng);

                        if (linkedHMLatLng.containsKey(sPhone))
                            linkedHMLatLng.remove(sPhone);

                        linkedHMLatLng.put(sPhone, alPathLatLng);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    if (alPathLatLng.size() > 1) {
                        fetchPath(alPathLatLng, finalI, sPhone);

                        String sDestination = previousLocation.getLatitude() + "," + previousLocation.getLongitude();
                        VolleyTask volleyTask = new VolleyTask(getApplicationContext(), "GET_DISTANCE", sLocation, sDestination, asyncInterface, sPhone);
                        /*previousLocation = new Location(LocationManager.GPS_PROVIDER);
                        previousLocation.setLatitude(alPathLatLng.get(finalI).latitude);
                        previousLocation.setLongitude(alPathLatLng.get(finalI).longitude);

                        Location location = new Location(LocationManager.GPS_PROVIDER);
                        location.setLatitude(Double.valueOf(saLocation[0]));
                        location.setLongitude(Double.valueOf(saLocation[1]));
                        checkTheDistance(location, previousLocation);*/
                    } else {
                        Marker mCarMarker;
                        //Bitmap icon = BitmapFactory.decodeResource(getResources(),R.drawable.benz_car);
                        mCarMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Marker in " +
                                "manipal").icon(BitmapDescriptorFactory.fromResource(R.drawable.benz_car)).anchor(0.5f, 0.5f)
                                //.rotation(bearing)
                                .flat(true));

                        /*mCarMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Marker in " +
                                "manipal").icon(BitmapDescriptorFactory.fromBitmap(icon)).anchor(0.5f, 0.5f)
                                //.rotation(bearing)
                                .flat(true));*/
                        alMarker.add(mCarMarker);
                        linkedHMMarkers.put(sPhone, mCarMarker);
                    }

                    //Toast.makeText(context, "changed", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Toast.makeText(MapsActivity.this, "moved", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }


    public void fetchPath(ArrayList<LatLng> alLatLng, int position, String key) {

        ArrayList<LatLng> path = new ArrayList<>();

        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(getResources().getString(R.string.google_maps_web_api_key))
                .build();

        //DirectionsApiRequest req = DirectionsApi.getDirections(context, "12.975025, 77.614576", "12.973672, 77.613551");
        String fromLatLng = String.valueOf(alLatLng.get(alLatLng.size() - 2).latitude) + ", " + String.valueOf(alLatLng.get(alLatLng.size() - 2).longitude);
        String toLatLng = String.valueOf(alLatLng.get(alLatLng.size() - 1).latitude) + ", " + String.valueOf(alLatLng.get(alLatLng.size() - 1).longitude);

        DirectionsApiRequest req = DirectionsApi.getDirections(context, fromLatLng, toLatLng);

        try {
            DirectionsResult res = req.await();

            //Loop through legs and steps to get encoded polylines of each step
            if (res.routes != null && res.routes.length > 0) {
                DirectionsRoute route = res.routes[0];

                if (route.legs != null) {
                    for (int i = 0; i < route.legs.length; i++) {
                        DirectionsLeg leg = route.legs[i];
                        if (leg.steps != null) {
                            for (int j = 0; j < leg.steps.length; j++) {
                                DirectionsStep step = leg.steps[j];
                                if (step.steps != null && step.steps.length > 0) {
                                    for (int k = 0; k < step.steps.length; k++) {
                                        DirectionsStep step1 = step.steps[k];
                                        EncodedPolyline points1 = step1.polyline;
                                        if (points1 != null) {
                                            //Decode polyline and add points to list of route coordinates
                                            List<com.google.maps.model.LatLng> coords1 = points1.decodePath();
                                            for (com.google.maps.model.LatLng coord1 : coords1) {
                                                path.add(new LatLng(coord1.lat, coord1.lng));
                                            }
                                        }
                                    }
                                } else {
                                    EncodedPolyline points = step.polyline;
                                    if (points != null) {
                                        //Decode polyline and add points to list of route coordinates
                                        List<com.google.maps.model.LatLng> coords = points.decodePath();
                                        for (com.google.maps.model.LatLng coord : coords) {
                                            path.add(new LatLng(coord.lat, coord.lng));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("Maps", ex.getLocalizedMessage());
        }

        //Draw the polyline
        if (path.size() > 0) {
            if(position==111222) {
                PolylineOptions polyOptions = new PolylineOptions();
                //polyline = this.mMap.addPolyline(new PolylineOptions());
                for(int i=0; i<path.size(); i++) {
                    polyOptions.add(path.get(i)).color(Color.RED).width(7).geodesic(true);
                    //polyline = mMap.addPolyline(polyOptions);
                }
                polyline = this.mMap.addPolyline(polyOptions);
            } else {
                MarkerAnimation markerAnimation;
                markerAnimation = new MarkerAnimation();
                markerAnimation.animateLine(path, mMap, linkedHMMarkers.get(key), MapsActivity.this, isFocusedOnMarker);
            }
            Log.e("Maps", path.getClass().getName());
        }
    }

    private boolean checkTheDistance(Location location, Location previousLocation) {

        return location.distanceTo(previousLocation) < radius;
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
                if(distance <= 2.5){
                    llBottomSheet.setVisibility(View.VISIBLE);
                    sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    sMarkerIDInRange = ID;
                } else {
                    llBottomSheet.setVisibility(View.GONE);
                    //sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
                break;
        }
    }

    public void getLocation() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(20000);
        locationRequest.setFastestInterval(5000);
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                onLocationChanged(location);
            } else {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location != null)
                    onLocationChanged(location);
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 100, this);
            if (locationManager != null) {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        }
    }
}
