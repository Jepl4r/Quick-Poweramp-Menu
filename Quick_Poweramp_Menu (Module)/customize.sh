# shellcheck disable=SC2148
# shellcheck disable=SC2034
SKIPUNZIP=1

ui_print "-"
ui_print "**************************************************"
ui_print "-"
ui_print "- Quick Poweramp Menu v1.2"
ui_print "-"
ui_print "**************************************************"
ui_print "-"
ui_print "- Device: $(getprop ro.product.model)"
ui_print "- Firmware: $(getprop ro.product.version)"
ui_print "-"

# Check Poweramp
if pm list packages 2>/dev/null | grep -q "^package:com.maxmpz.audioplayer$"; then
    ui_print "- Poweramp: FOUND"
else
    ui_print "- WARNING: Poweramp not found!"
    ui_print "- Install Poweramp for this module to work."
fi

# Extract module files
ui_print "- Extracting module files..."
unzip -o "$ZIPFILE" module.prop -d "$MODPATH" >&2
unzip -o "$ZIPFILE" service.sh -d "$MODPATH" >&2

# Extract and install the Quick Menu APK
ui_print "- Installing Poweramp Quick Menu app..."
unzip -o "$ZIPFILE" PowerampQuickMenu.apk -d "$MODPATH" >&2

if [ -f "${MODPATH}/PowerampQuickMenu.apk" ]; then
    # Install the APK silently
    pm install -r "${MODPATH}/PowerampQuickMenu.apk" 2>/dev/null
    if pm list packages 2>/dev/null | grep -q "^package:com.mat.powerampmenu$"; then
        ui_print "- Poweramp Quick Menu: INSTALLED"
    else
        # Fallback: try with -g flag (grant all permissions)
        pm install -r -g "${MODPATH}/PowerampQuickMenu.apk" 2>/dev/null
        if pm list packages 2>/dev/null | grep -q "^package:com.mat.powerampmenu$"; then
            ui_print "- Poweramp Quick Menu: INSTALLED"
        else
            ui_print "- WARNING: Auto-install failed!"
            ui_print "- APK saved to: ${MODPATH}/PowerampQuickMenu.apk"
            ui_print "- Install it manually via file manager."
        fi
    fi
else
    ui_print "- ERROR: APK not found in zip!"
fi

# Set permissions
ui_print "- Setting permissions..."
set_perm_recursive "$MODPATH" 0 0 0755 0644
set_perm "${MODPATH}/service.sh" 0 0 0755

ui_print "-"
ui_print "**************************************************"
ui_print "- INSTALLATION COMPLETE"
ui_print "-"
ui_print "- The native FiiO overlay menu will be"
ui_print "- automatically disabled at boot."
ui_print "-"
ui_print "- WARNING: Remember to disable the native"
ui_print "- multifunction button action in FiiO Settings"
ui_print "- to avoid conflicts."
ui_print "-"
ui_print "**************************************************"
ui_print "-"
ui_print "- Please REBOOT to activate the plugin."
ui_print "-"
ui_print "**************************************************"
