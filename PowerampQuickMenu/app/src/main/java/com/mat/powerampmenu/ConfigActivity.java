package com.mat.powerampmenu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConfigActivity extends Activity {

    private MenuPreferences menuPrefs;
    private List<String> selectedIds;
    private SelectedAdapter adapter;
    private TextView counterText;

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

        updateCounter();
    }

    private void updateCounter() {
        counterText.setText(selectedIds.size() + " / " + MenuPreferences.getMaxItems());
    }

    private void save() {
        menuPrefs.setSelectedIds(selectedIds);
        updateCounter();
    }

    private void showAddDialog() {
        if (selectedIds.size() >= MenuPreferences.getMaxItems()) {
            new AlertDialog.Builder(this, R.style.DialogTheme)
                .setMessage("Maximum " + MenuPreferences.getMaxItems() + " items reached.\nRemove one first.")
                .setPositiveButton("OK", null)
                .show();
            return;
        }

        PowerampAction[] all = PowerampAction.getAllActions();
        final List<PowerampAction> available = new ArrayList<PowerampAction>();
        for (PowerampAction a : all) {
            if (!selectedIds.contains(a.id)) {
                available.add(a);
            }
        }

        if (available.isEmpty()) {
            new AlertDialog.Builder(this, R.style.DialogTheme)
                .setMessage("All actions are already in the menu.")
                .setPositiveButton("OK", null)
                .show();
            return;
        }

        CharSequence[] names = new CharSequence[available.size()];
        for (int i = 0; i < available.size(); i++) {
            names[i] = available.get(i).displayName;
        }

        new AlertDialog.Builder(this, R.style.DialogTheme)
            .setTitle("Add Action")
            .setItems(names, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    selectedIds.add(available.get(which).id);
                    adapter.notifyDataSetChanged();
                    save();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

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
}
