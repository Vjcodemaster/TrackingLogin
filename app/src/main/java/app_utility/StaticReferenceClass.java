package app_utility;

public class StaticReferenceClass {
    public static final String ServiceIntent = "app_utility.TrackingService";

    public static String SNAP_TO_ROAD_URL = "https://roads.googleapis.com/v1/snapToRoads?path=";//&interpolate=true&key=AIzaSyCAigqCiSh3eI07JpdAJ30Pj6hMYzwZC84";
    public static String SNAP_TO_ROAD_PARAMS = "&interpolate=true&key=";
    public static String ROUTES_API_KEY = "AIzaSyCAigqCiSh3eI07JpdAJ30Pj6hMYzwZC84";

    public static String DISTANCE_ON_ROAD = "http://maps.googleapis.com/maps/api/distancematrix/json?origins=";

    public static String DISTANCE_MATRIX_PARAMS = "&mode=driving&language=en-EN&sensor=false";

    public static String DISTANCE_WITH_DIRECTION ="http://maps.google.com/maps/api/directions/json?origin="; //52.0,0&destination=52.0,0

    public static String DESTINATION = "&destination=";

    public static String DESTINATIONS = "&destinations=";


    public static String DISTANCE_WITH_DIRECTION_PARAMS = "&mode=driving&sensor=true";

    public static String getSnapToRoadURL(String sLatLng) {
        return SNAP_TO_ROAD_URL + sLatLng + SNAP_TO_ROAD_PARAMS + ROUTES_API_KEY;
    }

    public static String getDistanceMatrix(String sOrigin, String sDestination) {
        return DISTANCE_ON_ROAD + sOrigin + DESTINATIONS + sDestination + DISTANCE_MATRIX_PARAMS;
    }

    public static String getDistanceWithDirection(String sOrigin, String sDestination) {
        return DISTANCE_WITH_DIRECTION + sOrigin + DESTINATION + sDestination + DISTANCE_WITH_DIRECTION_PARAMS;
    }
}
