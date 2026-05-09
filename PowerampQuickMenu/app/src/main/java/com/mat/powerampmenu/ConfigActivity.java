package com.mat.powerampmenu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfigActivity extends Activity {

    private MenuPreferences menuPrefs;
    private List<String> selectedIds;
    private SelectedAdapter adapter;
    private TextView counterText;
    private Switch nowPlayingSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window w = getWindow();
        w.setStatusBarColor(Color.parseColor("#FF121212"));
        w.setNavigationBarColor(Color.parseColor("#FF121212"));

        setContentView(R.layout.activity_config);

        menuPrefs = new MenuPreferences(this);
        selectedIds = new ArrayList<String>(menuPrefs.getSelectedIds());

        ListView listView = findViewById(R.id.selected_list);
        counterText = findViewById(R.id.counter_text);
        ImageButton addButton = findViewById(R.id.btn_add);

        adapter = new SelectedAdapter();
        listView.setAdapter(adapter);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDialog();
            }
        });

        // Now Playing toggle
        nowPlayingSwitch = findViewById(R.id.switch_now_playing);
        nowPlayingSwitch.setChecked(menuPrefs.isShowNowPlaying());
        nowPlayingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                menuPrefs.setShowNowPlaying(isChecked);
                if (isChecked && !isNotificationAccessEnabled()) {
                    promptNotificationAccess();
                }
            }
        });

        updateCounter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // If user comes back from notification settings, update toggle state
        if (nowPlayingSwitch != null && nowPlayingSwitch.isChecked() && !isNotificationAccessEnabled()) {
            // Still not enabled — keep toggle on, user may still enable it
        }
    }

    private void updateCounter() {
        counterText.setText(selectedIds.size() + " / " + MenuPreferences.getMaxItems());
    }

    private void save() {
        menuPrefs.setSelectedIds(selectedIds);
        updateCounter();
    }

    // --- Notification access ---

    private boolean isNotificationAccessEnabled() {
        ComponentName cn = new ComponentName(this, NLService.class);
        String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        return flat != null && flat.contains(cn.flattenToString());
    }

    private void promptNotificationAccess() {
        new AlertDialog.Builder(this, R.style.DialogTheme)
            .setTitle("Notification Access Required")
            .setMessage("To show track details and album art in the quick menu, the app needs Notification Access.\n\nThis is only used to read the current song from Poweramp.")
            .setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                    startActivity(intent);
                }
            })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    nowPlayingSwitch.setChecked(false);
                    menuPrefs.setShowNowPlaying(false);
                }
            })
            .show();
    }

    // --- Add Action dialog ---

    private void showAddDialog() {
        if (selectedIds.size() >= MenuPreferences.getMaxItems()) {
            new AlertDialog.Builder(this, R.style.DialogTheme)
                .setMessage("Maximum " + MenuPreferences.getMaxItems() + " items reached.\nRemove one first.")
                .setPositiveButton("OK", null)
                .show();
            return;
        }

        // Build grouped list of available (not yet selected) actions
        Map<String, List<PowerampAction>> grouped = PowerampAction.getGroupedActions();
        Map<String, List<PowerampAction>> available = new LinkedHashMap<String, List<PowerampAction>>();
        for (Map.Entry<String, List<PowerampAction>> entry : grouped.entrySet()) {
            List<PowerampAction> filtered = new ArrayList<PowerampAction>();
            for (PowerampAction a : entry.getValue()) {
                if (!selectedIds.contains(a.id)) {
                    filtered.add(a);
                }
            }
            if (!filtered.isEmpty()) {
                available.put(entry.getKey(), filtered);
            }
        }

        if (available.isEmpty()) {
            new AlertDialog.Builder(this, R.style.DialogTheme)
                .setMessage("All actions are already in the menu.")
                .setPositiveButton("OK", null)
                .show();
            return;
        }

        // Build scrollable grouped layout
        ScrollView scrollView = new ScrollView(this);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(0, dp(8), 0, dp(8));

        for (Map.Entry<String, List<PowerampAction>> entry : available.entrySet()) {
            // Category header
            TextView header = new TextView(this);
            header.setText(entry.getKey().toUpperCase());
            header.setTextSize(11);
            header.setTextColor(0x80FFFFFF);
            header.setTypeface(null, Typeface.BOLD);
            header.setLetterSpacing(0.1f);
            LinearLayout.LayoutParams hLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
            hLp.setMargins(dp(24), dp(10), dp(24), dp(2));
            header.setLayoutParams(hLp);
            container.addView(header);

            // Action items in this group
            for (final PowerampAction action : entry.getValue()) {
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setBackgroundResource(R.drawable.menu_item_ripple);
                row.setClickable(true);
                row.setFocusable(true);

                LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(48));
                row.setLayoutParams(rowLp);
                row.setPadding(dp(24), 0, dp(24), 0);

                ImageView icon = new ImageView(this);
                LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(dp(22), dp(22));
                icon.setLayoutParams(iconLp);
                icon.setImageResource(action.iconResId);
                row.addView(icon);

                TextView label = new TextView(this);
                LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
                labelLp.setMarginStart(dp(14));
                label.setLayoutParams(labelLp);
                label.setText(action.displayName);
                label.setTextSize(15);
                label.setTextColor(0xFFFFFFFF);
                row.addView(label);

                row.setTag(action);
                container.addView(row);
            }
        }

        scrollView.addView(container);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        builder.setTitle("Add Action");
        builder.setView(scrollView);
        builder.setNegativeButton("Cancel", null);

        final AlertDialog dialog = builder.create();
        dialog.show();

        // Make dialog width match popup menu width (85% of screen)
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);
            window.setAttributes(lp);
        }

        // Set click listeners on action rows
        setRowClickListeners(container, dialog);
    }

    private void setRowClickListeners(ViewGroup parent, final AlertDialog dialog) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            Object tag = child.getTag();
            if (tag instanceof PowerampAction) {
                final PowerampAction action = (PowerampAction) tag;
                child.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedIds.add(action.id);
                        adapter.notifyDataSetChanged();
                        save();
                        dialog.dismiss();
                    }
                });
            }
        }
    }

    // --- Main list ---

    private class SelectedAdapter extends BaseAdapter {
        @Override
        public int getCount() { return selectedIds.size(); }

        @Override
        public String getItem(int pos) { return selectedIds.get(pos); }

        @Override
        public long getItemId(int pos) { return pos; }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(ConfigActivity.this)
                    .inflate(R.layout.item_selected_action, parent, false);
            }

            String id = selectedIds.get(position);
            PowerampAction action = PowerampAction.findById(id);
            if (action == null) return convertView;

            ImageView icon = convertView.findViewById(R.id.action_icon);
            TextView name = convertView.findViewById(R.id.action_name);
            ImageButton btnUp = convertView.findViewById(R.id.btn_up);
            ImageButton btnDown = convertView.findViewById(R.id.btn_down);
            ImageButton btnRemove = convertView.findViewById(R.id.btn_remove);

            icon.setImageResource(action.iconResId);
            name.setText(action.displayName);

            btnUp.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);
            btnDown.setVisibility(position == getCount() - 1 ? View.INVISIBLE : View.VISIBLE);

            btnUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (position > 0) {
                        Collections.swap(selectedIds, position, position - 1);
                        notifyDataSetChanged();
                        save();
                    }
                }
            });

            btnDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (position < getCount() - 1) {
                        Collections.swap(selectedIds, position, position + 1);
                        notifyDataSetChanged();
                        save();
                    }
                }
            });

            btnRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedIds.remove(position);
                    notifyDataSetChanged();
                    save();
                }
            });

            return convertView;
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
