package com.mat.powerampmenu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class DoubleTapReceiver extends BroadcastReceiver {

    private static final String PA_API_COMMAND = "com.maxmpz.audioplayer.API_COMMAND";
    private static final String PA_RECEIVER = "com.maxmpz.audioplayer.player.PowerampAPIReceiver";
    private static final String PA_PACKAGE = "com.maxmpz.audioplayer";

    @Override
    public void onReceive(Context context, Intent intent) {
        MenuPreferences prefs = new MenuPreferences(context);
        String actionId = prefs.getDoubleTapActionId();

        if (actionId == null || actionId.isEmpty()) return;

        PowerampAction action = PowerampAction.findById(actionId);
        if (action == null) return;

        executeAction(context, action);
    }

    private void executeAction(Context context, PowerampAction action) {
        try {
            switch (action.type) {
                case COMMAND:
                    Intent cmdIntent = new Intent(PA_API_COMMAND);
                    cmdIntent.putExtra("cmd", action.command);
                    cmdIntent.setClassName(PA_PACKAGE, PA_RECEIVER);
                    context.sendBroadcast(cmdIntent);
                    break;

                case COMMAND_EXTRA:
                    Intent extraIntent = new Intent(PA_API_COMMAND);
                    extraIntent.putExtra("cmd", action.command);
                    extraIntent.putExtra(action.extraKey, action.extraValue);
                    extraIntent.setClassName(PA_PACKAGE, PA_RECEIVER);
                    context.sendBroadcast(extraIntent);
                    break;

                case SCREEN:
                    Intent screenIntent = new Intent(action.command);
                    screenIntent.setPackage(PA_PACKAGE);
                    screenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(screenIntent);
                    break;

                case CATEGORY:
                    Intent catIntent = new Intent("com.maxmpz.audioplayer.ACTION_SHOW_LIST");
                    catIntent.setData(Uri.parse(action.command));
                    catIntent.setPackage(PA_PACKAGE);
                    catIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(catIntent);
                    break;
            }
        } catch (Exception ignored) {
        }
    }
}
