package bp;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Atiar on 5/23/18.
 */

public class SharedPrefarences {

    private static final String PREFS_NAME = "preferenceName";


    /*****************************//* Strat shared preferences *//******************************/


    public static boolean setPreference(Context context, String key, String value) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        return editor.commit();
    }

    public static String getPreference(Context context, String key) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return settings.getString(key, "None");
    }

    public static boolean setPreferenceInt(Context context, String key, int value) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        return editor.commit();
    }

    public static int getPreferenceInt(Context context, String key) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return settings.getInt(key, 0);
    }
    public static int getPreferenceIntDrawable(Context context, String key) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return settings.getInt(key,0);
    }

    /*****************************//* End shared preferences *//******************************/


}
