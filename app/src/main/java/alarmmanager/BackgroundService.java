package alarmmanager;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import bp.BP;
import bp.SharedPrefarences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rettrofit.APIClient;
import rettrofit.APIInterface;
import rettrofit.DataModel;


public class BackgroundService extends IntentService {
    public static final String ACTION="alarmmanager.ResponseBroadcastReceiver";
    public static final String LOG_TAG = "Background Service Atiar";
    private static APIInterface apiInterface;

    // Must create a default constructor
    public BackgroundService() {
        // Used to name the worker thread, important only for debugging.
        super("backgroundService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        apiInterface = APIClient.getClient().create(APIInterface.class);

        // This describes what will happen when service is triggered
        Log.e("backgroundService","Service running");

        //create a broadcast to send the toast message
        Intent toastIntent;
        toastIntent = new Intent(String.valueOf(ResponseBroadcastReceiver.class));
        toastIntent.putExtra("toastMessage","I'm running after ever 15 minutes");
        sendBroadcast(toastIntent);

        codeToRun(this);

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
                BP.getInventoryNumber(context),
                "Android",
                BP.getPublicIP(context),
                BP.getTotalInternalMemorySize() +" : " + BP.getTotalExternalMemorySize(),
                BP.getCPUDetails(),
                BP.getDeviceIMEI(context),
                BP.getTotalRAM()
        );
    }


    private void logTag(Context context) {

        BP.t(context, LOG_TAG, "Version Name: "+BP.getVersionname());
        BP.t(context, LOG_TAG, "Device Name: "+BP.getDevicename());
        BP.t(context, LOG_TAG, "Mac: "+BP.getDeviceMac());
        BP.t(context, LOG_TAG, "Local IP: "+BP.getDeviceLocalIP());
        BP.t(context, LOG_TAG, "Public IP: "+BP.getPublicIP(context));
        BP.t(context, LOG_TAG, "Inventory: "+BP.getInventoryNumber(context));
        BP.t(context, LOG_TAG, "IMEI: "+BP.getDeviceIMEI(context));
        BP.t(context, LOG_TAG, "Inter Memory: "+BP.getTotalInternalMemorySize());
        BP.t(context, LOG_TAG, "External Memory: "+BP.getTotalExternalMemorySize());
        BP.t(context, LOG_TAG, "Ram: "+BP.getTotalRAM());
        BP.t(context, LOG_TAG, "Location: "+ SharedPrefarences.getPreference(context, "location"));

    }



}
