package screenonoff;

import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.evernote.android.job.JobManager;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

import alarmmanager.ToastBroadcastReceiver;
import firebaseDispatcher.ScheduledJobService;
import mobitsolutions.com.pingutility.NoteSyncJob;


/**
 * Created by Atiar on 2/8/18.
 */

public class ScreenReceiver extends BroadcastReceiver {

    MediaPlayer mp;

    public static boolean wasScreenOn = true;

    Context mContext = null;
    Vibrator vibe;

    Long Timer = System.currentTimeMillis();
    Long Threshold = 1000L;
    String Screen = "null";

    int screenTimeOut;

    //for new onReceive
    Context cntx = null;
    Long a, seconds_screenoff = 0L, OLD_TIME = 0L, seconds_screenon = 0L, actual_diff = 0L;
    Boolean OFF_SCREEN = true, ON_SCREEN = true, sent_msg = true;




    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive(final Context context, final Intent intent) {
        KeyguardManager myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        JobManager.create(context);
        if (Build.VERSION.SDK_INT >=6){
            //firebase job scheduler / dispatcher
            scheduleJob(context);
            NoteSyncJob.scheduleJob();

        }else{

            //evernote job scheduler
            NoteSyncJob.scheduleJob();

            //===========Alarm Manager ===========
            Intent toastIntent= new Intent(context, ToastBroadcastReceiver.class);
            PendingIntent toastAlarmIntent = PendingIntent.getBroadcast(context, 0, toastIntent,PendingIntent.FLAG_UPDATE_CURRENT);
            long startTime=System.currentTimeMillis(); //alarm starts immediately
            AlarmManager backupAlarmMgr=(AlarmManager)context.getSystemService(context.ALARM_SERVICE);
            backupAlarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,startTime,AlarmManager.INTERVAL_FIFTEEN_MINUTES,toastAlarmIntent); // alarm will repeat after every 15 minutes

            try {
                backupAlarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, 15000,toastAlarmIntent); // alarm will repeat after every 15 minutes
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }


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



}


