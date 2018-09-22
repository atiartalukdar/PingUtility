package mobitsolutions.com.pingutility;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.evernote.android.job.JobManager;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

import alarmmanager.ToastBroadcastReceiver;
import bp.BP;
import bp.MyLocationService;
import bp.SharedPrefarences;
import firebaseDispatcher.ScheduledJobService;
import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rettrofit.APIClient;
import rettrofit.APIInterface;
import rettrofit.DataModel;

public class MainActivity extends AppCompatActivity {
    public static final String LOG_TAG = "MainActivity Atiar";

    EditText editText;
    Button button;
    private static APIInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        //Checking the permission 1. Location Permission and 2. Read Phone State permission,
        //if permission is not alloed it will ask for the permission

        JobManager.create(this);

        checkPermissions();

        // TODO: Use your own attributes to track content views in your app
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("Tweet")
                .putContentType("Video")
                .putContentId("1234")
                .putCustomAttribute("Favorites Count", 20)
                .putCustomAttribute("Screen Orientation", "Landscape"));

        //getting the Rettrofit api Interface references.
        apiInterface = APIClient.getClient().create(APIInterface.class);

        //Set the public ip address to sharedpreferences.
        BP.setPublicIP(this);

        //Initialize the editText
        editText = findViewById(R.id.usrusr);

        //Starting the location service to get gps location on the 5 mins of interval
        Intent serviceIntent = new Intent(this, MyLocationService.class);
        startService(serviceIntent);

    }

    //============ Submit button =============//

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void buttonOn(View v) {
        if (editText.getText().toString().trim().equals("") || editText.getText().toString().trim().length() == 0) {
            editText.setError(getString(R.string.warning));
            final Animation animShake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);
            editText.startAnimation(animShake);
        } else {
            //set the inventory no to sharedpreferences.
            BP.setInventoryNumber(getApplicationContext(), editText.getText().toString());

            if (Build.VERSION.SDK_INT >=6){
                //firebase job scheduler / dispatcher
                scheduleJob(this);
                NoteSyncJob.scheduleJob();
            }else{

                //evernote job scheduler
                NoteSyncJob.scheduleJob();

                //===========Alarm Manager ===========
                Intent toastIntent= new Intent(getApplicationContext(), ToastBroadcastReceiver.class);
                PendingIntent toastAlarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, toastIntent,PendingIntent.FLAG_UPDATE_CURRENT);
                long startTime=System.currentTimeMillis(); //alarm starts immediately
                AlarmManager backupAlarmMgr=(AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
                backupAlarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,startTime,AlarmManager.INTERVAL_FIFTEEN_MINUTES,toastAlarmIntent); // alarm will repeat after every 15 minutes

               try {
                   backupAlarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, 15000,toastAlarmIntent); // alarm will repeat after every 15 minutes
               }catch (Exception e){
                   e.printStackTrace();
               }

            }


            try{
                codeToRun(this);
            }catch (Exception e){
                e.printStackTrace();
            }



            //clear the edittext
            editText.setText(null);
            MainActivity.super.onBackPressed();

        }
    }



    // ============== send data to server ==================//
    public static void addDataToServer(String cur_lat, String cur_long,String version_name,String device_name,String mac_address,
                                       String local_ip, String isMobileDevice,String inventory_no, String OS,String PublicIp,
                                       String HardDisk,String CPU,String serial_no,String RAM) {
        //final DataModel dataModel = new DataModel(cur_lat, cur_long);

        Call<DataModel> call1 = apiInterface.addRecord(cur_lat,cur_long,version_name,device_name,mac_address,local_ip,isMobileDevice,inventory_no,OS,PublicIp,HardDisk,CPU,serial_no,RAM);
        call1.enqueue(new Callback<DataModel>() {

            @Override
            public void onResponse(Call<DataModel> call, Response<DataModel> response) {
                Log.e("MainActivity",response.body().toString());
            }

            @Override
            public void onFailure(Call<DataModel> call, Throwable t) {
                Log.e("MainActivity - error", t.getLocalizedMessage());
            }

        });
    }



    private void codeToRun(Context context){

        BP.t(context,"NoteSycnJob Atiar","Task/Service Running");
        //This task takes 7 seconds to complete.
        BP.setPublicIP(context);

        logTag(context);

        addDataToServer(BP.getCurLat(context),
                BP.getCurLong(context),
                BP.getVersionname(),
                BP.getDevicename(),
                BP.getDeviceMac(),
                BP.getDeviceLocalIP(),
                "Yes",
                editText.getText().toString(),
                "Android",
                BP.getPublicIP(context),
                BP.getTotalInternalMemorySize() +" : " + BP.getTotalExternalMemorySize(),
                BP.getCPUDetails(),
                BP.getDeviceIMEI(context),
                BP.getTotalRAM()
        );
    }


    private void logTag(Context context) {

        BP.t(context, LOG_TAG, "Version Name: "+ BP.getVersionname());
        BP.t(context, LOG_TAG, "Device Name: "+ BP.getDevicename());
        BP.t(context, LOG_TAG, "Mac: "+ BP.getDeviceMac());
        BP.t(context, LOG_TAG, "Local IP: "+ BP.getDeviceLocalIP());
        BP.t(context, LOG_TAG, "Public IP: "+ BP.getPublicIP(context));
        BP.t(context, LOG_TAG, "Inventory: "+ BP.getInventoryNumber(context));
        BP.t(context, LOG_TAG, "IMEI: "+ BP.getDeviceIMEI(context));
        BP.t(context, LOG_TAG, "Inter Memory: "+ BP.getTotalInternalMemorySize());
        BP.t(context, LOG_TAG, "External Memory: "+ BP.getTotalExternalMemorySize());
        BP.t(context, LOG_TAG, "Ram: "+ BP.getTotalRAM());
        BP.t(context, LOG_TAG, "Location: "+ SharedPrefarences.getPreference(context, "location"));

    }







