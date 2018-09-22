package mobitsolutions.com.pingutility;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import bp.BP;
import bp.SharedPrefarences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rettrofit.APIClient;
import rettrofit.APIInterface;
import rettrofit.DataModel;

public class NoteSyncJob extends Job {

    private static boolean status = false;
    public static final String TAG = "job_note_sync";
    public static final String LOG_TAG = "job_note_sync";
    private static APIInterface apiInterface;
    private boolean notification = false;

    @Override
    @NonNull
    protected Result onRunJob(@NonNull Params params) {
        Log.e(TAG,"Running");
        apiInterface = APIClient.getClient().create(APIInterface.class);
        Intent intent = new Intent(getContext(), SeparateProcessService.class);
        getContext().startService(intent);

    if (notification){
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, new Intent(getContext(), MainActivity.class), 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(TAG, "Job Demo", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Job demo job");
            getContext().getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(getContext(), TAG)
                .setContentTitle("ID " + params.getId())
                .setContentText("Job ran, exact " + params.isExact() + " , periodic " + params.isPeriodic() + ", transient " + params.isTransient())
                .setAutoCancel(true)
                .setChannelId(TAG)
                .setSound(null)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setShowWhen(true)
                .setColor(Color.GREEN)
                .setLocalOnly(true)
                .build();

        NotificationManagerCompat.from(getContext()).notify(new Random().nextInt(), notification);

    }

        try {
            codeToRun();

        }catch (Exception e){
            e.printStackTrace();
        }

        return Result.RESCHEDULE;
    }

    public static void scheduleJob() {
        Set<JobRequest> jobRequests = JobManager.instance().getAllJobRequestsForTag(NoteSyncJob.TAG);
        if (!jobRequests.isEmpty()) {
            return;
        }
        new JobRequest.Builder(NoteSyncJob.TAG)
                .setPeriodic(TimeUnit.MINUTES.toMillis(15), TimeUnit.MINUTES.toMillis(10))
                .setUpdateCurrent(true) // calls cancelAllForTag(NoteSyncJob.TAG) for you
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                .setRequirementsEnforced(true)
                .build()
                .schedule();
    }













    public static void addDataToServer(String cur_lat, String cur_long,String version_name,String device_name,String mac_address,
                                       String local_ip, String isMobileDevice,String inventory_no, String OS,String PublicIp,
                                       String HardDisk,String CPU,String serial_no,String RAM) {
        //final DataModel dataModel = new DataModel(cur_lat, cur_long);

        Call<DataModel> call1 = apiInterface.addRecord(cur_lat,cur_long,version_name,device_name,mac_address,local_ip,isMobileDevice,inventory_no,OS,PublicIp,HardDisk,CPU,serial_no,RAM);
        call1.enqueue(new Callback<DataModel>() {

            @Override
            public void onResponse(Call<DataModel> call, Response<DataModel> response) {
                Log.e("MainActivity",response.body().toString());
                if (response.body().getStatus()==1){
                    status=true;
                }else {
                    status = false;
                }
            }

            @Override
            public void onFailure(Call<DataModel> call, Throwable t) {
                Log.e("MainActivity - error", t.getLocalizedMessage());
            }

        });
    }


    private void logTag() {

        BP.t(getContext(), LOG_TAG, "Version Name: "+ BP.getVersionname());
        BP.t(getContext(), LOG_TAG, "Device Name: "+ BP.getDevicename());
        BP.t(getContext(), LOG_TAG, "Mac: "+ BP.getDeviceMac());
        BP.t(getContext(), LOG_TAG, "Local IP: "+ BP.getDeviceLocalIP());
        BP.t(getContext(), LOG_TAG, "Public IP: "+ BP.getPublicIP(getContext()));
        BP.t(getContext(), LOG_TAG, "Inventory: "+ BP.getInventoryNumber(getContext()));
        BP.t(getContext(), LOG_TAG, "IMEI: "+ BP.getDeviceIMEI(getContext()));
        BP.t(getContext(), LOG_TAG, "Inter Memory: "+ BP.getTotalInternalMemorySize());
        BP.t(getContext(), LOG_TAG, "External Memory: "+ BP.getTotalExternalMemorySize());
        BP.t(getContext(), LOG_TAG, "Ram: "+ BP.getTotalRAM());
        BP.t(getContext(), LOG_TAG, "Location: "+ SharedPrefarences.getPreference(getContext(), "location"));

    }

    private void codeToRun(){

        BP.t(getContext(),"NoteSycnJob Atiar","Task/Service Running");
        //This task takes 7 seconds to complete.
        BP.setPublicIP(getContext());

        logTag();

        addDataToServer(BP.getCurLat(getContext()),
                BP.getCurLong(getContext()),
                BP.getVersionname(),
                BP.getDevicename(),
                BP.getDeviceMac(),
                BP.getDeviceLocalIP(),
                "Yes",
                BP.getInventoryNumber(getContext()),
                "Android",
                BP.getPublicIP(getContext()),
                BP.getTotalInternalMemorySize() +" : " + BP.getTotalExternalMemorySize(),
                BP.getCPUDetails(),
                BP.getDeviceIMEI(getContext()),
                BP.getTotalRAM()
        );
    }

}