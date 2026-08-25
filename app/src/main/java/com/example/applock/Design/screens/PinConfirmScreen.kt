package com.example.applock.Design.screens

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.example.applock.Design.components.NumberPad
import com.example.applock.R
import com.example.applock.data.DataStoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sqrt


// =============================================================
// PIN CONFIRM SCREEN
// =============================================================

@Composable
fun PinConfirmScreen(
    navController: NavController,
    context: Context,
    type: String,
    value: String
) {

    val scope =
        rememberCoroutineScope()


    val appContext =
        remember {
            context.applicationContext
        }


    val dataStore =
        remember {
            DataStoreManager(
                appContext
            )
        }


    val lifecycleOwner =
        LocalLifecycleOwner.current


    // =========================================================
    // START APP LIST PRELOAD IMMEDIATELY
    //
    // APPS + ICONS dono yahin preload honge.
    // =========================================================

    LaunchedEffect(Unit) {

        AppListCache.preload(
            appContext
        )
    }


    // =========================================================
    // CONFIRM STATES
    // =========================================================

    var confirmPin by remember {
        mutableStateOf("")
    }

    var confirmPattern by remember {
        mutableStateOf<List<Int>>(emptyList())
    }

    var error by remember {
        mutableStateOf("")
    }

    var patternError by remember {
        mutableStateOf(false)
    }

    var patternConfirmed by remember {
        mutableStateOf(false)
    }

    var clearErrorJob by remember {
        mutableStateOf<Job?>(null)
    }


    // =========================================================
    // PERMISSION DIALOG
    // =========================================================

    var showPermissionDialog by remember {
        mutableStateOf(false)
    }


    // =========================================================
    // SECURITY QUESTION DIALOG
    // =========================================================

    var showSecurityDialog by remember {
        mutableStateOf(false)
    }


    // =========================================================
    // NAVIGATION GUARD
    // =========================================================

    var isNavigatingToAppList by remember {
        mutableStateOf(false)
    }


    // =========================================================
    // PERMISSION STATES
    // =========================================================

    var overlayAllowed by remember {

        mutableStateOf(
            Settings.canDrawOverlays(
                context
            )
        )
    }


    var accessibilityAllowed by remember {

        mutableStateOf(
            isAccessibilityServiceEnabled(
                context
            )
        )
    }


    var autoStartAllowed by remember {

        mutableStateOf(false)
    }


    // =========================================================
    // SECURITY QUESTION STATES
    // =========================================================

    var selectedQuestion by remember {
        mutableStateOf("")
    }

    var securityAnswer by remember {
        mutableStateOf("")
    }

    var securityDropdownExpanded by remember {
        mutableStateOf(false)
    }


    // =========================================================
    // TYPE
    // =========================================================

    val isPattern =
        type.equals(
            "pattern",
            ignoreCase = true
        )


    // =========================================================
    // COLORS
    // =========================================================

    val backgroundColor =
        Color(0xFF29A0F0)

    val numberButtonColor =
        Color(0xFF69B9F3)

    val patternDotColor =
        Color(0xFF83CCFF)

    val errorColor =
        Color.Red


    val pinLength =
        value.length


    // =========================================================
    // CHECK PERMISSIONS
    // =========================================================

    fun checkPermissions() {

        overlayAllowed =
            Settings.canDrawOverlays(
                context
            )

        accessibilityAllowed =
            isAccessibilityServiceEnabled(
                context
            )
    }


    // =========================================================
    // ALL PERMISSIONS
    // =========================================================

    fun allPermissionsAllowed(): Boolean {

        return overlayAllowed &&
                accessibilityAllowed &&
                autoStartAllowed
    }


    // =========================================================
    // OPEN SECURITY QUESTION
    // =========================================================

    fun openSecurityQuestion() {

        showPermissionDialog =
            false

        selectedQuestion =
            ""

        securityAnswer =
            ""

        securityDropdownExpanded =
            false

        showSecurityDialog =
            true
    }


    // =========================================================
    // CURRENT PERMISSION DIALOG STATE
    // =========================================================

    val currentShowPermissionDialog by
    rememberUpdatedState(
        showPermissionDialog
    )


    // =========================================================
    // LIFECYCLE
    // =========================================================

    DisposableEffect(
        lifecycleOwner
    ) {

        val observer =
            LifecycleEventObserver {
                    _,
                    event ->

                if (
                    event ==
                    Lifecycle.Event.ON_RESUME
                ) {

                    if (
                        currentShowPermissionDialog
                    ) {

                        val newOverlayAllowed =
                            Settings.canDrawOverlays(
                                context
                            )

                        val newAccessibilityAllowed =
                            isAccessibilityServiceEnabled(
                                context
                            )


                        overlayAllowed =
                            newOverlayAllowed

                        accessibilityAllowed =
                            newAccessibilityAllowed


                        if (
                            newOverlayAllowed &&
                            newAccessibilityAllowed &&
                            autoStartAllowed
                        ) {

                            showPermissionDialog =
                                false

                            showSecurityDialog =
                                true
                        }
                    }
                }
            }


        lifecycleOwner
            .lifecycle
            .addObserver(
                observer
            )


        onDispose {

            lifecycleOwner
                .lifecycle
                .removeObserver(
                    observer
                )
        }
    }


    // =========================================================
    // PERMISSION DIALOG OPEN CHECK
    // =========================================================

    LaunchedEffect(
        showPermissionDialog
    ) {

        if (
            showPermissionDialog
        ) {

            checkPermissions()

            if (
                allPermissionsAllowed()
            ) {

                showPermissionDialog =
                    false

                showSecurityDialog =
                    true
            }
        }
    }


    // =========================================================
    // AUTO MOVE TO SECURITY QUESTION
    // =========================================================

    LaunchedEffect(
        overlayAllowed,
        accessibilityAllowed,
        autoStartAllowed
    ) {

        if (
            showPermissionDialog &&
            overlayAllowed &&
            accessibilityAllowed &&
            autoStartAllowed
        ) {

            showPermissionDialog =
                false

            showSecurityDialog =
                true
        }
    }


    // =========================================================
    // CLEAR PATTERN ERROR
    // =========================================================

    fun clearPatternError() {

        clearErrorJob?.cancel()

        clearErrorJob =
            null

        patternError =
            false

        error =
            ""

        confirmPattern =
            emptyList()
    }


    // =========================================================
    // SHOW PATTERN ERROR
    // =========================================================

    fun showPatternError() {

        clearErrorJob?.cancel()

        patternError =
            true

        patternConfirmed =
            false

        error =
            "Password does not match. Please try again"

        clearErrorJob =
            scope.launch {

                delay(1500)

                confirmPattern =
                    emptyList()

                patternError =
                    false

                error =
                    ""

                clearErrorJob =
                    null
            }
    }


    // =========================================================
    // SAVE PIN
    // =========================================================

    fun continueWithPin() {

        checkPermissions()

        showPermissionDialog =
            true

        scope.launch(Dispatchers.IO) {

            try {

                dataStore.savePin(
                    value
                )

                dataStore.saveAuthType(
                    "pin"
                )

            } catch (e: Exception) {

                e.printStackTrace()
            }
        }
    }


    // =========================================================
    // SAVE PATTERN
    // =========================================================

    fun continueWithPattern() {

        checkPermissions()

        showPermissionDialog =
            true

        scope.launch(Dispatchers.IO) {

            try {

                dataStore.savePattern(
                    value
                )

                dataStore.saveAuthType(
                    "pattern"
                )

            } catch (e: Exception) {

                e.printStackTrace()
            }
        }
    }


    // =========================================================
    // OPEN OVERLAY
    // =========================================================

    fun openOverlayPermission() {

        try {

            val intent =
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse(
                        "package:${context.packageName}"
                    )
                )

            context.startActivity(
                intent
            )

        } catch (e: Exception) {

            try {

                context.startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION
                    )
                )

            } catch (e2: Exception) {

                try {

                    context.startActivity(
                        Intent(
                            Settings.ACTION_SETTINGS
                        )
                    )

                } catch (e3: Exception) {

                    e3.printStackTrace()
                }
            }
        }
    }


    // =========================================================
    // OPEN ACCESSIBILITY
    // =========================================================

    fun openAccessibilitySettings() {

        try {

            context.startActivity(
                Intent(
                    Settings.ACTION_ACCESSIBILITY_SETTINGS
                )
            )

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }


    // =========================================================
    // GO TO APP LIST
    //
    // IMPORTANT:
    // No delay.
    // No DataStore wait.
    // Direct navigation.
    // =========================================================

    fun goToAppList() {

        if (
            isNavigatingToAppList
        ) {
            return
        }


        isNavigatingToAppList =
            true


        showSecurityDialog =
            false

        showPermissionDialog =
            false


        navController.navigate(
            "appList"
        ) {

            popUpTo("create") {

                inclusive =
                    true
            }

            launchSingleTop =
                true
        }
    }


    // =============================================================
    // MAIN UI
    // =============================================================

    Box(

        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    backgroundColor
                )
    ) {

        Column(

            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = 30.dp
                    ),

            horizontalAlignment =
                Alignment.CenterHorizontally,

            verticalArrangement =
                Arrangement.Center
        ) {


            // =================================================
            // HEADING
            // =================================================

            Text(

                text =
                    if (isPattern) {

                        "Confirm pattern"

                    } else {

                        "Confirm passcode"
                    },

                color =
                    Color.White,

                fontSize =
                    25.sp
            )


            Spacer(
                modifier =
                    Modifier.height(
                        28.dp
                    )
            )


            // =================================================
            // STEP INDICATOR
            // =================================================

            Row(

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Box(

                    modifier =
                        Modifier
                            .size(25.dp)
                            .border(
                                width = 2.dp,
                                color = Color.White,
                                shape = CircleShape
                            ),

                    contentAlignment =
                        Alignment.Center
                ) {

                    Text(
                        text = "1",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }


                Box(

                    modifier =
                        Modifier
                            .width(95.dp)
                            .height(2.dp)
                            .background(
                                Color.White
                            )
                )


                Box(

                    modifier =
                        Modifier
                            .size(31.dp)
                            .background(
                                Color.White.copy(
                                    alpha = 0.08f
                                ),
                                CircleShape
                            )
                            .border(
                                width = 2.dp,
                                color =
                                    Color.White.copy(
                                        alpha = 0.35f
                                    ),
                                shape = CircleShape
                            ),

                    contentAlignment =
                        Alignment.Center
                ) {

                    Box(

                        modifier =
                            Modifier
                                .size(21.dp)
                                .background(
                                    Color.White,
                                    CircleShape
                                ),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        Text(
                            text = "2",
                            color = backgroundColor,
                            fontSize = 15.sp
                        )
                    }
                }
            }


            Spacer(
                modifier =
                    Modifier.height(
                        40.dp
                    )
            )


            // =================================================
            // PATTERN
            // =================================================

            if (isPattern) {

                Text(

                    text =
                        "Draw pattern again",

                    color =
                        Color.White,

                    fontSize =
                        18.sp
                )


                Box(

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(24.dp),

                    contentAlignment =
                        Alignment.Center
                ) {

                    if (
                        error.isNotEmpty()
                    ) {

                        Text(

                            text =
                                error,

                            color =
                                errorColor,

                            fontSize =
                                15.sp
                        )
                    }
                }


                Spacer(
                    modifier =
                        Modifier.height(
                            20.dp
                        )
                )


                ConfirmPatternGrid(

                    selectedDots =
                        confirmPattern,

                    isError =
                        patternError,

                    onPatternChanged = {
                            dots ->

                        confirmPattern =
                            dots

                        if (
                            patternError
                        ) {

                            clearErrorJob?.cancel()

                            clearErrorJob =
                                null

                            patternError =
                                false

                            error =
                                ""
                        }
                    },

                    onPatternFinished = {
                            pattern ->

                        val originalPattern =
                            value
                                .split("-")
                                .mapNotNull {
                                    it.toIntOrNull()
                                }


                        if (
                            pattern.size < 4
                        ) {

                            showPatternError()

                            return@ConfirmPatternGrid
                        }


                        if (
                            pattern !=
                            originalPattern
                        ) {

                            showPatternError()

                            return@ConfirmPatternGrid
                        }


                        clearErrorJob?.cancel()

                        clearErrorJob =
                            null

                        patternError =
                            false

                        error =
                            ""

                        confirmPattern =
                            pattern

                        patternConfirmed =
                            true
                    },

                    dotColor =
                        patternDotColor,

                    errorColor =
                        errorColor,

                    backgroundColor =
                        backgroundColor
                )


                Spacer(
                    modifier =
                        Modifier.height(
                            35.dp
                        )
                )


                Row(

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                end = 30.dp
                            ),

                    horizontalArrangement =
                        Arrangement.End
                ) {

                    Text(

                        text =
                            "Continue",

                        color =
                            if (
                                patternConfirmed
                            ) {

                                Color.White

                            } else {

                                Color.White.copy(
                                    alpha = 0.35f
                                )
                            },

                        fontSize =
                            20.sp,

                        modifier =
                            Modifier
                                .clickable(
                                    enabled =
                                        patternConfirmed
                                ) {

                                    continueWithPattern()
                                }
                                .padding(
                                    10.dp
                                )
                    )
                }

            }


            // =================================================
            // PIN
            // =================================================

            else {

                Text(

                    text =
                        "Confirm $pinLength - Digit pin",

                    color =
                        Color.White,

                    fontSize =
                        18.sp
                )


                Spacer(
                    modifier =
                        Modifier.height(
                            18.dp
                        )
                )


                Row(

                    horizontalArrangement =
                        Arrangement.spacedBy(
                            14.dp
                        )
                ) {

                    repeat(
                        pinLength
                    ) { index ->

                        Box(

                            modifier =
                                Modifier
                                    .size(20.dp)
                                    .background(

                                        if (
                                            index <
                                            confirmPin.length
                                        ) {

                                            Color.White

                                        } else {

                                            Color.White.copy(
                                                alpha = 0.45f
                                            )
                                        },

                                        CircleShape
                                    )
                        )
                    }
                }


                Spacer(
                    modifier =
                        Modifier.height(
                            20.dp
                        )
                )


                Box(

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(20.dp),

                    contentAlignment =
                        Alignment.Center
                ) {

                    if (
                        error.isNotEmpty()
                    ) {

                        Text(

                            text =
                                error,

                            color =
                                Color.White,

                            fontSize =
                                14.sp
                        )
                    }
                }


                Spacer(
                    modifier =
                        Modifier.height(
                            30.dp
                        )
                )


                NumberPad(

                    onNumberClick = {
                            number ->

                        if (
                            confirmPin.length <
                            pinLength
                        ) {

                            confirmPin +=
                                number

                            error =
                                ""
                        }
                    },

                    onDelete = {

                        if (
                            confirmPin.isNotEmpty()
                        ) {

                            confirmPin =
                                confirmPin.dropLast(
                                    1
                                )

                            error =
                                ""
                        }
                    },

                    buttonColor =
                        numberButtonColor
                )


                Spacer(
                    modifier =
                        Modifier.height(
                            55.dp
                        )
                )


                Row(

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                end = 30.dp
                            ),

                    horizontalArrangement =
                        Arrangement.End
                ) {

                    Text(

                        text =
                            "Continue",

                        color =
                            if (
                                confirmPin.length ==
                                pinLength
                            ) {

                                Color.White

                            } else {

                                Color.White.copy(
                                    alpha = 0.35f
                                )
                            },

                        fontSize =
                            20.sp,

                        modifier =
                            Modifier
                                .clickable(

                                    enabled =
                                        confirmPin.length ==
                                                pinLength

                                ) {

                                    if (
                                        confirmPin !=
                                        value
                                    ) {

                                        error =
                                            "Enter your correct password"

                                        confirmPin =
                                            ""

                                        return@clickable
                                    }


                                    continueWithPin()
                                }
                                .padding(
                                    10.dp
                                )
                    )
                }
            }
        }


        // =========================================================
        // PERMISSION DIALOG
        // =========================================================

        if (
            showPermissionDialog
        ) {

            PermissionRequiredDialog(

                overlayAllowed =
                    overlayAllowed,

                accessibilityAllowed =
                    accessibilityAllowed,

                autoStartAllowed =
                    autoStartAllowed,

                onOverlayAllow = {

                    openOverlayPermission()
                },

                onAccessibilityAllow = {

                    openAccessibilitySettings()
                },

                onAutoStartAllow = {

                    autoStartAllowed =
                        true
                },

                onDone = {
                }
            )
        }


        // =========================================================
        // SECURITY QUESTION DIALOG
        // =========================================================

        if (
            showSecurityDialog
        ) {

            SecurityQuestionDialog(

                selectedQuestion =
                    selectedQuestion,

                answer =
                    securityAnswer,

                dropdownExpanded =
                    securityDropdownExpanded,

                onDropdownClick = {

                    securityDropdownExpanded =
                        !securityDropdownExpanded
                },

                onQuestionSelected = {
                        question ->

                    selectedQuestion =
                        question

                    securityDropdownExpanded =
                        false
                },

                onAnswerChanged = {
                        newAnswer ->

                    securityAnswer =
                        newAnswer
                },

                onSkip = {

                    // FORAN APP LIST
                    goToAppList()
                },

                onSave = {

                    val question =
                        selectedQuestion

                    val answer =
                        securityAnswer.trim()


                    // FORAN APP LIST
                    goToAppList()


                    // Background save
                    scope.launch(
                        Dispatchers.IO
                    ) {

                        try {

                            dataStore
                                .saveSecurityQuestion(
                                    question
                                )

                            dataStore
                                .saveSecurityAnswer(
                                    answer
                                )

                        } catch (e: Exception) {

                            e.printStackTrace()
                        }
                    }
                }
            )
        }
    }
}


