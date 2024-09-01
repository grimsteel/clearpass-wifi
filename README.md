# `clearpass-wifi`

> Open source implementation of ClearPass QuickConnect

The official ClearPass app for Android requests a lot of intrusive permissions, including but not limited to:

* access precise location
* read/write full file storage (photos, videos, music, audio)
* make/manage phone calls

The app claims that it uses these permissions to "save logs", "obtain device information", and "validate that the app has successfully connected the device".

Logs should be saved to the app's internal storage, specific device information is _not_ required to onboard the device, and the user can validate the connection was successful themself.

This open source implementation of the onboarding protocol doesn't require any of these permissions.

## How to Use

1. Download the "quick1x.networkconfig" file from your organization's Wi-Fi onboarding portal.
2. Click the "+" on the app's home screen
3. Click "Select quick1x config"
4. Select the quick1x.networkconfig file
5. On the network edit screen that shows up, click the "Add Wi-Fi network suggestion" button

## Screenshots

<p float="left">
  <img src="https://github.com/user-attachments/assets/61b09f79-0383-4b4c-968d-1df6de9f94a0" width="300" />
  <img src="https://github.com/user-attachments/assets/9fa0b155-bdbc-4f8e-8ddf-835786e36c92" width="300" />
  <img src="https://github.com/user-attachments/assets/261a8dc0-feb7-4767-a72d-46c680976907" width="300" />
</p>
