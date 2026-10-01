# Task Reminder — Android

A simple daily task reminder app.

## Behavior
- Add a task and a daily time.
- At that time every day, Android opens a full-screen alarm screen.
- The task name is spoken repeatedly with Android Text-to-Speech until **OK — STOP ALERT** is pressed.
- Pressing OK only acknowledges that day's alert. The task remains pending and will alert again tomorrow.
- Press **Mark Done** in the app to permanently complete the task and cancel future alarms.
- Completed tasks are kept in History with completion date/time.
- Alarms are restored after device reboot/app update.

## Important Android permissions
On Android 12+, exact alarms may require allowing the app to schedule exact alarms in system settings. Android 13+ requires notification permission.

## Build
The repository contains `.github/workflows/build-apk.yml`. It builds a debug APK automatically after pushes to `main`, and it can also be started manually from GitHub Actions.