// =============================================================
// PERMISSION REQUIRED DIALOG
// =============================================================

@Composable
private fun PermissionRequiredDialog(

    overlayAllowed: Boolean,

    accessibilityAllowed: Boolean,

    autoStartAllowed: Boolean,

    onOverlayAllow: () -> Unit,

    onAccessibilityAllow: () -> Unit,

    onAutoStartAllow: () -> Unit,

    onDone: () -> Unit

) {

    Dialog(

        onDismissRequest = {
        },

        properties =
            DialogProperties(

                dismissOnBackPress =
                    false,

                dismissOnClickOutside =
                    false,

                usePlatformDefaultWidth =
                    false
            )
    ) {

        Box(

            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Color.Transparent
                    ),

            contentAlignment =
                Alignment.Center
        ) {

            Column(

                modifier =
                    Modifier
                        .fillMaxWidth(0.90f)
                        .wrapContentHeight()
                        .background(
                            Color.White,
                            RoundedCornerShape(20.dp)
                        )
                        .padding(
                            start = 25.dp,
                            end = 25.dp,
                            top = 24.dp,
                            bottom = 24.dp
                        )
            ) {

                Text(

                    text =
                        "Permissions Required",

                    color =
                        Color(0xFF333333),

                    fontSize =
                        20.sp,

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                bottom = 24.dp
                            ),

                    textAlign =
                        TextAlign.Center
                )


                PermissionRow(

                    icon = {

                        Image(

                            painter =
                                painterResource(
                                    id =
                                        R.drawable.group1
                                ),

                            contentDescription =
                                "Show Over Other Apps",

                            modifier =
                                Modifier.size(
                                    20.dp
                                ),

                            contentScale =
                                ContentScale.Fit
                        )
                    },

                    title =
                        "Show Over Other Apps",

                    description =
                        "Allow Lock Screen to show over\nother apps",

                    allowed =
                        overlayAllowed,

                    onAllow =
                        onOverlayAllow
                )


                PermissionDivider()


                PermissionRow(

                    icon = {

                        Image(

                            painter =
                                painterResource(
                                    id =
                                        R.drawable.group2
                                ),

                            contentDescription =
                                "Detect Launched App",

                            modifier =
                                Modifier.size(
                                    20.dp
                                ),

                            contentScale =
                                ContentScale.Fit
                        )
                    },

                    title =
                        "Detect Launched App",

                    description =
                        "Permit to detect which app is\nlaunched by granting access to...",

                    allowed =
                        accessibilityAllowed,

                    onAllow =
                        onAccessibilityAllow
                )


                PermissionDivider()


                PermissionRow(

                    icon = {

                        Image(

                            painter =
                                painterResource(
                                    id =
                                        R.drawable.group3
                                ),

                            contentDescription =
                                "Auto Start",

                            modifier =
                                Modifier.size(
                                    20.dp
                                ),

                            contentScale =
                                ContentScale.Fit
                        )
                    },

                    title =
                        "Auto Start",

                    description =
                        "Always Keep AppLock Pro Running",

                    allowed =
                        autoStartAllowed,

                    onAllow =
                        onAutoStartAllow
                )


                PermissionDivider()


                Text(

                    text =
                        "Permissions are required for the application to work\nproperly and efficiently",

                    color =
                        Color(0xFFBDBDBD),

                    fontSize =
                        12.sp,

                    lineHeight =
                        13.sp,

                    modifier =
                        Modifier.padding(
                            start = 12.dp,
                            top = 20.dp
                        )
                )
            }
        }
    }
}


