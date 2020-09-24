# AOSP Example OTA agent

This project implements an example OTA agent that regularly polls the Memfault
releases HTTP API to check whether a new update is available.

When an update is available, it downloads the payload and installs it using the
RecoverySystem APIs (non A/B OTA). A similar agent could also easily be
implemented using the A/B OTA APIs of the UpdateEngine APIs.

Disclaimer: this agent is the most basic implementation to show how to interact
with the Memfault HTTP APIs and is not suitable for production as-is. Subjects
that are not taken care of include but are not limited to:

- Required sepolicy changes to allow use of the RecoverySystem APIs
- File download resumption
- Proper handling of various edge cases

## Usage

- In the Memfault web UI, go to Settings to find your project's key and add it
  to the gradle.properties file: `MEMFAULT_PROJECT_API_KEY=<KEY_HERE>`
- Build the .apk, sign it with the platform key and install it as a system app.
- Implement the required sepolicy changes to allow the app to use RecoverySystem
  APIs or temporarily disable sepolicy enforcement (`adb shell setenforce 0`).
- Trigger checking for updates. The worker job to poll for updates is scheduled
  in response to the `BOOT_COMPLETED` intent. To re-trigger polling, just send a
  targeted intent:

```
am broadcast --receiver-include-background \
  -a android.intent.action.BOOT_COMPLETED \
  -n com.memfault.example_ota/.EventReceiver
```

In case it's not possible to get a root adb shell, you can also use the `MAIN`
intent action:

```
am broadcast --receiver-include-background \
-a android.intent.action.MAIN \
-n com.memfault.example_ota/.EventReceiver
```
