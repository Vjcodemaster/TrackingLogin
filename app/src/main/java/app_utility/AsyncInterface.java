package app_utility;

public interface AsyncInterface {
    void onResultReceived(String sMessage, int nCase, String sResult);

    void onResultReceived(String sMessage, int nCase, double dLatitudeStart, double dLongitudeStart, double dLatitudeEnd, double dLongitudeEnd);

    void onDistanceReceived(String sMessage, int nCase, String sDistance, String sTime, String ID);
}
