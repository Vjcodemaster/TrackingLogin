package app_utility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TrackingBroadCastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //Log.v(intent.getAction(),intent.getAction());
        String in = intent.getAction();
        Intent service;
        assert in != null;
        switch(in)
        {
            case "android.intent.action.BOOT_COMPLETED":
                service = new Intent(context, TrackingService.class);
                context.startService(service);
                break;
            case "app_utility.TrackingService.ServiceStopped":
                service = new Intent(context, TrackingService.class);
                context.startService(service);
                break;
            case "android.intent.action.ac.user.accept":
                TrackingService.refOfService.acceptListener();
                break;
            case "android.intent.action.ac.user.decline":
                TrackingService.refOfService.declineListener();
                break;
        }


     /*   if ("app_utility.TrackingService.ServiceStopped".equals(intent.getAction()))
        {

        }*/
        /*if("com.trackingservice.location".equals(intent.getAction()))
        {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "wake lock active");
            wl.acquire();

            //TrackingService.refOfService.getLocation();
            // Put here YOUR code.

            wl.release();

        }*/
        // an Intent broadcast.
       // throw new UnsupportedOperationException("Not yet implemented");
    }
}
