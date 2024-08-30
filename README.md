# `clearpass-wifi`

> Open source implementation of ClearPass QuickConnect

The official ClearPass app for Android requests a lot of intrusive permissions, including but not limited to:

* access precise location
* read/write full file storage (photos, videos, music, audio)
* make/manage phone calls

The app claims that it uses these permissions to "save logs", "obtain device information", and "validate that the app has succesfully connected the device".

Logs should be saved to the app's internal storage, specific device information is _not_ required to onboard the device, and the user can validate the connection was successful themself.

This open source implementation of the onboarding protocol doesn't require any of these permissions.
