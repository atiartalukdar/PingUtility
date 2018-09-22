package bp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mobitsolutions.com.pingutility.BuildConfig;


public class BP {
    private static final String LOG_TAG = "BP - Atiar";

    public static String getAndroidVersionName() {
        StringBuilder builder = new StringBuilder();
        builder.append("android : ").append(Build.VERSION.RELEASE);

        Field[] fields = Build.VERSION_CODES.class.getFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            int fieldValue = -1;

            try {
                fieldValue = field.getInt(new Object());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            if (fieldValue == Build.VERSION.SDK_INT) {
                builder.append(" : ").append(fieldName).append(" : ");
                builder.append("sdk= ").append(fieldValue);
            }
        }

        Log.d(LOG_TAG, "OS: " + builder.toString());
        return builder.toString();
    }

    /*    cur_lat:
    cur_long:
    version_name:versionname
    device_name:device name
    mac_address:mac
    local_ip:localip
    isMobileDevice:yes
    inventory_no:invetorynumber
    serial_no:serial(udid or device unique id)
    OS:os
    PublicIp:publicip
    HardDisk:hard - Done
    CPU:cpu  - DOne
    RAM:ram

       String manufacturer = Build.MANUFACTURER;
   String model = Build.MODEL;
   int version = Build.VERSION.SDK_INT;
   String versionRelease = Build.VERSION.RELEASE;

Log.e("MyActivity", "manufacturer " + manufacturer
            + " \n model " + model
            + " \n version " + version
            + " \n versionRelease " + versionRelease
    );

    */

    public static String getCurLat(Context context){
        return SharedPrefarences.getPreference(context,"locationLat");
    }

    public static String getCurLong(Context context){
        return SharedPrefarences.getPreference(context,"locationLang");
    }

    public static String getVersionname() {
        return "Version: " + Build.VERSION.SDK_INT;
    }

    public static String getDevicename() {
        return "Android: " + Build.VERSION.RELEASE +
                "\nManufacturer: " + Build.MANUFACTURER+
                "Model: "+Build.MODEL;

    }

    public static String getDeviceMac() {

        Utils.getMACAddress("wlan0");
        Utils.getMACAddress("eth0");
        Utils.getIPAddress(true); // IPv4
        Utils.getIPAddress(false); // IPv6

        return Utils.getMACAddress("wlan0");
    }

    public static String getDeviceLocalIP() {

        Utils.getMACAddress("wlan0");
        Utils.getMACAddress("eth0");
        Utils.getIPAddress(true); // IPv4
        Utils.getIPAddress(false); // IPv6

        return Utils.getIPAddress(true); // IPv4
    }

    public static String getInventoryNumber(Context context){
        return SharedPrefarences.getPreference(context,"inventory");
    }

    public static void setInventoryNumber(Context context,String Inventory){
        SharedPrefarences.setPreference(context,"inventory",Inventory);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String getDeviceSerial(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return "Null";
        }
        return Build.getSerial();

    }

    public static String getDeviceIMEI(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            return "READ_PHONE_STATE Permission Required";
        }

        TelephonyManager tManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String uid = tManager.getDeviceId();

        return uid;

    }


    public static String getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long BlockSize = stat.getBlockSize();
        long TotalBlocks = stat.getBlockCount();
        return formatSize(TotalBlocks * BlockSize);
    }


    public static String getTotalExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.
                    getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long BlockSize = stat.getBlockSize();
            long TotalBlocks = stat.getBlockCount();
            return formatSize(TotalBlocks * BlockSize);
        } else {
            return "No External Storage";
        }
    }


    public static String getCPUDetails(){

        /*
         *Created By Atiar Talukdar
         * 01/01/2018
         * contact@atiar.info
         */

        ProcessBuilder processBuilder;
        String cpuDetails = "";
        String[] DATA = {"/system/bin/cat", "/proc/cpuinfo"};
        InputStream is;
        Process process ;
        byte[] bArray ;
        bArray = new byte[1024];

        try{
            processBuilder = new ProcessBuilder(DATA);

            process = processBuilder.start();

            is = process.getInputStream();

            while(is.read(bArray) != -1){
                cpuDetails = cpuDetails + new String(bArray);   //Stroing all the details in cpuDetails
            }
            is.close();

        } catch(IOException ex){
            ex.printStackTrace();
        }

        return cpuDetails;
    }


    public static String getTotalRAM() {

        RandomAccessFile reader = null;
        String load = null;
        DecimalFormat twoDecimalForm = new DecimalFormat("#.##");
        double totRam = 0;
        String lastValue = "";
        try {
            reader = new RandomAccessFile("/proc/meminfo", "r");
            load = reader.readLine();

            // Get the Number value from the string
            Pattern p = Pattern.compile("(\\d+)");
            Matcher m = p.matcher(load);
            String value = "";
            while (m.find()) {
                value = m.group(1);
                // System.out.println("Ram : " + value);
            }
            reader.close();

            totRam = Double.parseDouble(value);
            // totRam = totRam / 1024;

            double mb = totRam / 1024.0;
            double gb = totRam / 1048576.0;
            double tb = totRam / 1073741824.0;

            if (tb > 1) {
                lastValue = twoDecimalForm.format(tb).concat(" TB");
            } else if (gb > 1) {
                lastValue = twoDecimalForm.format(gb).concat(" GB");
            } else if (mb > 1) {
                lastValue = twoDecimalForm.format(mb).concat(" MB");
            } else {
                lastValue = twoDecimalForm.format(totRam).concat(" KB");
            }



        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            // Streams.close(reader);
        }

        return lastValue;
    }


    public static void setPublicIP(final Context context){
        Ion.with(context)
                .load("https://api.ipify.org/?format=json")
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        // do stuff with the result or error
                        String ip = "No IP";

                        try {
                            ip = result.toString();
                        }catch (Exception e1){
                            e.printStackTrace();
                        }

                        try {
                            if (ip.equals(null) || ip.length() <=10){
                                BP.t(context,LOG_TAG,"No IP Found in setPublicIP - "+ ip);
                            }else {
                                JSONObject obj = new JSONObject(ip);

                                SharedPrefarences.setPreference(context,"publicIP",obj.getString("ip"));
                                Log.e(LOG_TAG,obj.getString("ip"));
                            }

                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }

                    }
                });

    }

    public static String getPublicIP(Context context){
        return SharedPrefarences.getPreference(context,"publicIP");
    }


    public static String getNetworkDomain(){
        String netDomain = "";

        return netDomain;
    }

    public static String getWorkGroup(){
        String workGroup = "";

        return workGroup;
    }


    //To print log and toast.
    public static void t(Context context, String Tag,String message){
        //Toast.makeText(context,"from - "+LOG_TAG + " - " +message,Toast.LENGTH_LONG).show();
        Log.e(Tag,message);
    }


    public static boolean externalMemoryAvailable() {
        return Environment.
                getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    public static String formatSize(long size) {
        String suffixSize = null;

        if (size >= 1024) {
            suffixSize = "KB";
            size /= 1024;
            if (size >= 1024) {
                suffixSize = "MB";
                size /= 1024;
            }
        }

        StringBuilder BufferSize = new StringBuilder(
                Long.toString(size));

        int commaOffset = BufferSize.length() - 3;
        while (commaOffset > 0) {
            BufferSize.insert(commaOffset, ',');
            commaOffset -= 3;
        }

        if (suffixSize != null) BufferSize.append(suffixSize);
        return BufferSize.toString();
    }



}
