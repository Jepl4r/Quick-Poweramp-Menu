#!/system/bin/sh
# Quick Poweramp Menu - Button Daemon
# Short press: open Poweramp
# Double press: execute double-tap action (broadcast)
# Long press (400ms): open Quick Menu
# ×××××××××××××××××××××××××××××××××××× #

until [ "$(getprop sys.boot_completed)" = 1 ]; do
    sleep 1
done

MODDIR=${0%/*}
LOG="${MODDIR}/daemon.log"
PID_FILE="${MODDIR}/daemon.pid"
POWERAMP_PKG="com.maxmpz.audioplayer"
MENU_PKG="com.mat.powerampmenu"
MENU_ACTION="com.mat.powerampmenu.SHOW_MENU"
DOUBLE_TAP_ACTION="com.mat.powerampmenu.DOUBLE_TAP"
LONG_PRESS_MS=400
DOUBLE_TAP_WINDOW_MS=300

:> "$LOG"

log() {
    echo "[$(date "+%H:%M:%S")] $1" >> "$LOG"
}

# Disable native MultiFuncButton overlay
pm disable com.android.settings/com.android.settings.fiio.general.MultiFuncButtonHandler 2>/dev/null
log "Disabled native MultiFuncButton overlay"

# Verify packages
if ! pm list packages 2>/dev/null | grep -q "^package:${POWERAMP_PKG}$"; then
    log "ERROR: Poweramp not installed!"
    exit 1
fi

if ! pm list packages 2>/dev/null | grep -q "^package:${MENU_PKG}$"; then
    log "WARNING: Poweramp Quick Menu app not installed, long press and double tap will be disabled"
    MENU_AVAILABLE=0
else
    MENU_AVAILABLE=1
fi

log "Starting Poweramp button daemon..."

# Main daemon loop
(while true; do
    # Wait for KEY_TV DOWN event
    getevent -lqc 1 /dev/input/event1 2>/dev/null | grep -q 'KEY_TV.*DOWN' || continue

    long_press_fired=0

    # Start a background timer for long press
    (
        sleep_time=$(awk "BEGIN {printf \"%.1f\", $LONG_PRESS_MS / 1000}")
        sleep "$sleep_time"
        touch "${MODDIR}/.longpress_fire" 2>/dev/null
    ) &
    timer_pid=$!

    # Poll for KEY_TV UP, checking if the timer has fired
    while true; do
        # Check if long press timer fired
        if [ -f "${MODDIR}/.longpress_fire" ]; then
            rm -f "${MODDIR}/.longpress_fire"
            long_press_fired=1

            # Open Quick Menu (only if Poweramp is running)
            if [ "$MENU_AVAILABLE" -eq 1 ] && pidof "$POWERAMP_PKG" >/dev/null 2>&1; then
                am start -a "$MENU_ACTION" \
                    -n "${MENU_PKG}/.MenuActivity" \
                    --activity-no-history \
                    --activity-brought-to-front 2>/dev/null
                log "Long press: opened Quick Menu"
            else
                log "Long press: skipped - Poweramp not running or Menu not available"
            fi

            # Wait for KEY_UP to finish the event
            while true; do
                getevent -lqc 1 /dev/input/event1 2>/dev/null | grep -q 'KEY_TV.*UP' && break
            done
            break
        fi

        # Check for KEY_UP (short press)
        up_event="$(timeout 0.05 getevent -lqc 1 /dev/input/event1 2>/dev/null)"
        if echo "$up_event" | grep -q 'KEY_TV.*UP'; then
            kill "$timer_pid" 2>/dev/null
            wait "$timer_pid" 2>/dev/null
            rm -f "${MODDIR}/.longpress_fire"
            break
        fi
    done

    # Handle short press — wait for possible second tap
    if [ "$long_press_fired" -eq 0 ]; then
        dt_sleep=$(awk "BEGIN {printf \"%.2f\", $DOUBLE_TAP_WINDOW_MS / 1000}")
        second_down="$(timeout "$dt_sleep" getevent -lqc 1 /dev/input/event1 2>/dev/null)"

        if echo "$second_down" | grep -q 'KEY_TV.*DOWN'; then
            # Second tap — wait for KEY_UP
            while true; do
                getevent -lqc 1 /dev/input/event1 2>/dev/null | grep -q 'KEY_TV.*UP' && break
            done

            # Send broadcast for double-tap action
            if [ "$MENU_AVAILABLE" -eq 1 ] && pidof "$POWERAMP_PKG" >/dev/null 2>&1; then
                am broadcast -a "$DOUBLE_TAP_ACTION" \
                    -n "${MENU_PKG}/.DoubleTapReceiver" 2>/dev/null
                log "Double tap: sent broadcast"
            else
                log "Double tap: skipped - Poweramp not running or Menu not available"
            fi
        else
            # Single press — launch Poweramp
            am start -n "$(cmd package resolve-activity --brief "$POWERAMP_PKG" 2>/dev/null | tail -1)" \
                --activity-brought-to-front 2>/dev/null || \
            monkey -p "$POWERAMP_PKG" -c android.intent.category.LAUNCHER 1 2>/dev/null
            log "Short press: launched Poweramp"
        fi
    fi
done) &

DAEMON_PID=$!
echo "$DAEMON_PID" > "$PID_FILE"
chmod 0644 "$PID_FILE"
log "Daemon started (PID: $DAEMON_PID)"

su -lp 2000 -c "cmd notification post -S bigtext -t 'Quick Poweramp Menu' 'Tag' 'Quick Menu for Poweramp is active.'" >/dev/null 2>&1
