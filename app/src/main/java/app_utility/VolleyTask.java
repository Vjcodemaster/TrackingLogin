package app_utility;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import static app_utility.TrackingService.ref;

public class VolleyTask {
    private Context context;
    private String sLatLng;
    private String sOrigin;
    private String sDestination;
    private int mStatusCode;
    SharedPreferenceClass sharedPreferenceClass;
    AsyncInterface asyncInterface;
    String sPhone;
    //private AsyncInterface asyncInterface;

    public VolleyTask(Context context, String sCase, String sLatLng) {
        this.context = context;
        this.sLatLng = sLatLng;
        //this.asyncInterface = asyncInterface;
        Volley(sCase);
    }
    public VolleyTask(Context context, String sCase, String sOrigin, String sDestination, AsyncInterface asyncInterface, String sPhone) {
        this.context = context;
        this.sOrigin = sOrigin;
        this.sDestination = sDestination;
        this.asyncInterface = asyncInterface;
        this.sPhone = sPhone;
        Volley(sCase);
    }

    private void Volley(String sCase) {
        switch (sCase) {
            case "SNAP_TO_ROAD":
                //setProgressBar();
                snapToRoad();
                break;
            case "GET_DISTANCE":
                getDistanceWithDirection();
        }
    }

    private void snapToRoad(){
        StringRequest request = new StringRequest(
                Request.Method.GET, StaticReferenceClass.getSnapToRoadURL(sLatLng), //BASE_URL + Endpoint.USER
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        // Success
                        onPostSnapToRoad(mStatusCode, response);
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (mStatusCode == 401) {
                            // HTTP Status Code: 401 Unauthorized
                        }
                    }
                }) {

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                mStatusCode = response.statusCode;
                return super.parseNetworkResponse(response);
            }

            /*@Override
            public byte[] getBody() {
                return new JSONObject(params).toString().getBytes();
            }*/

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };
        // add the request object to the queue to be executed
        ApplicationController.getInstance().addToRequestQueue(request);
    }

    private void onPostSnapToRoad(int mStatusCode, String response){
        switch (mStatusCode) {
            case 200: //success
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(response);
                    JSONArray jaSnapped = jsonObject.getJSONArray("snappedPoints");
                    JSONObject jbLocation = jaSnapped.getJSONObject(0).getJSONObject("location");
                    final String sLatLng = jbLocation.getString("latitude") + "," + jbLocation.getString("longitude");

                    sharedPreferenceClass = new SharedPreferenceClass(context);
                    ref.child("users").addListenerForSingleValueEvent(
                            new ValueEventListener() {

                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    boolean isPresent = dataSnapshot.hasChild(sharedPreferenceClass.getUserID());
                                    if(isPresent) {
                                        HashMap<String, Object> result = new HashMap<>();
                                        result.put("location", sLatLng);
                                        ref.child("users").child(sharedPreferenceClass.getUserID()).updateChildren(result);
                                        //Toast.makeText(context, "Notified", Toast.LENGTH_SHORT).show();
                                    }
                                    //collectPhoneNumbers((Map<String,Object>) dataSnapshot.getValue());
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    //handle databaseError
                                }
                            });
                    //asyncInterface.onResultReceived("SNAP_TO_ROAD", 1, sLatLng);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }


    private void getDistanceWithDirection(){

        /*RequestFuture<JSONObject> future = RequestFuture.newFuture();
        StringRequest request = new StringRequest(Request.Method.GET, StaticReferenceClass.getDistanceWithDirection(sDestination, sOrigin), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Success
                onPostDistanceWithDirection(mStatusCode, response);
            }
        }, future);
        try {
            JSONObject response = future.get(); // this will block
        } catch (InterruptedException | ExecutionException e) {
            // exception handling
        }
        ApplicationController.getInstance().addToRequestQueue(request);*/

        /*try {
            JSONObject response = future.get(); // this will block
        } catch (InterruptedException | ExecutionException e) {
            // exception handling
        }*/

        StringRequest request = new StringRequest(
                Request.Method.GET, StaticReferenceClass.getDistanceWithDirection(sDestination, sOrigin), //BASE_URL + Endpoint.USER
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        // Success
                        onPostDistanceWithDirection(mStatusCode, response);
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (mStatusCode == 401) {
                            // HTTP Status Code: 401 Unauthorized
                        }
                    }
                }) {

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                mStatusCode = response.statusCode;
                return super.parseNetworkResponse(response);
            }

            /*@Override
            public byte[] getBody() {
                return new JSONObject(params).toString().getBytes();
            }*/

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };
        // add the request object to the queue to be executed
        ApplicationController.getInstance().addToRequestQueue(request);
    }

    private void onPostDistanceWithDirection(int mStatusCode, String response){
        switch (mStatusCode) {
            case 200: //success
                JSONObject jsonResponse, jsonExtract;
                JSONArray jsonArray;
                JSONArray jsonArrayLegs;
                try {
                    jsonResponse = new JSONObject(response);
                    jsonArray = jsonResponse.getJSONArray("routes");
                    jsonArrayLegs = jsonArray.getJSONObject(0).getJSONArray("legs");
                    jsonExtract = jsonArrayLegs.getJSONObject(0).getJSONObject("distance");
                    String sDistance = jsonExtract.getString("text");
                    //String sLongStart = jsonExtract.getString("lng");
                    jsonExtract = jsonArrayLegs.getJSONObject(0).getJSONObject("duration");
                    String sTime = jsonExtract.getString("text");
                    //String sLongEnd = jsonExtract.getString("lng");
                    asyncInterface.onDistanceReceived("UPDATE_DISTANCE", 2, sDistance, sTime, sPhone);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
