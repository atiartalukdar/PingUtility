package bp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rettrofit.APIClient;
import rettrofit.APIInterface;
import rettrofit.DataModel;

public class MyService extends Service {
    private static APIInterface apiInterface;
    public static final String LOG_TAG = "MyService Atiar";


    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        codeToRun(getApplicationContext());
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate()
    {
        apiInterface = APIClient.getClient().create(APIInterface.class);

    }

    @Override
    public void onDestroy()
    {
        /*Intent serviceIntent = new Intent(this, MyLocationService.class);
        startService(serviceIntent);
*/
        super.onDestroy();

        Log.i("EXIT", "ondestroy!");
        Intent broadcastIntent = new Intent("info.atiar.ActivityRecognition.RestartSensor");
        sendBroadcast(broadcastIntent);

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


}
