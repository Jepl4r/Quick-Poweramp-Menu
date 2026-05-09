package com.mat.powerampmenu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class MenuActivity extends Activity {

    private static final String PA_API_COMMAND = "com.maxmpz.audioplayer.API_COMMAND";
    private static final String PA_RECEIVER = "com.maxmpz.audioplayer.player.PowerampAPIReceiver";
    private static final String PA_PACKAGE = "com.maxmpz.audioplayer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window w = getWindow();
        w.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        showMenu();
    }

    private void showMenu() {
        MenuPreferences prefs = new MenuPreferences(this);
        final List<PowerampAction> actions = prefs.getSelectedActions();

        if (actions.isEmpty()) {
            finish();
            return;
        }

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setBackgroundResource(R.drawable.dialog_background);
        int pad = dp(8);
        container.setPadding(0, pad, 0, pad);

        for (PowerampAction action : actions) {
            container.addView(createMenuItem(action));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MenuDialogTheme);
        builder.setView(container);
        builder.setCancelable(true);

        final AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface d) {
                finish();
                overridePendingTransition(0, 0);
            }
        });

        for (int i = 0; i < container.getChildCount(); i++) {
            View item = container.getChildAt(i);
            final PowerampAction action = actions.get(i);
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    executeAction(action);
                    dialog.dismiss();
                }
            });
        }

        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);
            window.setAttributes(lp);
        }
    }

    private View createMenuItem(PowerampAction action) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackgroundResource(R.drawable.menu_item_ripple);
        row.setClickable(true);
        row.setFocusable(true);

        int h = dp(52);
        int padH = dp(24);
        row.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, h));
        row.setPadding(padH, 0, padH, 0);

        ImageView icon = new ImageView(this);
        int iconSize = dp(24);
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(iconSize, iconSize);
        icon.setLayoutParams(iconLp);
        icon.setImageResource(action.iconResId);
        row.addView(icon);

        TextView label = new TextView(this);
        LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        labelLp.setMarginStart(dp(16));
        label.setLayoutParams(labelLp);
        label.setText(action.displayName);
        label.setTextSize(16);
        label.setTextColor(0xFFFFFFFF);
        row.addView(label);

        return row;
    }

    private void executeAction(PowerampAction action) {
        try {
            switch (action.type) {
                case COMMAND:
                    Intent cmdIntent = new Intent(PA_API_COMMAND);
                    cmdIntent.putExtra("cmd", action.command);
                    cmdIntent.setClassName(PA_PACKAGE, PA_RECEIVER);
                    sendBroadcast(cmdIntent);
                    break;

                case COMMAND_EXTRA:
                    Intent extraIntent = new Intent(PA_API_COMMAND);
                    extraIntent.putExtra("cmd", action.command);
                    extraIntent.putExtra(action.extraKey, action.extraValue);
                    extraIntent.setClassName(PA_PACKAGE, PA_RECEIVER);
                    sendBroadcast(extraIntent);
                    break;

                case SCREEN:
                    Intent screenIntent = new Intent(action.command);
                    screenIntent.setPackage(PA_PACKAGE);
                    startActivity(screenIntent);
                    break;

                case CATEGORY:
                    Intent catIntent = new Intent("com.maxmpz.audioplayer.ACTION_SHOW_LIST");
                    catIntent.setData(Uri.parse(action.command));
                    catIntent.setPackage(PA_PACKAGE);
                    startActivity(catIntent);
                    break;
            }
        } catch (Exception ignored) {
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
