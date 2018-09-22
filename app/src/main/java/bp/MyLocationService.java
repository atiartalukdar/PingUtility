package bp;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;

import alarmmanager.ToastBroadcastReceiver;
import rettrofit.APIClient;
import rettrofit.APIInterface;

public class MyLocationService extends Service
{

    public static final int notify = 1000 * 60 * 5;  //interval between two services(Here Service run every 5 Minute)
    private Handler mHandler = new Handler();   //run on another Thread to avoid crash
    private Timer mTimer = null;    //timer handling


    private static final String TAG = "lService Atiar -";
    private static final String LOG_TAG = "lService Atiar -";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 4*60*1000;
    private static final float LOCATION_DISTANCE = 0f;
    private static APIInterface apiInterface;


    private class LocationListener implements android.location.LocationListener
    {
        Location mLastLocation;

        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            String LatLang = location.getLatitude() + ":"+location.getLongitude();
            Log.e(TAG, "onLocationChanged: " + LatLang);
            BP.t(getApplicationContext(),TAG,LatLang);

            SharedPrefarences.setPreference(getApplicationContext(),"locationLat",location.getLatitude()+"");
            SharedPrefarences.setPreference(getApplicationContext(),"locationLang",location.getLongitude()+"");

            mLastLocation.set(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.e(TAG, "onStartCommand");

        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        apiInterface = APIClient.getClient().create(APIInterface.class);

        //===========Alarm Manager ===========
        Intent toastIntent= new Intent(getApplicationContext(), ToastBroadcastReceiver.class);
        PendingIntent toastAlarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, toastIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        long startTime=System.currentTimeMillis(); //alarm starts immediately
        AlarmManager backupAlarmMgr=(AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        backupAlarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,startTime,AlarmManager.INTERVAL_FIFTEEN_MINUTES,toastAlarmIntent); // alarm will repeat after every 15 minutes


        Log.e(TAG, "onCreate");
        initializeLocationManager();
        //mAPIService = ApiUtils.getAPIService();

        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy()
    {
        /*Intent serviceIntent = new Intent(this, MyLocationService.class);
        startService(serviceIntent);
*/
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        mTimer.cancel();    //For Cancel Timer

        Log.i("EXIT", "ondestroy!");
        Intent broadcastIntent = new Intent("info.atiar.ActivityRecognition.RestartSensor");
        sendBroadcast(broadcastIntent);

        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }


    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    private void logTag() {

        BP.t(getApplicationContext(), LOG_TAG, "Version Name: "+ BP.getVersionname());
        BP.t(getApplicationContext(), LOG_TAG, "Device Name: "+ BP.getDevicename());
        BP.t(getApplicationContext(), LOG_TAG, "Mac: "+ BP.getDeviceMac());
        BP.t(getApplicationContext(), LOG_TAG, "Local IP: "+ BP.getDeviceLocalIP());
        BP.t(getApplicationContext(), LOG_TAG, "Public IP: "+ BP.getPublicIP(getApplicationContext()));
        //BP.t(getApplicationContext(), LOG_TAG, editText.getText().toString());
        BP.t(getApplicationContext(), LOG_TAG, "IMEI: "+ BP.getDeviceIMEI(getApplicationContext()));
        BP.t(getApplicationContext(), LOG_TAG, "Inter Memory: "+ BP.getTotalInternalMemorySize());
        BP.t(getApplicationContext(), LOG_TAG, "External Memory: "+ BP.getTotalExternalMemorySize());
        BP.t(getApplicationContext(), LOG_TAG, "Ram: "+ BP.getTotalRAM());
        BP.t(getApplicationContext(), LOG_TAG, "Location: "+ SharedPrefarences.getPreference(getApplicationContext(), "location"));

    }


    public boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
