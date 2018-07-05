package app_utility;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Property;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class MarkerAnimation {
    //static GoogleMap map;
    private ArrayList<LatLng> _trips = new ArrayList<>();
    private Marker _marker;
    private Context context;
    private float bearing;
    private boolean isMarkerRotating;
    private GoogleMap map;
    private Location previousLocation;
    private Polyline greyPolyLine, blackPolyLine, polyLine;
    private PolylineOptions rectOptions = new PolylineOptions();
    private final Interpolator interpolator = new LinearInterpolator();
    PolylineOptions polyOptions = new PolylineOptions();
    private ArrayList<LatLng> list = new ArrayList<>();
    private Handler h = new Handler();
    Runnable runnable;
    LatLng updatePath;
    long animationDuration;
    Double radius = 20.0;
    boolean isFocusedOnMarker;


    //PolylineOptions polyBlackOptions = new PolylineOptions();
    private LatLngInterpolator _latLngInterpolator = new LatLngInterpolator.Spherical();

    public void animateLine(ArrayList<LatLng> Trips, GoogleMap map, Marker marker, Context current, boolean isFocusedOnMarker) {
        _trips.addAll(Trips);
        _marker = marker;
        context = current;
        this.map = map;
        this.isFocusedOnMarker = isFocusedOnMarker;
        animateMarker();
    }


    public void animateMarker() {
        final ObjectAnimator animator;
        final long start = SystemClock.uptimeMillis();
        Projection proj = map.getProjection();
        //Point startPoint = proj.toScreenLocation(previouslatLng);
        //final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = _trips.size() * 150;

        final Interpolator interpolator = new LinearInterpolator();

        TypeEvaluator<LatLng> typeEvaluator = new TypeEvaluator<LatLng>() {
            @Override
            public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
                Location location = new Location(LocationManager.GPS_PROVIDER);
                location.setLatitude(endValue.latitude);
                location.setLongitude(endValue.longitude);
                if (previousLocation != null) {
                    bearing = previousLocation.bearingTo(location);
                    if(!checkTheDistance(location, previousLocation)){
                        animationDuration = 2300;
                    } else {
                        animationDuration = 1200;
                    }
                }

                previousLocation = location;
                if (bearing != 0.0) {
                    rotateMarker(_marker, bearing);
                }
                //LatLng latLng = new LatLng(endValue.latitude, endValue.longitude);
                //list = new ArrayList<>();
                //list.add(latLng);
                //updatePath = new LatLng(_latLngInterpolator.interpolate(fraction, startValue, endValue).latitude,_latLngInterpolator.interpolate(fraction, startValue, endValue).longitude);
                return _latLngInterpolator.interpolate(fraction, startValue, endValue);
            }
        };
        Property<Marker, LatLng> property = Property.of(Marker.class, LatLng.class, "position");
        //if(_trips.size()<=2) {
        animator = ObjectAnimator.ofObject(_marker, property, typeEvaluator, _trips.get(0));
        /*polyLine = initializePolyLine();
        h.post(new Runnable() {
            @Override
            public void run() {
                runnable = this;
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                        *//*double lng = t * toPosition.longitude + (1 - t)
                                * startLatLng.longitude;
                        double lat = t * toPosition.latitude + (1 - t)
                                * startLatLng.latitude;
                        mCarMarker.setPosition(new LatLng(lat, lng));
                        updatePolyLine(new LatLng(lat, lng));*//*
                _marker.setPosition(updatePath);
                //mCarMarker.setVisible(false);
                updatePolyLine(updatePath);
                if (t < 1.0) {
                    // Post again 16ms later.
                    h.postDelayed(this, 16);
                } else {
                    *//*if (hideMarke) {
                        mCarMarker.setVisible(false);
                    } else {
                        mCarMarker.setVisible(true);
                    }*//*
                }
            }
        });*/
        //polyOptions.add(_trips.get(0)).color(Color.BLUE).width(6).geodesic(true);
        //polyOptions.color(Color.BLUE).width(6).geodesic(true);
        //polyLine = map.addPolyline(polyOptions);
        //Log.e("list", list.toString());
        polyOptions.add(_trips.get(0)).color(Color.BLUE).width(6).geodesic(true);
        map.addPolyline(polyOptions);

        //polyLine = initializePolyLine();
        /*these 2 lines will animate the marker along with the polyline
        _marker.setPosition(_trips.get(0));

        updatePolyLine(_marker.getPosition());
         */
        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                updatePolyLine(_marker.getPosition());
            }
        }, 15000);*/

        //_marker.setPosition(_trips.get(0));

        //updatePolyLine(_marker.getPosition());

        /*ObjectAnimator objectAnimator =
                ObjectAnimator.ofFloat(, View.X,
                        View.Y, path);
        objectAnimator.setDuration(3000);
        objectAnimator.start();*/
        /*Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                updatePolyLine(_trips.get(0));
            }
        };
        new Handler().postDelayed(runnable, 50 );*/

        //} else {
        //animator = ObjectAnimator.ofObject(_marker, property, typeEvaluator, _trips.get(_trips.size()-2));
        //}

        //PolylineOptions greyOptions = new PolylineOptions();
       /* polyOptions.add(_trips.get(0));
        polyOptions.width(7);
        polyOptions.color(Color.GRAY);
        polyOptions.startCap(new SquareCap());
        polyOptions.endCap(new SquareCap());
        polyOptions.jointType(ROUND);
        greyPolyLine = map.addPolyline(polyOptions);

        polyBlackOptions.add(_trips.get(0));
        polyBlackOptions.width(7);
        polyBlackOptions.color(Color.BLACK);
        polyBlackOptions.startCap(new SquareCap());
        polyBlackOptions.endCap(new SquareCap());
        polyBlackOptions.jointType(ROUND);
        blackPolyLine = map.addPolyline(polyBlackOptions);
        animatePolyLine();*/
        //ObjectAnimator animator = ObjectAnimator.o(view, "alpha", 0.0f);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationCancel(Animator animation) {
                //  animDrawable.stop();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

                //  animDrawable.stop();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                if (isFocusedOnMarker)
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(_trips.get(0), 15.5f));
                //markerAnimation.animateLine(path,mMap,mCarMarker,MapsActivity.this);

                //  animDrawable.stop();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //  animDrawable.stop();
                if (_trips.size() > 1) {
                    _trips.remove(0);
                    h.removeCallbacks(runnable);
//list.remove(0);
                    animateMarker();

                    //_marker.setPosition(_trips.get(0));

                    //updatePolyLine(_marker.getPosition());
                }

            }
        });

        animator.setDuration(animationDuration);
        animator.start();
    }

    private Polyline initializePolyLine() {
        //polyLinePoints = new ArrayList<LatLng>();
        rectOptions.add(_trips.get(0)).color(Color.BLUE).width(6).geodesic(true);
        return map.addPolyline(rectOptions);
    }

    private void rotateMarker(final Marker marker, final float toRotation) {
        if (!isMarkerRotating) {
            final Handler handler = new Handler();
            final long start = SystemClock.uptimeMillis();
            final float startRotation = marker.getRotation();
            final long duration = 500;
            final Interpolator interpolator = new LinearInterpolator();


            handler.post(new Runnable() {
                @Override
                public void run() {
                    isMarkerRotating = true;

                    long elapsed = SystemClock.uptimeMillis() - start;
                    float t = interpolator.getInterpolation((float) elapsed / duration);
                    /*if(_trips.size()>=2) {
                        double lat = t * _trips.get(_trips.size() - 1).latitude + (1 - t) * _trips.get(_trips.size() - 2).latitude;
                        double lng = t * _trips.get(_trips.size() - 1).longitude + (1 - t) * _trips.get(_trips.size() - 2).longitude;
                        LatLng newPosition = new LatLng(lat, lng);
                        updatePolyLine(newPosition);
                    }*/
                    float rot = t * toRotation + (1 - t) * startRotation;

                    marker.setRotation(-rot > 180 ? rot / 2 : rot);
                    if (t < 1.0) {
                        // Post again 16ms later.

                        handler.postDelayed(this, 16);
                    } else {
                        isMarkerRotating = false;
                    }
                    //updatePolyLine(_marker.getPosition());
                }
            });
            //addPolyLine();
        }
        //markerAnimation.animateLine(alLatLng,mMap,mCarMarker,MapsActivity.this);
        /*if (flag == 1) {
            addUpdatePolyLine();
        }*/
    }

    private void updatePolyLine(LatLng latLng) {
        List<LatLng> points = polyLine.getPoints();
        points.add(latLng);
        polyLine.setPoints(points);
    }

    private void addPolyLine() {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                // Post again 16ms later.
                polyOptions.add(_trips.get(0)).color(Color.BLUE).width(6).geodesic(true);
                map.addPolyline(polyOptions);
                handler.postDelayed(this, 16);
            }
        });
    }

    private boolean checkTheDistance(Location location, Location previousLocation) {

        return location.distanceTo(previousLocation) > radius;
    }

    public void animateMarker(final LatLng toPosition, final boolean hideMarke) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = map.getProjection();
        Point startPoint = proj.toScreenLocation(_marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 5000;
        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                _marker.setPosition(new LatLng(lat, lng));
                updatePolyLine(_marker.getPosition());
                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarke) {
                        _marker.setVisible(false);
                    } else {
                        _marker.setVisible(true);
                    }
                }
            }
        });
    }
    /*private void updatePolyLine(LatLng latLng) {
        List<LatLng> points = polyLine.getPoints();
        points.add(latLng);
        polyLine.setPoints(points);
    }*/
    /*private void animatePolyLine() {

        ValueAnimator animator = ValueAnimator.ofInt(0, 100);
        animator.setDuration(1000);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {

                List<LatLng> latLngList = blackPolyLine.getPoints();
                int initialPointSize = latLngList.size();
                int animatedValue = (int) animator.getAnimatedValue();
                int newPoints = (animatedValue * _trips.size()) / 100;

                if (initialPointSize < newPoints ) {
                    latLngList.addAll(_trips.subList(initialPointSize, newPoints));
                    blackPolyLine.setPoints(latLngList);
                }
            }
        });

        animator.addListener(polyLineAnimationListener);
        animator.start();

    }

    Animator.AnimatorListener polyLineAnimationListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animator) {

            //addMarker(listLatLng.get(listLatLng.size()-1));
        }

        @Override
        public void onAnimationEnd(Animator animator) {

            List<LatLng> blackLatLng = greyPolyLine.getPoints();
            List<LatLng> greyLatLng = greyPolyLine.getPoints();

            greyLatLng.clear();
            greyLatLng.addAll(blackLatLng);
            blackLatLng.clear();

            blackPolyLine.setPoints(blackLatLng);
            greyPolyLine.setPoints(greyLatLng);

            blackPolyLine.setZIndex(2);

            animator.start();
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {


        }
    };*/

}