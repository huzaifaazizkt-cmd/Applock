package com.example.applock.Design.screens

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.WindowManager

import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

import com.example.applock.MainActivity
import com.example.applock.data.DataStoreManager
import com.example.applock.service.AppLockServiceHolder

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

import java.text.SimpleDateFormat
import java.util.Locale


class LockScreenActivity : FragmentActivity() {

    private val TAG = "APPLOCK_DEBUG"

    private var targetPackage: String? = null

    private lateinit var dataStore: DataStoreManager

    // =========================================================
    // CAMERA
    // =========================================================

    private var imageCapture: ImageCapture? = null

    private var pendingIntruderCapture =
        false

    private var cameraProvider:
            ProcessCameraProvider? = null

    private val CAMERA_PERMISSION =
        Manifest.permission.CAMERA

    private val CAMERA_REQUEST_CODE =
        501

    // =========================================================
    // BIOMETRIC
    // =========================================================

    private var biometricPrompt:
            BiometricPrompt? = null

    private var biometricStarted =
        false


    // =========================================================
    // CREATE
    // =========================================================

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )


        dataStore =
            DataStoreManager(this)


        Log.d(
            TAG,
            "================================"
        )

        Log.d(
            TAG,
            "LOCK SCREEN CREATED"
        )

        Log.d(
            TAG,
            "================================"
        )


        // =====================================================
        // SHOW WHEN LOCKED
        // =====================================================

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O_MR1
        ) {

            setShowWhenLocked(true)

            setTurnScreenOn(true)
        }


        window.addFlags(

            WindowManager.LayoutParams
                .FLAG_KEEP_SCREEN_ON or

                    WindowManager.LayoutParams
                        .FLAG_DISMISS_KEYGUARD or

                    WindowManager.LayoutParams
                        .FLAG_SHOW_WHEN_LOCKED or

                    WindowManager.LayoutParams
                        .FLAG_TURN_SCREEN_ON
        )


        // =====================================================
        // TARGET APP
        // =====================================================

        targetPackage =
            intent.getStringExtra(
                "packageName"
            )


        Log.d(
            TAG,
            "TARGET APP = $targetPackage"
        )


        if (
            targetPackage.isNullOrEmpty()
        ) {

            Log.e(
                TAG,
                "TARGET PACKAGE IS NULL"
            )

            goHome()

            return
        }


        // =====================================================
        // LOCK SCREEN OPEN
        // =====================================================

        AppLockServiceHolder
            .isLockScreenOpen = true


        // =====================================================
        // BACK BUTTON
        // =====================================================

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {

                override fun
                        handleOnBackPressed() {

                    Log.d(
                        TAG,
                        "BACK PRESSED ON LOCK SCREEN"
                    )

                    goHome()
                }
            }
        )


        // =====================================================
        // UNLOCK SCREEN
        // =====================================================

        setContent {

            UnlockScreen(

                onUnlockSuccess = {

                    unlockAndOpenApp()
                },


                onFingerprintRequest = {

                    showBiometricPrompt()
                },


                onIntruderCapture = {

                    Log.d(
                        TAG,
                        "INTRUDER CAMERA REQUEST"
                    )

                    captureIntruderPhoto()
                },


                onForgotPasswordSuccess = {

                    openResetPassword()
                }
            )
        }
    }


    // =========================================================
    // FORGOT PASSWORD
    // =========================================================

    private fun openResetPassword() {

        Log.d(
            TAG,
            "FORGOT PASSWORD VERIFIED"
        )

        Log.d(
            TAG,
            "OPENING RESET PASSWORD"
        )


        AppLockServiceHolder
            .isLockScreenOpen = false


        AppLockServiceHolder
            .currentUnlockedApp = null


        AppLockServiceHolder
            .lastUnlockTime = 0L


        AppLockServiceHolder
            .clearSuppressedPackage()


        val resetIntent =
            Intent(
                this,
                MainActivity::class.java
            ).apply {

                putExtra(
                    "openResetPassword",
                    true
                )


                addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
            }


        try {

            startActivity(
                resetIntent
            )

        } catch (e: Exception) {

            Log.e(
                TAG,
                "RESET PASSWORD OPEN ERROR",
                e
            )
        }


        finish()
    }


    // =========================================================
    // UNLOCK + OPEN APP
    // =========================================================

    private fun unlockAndOpenApp() {

        val packageName =
            targetPackage


        Log.d(
            TAG,
            "================================"
        )

        Log.d(
            TAG,
            "UNLOCK SUCCESS"
        )

        Log.d(
            TAG,
            "PACKAGE = $packageName"
        )

        Log.d(
            TAG,
            "================================"
        )


        if (
            packageName.isNullOrEmpty()
        ) {

            goHome()

            return
        }


        // -----------------------------------------------------
        // Remove Back suppression
        // -----------------------------------------------------

        AppLockServiceHolder
            .clearSuppressedPackage()


        // -----------------------------------------------------
        // Remember unlocked app
        // -----------------------------------------------------

        AppLockServiceHolder
            .currentUnlockedApp =
            packageName


        AppLockServiceHolder
            .lastUnlockTime =
            System.currentTimeMillis()


        AppLockServiceHolder
            .isLockScreenOpen = false


        // -----------------------------------------------------
        // Open target app
        // -----------------------------------------------------

        openApp()
    }


    // =========================================================
    // BIOMETRIC
    // =========================================================

    private fun showBiometricPrompt() {

        if (
            biometricStarted
        ) {

            return
        }


        val biometricManager =
            BiometricManager.from(this)


        val authenticators =
            BiometricManager.Authenticators
                .BIOMETRIC_STRONG or
                    BiometricManager.Authenticators
                        .BIOMETRIC_WEAK


        val canAuthenticate =
            biometricManager.canAuthenticate(
                authenticators
            )


        if (
            canAuthenticate !=
            BiometricManager.BIOMETRIC_SUCCESS
        ) {

            Log.e(
                TAG,
                "BIOMETRIC NOT AVAILABLE = $canAuthenticate"
            )

            return
        }


        val promptInfo =
            BiometricPrompt.PromptInfo
                .Builder()
                .setTitle(
                    "Fingerprint Lock"
                )
                .setSubtitle(
                    "Use your fingerprint to unlock"
                )
                .setNegativeButtonText(
                    "Use PIN / Pattern"
                )
                .build()


        val executor =
            ContextCompat.getMainExecutor(
                this
            )


        biometricPrompt =
            BiometricPrompt(
                this,
                executor,
                object :
                    BiometricPrompt
                    .AuthenticationCallback() {

                    override fun
                            onAuthenticationSucceeded(
                        result:
                        BiometricPrompt.AuthenticationResult
                    ) {

                        super
                            .onAuthenticationSucceeded(
                                result
                            )


                        biometricStarted =
                            false


                        Log.d(
                            TAG,
                            "FINGERPRINT SUCCESS"
                        )


                        unlockAndOpenApp()
                    }


                    override fun
                            onAuthenticationError(
                        errorCode: Int,
                        errString: CharSequence
                    ) {

                        super
                            .onAuthenticationError(
                                errorCode,
                                errString
                            )


                        biometricStarted =
                            false


                        Log.d(
                            TAG,
                            "BIOMETRIC ERROR = $errString"
                        )
                    }


                    override fun
                            onAuthenticationFailed() {

                        super
                            .onAuthenticationFailed()


                        Log.d(
                            TAG,
                            "FINGERPRINT FAILED"
                        )
                    }
                }
            )


        biometricStarted =
            true


        biometricPrompt
            ?.authenticate(
                promptInfo
            )
    }


    // =========================================================
    // CAMERA PERMISSION
    // =========================================================

    private fun captureIntruderPhoto() {

        if (
            ContextCompat.checkSelfPermission(
                this,
                CAMERA_PERMISSION
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {

            Log.d(
                TAG,
                "CAMERA PERMISSION NOT GRANTED"
            )


            pendingIntruderCapture =
                true


            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    CAMERA_PERMISSION
                ),
                CAMERA_REQUEST_CODE
            )


            return
        }


        startCameraAndCapture()
    }


    // =========================================================
    // CAMERA PERMISSION RESULT
    // =========================================================

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )


        if (
            requestCode !=
            CAMERA_REQUEST_CODE
        ) {

            return
        }


        if (
            grantResults.isNotEmpty() &&
            grantResults[0] ==
            PackageManager.PERMISSION_GRANTED
        ) {

            Log.d(
                TAG,
                "CAMERA PERMISSION GRANTED"
            )


            pendingIntruderCapture =
                false


            startCameraAndCapture()

        } else {

            Log.e(
                TAG,
                "CAMERA PERMISSION DENIED"
            )


            pendingIntruderCapture =
                false
        }
    }


    // =========================================================
    // START CAMERA
    // =========================================================

    private fun startCameraAndCapture() {

        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(
                this
            )


        cameraProviderFuture.addListener(

            {

                try {

                    val provider =
                        cameraProviderFuture.get()


                    cameraProvider =
                        provider


                    provider.unbindAll()


                    val cameraSelector =
                        CameraSelector.Builder()
                            .requireLensFacing(
                                CameraSelector
                                    .LENS_FACING_FRONT
                            )
                            .build()


                    val capture =
                        ImageCapture.Builder()
                            .setCaptureMode(
                                ImageCapture
                                    .CAPTURE_MODE_MINIMIZE_LATENCY
                            )
                            .setTargetRotation(
                                windowManager
                                    .defaultDisplay
                                    .rotation
                            )
                            .build()


                    imageCapture =
                        capture


                    provider.bindToLifecycle(
                        this,
                        cameraSelector,
                        capture
                    )


                    Log.d(
                        TAG,
                        "FRONT CAMERA READY"
                    )


                    takeIntruderPhoto(
                        capture
                    )

                } catch (e: Exception) {

                    Log.e(
                        TAG,
                        "CAMERA START ERROR",
                        e
                    )
                }

            },

            ContextCompat.getMainExecutor(
                this
            )
        )
    }


    // =========================================================
    // TAKE INTRUDER PHOTO
    // =========================================================

    private fun takeIntruderPhoto(
        capture: ImageCapture
    ) {

        val fileName =
            "Intruder_" +
                    SimpleDateFormat(
                        "yyyyMMdd_HHmmss",
                        Locale.US
                    ).format(
                        System.currentTimeMillis()
                    ) +
                    ".jpg"


        val contentValues =
            ContentValues().apply {

                put(
                    MediaStore.Images.Media.DISPLAY_NAME,
                    fileName
                )


                put(
                    MediaStore.Images.Media.MIME_TYPE,
                    "image/jpeg"
                )


                if (
                    Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.Q
                ) {

                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        "Pictures/AppLock/Intruder"
                    )


                    put(
                        MediaStore.Images.Media.IS_PENDING,
                        1
                    )
                }
            }


        val outputOptions =
            ImageCapture
                .OutputFileOptions
                .Builder(
                    contentResolver,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )
                .build()


        capture.takePicture(

            outputOptions,

            ContextCompat.getMainExecutor(
                this
            ),

            object :
                ImageCapture
                .OnImageSavedCallback {

                override fun onImageSaved(
                    outputFileResults:
                    ImageCapture.OutputFileResults
                ) {

                    Log.d(
                        TAG,
                        "INTRUDER PHOTO SAVED"
                    )


                    val savedUri =
                        outputFileResults.savedUri


                    if (
                        Build.VERSION.SDK_INT >=
                        Build.VERSION_CODES.Q
                    ) {

                        savedUri?.let { uri ->

                            val values =
                                ContentValues().apply {

                                    put(
                                        MediaStore.Images.Media.IS_PENDING,
                                        0
                                    )
                                }


                            contentResolver.update(
                                uri,
                                values,
                                null,
                                null
                            )
                        }
                    }


                    if (
                        savedUri != null
                    ) {

                        CoroutineScope(
                            Dispatchers.IO
                        ).launch {

                            try {

                                dataStore
                                    .saveIntruderPhoto(
                                        savedUri.toString()
                                    )

                            } catch (e: Exception) {

                                Log.e(
                                    TAG,
                                    "INTRUDER URI DATASTORE ERROR",
                                    e
                                )
                            }
                        }
                    }


                    releaseCamera()
                }


                override fun onError(
                    exception:
                    ImageCaptureException
                ) {

                    Log.e(
                        TAG,
                        "INTRUDER PHOTO ERROR",
                        exception
                    )


                    releaseCamera()
                }
            }
        )
    }


    // =========================================================
    // RELEASE CAMERA
    // =========================================================

    private fun releaseCamera() {

        try {

            cameraProvider
                ?.unbindAll()

        } catch (e: Exception) {

            Log.e(
                TAG,
                "CAMERA RELEASE ERROR",
                e
            )
        }


        imageCapture =
            null

        cameraProvider =
            null
    }


    // =========================================================
    // NEW INTENT
    // =========================================================

    override fun onNewIntent(
        intent: Intent
    ) {

        super.onNewIntent(
            intent
        )


        setIntent(
            intent
        )


        targetPackage =
            intent.getStringExtra(
                "packageName"
            )


        Log.d(
            TAG,
            "NEW TARGET PACKAGE = $targetPackage"
        )


        AppLockServiceHolder
            .isLockScreenOpen = true
    }


    // =========================================================
    // GO HOME
    // =========================================================

    private fun goHome() {

        Log.d(
            TAG,
            "================================"
        )

        Log.d(
            TAG,
            "BACK -> GO HOME"
        )

        Log.d(
            TAG,
            "TARGET PACKAGE = $targetPackage"
        )

        Log.d(
            TAG,
            "================================"
        )


        // -----------------------------------------------------
        // IMPORTANT:
        // Do NOT clear currentUnlockedApp here.
        //
        // Target app is suppressed only until Launcher/Home
        // is detected by AppLockService.
        // -----------------------------------------------------

        AppLockServiceHolder
            .suppressPackage(
                targetPackage
            )


        AppLockServiceHolder
            .isLockScreenOpen = false


        try {

            val homeIntent =
                Intent(
                    Intent.ACTION_MAIN
                ).apply {

                    addCategory(
                        Intent.CATEGORY_HOME
                    )


                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                    )
                }


            startActivity(
                homeIntent
            )


            Log.d(
                TAG,
                "HOME ACTIVITY STARTED"
            )

        } catch (e: Exception) {

            Log.e(
                TAG,
                "HOME OPEN ERROR",
                e
            )
        }


        try {

            finishAffinity()

        } catch (e: Exception) {

            Log.e(
                TAG,
                "FINISH AFFINITY ERROR",
                e
            )
        }


        finishAndRemoveTask()
    }


    // =========================================================
    // OPEN TARGET APP
    // =========================================================

    private fun openApp() {

        val packageName =
            targetPackage


        if (
            packageName.isNullOrEmpty()
        ) {

            Log.e(
                TAG,
                "TARGET PACKAGE NULL"
            )


            goHome()

            return
        }


        try {

            Log.d(
                TAG,
                "OPENING APP = $packageName"
            )


            val appIntent =
                packageManager
                    .getLaunchIntentForPackage(
                        packageName
                    )


            if (
                appIntent == null
            ) {

                Log.e(
                    TAG,
                    "LAUNCH INTENT NULL = $packageName"
                )


                goHome()

                return
            }


            appIntent.addFlags(

                Intent.FLAG_ACTIVITY_NEW_TASK or

                        Intent.FLAG_ACTIVITY_CLEAR_TOP or

                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            )


            startActivity(
                appIntent
            )


            Log.d(
                TAG,
                "TARGET APP STARTED = $packageName"
            )

        } catch (e: Exception) {

            Log.e(
                TAG,
                "OPEN APP ERROR",
                e
            )


            goHome()

            return
        }


        AppLockServiceHolder
            .isLockScreenOpen = false


        finish()
    }


    // =========================================================
    // DESTROY
    // =========================================================

    override fun onDestroy() {

        Log.d(
            TAG,
            "LOCK SCREEN DESTROYED"
        )


        releaseCamera()


        biometricPrompt =
            null

        biometricStarted =
            false


        AppLockServiceHolder
            .isLockScreenOpen = false


        super.onDestroy()
    }
}