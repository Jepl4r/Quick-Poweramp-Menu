package com.mat.powerampmenu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

public class MenuActivity extends Activity {

    private static final String PA_API_COMMAND = "com.maxmpz.audioplayer.API_COMMAND";
    private static final String PA_RECEIVER = "com.maxmpz.audioplayer.player.PowerampAPIReceiver";
    private static final String PA_PACKAGE = "com.maxmpz.audioplayer";

    private MediaController activeController;
    private MediaController.Callback mediaCallback;

    // Header views for live updates
    private LinearLayout headerContainer;
    private ImageView headerArtView;
    private TextView headerTitleView;
    private TextView headerSubtitleView;
    private View headerDivider;

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterMediaCallback();
    }

    // --- Notification Listener permission check ---

    private boolean isNotificationAccessEnabled() {
        ComponentName cn = new ComponentName(this, NLService.class);
        String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        return flat != null && flat.contains(cn.flattenToString());
    }

    // --- MediaSession access ---

    private MediaController findPowerampController() {
        if (!isNotificationAccessEnabled()) return null;
        try {
            MediaSessionManager msm = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
            ComponentName cn = new ComponentName(this, NLService.class);
            List<MediaController> controllers = msm.getActiveSessions(cn);
            for (MediaController mc : controllers) {
                if (PA_PACKAGE.equals(mc.getPackageName())) {
                    return mc;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private void registerMediaCallback(MediaController controller) {
        unregisterMediaCallback();
        activeController = controller;
        mediaCallback = new MediaController.Callback() {
            @Override
            public void onMetadataChanged(MediaMetadata metadata) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateHeader(metadata);
                    }
                });
            }
        };
        activeController.registerCallback(mediaCallback);
    }

    private void unregisterMediaCallback() {
        if (activeController != null && mediaCallback != null) {
            try {
                activeController.unregisterCallback(mediaCallback);
            } catch (Exception ignored) {
            }
            activeController = null;
            mediaCallback = null;
        }
    }

    // --- Header creation and live update ---

    private void updateHeader(MediaMetadata metadata) {
        if (headerContainer == null) return;

        if (metadata == null) {
            headerContainer.setVisibility(View.GONE);
            if (headerDivider != null) headerDivider.setVisibility(View.GONE);
            return;
        }

        String title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE);
        String artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST);
        String album = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM);
        Bitmap art = metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);

        if (TextUtils.isEmpty(title)) {
            headerContainer.setVisibility(View.GONE);
            if (headerDivider != null) headerDivider.setVisibility(View.GONE);
            return;
        }

        headerContainer.setVisibility(View.VISIBLE);
        if (headerDivider != null) headerDivider.setVisibility(View.VISIBLE);

        // Update art
        if (headerArtView != null) {
            if (art != null) {
                headerArtView.setImageBitmap(art);
                headerArtView.setVisibility(View.VISIBLE);
            } else {
                headerArtView.setVisibility(View.GONE);
            }
        }

        // Update title
        if (headerTitleView != null) {
            headerTitleView.setText(title);
        }

        // Update subtitle
        if (headerSubtitleView != null) {
            String subtitle = "";
            if (!TextUtils.isEmpty(artist)) subtitle = artist;
            if (!TextUtils.isEmpty(album)) {
                if (!subtitle.isEmpty()) subtitle += " \u2014 ";
                subtitle += album;
            }
            if (!subtitle.isEmpty()) {
                headerSubtitleView.setText(subtitle);
                headerSubtitleView.setVisibility(View.VISIBLE);
            } else {
                headerSubtitleView.setVisibility(View.GONE);
            }
        }
    }

    private View createNowPlayingHeader(MediaMetadata metadata) {
        // Outer wrapper so we can show/hide the whole thing
        headerContainer = new LinearLayout(this);
        headerContainer.setOrientation(LinearLayout.HORIZONTAL);
        headerContainer.setGravity(Gravity.CENTER_VERTICAL);
        headerContainer.setPadding(dp(16), dp(12), dp(16), dp(12));

        // Album art
        headerArtView = new ImageView(this);
        int artSize = dp(48);
        LinearLayout.LayoutParams artLp = new LinearLayout.LayoutParams(artSize, artSize);
        artLp.setMarginEnd(dp(12));
        headerArtView.setLayoutParams(artLp);
        headerArtView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        headerArtView.setClipToOutline(true);
        headerArtView.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override
            public void getOutline(View view, android.graphics.Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), dp(6));
            }
        });
        headerArtView.setVisibility(View.GONE);
        headerContainer.addView(headerArtView);

        // Text column
        LinearLayout textCol = new LinearLayout(this);
        textCol.setOrientation(LinearLayout.VERTICAL);
        textCol.setLayoutParams(new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        headerTitleView = new TextView(this);
        headerTitleView.setTextSize(14);
        headerTitleView.setTextColor(0xFFFFFFFF);
        headerTitleView.setTypeface(null, Typeface.BOLD);
        headerTitleView.setMaxLines(1);
        headerTitleView.setEllipsize(TextUtils.TruncateAt.END);
        textCol.addView(headerTitleView);

        headerSubtitleView = new TextView(this);
        headerSubtitleView.setTextSize(12);
        headerSubtitleView.setTextColor(0xB3FFFFFF);
        headerSubtitleView.setMaxLines(1);
        headerSubtitleView.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        subLp.topMargin = dp(2);
        headerSubtitleView.setLayoutParams(subLp);
        headerSubtitleView.setVisibility(View.GONE);
        textCol.addView(headerSubtitleView);

        headerContainer.addView(textCol);

        // Initial populate
        updateHeader(metadata);

        return headerContainer;
    }

    // --- Menu ---

    private void showMenu() {
        MenuPreferences prefs = new MenuPreferences(this);
        final List<PowerampAction> actions = prefs.getSelectedActions();

        if (actions.isEmpty()) {
            finish();
            return;
        }

        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundResource(R.drawable.dialog_background);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(8);
        container.setPadding(0, 0, 0, pad);

        // Now Playing header (only if enabled in settings and notification access is granted)
        boolean showNowPlaying = prefs.isShowNowPlaying();
        MediaController controller = showNowPlaying ? findPowerampController() : null;
        if (controller != null) {
            MediaMetadata metadata = controller.getMetadata();
            container.addView(createNowPlayingHeader(metadata));

            // Divider
            headerDivider = new View(this);
            headerDivider.setBackgroundColor(0x33FFFFFF);
            LinearLayout.LayoutParams divLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
            divLp.setMargins(dp(16), 0, dp(16), dp(4));
            headerDivider.setLayoutParams(divLp);
            container.addView(headerDivider);

            // Register for live updates
            registerMediaCallback(controller);
        }

        // Flat action list
        for (PowerampAction action : actions) {
            container.addView(createMenuItem(action));
        }

        scrollView.addView(container);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MenuDialogTheme);
        builder.setView(scrollView);
        builder.setCancelable(true);

        final AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface d) {
                unregisterMediaCallback();
                finish();
                overridePendingTransition(0, 0);
            }
        });

        // Click listeners
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            Object tag = child.getTag();
            if (tag instanceof PowerampAction) {
                final PowerampAction action = (PowerampAction) tag;
                child.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        executeAction(action);
                        dialog.dismiss();
                    }
                });
            }
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
        row.setTag(action);

        int h = dp(48);
        int padH = dp(24);
        row.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, h));
        row.setPadding(padH, 0, padH, 0);

        ImageView icon = new ImageView(this);
        int iconSize = dp(22);
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(iconSize, iconSize);
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
