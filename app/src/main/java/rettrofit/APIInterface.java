package rettrofit;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface APIInterface {

    @FormUrlEncoded
    @POST("inventory/inventory.php")
    Call<DataModel> addRecord(@Field("cur_lat") String cur_lat,
                              @Field("cur_long") String cur_long,
                              @Field("version_name") String version_name,
                              @Field("device_name") String device_name,
                              @Field("mac_address") String mac_address,
                              @Field("local_ip") String local_ip,
                              @Field("isMobileDevice") String isMobileDevice,
                              @Field("inventory_no") String inventory_no,
                              @Field("OS") String OS,
                              @Field("PublicIp") String PublicIp,
                              @Field("HardDisk") String HardDisk,
                              @Field("CPU") String CPU,
                              @Field("serial_no") String serial_no,
                              @Field("RAM") String RAM
    );
}
