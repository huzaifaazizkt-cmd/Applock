# Implementation Plan - Fix `ACTION_HOVER_EXIT` Crash

The application is crashing with `java.lang.IllegalStateException: The ACTION_HOVER_EXIT event was not cleared`. This is a known issue in Jetpack Compose versions `1.6.0` through `1.6.4`, occurring when a composable with hover focus (like a `Popup`, `Dialog`, or a conditionally rendered element) is removed from the composition before the hover event is cleared.

## User Review Required

> [!IMPORTANT]
> The fix involves upgrading the Compose BOM version. The project already has a newer version (`2026.02.01`) defined in `libs.versions.toml`, but `app/build.gradle.kts` is currently hardcoded to use an older version (`2024.02.01`).

## Proposed Changes

### Build Configuration

#### [MODIFY] [build.gradle.kts](file:///D:/applock/app/build.gradle.kts)
- Update the Compose BOM implementation to use the version defined in `libs.versions.toml` (`libs.androidx.compose.bom`).
- Clean up other Compose dependencies to consistently use the BOM versions.

### UI Screens (Defensive Measures)

While the library update is the primary fix, adding small delays or ensuring focus is cleared before dismissing `Popup`s can prevent similar issues in other versions.

#### [MODIFY] [PinConfirmScreen.kt](file:///D:/applock/app/src/main/java/com/example/applock/Design/screens/PinConfirmScreen.kt)
- In `SecurityQuestionDialog`, ensure the `Popup` dismissal doesn't conflict with immediate focus changes. (The library update should handle this, but I will check for any unusual pointer input handling).

## Verification Plan

### Automated Tests
- Run `gradlew :app:assembleDebug` to ensure the project builds with the new Compose version.
- If unit tests exist for screens using `Popup`, run them: `gradlew :app:test`.

### Manual Verification
- Deploy the app to a device/emulator.
- Navigate to the **Pin Confirm Screen** (during setup).
- Open the **Security Question Dialog**.
- Interact with the dropdown (specifically try to hover and click quickly if using a mouse or accessibility tools).
- Verify that the app no longer crashes when a question is selected and the `Popup` closes.
- Navigate through the **Settings Screen** and expand/collapse items to ensure stability.