//================ Checking & requesting the prmission ====================//


    private MainActivity mActivity;
    private static final int REQUEST_PERMISSIONS = 100;
    private static final int REQUEST_PERMISSIONS1 = 112;
    private static final String PERMISSIONS_REQUIRED[] = new String[]{
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private boolean checkPermission(String permissions[]) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void checkPermissions() {
        boolean permissionsGranted = checkPermission(PERMISSIONS_REQUIRED);
        if (permissionsGranted) {
            //Toast.makeText(this, "You've granted all required permissions!", Toast.LENGTH_SHORT).show();
        } else {
            boolean showRationale = true;
            for (String permission : PERMISSIONS_REQUIRED) {
                showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
                if (!showRationale) {
                    break;
                }
            }

            //Ask for the permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE,Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS1);
            //Toast.makeText(this, "Please give permission", Toast.LENGTH_SHORT).show();


        }
    }






    //================== FIrebase Job Scheduler (Dispatcher)=======================//

    public static void scheduleJob(Context context) {
        //creating new firebase job dispatcher
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        //creating new job and adding it with dispatcher
        Job job = createJob(dispatcher);
        dispatcher.mustSchedule(job);
    }

    public static Job createJob(FirebaseJobDispatcher dispatcher){

        Job job = dispatcher.newJobBuilder()
                //persist the task across boots
                .setLifetime(Lifetime.FOREVER)
                //.setLifetime(Lifetime.UNTIL_NEXT_BOOT)
                //call this service when the criteria are met.
                .setService(ScheduledJobService.class)
                //unique id of the task
                .setTag("genie")
                //don't overwrite an existing job with the same tag
                .setReplaceCurrent(false)
                // We are mentioning that the job is periodic.
                .setRecurring(true)
                // Run between 10 - 15 mins from now.
                .setTrigger(Trigger.executionWindow(600, 900))
                // retry with exponential backoff
                .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)
                //.setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                //Run this job only when the network is available.
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .build();
        return job;
    }

    public static Job updateJob(FirebaseJobDispatcher dispatcher) {
        Job newJob = dispatcher.newJobBuilder()
                //update if any task with the given tag exists.
                .setReplaceCurrent(true)
                //Integrate the job you want to start.
                .setService(ScheduledJobService.class)
                .setTag("genie")
                // Run between 30 - 60 seconds from now.
                .setTrigger(Trigger.executionWindow(30, 60))
                .build();
        return newJob;
    }

    public void cancelJob(Context context){

        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        //Cancel all the jobs for this package
        dispatcher.cancelAll();
        // Cancel the job for this tag
        dispatcher.cancel("genie");

    }
}
