package com.mat.powerampmenu;

import android.service.notification.NotificationListenerService;

/**
 * Required to get access to active MediaSessions via MediaSessionManager.
 * The user must enable this service in Settings > Notification access.
 * This service doesn't actually process notifications, it only exists
 * so the app can call MediaSessionManager.getActiveSessions().
 */
public class NLService extends NotificationListenerService {
}
