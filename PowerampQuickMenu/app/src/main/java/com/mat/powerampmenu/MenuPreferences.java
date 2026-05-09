package com.mat.powerampmenu;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MenuPreferences {

    private static final String PREFS_NAME = "menu_config";
    private static final String KEY_SELECTED_IDS = "selected_ids";
    private static final String KEY_SHOW_NOW_PLAYING = "show_now_playing";
    private static final String KEY_DOUBLE_TAP_ACTION = "double_tap_action";
    private static final int MAX_ITEMS = 10;

    // Default menu items
    private static final String DEFAULT_IDS = "like,shuffle,repeat,open_eq,cat_library";

    private final SharedPreferences prefs;

    public MenuPreferences(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public List<String> getSelectedIds() {
        String raw = prefs.getString(KEY_SELECTED_IDS, DEFAULT_IDS);
        if (raw == null || raw.isEmpty()) return new ArrayList<String>();
        List<String> ids = new ArrayList<String>(Arrays.asList(raw.split(",")));
        List<String> valid = new ArrayList<String>();
        for (String id : ids) {
            if (PowerampAction.findById(id) != null) {
                valid.add(id);
            }
        }
        if (valid.size() != ids.size()) {
            setSelectedIds(valid);
        }
        return valid;
    }

    public void setSelectedIds(List<String> ids) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ids.size() && i < MAX_ITEMS; i++) {
            if (i > 0) sb.append(",");
            sb.append(ids.get(i));
        }
        prefs.edit().putString(KEY_SELECTED_IDS, sb.toString()).apply();
    }

    public List<PowerampAction> getSelectedActions() {
        List<String> ids = getSelectedIds();
        List<PowerampAction> result = new ArrayList<>();
        for (String id : ids) {
            PowerampAction action = PowerampAction.findById(id);
            if (action != null) result.add(action);
        }
        return result;
    }

    public static int getMaxItems() {
        return MAX_ITEMS;
    }

    public boolean isShowNowPlaying() {
        return prefs.getBoolean(KEY_SHOW_NOW_PLAYING, true);
    }

    public void setShowNowPlaying(boolean show) {
        prefs.edit().putBoolean(KEY_SHOW_NOW_PLAYING, show).apply();
    }

    public String getDoubleTapActionId() {
        return prefs.getString(KEY_DOUBLE_TAP_ACTION, "");
    }

    public void setDoubleTapActionId(String actionId) {
        prefs.edit().putString(KEY_DOUBLE_TAP_ACTION, actionId).apply();
    }
}