// =============================================================
// PERMISSION ROW
// =============================================================

@Composable
private fun PermissionRow(

    icon:
    @Composable () -> Unit,

    title: String,

    description: String,

    allowed: Boolean,

    onAllow: () -> Unit

) {

    Row(

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 8.dp
                ),

        verticalAlignment =
            Alignment.Top
    ) {

        Box(

            modifier =
                Modifier
                    .width(32.dp)
                    .padding(top = 4.dp),

            contentAlignment =
                Alignment.Center
        ) {

            icon()
        }


        Column(

            modifier =
                Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
        ) {

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(

                    text =
                        title,

                    color =
                        Color(0xFF333333),

                    fontSize =
                        15.sp,

                    modifier =
                        Modifier.weight(
                            1f
                        )
                )


                Icon(

                    imageVector =
                        Icons.Outlined.KeyboardArrowDown,

                    contentDescription =
                        null,

                    tint =
                        Color(0xFFB5B5B5),

                    modifier =
                        Modifier.size(
                            18.dp
                        )
                )
            }


            Spacer(
                modifier =
                    Modifier.height(
                        7.dp
                    )
            )


            Text(

                text =
                    description,

                color =
                    Color(0xFFBDBDBD),

                fontSize =
                    13.sp,

                lineHeight =
                    18.sp
            )
        }


        Box(

            modifier =
                Modifier
                    .padding(top = 8.dp)
                    .width(72.dp)
                    .height(40.dp)
                    .background(

                        color =
                            if (allowed) {

                                Color(0xFF4CAF50)

                            } else {

                                Color(0xFF2196F3)
                            },

                        shape =
                            RoundedCornerShape(
                                4.dp
                            )
                    )
                    .clickable(
                        enabled = !allowed
                    ) {

                        if (!allowed) {

                            onAllow()
                        }
                    },

            contentAlignment =
                Alignment.Center
        ) {

            Text(

                text =
                    if (allowed) {

                        "Allowed"

                    } else {

                        "Allow"
                    },

                color =
                    Color.White,

                fontSize =
                    13.sp,

                textAlign =
                    TextAlign.Center
            )
        }
    }
}


