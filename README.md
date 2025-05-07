# CleanSlate

CleanSlate is an Android application designed to ensure user privacy and protect sensitive data. It allows users to remotely wipe their device in the unfortunate event of it being lost or stolen. With CleanSlate, your privacy is always in your hands.

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="80">](https://f-droid.org/packages/com.destructo.botox/)

Or download the latest APK from the [Releases Section](https://github.com/khalifarsm/botox-android/releases/latest).

## Features

- **Remote Wipe:** Securely erase all data on your device remotely.
- **Authentication System:** Prevent unauthorized access to the wiping function.
- **Customizable Wipe Triggers:** Configure how and when the remote wipe can be initiated.
- **Minimal Footprint:** Lightweight and optimized for minimal impact on device performance.

## Installation

1. Clone this repository to your local machine:
   ```bash
   git clone https://github.com/khalifarsm/botox-android.git
   ```
2. Open the project in Android Studio.
3. Sync the Gradle files and ensure all dependencies are installed.
4. Build and run the app on your Android device or emulator.

## Usage

1. install the app to your device and grant all priveleges required.
2. register your account
3. save you user ID and password in a safe place.
4. [In case of a lost or stolen device, initiate a wipe through the chosen trigger](https://cleanslate.mobi/reset).

## Permissions

CleanSlate requires the following permissions for full functionality:

- **Device Administration:** To securely wipe the device.
- **Internet Access:** For online dashboard or email notifications.

## Security and Privacy

CleanSlate ensures your data is securely erased and inaccessible to unauthorized users. It does not store or share user data without consent.
https://cleanslate.mobi/privacy

## 🔒 Security Features
To enhance security, we have replaced Firebase FCM with secure WebSocket communication, implemented a wipe token system, ensured that remote wipe commands are decryptable only by the application, added advanced in-app logging for remote commands, and enforced user confirmation before any wipe action can be executed.

| Feature | Status |
|--------|--------|
| **Implement a wipe token**: Each user should generate a local-only encrypted token during setup, used to validate remote wipe requests. This token is never stored on your server. | ✅ Complete |
| **Strip debug metadata before production** (`minifyEnabled true` and remove `DebugProbesKt.bin`). | ✅ Complete |
| **Make remote wipe command decryptable only by the app (client-side).** | ✅ Complete |
| **Show clear user onboarding before enabling Device Admin, explaining its impact.** | ✅ Complete |
| **Disable Firebase Analytics and tracking features in production.** <br> _Replaced with secure WebSocket._ | ✅ Complete |
| **Provide an activity log within the app to show received remote commands.** | ✅ Complete |
| **Use self-hosted FCM alternatives (like ntfy.sh) or open-source push systems if needed.** <br> _Replaced with secure WebSocket._ | ✅ Complete |
| **Ensure app cannot silently wipe without visible confirmation unless explicitly set by user.** | ✅ Complete |


## Contributing

Contributions are welcome! Follow these steps to contribute:

1. Fork the repository.
2. Create a new branch for your feature or bug fix:
   ```bash
   git checkout -b feature-name
   ```
3. Commit your changes and push the branch:
   ```bash
   git commit -m "Add feature or fix bug"
   git push origin feature-name
   ```
4. Create a pull request to the `main` branch of this repository.

## License

This project is licensed under the [MIT License](LICENSE).

## Contact

For any questions or feedback, please reach out to us at [support@cleanslateapp.com](mailto:support@cleanslateapp.com).

---

We hope CleanSlate helps you feel secure, knowing your privacy is protected even in challenging situations.

