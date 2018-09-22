package rettrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient {

       //public static final String BASE_URL = "http://110.36.237.219/inventory/inventory.php";
       public static final String BASE_URL = "http://110.36.237.219/";
       private static Retrofit retrofit = null;

       public static Retrofit getClient() {
           if (retrofit == null) {
               retrofit = new Retrofit.Builder()
                       .baseUrl(BASE_URL)
                       .addConverterFactory(GsonConverterFactory.create())
                       .build();
           }
           return retrofit;
       }
   }


