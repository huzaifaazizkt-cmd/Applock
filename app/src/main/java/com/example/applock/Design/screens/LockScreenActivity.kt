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
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.applock.data.DataStoreManager
import com.example.applock.service.AppLockServiceHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale


class LockScreenActivity :
    FragmentActivity() {

    private val TAG =
        "APPLOCK_DEBUG"


    private var targetPackage:
            String? =
        null


    // =========================================================
    // DATASTORE
    // =========================================================

    private lateinit var dataStore:
            DataStoreManager


    // =========================================================
    // CAMERA
    // =========================================================

    private var imageCapture:
            ImageCapture? =
        null


    private var pendingIntruderCapture =
        false


    private var cameraProvider:
            ProcessCameraProvider? =
        null


    private val CAMERA_PERMISSION =
        Manifest.permission.CAMERA


    private val CAMERA_REQUEST_CODE =
        501


    // =========================================================
    // CREATE
    // =========================================================

    override fun onCreate(
        savedInstanceState:
        Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )


        // =====================================================
        // DATASTORE
        // =====================================================

        dataStore =
            DataStoreManager(
                this
            )


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

            setShowWhenLocked(
                true
            )

            setTurnScreenOn(
                true
            )
        }


        window.addFlags(

            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
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

        AppLockServiceHolder.isLockScreenOpen =
            true


        // =====================================================
        // BACK = HOME
        // =====================================================

        onBackPressedDispatcher.addCallback(

            this,

            object :
                OnBackPressedCallback(
                    true
                ) {

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

                        return@UnlockScreen
                    }


                    // =================================================
                    // SAVE UNLOCKED APP
                    // =================================================

                    AppLockServiceHolder.currentUnlockedApp =
                        packageName


                    AppLockServiceHolder.lastUnlockTime =
                        System.currentTimeMillis()


                    // =================================================
                    // CLOSE LOCK SCREEN STATE
                    // =================================================

                    AppLockServiceHolder.isLockScreenOpen =
                        false


                    // =================================================
                    // OPEN TARGET APP
                    // =================================================

                    openApp()
                },


                // =================================================
                // INTRUDER CAMERA
                // =================================================

                onIntruderCapture = {

                    Log.d(
                        TAG,
                        "INTRUDER CAMERA REQUEST"
                    )

                    captureIntruderPhoto()
                }
            )
        }
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
    // PERMISSION RESULT
    // =========================================================

    override fun onRequestPermissionsResult(

        requestCode:
        Int,

        permissions:
        Array<out String>,

        grantResults:
        IntArray

    ) {

        super.onRequestPermissionsResult(

            requestCode,

            permissions,

            grantResults
        )


        if (
            requestCode ==
            CAMERA_REQUEST_CODE
        ) {

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
    }


    // =========================================================
    // START CAMERA
    // =========================================================

    private fun startCameraAndCapture() {

        val cameraProviderFuture =
            ProcessCameraProvider
                .getInstance(
                    this
                )


        cameraProviderFuture.addListener({

            try {

                val provider =
                    cameraProviderFuture.get()


                cameraProvider =
                    provider


                provider.unbindAll()


                // =================================================
                // FRONT CAMERA
                // =================================================

                val cameraSelector =
                    CameraSelector.Builder()
                        .requireLensFacing(
                            CameraSelector
                                .LENS_FACING_FRONT
                        )
                        .build()


                // =================================================
                // IMAGE CAPTURE
                // =================================================

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


                // =================================================
                // BIND CAMERA
                // =================================================

                provider.bindToLifecycle(

                    this,

                    cameraSelector,

                    capture
                )


                Log.d(
                    TAG,
                    "FRONT CAMERA READY"
                )


                // =================================================
                // CAPTURE
                // =================================================

                takeIntruderPhoto(
                    capture
                )

            } catch (
                e: Exception
            ) {

                Log.e(
                    TAG,
                    "CAMERA START ERROR",
                    e
                )
            }

        }, ContextCompat.getMainExecutor(this))
    }


    // =========================================================
    // TAKE INTRUDER PHOTO
    // =========================================================

    private fun takeIntruderPhoto(
        capture:
        ImageCapture
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
                    ImageCapture
                    .OutputFileResults

                ) {

                    Log.d(
                        TAG,
                        "================================"
                    )

                    Log.d(
                        TAG,
                        "INTRUDER PHOTO SAVED"
                    )

                    Log.d(
                        TAG,
                        "FILE = ${outputFileResults.savedUri}"
                    )

                    Log.d(
                        TAG,
                        "================================"
                    )


                    // =================================================
                    // GET SAVED URI
                    // =================================================

                    val savedUri =
                        outputFileResults.savedUri


                    // =================================================
                    // COMPLETE MEDIASTORE ITEM
                    // =================================================

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


                    // =================================================
                    // SAVE PHOTO URI IN DATASTORE
                    // =================================================

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


                                Log.d(
                                    TAG,
                                    "INTRUDER URI SAVED IN DATASTORE = $savedUri"
                                )

                            } catch (
                                e: Exception
                            ) {

                                Log.e(
                                    TAG,
                                    "INTRUDER URI DATASTORE ERROR",
                                    e
                                )
                            }
                        }
                    }


                    // =================================================
                    // RELEASE CAMERA
                    // =================================================

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

        } catch (
            e: Exception
        ) {

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
        intent:
        Intent
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


        AppLockServiceHolder.isLockScreenOpen =
            true
    }


    // =========================================================
    // GO HOME
    // =========================================================

    private fun goHome() {

        Log.d(
            TAG,
            "GO HOME"
        )


        AppLockServiceHolder.clear()


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
                                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                    )
                }


            startActivity(
                homeIntent
            )

        } catch (
            e: Exception
        ) {

            Log.e(
                TAG,
                "HOME OPEN ERROR",
                e
            )
        }


        try {

            finishAffinity()

        } catch (
            e: Exception
        ) {

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

        } catch (
            e: Exception
        ) {

            Log.e(
                TAG,
                "OPEN APP ERROR",
                e
            )

            goHome()

            return
        }


        try {

            finishAffinity()

        } catch (
            e: Exception
        ) {

            Log.e(
                TAG,
                "FINISH AFFINITY ERROR",
                e
            )
        }


        finishAndRemoveTask()
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


        /*
         * Do not clear currentUnlockedApp here.
         */

        AppLockServiceHolder.isLockScreenOpen =
            false


        super.onDestroy()
    }
}