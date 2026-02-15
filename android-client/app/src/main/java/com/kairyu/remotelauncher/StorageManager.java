package com.kairyu.remotelauncher;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class StorageManager {
    private static final String PREFS_NAME = "RemoteLauncherPrefs";
    private static final String KEY_CUSTOM_APPS = "custom_apps";
    private static final String KEY_COMPUTERS = "saved_computers";

    public static void saveComputer(Context context, SavedComputer computer) {
        List<SavedComputer> list = getSavedComputers(context);
        list.add(computer);

        try {
            JSONArray array = new JSONArray();
            for (SavedComputer item : list) {
                JSONObject obj = new JSONObject();
                obj.put("name", item.name);
                obj.put("ip", item.ip);
                array.put(obj);
            }
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit().putString(KEY_COMPUTERS, array.toString()).apply();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static List<SavedComputer> getSavedComputers(Context context) {
        List<SavedComputer> list = new ArrayList<>();
        String json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_COMPUTERS, "[]");
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                list.add(new SavedComputer(obj.getString("name"), obj.getString("ip")));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public static void saveCustomApp(Context context, AppItem app) {
        List<AppItem> currentList = getCustomApps(context);
        currentList.add(app);

        // Convert to JSON and Save
        try {
            JSONArray array = new JSONArray();
            for (AppItem item : currentList) {
                JSONObject obj = new JSONObject();
                obj.put("name", item.name);
                obj.put("path", item.path);
                array.put(obj);
            }

            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString(KEY_CUSTOM_APPS, array.toString()).apply();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static List<AppItem> getCustomApps(Context context) {
        List<AppItem> list = new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String jsonString = prefs.getString(KEY_CUSTOM_APPS, "[]");

        try {
            JSONArray array = new JSONArray(jsonString);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                list.add(new AppItem(obj.getString("name"), obj.getString("path"), true));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
}