// =============================================================
// DIVIDER
// =============================================================

@Composable
private fun PermissionDivider() {

    Spacer(
        modifier =
            Modifier.height(
                9.dp
            )
    )


    Box(

        modifier =
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Color(0xFFE5E5E5)
                )
    )


    Spacer(
        modifier =
            Modifier.height(
                9.dp
            )
    )
}


// =============================================================
// SECURITY QUESTION DIALOG
// =============================================================

@Composable
private fun SecurityQuestionDialog(

    selectedQuestion: String,

    answer: String,

    dropdownExpanded: Boolean,

    onDropdownClick: () -> Unit,

    onQuestionSelected:
        (String) -> Unit,

    onAnswerChanged:
        (String) -> Unit,

    onSkip: () -> Unit,

    onSave: () -> Unit

) {

    val questions =
        listOf(

            "What is your Name ?",

            "What is your father name ?",

            "What is your Pet Name?",

            "What is your dream job?"
        )


    Dialog(

        onDismissRequest = {
        },

        properties =
            DialogProperties(

                dismissOnBackPress =
                    false,

                dismissOnClickOutside =
                    false,

                usePlatformDefaultWidth =
                    false
            )
    ) {

        Box(

            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Color.Transparent
                    ),

            contentAlignment =
                Alignment.Center
        ) {

            Column(

                modifier =
                    Modifier
                        .fillMaxWidth(0.90f)
                        .wrapContentHeight()
                        .background(
                            Color.White,
                            RoundedCornerShape(26.dp)
                        )
                        .padding(
                            start = 25.dp,
                            end = 25.dp,
                            top = 30.dp,
                            bottom = 30.dp
                        )
            ) {

                Text(

                    text =
                        "Security Questions",

                    color =
                        Color(0xFF333333),

                    fontSize =
                        19.sp,

                    modifier =
                        Modifier.fillMaxWidth(),

                    textAlign =
                        TextAlign.Center
                )


                Spacer(
                    modifier =
                        Modifier.height(
                            8.dp
                        )
                )


                Text(

                    text =
                        "Set a keyword to Recover your passcode when you\nforget it.",

                    color =
                        Color(0xFFBDBDBD),

                    fontSize =
                        11.sp,

                    lineHeight =
                        18.sp,

                    modifier =
                        Modifier.fillMaxWidth(),

                    textAlign =
                        TextAlign.Center
                )


                Spacer(
                    modifier =
                        Modifier.height(
                            28.dp
                        )
                )


                Text(

                    text =
                        "Select Security Questions",

                    color =
                        Color(0xFF333333),

                    fontSize =
                        14.sp
                )


                Spacer(
                    modifier =
                        Modifier.height(
                            8.dp
                        )
                )


                Box(

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                ) {

                    Row(

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .background(
                                    Color(0xFFF8F8F8),
                                    RoundedCornerShape(
                                        15.dp
                                    )
                                )
                                .clickable {
                                    onDropdownClick()
                                }
                                .padding(
                                    start = 18.dp,
                                    end = 14.dp
                                ),

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Text(

                            text =
                                if (
                                    selectedQuestion.isEmpty()
                                ) {

                                    "Select Security Question"

                                } else {

                                    selectedQuestion
                                },

                            color =
                                if (
                                    selectedQuestion.isEmpty()
                                ) {

                                    Color(0xFFBDBDBD)

                                } else {

                                    Color(0xFF333333)
                                },

                            fontSize =
                                14.sp,

                            maxLines =
                                1,

                            modifier =
                                Modifier.weight(
                                    1f
                                )
                        )


                        Icon(

                            imageVector =
                                Icons.Outlined.KeyboardArrowDown,

                            contentDescription =
                                "Select Security Question",

                            tint =
                                Color(0xFF8F8F8F),

                            modifier =
                                Modifier.size(
                                    22.dp
                                )
                        )
                    }


                    if (
                        dropdownExpanded
                    ) {

                        Popup(

                            alignment =
                                Alignment.TopEnd,

                            onDismissRequest = {
                                onDropdownClick()
                            },

                            properties =
                                PopupProperties(
                                    focusable = true
                                )
                        ) {

                            Column(

                                modifier =
                                    Modifier
                                        .width(210.dp)
                                        .background(
                                            Color.White,
                                            RoundedCornerShape(
                                                15.dp
                                            )
                                        )
                                        .border(
                                            width = 1.dp,
                                            color =
                                                Color(
                                                    0xFFE5E5E5
                                                ),
                                            shape =
                                                RoundedCornerShape(
                                                    15.dp
                                                )
                                        )
                            ) {

                                questions.forEachIndexed {
                                        index,
                                        question ->

                                    Text(

                                        text =
                                            question,

                                        color =
                                            Color(
                                                0xFF333333
                                            ),

                                        fontSize =
                                            13.sp,

                                        maxLines =
                                            1,

                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .clickable {

                                                    onQuestionSelected(
                                                        question
                                                    )
                                                }
                                                .padding(
                                                    horizontal = 18.dp,
                                                    vertical = 14.dp
                                                )
                                    )


                                    if (
                                        index <
                                        questions.lastIndex
                                    ) {

                                        Box(

                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .height(1.dp)
                                                    .background(
                                                        Color(
                                                            0xFFF0F0F0
                                                        )
                                                    )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }


                Spacer(
                    modifier =
                        Modifier.height(
                            23.dp
                        )
                )


                Text(

                    text =
                        "Enter Security Answer",

                    color =
                        Color(0xFF333333),

                    fontSize =
                        14.sp
                )


                Spacer(
                    modifier =
                        Modifier.height(
                            8.dp
                        )
                )


                BasicTextField(

                    value =
                        answer,

                    onValueChange =
                        onAnswerChanged,

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .background(
                                Color(0xFFF8F8F8),
                                RoundedCornerShape(
                                    15.dp
                                )
                            )
                            .padding(
                                horizontal = 15.dp
                            ),

                    singleLine =
                        true,

                    textStyle =
                        TextStyle(

                            color =
                                Color(0xFF333333),

                            fontSize =
                                12.sp,

                            lineHeight =
                                16.sp
                        ),

                    decorationBox = {
                            innerTextField ->

                        Box(

                            modifier =
                                Modifier.fillMaxSize(),

                            contentAlignment =
                                Alignment.CenterStart
                        ) {

                            if (
                                answer.isEmpty()
                            ) {

                                Text(

                                    text =
                                        "Enter your answer",

                                    color =
                                        Color(
                                            0xFFBDBDBD
                                        ),

                                    fontSize =
                                        12.sp,

                                    maxLines =
                                        1
                                )
                            }


                            innerTextField()
                        }
                    }
                )


                Spacer(
                    modifier =
                        Modifier.height(
                            30.dp
                        )
                )


                Row(

                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.End,

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Text(

                        text =
                            "Skip",

                        color =
                            Color(0xFF2196F3),

                        fontSize =
                            16.sp,

                        modifier =
                            Modifier
                                .clickable {
                                    onSkip()
                                }
                                .padding(
                                    horizontal = 20.dp,
                                    vertical = 10.dp
                                )
                    )


                    Spacer(
                        modifier =
                            Modifier.width(
                                20.dp
                            )
                    )


                    val saveEnabled =
                        selectedQuestion.isNotEmpty() &&
                                answer.trim().isNotEmpty()


                    Text(

                        text =
                            "Save",

                        color =
                            if (saveEnabled) {

                                Color(0xFF2196F3)

                            } else {

                                Color(0xFF90CAF9)
                            },

                        fontSize =
                            16.sp,

                        modifier =
                            Modifier
                                .clickable(
                                    enabled =
                                        saveEnabled
                                ) {

                                    onSave()
                                }
                                .padding(
                                    horizontal = 20.dp,
                                    vertical = 10.dp
                                )
                    )
                }
            }
        }
    }
}


// =============================================================
// ACCESSIBILITY SERVICE CHECK
// =============================================================

private fun isAccessibilityServiceEnabled(
    context: Context
): Boolean {

    return try {

        val enabledServices =
            Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )


        if (
            enabledServices.isNullOrEmpty()
        ) {

            false

        } else {

            val packageName =
                context.packageName


            enabledServices
                .split(":")
                .any { serviceName ->

                    val component =
                        ComponentName
                            .unflattenFromString(
                                serviceName
                            )


                    component
                        ?.packageName
                        ?.equals(
                            packageName,
                            ignoreCase = true
                        ) == true
                }
        }

    } catch (e: Exception) {

        e.printStackTrace()

        false
    }
}


// =============================================================
// CONFIRM PATTERN GRID
// =============================================================

@Composable
private fun ConfirmPatternGrid(

    selectedDots: List<Int>,

    isError: Boolean,

    onPatternChanged:
        (List<Int>) -> Unit,

    onPatternFinished:
        (List<Int>) -> Unit,

    dotColor: Color =
        Color(0xFF83CCFF),

    errorColor: Color =
        Color.Red,

    backgroundColor: Color =
        Color(0xFF29A0F0)

) {

    val latestOnPatternChanged by
    rememberUpdatedState(
        onPatternChanged
    )


    val latestOnPatternFinished by
    rememberUpdatedState(
        onPatternFinished
    )


    Box(

        modifier =
            Modifier
                .size(300.dp)
                .pointerInput(Unit) {

                    var currentDots =
                        mutableListOf<Int>()

                    var patternFinished =
                        false


                    detectDragGestures(

                        onDragStart = {
                                offset ->

                            currentDots =
                                mutableListOf()

                            patternFinished =
                                false


                            val dot =
                                findConfirmDot(

                                    touch =
                                        offset,

                                    width =
                                        size.width.toFloat(),

                                    height =
                                        size.height.toFloat()
                                )


                            if (
                                dot != null
                            ) {

                                currentDots.add(
                                    dot
                                )

                                latestOnPatternChanged(
                                    currentDots.toList()
                                )
                            }
                        },


                        onDrag = {
                                change,
                                _ ->

                            change.consume()


                            val dot =
                                findConfirmDot(

                                    touch =
                                        change.position,

                                    width =
                                        size.width.toFloat(),

                                    height =
                                        size.height.toFloat()
                                )


                            if (
                                dot != null &&
                                !currentDots.contains(
                                    dot
                                )
                            ) {

                                currentDots.add(
                                    dot
                                )

                                latestOnPatternChanged(
                                    currentDots.toList()
                                )
                            }
                        },


                        onDragEnd = {

                            if (
                                !patternFinished
                            ) {

                                patternFinished =
                                    true


                                val finalPattern =
                                    currentDots.toList()


                                if (
                                    finalPattern.isNotEmpty()
                                ) {

                                    latestOnPatternFinished(
                                        finalPattern
                                    )
                                }
                            }
                        },


                        onDragCancel = {

                            if (
                                !patternFinished
                            ) {

                                patternFinished =
                                    true


                                val finalPattern =
                                    currentDots.toList()


                                if (
                                    finalPattern.isNotEmpty()
                                ) {

                                    latestOnPatternFinished(
                                        finalPattern
                                    )
                                }
                            }
                        }
                    )
                }

    ) {

        Canvas(

            modifier =
                Modifier.fillMaxSize()

        ) {

            val positions =
                getConfirmPositions(

                    width =
                        size.width,

                    height =
                        size.height
                )


            if (
                selectedDots.size >= 2
            ) {

                for (
                i in 0 until
                        selectedDots.size - 1
                ) {

                    drawLine(

                        color =
                            if (isError) {

                                errorColor

                            } else {

                                Color.White
                            },

                        start =
                            positions[
                                selectedDots[i]
                            ],

                        end =
                            positions[
                                selectedDots[i + 1]
                            ],

                        strokeWidth =
                            8f
                    )
                }
            }


            positions.forEachIndexed {
                    index,
                    position ->

                val selected =
                    selectedDots.contains(
                        index
                    )


                drawCircle(

                    color =
                        dotColor,

                    radius =
                        17.5.dp.toPx(),

                    center =
                        position
                )


                drawCircle(

                    color =
                        backgroundColor,

                    radius =
                        13.5.dp.toPx(),

                    center =
                        position
                )


                drawCircle(

                    color =
                        when {

                            selected &&
                                    isError ->

                                errorColor

                            selected ->

                                Color.White

                            else ->

                                dotColor
                        },

                    radius =
                        9.dp.toPx(),

                    center =
                        position
                )
            }
        }
    }
}


// =============================================================
// PATTERN POSITIONS
// =============================================================

private fun getConfirmPositions(
    width: Float,
    height: Float
): List<Offset> {

    val x1 =
        width * 0.1667f

    val x2 =
        width * 0.5f

    val x3 =
        width * 0.8333f


    val y1 =
        height * 0.1667f

    val y2 =
        height * 0.5f

    val y3 =
        height * 0.8333f


    return listOf(

        Offset(x1, y1),
        Offset(x2, y1),
        Offset(x3, y1),

        Offset(x1, y2),
        Offset(x2, y2),
        Offset(x3, y2),

        Offset(x1, y3),
        Offset(x2, y3),
        Offset(x3, y3)
    )
}


// =============================================================
// FIND PATTERN DOT
// =============================================================

private fun findConfirmDot(

    touch: Offset,

    width: Float,

    height: Float

): Int? {

    val positions =
        getConfirmPositions(

            width =
                width,

            height =
                height
        )


    positions.forEachIndexed {
            index,
            dot ->

        val dx =
            touch.x - dot.x

        val dy =
            touch.y - dot.y


        val distance =
            sqrt(
                dx * dx +
                        dy * dy
            )


        if (
            distance <= 55f
        ) {

            return index
        }
    }


    return null
}