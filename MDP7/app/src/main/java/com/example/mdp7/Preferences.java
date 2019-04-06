package com.example.mdp7;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences{

    private static SharedPreferences sharedpreferences;

    public static String readPreference(Context context, int key){
        sharedpreferences = context.getSharedPreferences("PREF",Context.MODE_PRIVATE);
        return sharedpreferences.getString(context.getString(key),"");
    }

    //if no value is found for preferences, use default values
    public static String readPreference(Context context, int key, int defaultKey){
        sharedpreferences = context.getSharedPreferences("PREF",Context.MODE_PRIVATE);
        String defaultValue = context.getResources().getString(defaultKey);
        return sharedpreferences.getString(context.getString(key),defaultValue);
    }

    public static void savePreference(Context context, int key, String value){
        sharedpreferences = context.getSharedPreferences("PREF",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(context.getString(key),value);
        editor.apply();
    }
}
