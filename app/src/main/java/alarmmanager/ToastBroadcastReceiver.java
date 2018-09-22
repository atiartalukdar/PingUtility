package alarmmanager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.evernote.android.job.JobManager;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

import firebaseDispatcher.ScheduledJobService;
import mobitsolutions.com.pingutility.NoteSyncJob;

public class ToastBroadcastReceiver extends BroadcastReceiver {
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        JobManager.create(context);

        Intent serviceIntent= new Intent(context, BackgroundService.class);
        context.startService(serviceIntent);

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