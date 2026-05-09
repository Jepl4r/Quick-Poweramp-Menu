package com.mat.powerampmenu;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PowerampAction {

    public enum Type {
        COMMAND,
        COMMAND_EXTRA,
        SCREEN,
        CATEGORY
    }

    public static final String GROUP_PLAYBACK = "Playback";
    public static final String GROUP_NAVIGATION = "Navigation";
    public static final String GROUP_SEEK = "Seek";
    public static final String GROUP_SHUFFLE = "Shuffle";
    public static final String GROUP_REPEAT = "Repeat";
    public static final String GROUP_RATING = "Rating";
    public static final String GROUP_SCREENS = "Screens";
    public static final String GROUP_LIBRARY = "Library";

    public final String id;
    public final String displayName;
    public final String command;
    public final int iconResId;
    public final Type type;
    public final String extraKey;
    public final int extraValue;
    public final String group;

    public PowerampAction(String id, String displayName, String command, int iconResId, Type type, String group) {
        this.id = id;
        this.displayName = displayName;
        this.command = command;
        this.iconResId = iconResId;
        this.type = type;
        this.extraKey = null;
        this.extraValue = 0;
        this.group = group;
    }

    public PowerampAction(String id, String displayName, String command, int iconResId, Type type, String extraKey, int extraValue, String group) {
        this.id = id;
        this.displayName = displayName;
        this.command = command;
        this.iconResId = iconResId;
        this.type = type;
        this.extraKey = extraKey;
        this.extraValue = extraValue;
        this.group = group;
    }

    public static PowerampAction[] getAllActions() {
        return new PowerampAction[] {
            // Playback
            new PowerampAction("play_pause",   "Play / Pause",       "TOGGLE_PLAY_PAUSE",  R.drawable.ic_play_pause,    Type.COMMAND, GROUP_PLAYBACK),
            new PowerampAction("next",         "Next Track",         "NEXT",               R.drawable.ic_skip_next,     Type.COMMAND, GROUP_PLAYBACK),
            new PowerampAction("previous",     "Previous Track",     "PREVIOUS",           R.drawable.ic_skip_previous, Type.COMMAND, GROUP_PLAYBACK),
            new PowerampAction("stop",         "Stop",               "STOP",               R.drawable.ic_stop,          Type.COMMAND, GROUP_PLAYBACK),

            // Navigation
            new PowerampAction("next_in_cat",  "Next Album/Folder",  "NEXT_IN_CAT",        R.drawable.ic_next_cat,      Type.COMMAND, GROUP_NAVIGATION),
            new PowerampAction("prev_in_cat",  "Prev Album/Folder",  "PREVIOUS_IN_CAT",    R.drawable.ic_prev_cat,      Type.COMMAND, GROUP_NAVIGATION),

            // Seek
            new PowerampAction("seek_fwd",     "Seek Forward +10s",  "SEEK_JUMP_FORWARD",  R.drawable.ic_forward_10,    Type.COMMAND, GROUP_SEEK),
            new PowerampAction("seek_bwd",     "Seek Backward -10s", "SEEK_JUMP_BACKWARD", R.drawable.ic_replay_10,     Type.COMMAND, GROUP_SEEK),

            // Shuffle - toggle
            new PowerampAction("shuffle",      "Toggle Shuffle",     "SHUFFLE",            R.drawable.ic_shuffle,       Type.COMMAND, GROUP_SHUFFLE),
            // Shuffle - direct modes
            new PowerampAction("shuffle_off",       "Shuffle Off",                "SHUFFLE", R.drawable.ic_shuffle,  Type.COMMAND_EXTRA, "shuffle", 0, GROUP_SHUFFLE),
            new PowerampAction("shuffle_all",       "Shuffle All",                "SHUFFLE", R.drawable.ic_shuffle,  Type.COMMAND_EXTRA, "shuffle", 1, GROUP_SHUFFLE),
            new PowerampAction("shuffle_songs",     "Shuffle Songs",              "SHUFFLE", R.drawable.ic_shuffle,  Type.COMMAND_EXTRA, "shuffle", 2, GROUP_SHUFFLE),
            new PowerampAction("shuffle_cats",      "Shuffle Categories",         "SHUFFLE", R.drawable.ic_shuffle,  Type.COMMAND_EXTRA, "shuffle", 3, GROUP_SHUFFLE),
            new PowerampAction("shuffle_songs_cats","Shuffle Songs & Categories", "SHUFFLE", R.drawable.ic_shuffle,  Type.COMMAND_EXTRA, "shuffle", 4, GROUP_SHUFFLE),

            // Repeat - toggle
            new PowerampAction("repeat",       "Toggle Repeat",      "REPEAT",             R.drawable.ic_repeat,        Type.COMMAND, GROUP_REPEAT),
            // Repeat - direct modes
            new PowerampAction("repeat_off",      "Repeat Off",          "REPEAT", R.drawable.ic_repeat,  Type.COMMAND_EXTRA, "repeat", 0, GROUP_REPEAT),
            new PowerampAction("repeat_list",     "Repeat List",         "REPEAT", R.drawable.ic_repeat,  Type.COMMAND_EXTRA, "repeat", 1, GROUP_REPEAT),
            new PowerampAction("repeat_advance",  "Advance List",        "REPEAT", R.drawable.ic_repeat,  Type.COMMAND_EXTRA, "repeat", 2, GROUP_REPEAT),
            new PowerampAction("repeat_song",     "Repeat Song",         "REPEAT", R.drawable.ic_repeat,  Type.COMMAND_EXTRA, "repeat", 3, GROUP_REPEAT),

            // Rating
            new PowerampAction("like",         "Like / Dislike",     "LIKE",               R.drawable.ic_favorite,      Type.COMMAND, GROUP_RATING),

            // Screens
            new PowerampAction("open_eq",      "Equalizer",          "com.maxmpz.audioplayer.ACTION_OPEN_EQ",      R.drawable.ic_equalizer, Type.SCREEN, GROUP_SCREENS),
            new PowerampAction("open_search",  "Search",             "com.maxmpz.audioplayer.ACTION_OPEN_SEARCH",  R.drawable.ic_search,    Type.SCREEN, GROUP_SCREENS),
            new PowerampAction("now_playing",  "Now Playing",        "com.maxmpz.audioplayer.ACTION_SHOW_CURRENT", R.drawable.ic_music,     Type.SCREEN, GROUP_SCREENS),

            // Library categories
            new PowerampAction("cat_library",      "All Tracks",        "content://com.maxmpz.audioplayer.data/files",           R.drawable.ic_library,   Type.CATEGORY, GROUP_LIBRARY),
            new PowerampAction("cat_folders",      "Folders",           "content://com.maxmpz.audioplayer.data/folders",         R.drawable.ic_folder,    Type.CATEGORY, GROUP_LIBRARY),
            new PowerampAction("cat_albums",       "Albums",            "content://com.maxmpz.audioplayer.data/albums",          R.drawable.ic_album,     Type.CATEGORY, GROUP_LIBRARY),
            new PowerampAction("cat_artists",      "Artists",           "content://com.maxmpz.audioplayer.data/artists",         R.drawable.ic_artist,    Type.CATEGORY, GROUP_LIBRARY),
            new PowerampAction("cat_album_artists","Album Artists",     "content://com.maxmpz.audioplayer.data/album_artists",   R.drawable.ic_artist,    Type.CATEGORY, GROUP_LIBRARY),
            new PowerampAction("cat_composers",    "Composers",         "content://com.maxmpz.audioplayer.data/composers",       R.drawable.ic_artist,    Type.CATEGORY, GROUP_LIBRARY),
            new PowerampAction("cat_genres",       "Genres",            "content://com.maxmpz.audioplayer.data/genres",          R.drawable.ic_genre,     Type.CATEGORY, GROUP_LIBRARY),
            new PowerampAction("cat_years",        "Years",             "content://com.maxmpz.audioplayer.data/years",           R.drawable.ic_recent,    Type.CATEGORY, GROUP_LIBRARY),
            new PowerampAction("cat_playlists",    "Playlists",         "content://com.maxmpz.audioplayer.data/playlists",       R.drawable.ic_playlist,  Type.CATEGORY, GROUP_LIBRARY),
            new PowerampAction("cat_queue",        "Queue",             "content://com.maxmpz.audioplayer.data/queue",           R.drawable.ic_queue,     Type.CATEGORY, GROUP_LIBRARY),
            new PowerampAction("cat_top_rated",    "Top Rated",         "content://com.maxmpz.audioplayer.data/top_rated",       R.drawable.ic_star,      Type.CATEGORY, GROUP_LIBRARY),
            new PowerampAction("cat_recently",     "Recently Added",    "content://com.maxmpz.audioplayer.data/recently_added",  R.drawable.ic_recent,    Type.CATEGORY, GROUP_LIBRARY),
            new PowerampAction("cat_most_played",  "Most Played",       "content://com.maxmpz.audioplayer.data/most_played",     R.drawable.ic_trending,  Type.CATEGORY, GROUP_LIBRARY),
            new PowerampAction("cat_recently_played","Recently Played", "content://com.maxmpz.audioplayer.data/recently_played", R.drawable.ic_recent,    Type.CATEGORY, GROUP_LIBRARY),
            new PowerampAction("cat_long",         "Long Tracks",       "content://com.maxmpz.audioplayer.data/long",            R.drawable.ic_music,     Type.CATEGORY, GROUP_LIBRARY),
            new PowerampAction("cat_streams",      "Streams",           "content://com.maxmpz.audioplayer.data/streams",         R.drawable.ic_stream,    Type.CATEGORY, GROUP_LIBRARY),
        };
    }

    /**
     * Returns all actions grouped by category, preserving insertion order.
     */
    public static Map<String, List<PowerampAction>> getGroupedActions() {
        Map<String, List<PowerampAction>> grouped = new LinkedHashMap<String, List<PowerampAction>>();
        for (PowerampAction action : getAllActions()) {
            List<PowerampAction> list = grouped.get(action.group);
            if (list == null) {
                list = new ArrayList<PowerampAction>();
                grouped.put(action.group, list);
            }
            list.add(action);
        }
        return grouped;
    }

    public static PowerampAction findById(String id) {
        for (PowerampAction action : getAllActions()) {
            if (action.id.equals(id)) return action;
        }
        return null;
    }
}
