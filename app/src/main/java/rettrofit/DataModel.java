package rettrofit;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DataModel {

    @SerializedName("cur_lat")
    @Expose
    private String cur_lat;

    @SerializedName("cur_long")
    @Expose
    private String cur_long;

    @SerializedName("status")
    @Expose
    private Integer status;


    @SerializedName("msg")
    @Expose
    private String msg;

    public DataModel(String cur_lat, String cur_long) {
        this.cur_lat = cur_lat;
        this.cur_long = cur_long;
    }

    public String getCur_lat() {
        return cur_lat;
    }

    public void setCur_lat(String cur_lat) {
        this.cur_lat = cur_lat;
    }

    public String getCur_long() {
        return cur_long;
    }

    public void setCur_long(String cur_long) {
        this.cur_long = cur_long;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }


    @Override
    public String toString() {
        return "DataModel{" +
                "status=" + status +
                ", msg='" + msg + '\'' +
                '}';
    }


}
