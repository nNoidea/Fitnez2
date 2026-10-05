#!/usr/bin/env bash
#
# Captures the 4 README screenshots (timeline, monthly, graph, settings) at a
# standard 1080x2400 portrait phone resolution in dark mode with Material You
# dynamic colors. Uses the app's own first-start seed (Squat climbing over 7
# days) on a temporary emulator user, with the graph pre-set to the 7-day view.
#
# Usage: make screenshots
#        SCREENSHOT_AVD=Medium_Phone_API_36.1 make screenshots
#        ANDROID_SERIAL=emulator-5554 make screenshots   (when several devices)
#
# If no device is attached, a headless emulator is started automatically.
# The screenshots land in ./screenshots/ and are referenced by README.md.
# A temporary emulator user keeps existing app data untouched.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PKG="com.nnoidea.fitnez2"
ACTIVITY="$PKG/.MainActivity"
OUT="$ROOT/screenshots"
SCREENSHOT_AVD="${SCREENSHOT_AVD:-Medium_Phone_API_36.1}"
STARTED_EMULATOR=0
USER_ID=""

adb_() {
    if [ -n "${ANDROID_SERIAL:-}" ]; then
        adb -s "$ANDROID_SERIAL" "$@"
    else
        adb "$@"
    fi
}

if ! adb_ get-state >/dev/null 2>&1; then
    if ! command -v android >/dev/null 2>&1; then
        echo "error: no emulator/device found and the 'android' CLI is missing." >&2
        exit 1
    fi
    echo "No device attached; starting headless emulator '$SCREENSHOT_AVD'..."
    android emulator start --headless "$SCREENSHOT_AVD"
    STARTED_EMULATOR=1
fi

adb_ wait-for-device
until [ "$(adb_ shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; do
    sleep 2
done

# Standard portrait phone geometry so screenshots are neither tiny nor stretched.
adb_ shell wm size 1080x2400 >/dev/null
adb_ shell wm density 420 >/dev/null

# Headless emulators often sit with the display OFF, which screencaps as a
# black frame. Wake it and keep it on while capturing.
wake() {
    adb_ shell input keyevent KEYCODE_WAKEUP >/dev/null 2>&1 || true
    adb_ shell wm dismiss-keyguard >/dev/null 2>&1 || true
    adb_ shell svc power stayon true >/dev/null 2>&1 || true
    sleep 1
}

# Calm the UI for deterministic captures.
adb_ shell settings put global window_animation_scale 0 >/dev/null 2>&1 || true
adb_ shell settings put global transition_animation_scale 0 >/dev/null 2>&1 || true
adb_ shell settings put global animator_duration_scale 0 >/dev/null 2>&1 || true
adb_ shell wm dismiss-keyguard >/dev/null 2>&1 || true

# Build the current app and install it.
"$ROOT/gradlew" -p "$ROOT" installDebug

# Removing a running user fails, so stop it first.
remove_user() {
    adb_ shell am stop-user "$1" >/dev/null 2>&1 || true
    sleep 1
    adb_ shell pm remove-user "$1" >/dev/null 2>&1 || true
}

# Temporary user: seeding wipes the database, so isolate it from real data.
# Purge leftovers from interrupted runs (all ours by name) so we never hit
# the system's max-users limit.
adb_ shell pm list users 2>/dev/null | grep -oE "UserInfo\{[0-9]+:Screenshot" | grep -oE "[0-9]+" | while read -r stale; do
    [ -n "$stale" ] && remove_user "$stale"
done || true
if CREATE_OUT="$(adb_ shell pm create-user Screenshot 2>&1 | tr -d '\r')"; then
    USER_ID="$(echo "$CREATE_OUT" | grep -oE '[0-9]+' | tail -n 1)"
fi
if [ -z "$USER_ID" ]; then
    echo "warning: could not create a temporary user; falling back to 'pm clear' on the current user." >&2
    adb_ shell pm clear "$PKG" >/dev/null 2>&1 || true
    USER_ID=0
else
    adb_ shell pm install-existing --user "$USER_ID" "$PKG" >/dev/null 2>&1 || true
    adb_ shell am switch-user "$USER_ID" >/dev/null 2>&1 || true
    sleep 2
fi

# Dark mode AFTER switching users: ui_night_mode is per-user, so the temporary
# user needs it set explicitly. Material You dynamic colors then follow
# automatically on Android 12+ via dynamicDarkColorScheme().
adb_ shell cmd uimode night yes >/dev/null 2>&1 || true
adb_ shell settings put secure ui_night_mode 2 >/dev/null 2>&1 || true

cleanup() {
    adb_ shell settings put global window_animation_scale 1 >/dev/null 2>&1 || true
    adb_ shell settings put global transition_animation_scale 1 >/dev/null 2>&1 || true
    adb_ shell settings put global animator_duration_scale 1 >/dev/null 2>&1 || true
    adb_ shell wm size reset >/dev/null 2>&1 || true
    adb_ shell wm density reset >/dev/null 2>&1 || true
    if [ "$USER_ID" != "0" ] && [ -n "$USER_ID" ]; then
        adb_ shell am switch-user 0 >/dev/null 2>&1 || true
        sleep 1
        remove_user "$USER_ID"
    fi
    # Only shut down what we started; a pre-existing device is left running.
    if [ "$STARTED_EMULATOR" = "1" ]; then
        echo "Stopping headless emulator..."
        adb_ emu kill >/dev/null 2>&1 || true
        # 'emu kill' is asynchronous; wait until the device is really gone.
        for _ in $(seq 1 15); do
            adb_ get-state >/dev/null 2>&1 || break
            sleep 2
        done
    fi
}
trap cleanup EXIT

mkdir -p "$OUT"
wake

# First launch triggers the first-start seed (Squat climbing 60 -> 75 kg).
adb_ shell am start --user "$USER_ID" -n "$ACTIVITY" \
    --es extra_page_route timeline \
    --activity-clear-task >/dev/null
sleep 5

shot() {
    wake
    adb_ shell am start --user "$USER_ID" -n "$ACTIVITY" \
        --es extra_page_route "$1" \
        --activity-clear-task >/dev/null
    sleep "${3:-2}"
    adb_ exec-out screencap -p > "$OUT/$2"
    echo "captured $2"
}

shot timeline timeline.png 3
shot monthly monthly.png 2
shot graph graph.png 2
shot settings settings.png 2

# The README references these files; verify the links stay in sync.
missing=0
for f in timeline monthly graph settings; do
    if ! grep -q "screenshots/$f.png" "$ROOT/README.md"; then
        echo "warning: README.md does not reference screenshots/$f.png" >&2
        missing=1
    fi
done
if [ "$missing" = "0" ]; then
    echo "README.md references all 4 screenshots."
fi
