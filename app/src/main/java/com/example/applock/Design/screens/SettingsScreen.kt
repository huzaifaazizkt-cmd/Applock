package com.example.applock.Design.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.applock.R
import com.example.applock.data.DataStoreManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


// =============================================================
// SETTINGS SCREEN
// =============================================================

@Composable
fun SettingsScreen(
    onIntruderClick: () -> Unit
) {

    val context =
        LocalContext.current

    val dataStore =
        remember {
            DataStoreManager(context)
        }

    val scope =
        rememberCoroutineScope()


    // =========================================================
    // SETTINGS ITEMS
    // =========================================================

    val settingsItems = listOf(

        Triple(
            "Languages",
            R.drawable.language,
            "Select language"
        ),

        Triple(
            "Lock Setting",
            R.drawable.settingicon,
            "Lock settings"
        ),

        Triple(
            "Intruder",
            R.drawable.intruder,
            "Intruder settings"
        ),

        Triple(
            "Hide Settings",
            R.drawable.hideicon,
            "Hide settings"
        ),

        Triple(
            "Rate Us",
            R.drawable.rateus,
            "Rate us"
        ),

        Triple(
            "Share",
            R.drawable.share,
            ""
        ),

        Triple(
            "About",
            R.drawable.feedback,
            "About AppLock"
        ),

        Triple(
            "Privacy Policy",
            R.drawable.privacy,
            "About AppLock"
        ),
    )


    // =========================================================
    // EXPANDED ITEM
    // =========================================================

    var expandedItem by remember {
        mutableStateOf<String?>(null)
    }


    // =========================================================
    // HIDE FROM RECENTS
    // =========================================================

    var hideFromRecents by remember {
        mutableStateOf(false)
    }


    // =========================================================
    // RE-LOCK DIALOG
    // =========================================================

    var showRelockDialog by remember {
        mutableStateOf(false)
    }


    // =========================================================
    // SELECTED RE-LOCK OPTION
    // =========================================================

    var relockOption by remember {
        mutableStateOf(
            "Re-Lock After Quitting"
        )
    }


    // =========================================================
    // DELAY TO RE-LOCK DIALOG
    // =========================================================

    var showDelayDialog by remember {
        mutableStateOf(false)
    }


    // =========================================================
    // SELECTED DELAY OPTION
    // =========================================================

    var delayOption by remember {
        mutableStateOf("Never")
    }


    // =========================================================
    // LOAD HIDE FROM RECENTS
    // =========================================================

    LaunchedEffect(Unit) {

        hideFromRecents =
            dataStore
                .getHideFromRecents()
                .first()
    }


    // =========================================================
    // SCROLL
    // =========================================================

    val scrollState =
        rememberScrollState()


    // =========================================================
    // ROOT
    // =========================================================

    Box(

        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Color(0xFFF7F7F7)
                )
    ) {

        Column(

            modifier =
                Modifier
                    .fillMaxSize()
        ) {


            // =================================================
            // TITLE
            // =================================================

            Text(

                text =
                    "Settings",

                modifier =
                    Modifier.padding(
                        start = 54.dp,
                        top = 22.dp
                    ),

                color =
                    Color(0xFF333333),

                fontSize =
                    22.sp
            )


            Spacer(
                modifier =
                    Modifier.height(22.dp)
            )


            // =================================================
            // SCROLL AREA
            // =================================================

            Column(

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(
                            scrollState
                        )
                        .padding(
                            start = 14.dp,
                            end = 14.dp,
                            bottom = 25.dp
                        )
            ) {

                settingsItems.forEach { item ->

                    val title =
                        item.first

                    val icon =
                        item.second

                    val description =
                        item.third


                    SettingsItem(

                        title =
                            title,

                        iconRes =
                            icon,

                        description =
                            description,

                        expanded =
                            expandedItem == title,

                        hideFromRecents =
                            hideFromRecents,

                        onHideFromRecentsChange = {

                            hideFromRecents =
                                it

                            scope.launch {

                                dataStore
                                    .saveHideFromRecents(
                                        it
                                    )
                            }
                        },

                        onRelockClick = {

                            showRelockDialog =
                                true
                        },

                        onDelayClick = {

                            showDelayDialog =
                                true
                        },

                        delayOption =
                            delayOption,

                        onArrowClick = {

                            // =================================================
                            // INTRUDER
                            // =================================================

                            if (
                                title == "Intruder"
                            ) {

                                onIntruderClick()

                            } else {

                                expandedItem =
                                    if (
                                        expandedItem == title
                                    ) {

                                        null

                                    } else {

                                        title
                                    }
                            }
                        }
                    )


                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )
                }
            }
        }


        // =====================================================
        // RE-LOCK DIALOG
        // =====================================================

        if (showRelockDialog) {

            RelockOptionDialog(

                selectedOption =
                    relockOption,

                onOptionSelected = {

                    relockOption =
                        it
                },

                onCancel = {

                    showRelockDialog =
                        false
                },

                onConfirm = {

                    showRelockDialog =
                        false
                }
            )
        }


        // =====================================================
        // DELAY TO RE-LOCK DIALOG
        // =====================================================

        if (showDelayDialog) {

            DelayToRelockDialog(

                selectedOption =
                    delayOption,

                onOptionSelected = {

                    delayOption =
                        it
                },

                onCancel = {

                    showDelayDialog =
                        false
                },

                onConfirm = {

                    showDelayDialog =
                        false
                }
            )
        }
    }
}


// =============================================================
// SETTINGS ITEM
// =============================================================

@Composable
private fun SettingsItem(

    title: String,

    iconRes: Int,

    description: String,

    expanded: Boolean,

    hideFromRecents: Boolean,

    onHideFromRecentsChange: (Boolean) -> Unit,

    onRelockClick: () -> Unit,

    onDelayClick: () -> Unit,

    delayOption: String,

    onArrowClick: () -> Unit

) {

    Card(

        modifier =
            Modifier
                .fillMaxWidth()
                .shadow(

                    elevation =
                        5.dp,

                    shape =
                        RoundedCornerShape(
                            12.dp
                        ),

                    clip =
                        false,

                    ambientColor =
                        Color.Black.copy(
                            alpha = 0.10f
                        ),

                    spotColor =
                        Color.Black.copy(
                            alpha = 0.10f
                        )
                )
                .clip(
                    RoundedCornerShape(
                        12.dp
                    )
                ),

        shape =
            RoundedCornerShape(
                12.dp
            ),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    2.dp,

                pressedElevation =
                    1.dp
            )
    ) {

        Column(
            modifier =
                Modifier.fillMaxWidth()
        ) {


            // =================================================
            // HEADER
            // =================================================

            Row(

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .padding(
                            start = 8.dp,
                            end = 10.dp
                        ),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {


                // =================================================
                // ICON
                // =================================================

                Image(

                    painter =
                        painterResource(
                            id =
                                iconRes
                        ),

                    contentDescription =
                        title,

                    modifier =
                        Modifier.size(
                            38.dp
                        ),

                    contentScale =
                        ContentScale.FillBounds
                )


                Spacer(
                    modifier =
                        Modifier.width(
                            18.dp
                        )
                )


                // =================================================
                // TITLE
                // =================================================

                Text(

                    text =
                        title,

                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    color =
                        Color(0xFF333333),

                    fontSize =
                        15.sp
                )


                // =================================================
                // ARROW
                // =================================================

                Box(

                    modifier =
                        Modifier
                            .size(
                                32.dp
                            )
                            .clickable {

                                onArrowClick()
                            },

                    contentAlignment =
                        Alignment.Center
                ) {

                    Image(

                        painter =
                            painterResource(

                                id =
                                    if (
                                        title ==
                                        "Intruder"
                                    ) {

                                        R.drawable.sidearrow

                                    } else {

                                        if (
                                            expanded
                                        ) {

                                            R.drawable.uparrow

                                        } else {

                                            R.drawable.downicon
                                        }
                                    }
                            ),

                        contentDescription =

                            if (
                                title ==
                                "Intruder"
                            ) {

                                "Open Intruder"

                            } else {

                                if (
                                    expanded
                                ) {

                                    "Collapse"

                                } else {

                                    "Expand"
                                }
                            },

                        modifier =
                            Modifier.size(

                                if (
                                    title ==
                                    "Intruder"
                                ) {

                                    14.dp

                                } else {

                                    12.dp
                                }
                            ),

                        contentScale =
                            ContentScale.Fit
                    )
                }
            }


            // =================================================
            // LOCK SETTING
            // =================================================

            if (
                expanded &&
                title == "Lock Setting"
            ) {

                LockSettingExpandedContent(

                    onRelockClick =
                        onRelockClick,

                    onDelayClick =
                        onDelayClick,

                    delayOption =
                        delayOption
                )
            }


            // =================================================
            // HIDE SETTINGS
            // =================================================

            if (
                expanded &&
                title == "Hide Settings"
            ) {

                HideSettingsExpandedContent(

                    hideFromRecents =
                        hideFromRecents,

                    onHideFromRecentsChange =
                        onHideFromRecentsChange
                )
            }


            // =================================================
            // OTHER SETTINGS
            // =================================================

            if (
                expanded &&
                title != "Intruder" &&
                title != "Lock Setting" &&
                title != "Hide Settings"
            ) {

                Text(

                    text =
                        description,

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 64.dp,
                                end = 20.dp,
                                bottom = 14.dp
                            ),

                    color =
                        Color(0xFF666666),

                    fontSize =
                        13.sp
                )
            }
        }
    }
}


// =============================================================
// HIDE SETTINGS EXPANDED CONTENT
// =============================================================

@Composable
private fun HideSettingsExpandedContent(

    hideFromRecents: Boolean,

    onHideFromRecentsChange: (Boolean) -> Unit

) {

    Column(

        modifier =
            Modifier.fillMaxWidth()
    ) {

        Row(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 12.dp,
                        end = 10.dp,
                        top = 8.dp,
                        bottom = 8.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Image(

                painter =
                    painterResource(
                        id =
                            R.drawable.eyehide
                    ),

                contentDescription =
                    "Hide from recent screen",

                modifier =
                    Modifier.size(
                        22.dp
                    ),

                contentScale =
                    ContentScale.Fit
            )


            Spacer(
                modifier =
                    Modifier.width(
                        13.dp
                    )
            )


            Text(

                text =
                    "Apps Hide-from recent screen",

                color =
                    Color(0xFF444444),

                fontSize =
                    14.sp,

                modifier =
                    Modifier.weight(
                        1f
                    )
            )


            Switch(

                checked =
                    hideFromRecents,

                onCheckedChange =
                    onHideFromRecentsChange,

                modifier =
                    Modifier
                        .size(
                            width = 42.dp,
                            height = 24.dp
                        )
                        .scale(
                            0.56f
                        ),

                colors =
                    SwitchDefaults.colors(

                        checkedThumbColor =
                            Color.White,

                        checkedTrackColor =
                            Color(0xFF9C27B0),

                        uncheckedThumbColor =
                            Color(0xFFAAAAAA),

                        uncheckedTrackColor =
                            Color(0xFFE3E3E3),

                        uncheckedBorderColor =
                            Color.Transparent,

                        checkedBorderColor =
                            Color.Transparent
                    )
            )
        }
    }
}


// =============================================================
// LOCK SETTING
// =============================================================

@Composable
private fun LockSettingExpandedContent(

    onRelockClick: () -> Unit,

    onDelayClick: () -> Unit,

    delayOption: String

) {

    var fingerprintEnabled by remember {
        mutableStateOf(true)
    }

    var vibrationEnabled by remember {
        mutableStateOf(true)
    }

    var hideTrackEnabled by remember {
        mutableStateOf(true)
    }


    Column(
        modifier =
            Modifier.fillMaxWidth()
    ) {


        // =================================================
        // PASSWORD
        // =================================================

        Text(

            text =
                "Password",

            color =
                Color(0xFF888888),

            fontSize =
                16.sp,

            modifier =
                Modifier.padding(
                    start = 10.dp,
                    top = 6.dp,
                    bottom = 6.dp
                )
        )


        LockSettingRow(

            iconRes =
                R.drawable.redlock,

            title =
                "Reset Password",

            subtitle =
                "Pattern",

            onClick = {}
        )


        SettingDivider()


        LockSettingRow(

            iconRes =
                R.drawable.security,

            title =
                "Security settings",

            subtitle =
                "Set security email",

            onClick = {}
        )


        SettingDivider()


        LockSettingSwitchRow(

            iconRes =
                R.drawable.fingerprint,

            title =
                "Fingerprint lock",

            subtitle =
                "use Fingerprint to unlock apps",

            checked =
                fingerprintEnabled,

            onCheckedChange = {

                fingerprintEnabled =
                    it
            }
        )


        // =================================================
        // UNLOCK
        // =================================================

        Text(

            text =
                "Unlock",

            color =
                Color(0xFF888888),

            fontSize =
                16.sp,

            modifier =
                Modifier.padding(
                    start = 10.dp,
                    top = 8.dp,
                    bottom = 6.dp
                )
        )


        LockSettingSwitchRow(

            iconRes =
                R.drawable.vibration,

            title =
                "Vibration",

            subtitle =
                null,

            checked =
                vibrationEnabled,

            onCheckedChange = {

                vibrationEnabled =
                    it
            }
        )


        SettingDivider()


        LockSettingSwitchRow(

            iconRes =
                R.drawable.track,

            title =
                "Hide Track",

            subtitle =
                "Hide track when you draw pattern password",

            checked =
                hideTrackEnabled,

            onCheckedChange = {

                hideTrackEnabled =
                    it
            }
        )


        SettingDivider()


        // =================================================
        // RE-LOCK OPTION
        // =================================================

        LockSettingRow(

            iconRes =
                R.drawable.relock,

            title =
                "Re-Lock option",

            subtitle =
                "Re-lock after Quitting",

            onClick =
                onRelockClick
        )


        SettingDivider()


        // =================================================
        // DELAY TO RE-LOCK
        // =================================================

        LockSettingRow(

            iconRes =
                R.drawable.delay,

            title =
                "Delay to Re-Lock",

            subtitle =
                delayOption,

            onClick =
                onDelayClick
        )


        Spacer(
            modifier =
                Modifier.height(
                    8.dp
                )
        )
    }
}


// =============================================================
// RE-LOCK OPTION DIALOG
// =============================================================

@Composable
private fun RelockOptionDialog(

    selectedOption: String,

    onOptionSelected: (String) -> Unit,

    onCancel: () -> Unit,

    onConfirm: () -> Unit

) {

    Dialog(

        onDismissRequest =
            onCancel,

        properties =
            DialogProperties(
                usePlatformDefaultWidth = false
            )
    ) {

        Box(

            modifier =
                Modifier
                    .width(291.dp)
                    .height(178.dp)
                    .background(
                        Color.White,
                        RoundedCornerShape(
                            12.dp
                        )
                    )
        ) {

            Column(

                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = 8.dp
                        )
            ) {


                // =================================================
                // HEADING
                // =================================================

                Box(

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(22.dp),

                    contentAlignment =
                        Alignment.Center
                ) {

                    Text(

                        text =
                            "Re-Lock",

                        color =
                            Color(0xFF333333),

                        fontSize =
                            14.sp
                    )
                }


                Spacer(
                    modifier =
                        Modifier.height(7.dp)
                )


                // =================================================
                // OPTION 1
                // =================================================

                RelockOptionRow(

                    text =
                        "Re-Lock After Quitting",

                    selected =
                        selectedOption ==
                                "Re-Lock After Quitting",

                    onClick = {

                        onOptionSelected(
                            "Re-Lock After Quitting"
                        )
                    }
                )


                // =================================================
                // OPTION 2
                // =================================================

                RelockOptionRow(

                    text =
                        "Re-Lock after screen off",

                    selected =
                        selectedOption ==
                                "Re-Lock after screen off",

                    onClick = {

                        onOptionSelected(
                            "Re-Lock after screen off"
                        )
                    }
                )


                Spacer(
                    modifier =
                        Modifier.weight(
                            1f
                        )
                )


                // =================================================
                // BUTTONS
                // =================================================

                DialogButtons(

                    onCancel =
                        onCancel,

                    onConfirm =
                        onConfirm
                )
            }
        }
    }
}


// =============================================================
// RE-LOCK OPTION ROW
// =============================================================

@Composable
private fun RelockOptionRow(

    text: String,

    selected: Boolean,

    onClick: () -> Unit

) {

    Row(

        modifier =
            Modifier
                .fillMaxWidth()
                .height(38.dp)
                .clickable {
                    onClick()
                },

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        RadioButton(

            selected =
                selected,

            onClick =
                onClick,

            modifier =
                Modifier.size(
                    28.dp
                ),

            colors =
                RadioButtonDefaults.colors(

                    selectedColor =
                        Color(0xFF0396FF),

                    unselectedColor =
                        Color(0xFFBDBDBD)
                )
        )


        Spacer(
            modifier =
                Modifier.width(
                    5.dp
                )
        )


        Text(

            text =
                text,

            color =
                Color(0xFF444444),

            fontSize =
                13.sp
        )
    }
}


// =============================================================
// DELAY TO RE-LOCK DIALOG
// =============================================================

@Composable
private fun DelayToRelockDialog(

    selectedOption: String,

    onOptionSelected: (String) -> Unit,

    onCancel: () -> Unit,

    onConfirm: () -> Unit

) {

    val delayOptions = listOf(

        "Never",

        "5 Seconds",

        "15 Seconds",

        "30 Seconds",

        "1 Minute",

        "2 Minutes",

        "5 Minutes"
    )


    Dialog(

        onDismissRequest =
            onCancel,

        properties =
            DialogProperties(
                usePlatformDefaultWidth = false
            )
    ) {

        Box(

            modifier =
                Modifier
                    .width(291.dp)
                    .height(360.dp)
                    .background(
                        Color.White,
                        RoundedCornerShape(
                            12.dp
                        )
                    )
        ) {

            Column(

                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = 8.dp
                        )
            ) {


                // =================================================
                // HEADING
                // =================================================

                Box(

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(22.dp),

                    contentAlignment =
                        Alignment.Center
                ) {

                    Text(

                        text =
                            "Delay to Re-Lock",

                        color =
                            Color(0xFF333333),

                        fontSize =
                            14.sp
                    )
                }


                Spacer(
                    modifier =
                        Modifier.height(7.dp)
                )


                // =================================================
                // OPTIONS
                // =================================================

                delayOptions.forEach { option ->

                    DelayOptionRow(

                        text =
                            option,

                        selected =
                            selectedOption ==
                                    option,

                        onClick = {

                            onOptionSelected(
                                option
                            )
                        }
                    )
                }


                Spacer(
                    modifier =
                        Modifier.weight(
                            1f
                        )
                )


                // =================================================
                // BUTTONS
                // =================================================

                DialogButtons(

                    onCancel =
                        onCancel,

                    onConfirm =
                        onConfirm
                )
            }
        }
    }
}


// =============================================================
// DELAY OPTION ROW
// =============================================================

@Composable
private fun DelayOptionRow(

    text: String,

    selected: Boolean,

    onClick: () -> Unit

) {

    Row(

        modifier =
            Modifier
                .fillMaxWidth()
                .height(38.dp)
                .clickable {
                    onClick()
                },

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        RadioButton(

            selected =
                selected,

            onClick =
                onClick,

            modifier =
                Modifier.size(
                    28.dp
                ),

            colors =
                RadioButtonDefaults.colors(

                    selectedColor =
                        Color(0xFF0396FF),

                    unselectedColor =
                        Color(0xFFBDBDBD)
                )
        )


        Spacer(
            modifier =
                Modifier.width(
                    5.dp
                )
        )


        Text(

            text =
                text,

            color =
                Color(0xFF444444),

            fontSize =
                13.sp
        )
    }
}


// =============================================================
// DIALOG BUTTONS
// =============================================================

@Composable
private fun DialogButtons(

    onCancel: () -> Unit,

    onConfirm: () -> Unit

) {

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
                "Cancel",

            color =
                Color(0xFF818181),

            fontSize =
                14.sp,

            modifier =
                Modifier
                    .clickable {
                        onCancel()
                    }
                    .padding(
                        horizontal = 10.dp,
                        vertical = 6.dp
                    )
        )


        Spacer(
            modifier =
                Modifier.width(
                    4.dp
                )
        )


        Text(

            text =
                "Confirm",

            color =
                Color(0xFF0396FF),

            fontSize =
                14.sp,

            modifier =
                Modifier
                    .clickable {
                        onConfirm()
                    }
                    .padding(
                        horizontal = 10.dp,
                        vertical = 6.dp
                    )
        )
    }
}


// =============================================================
// NORMAL ROW
// =============================================================

@Composable
private fun LockSettingRow(

    iconRes: Int,

    title: String,

    subtitle: String?,

    onClick: () -> Unit

) {

    Row(

        modifier =
            Modifier
                .fillMaxWidth()
                .clickable {

                    onClick()
                }
                .padding(
                    start = 10.dp,
                    end = 14.dp,
                    top = 8.dp,
                    bottom = 8.dp
                ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {


        Box(

            modifier =
                Modifier.width(
                    28.dp
                ),

            contentAlignment =
                Alignment.Center
        ) {

            Image(

                painter =
                    painterResource(
                        id =
                            iconRes
                    ),

                contentDescription =
                    title,

                modifier =
                    Modifier.size(
                        19.dp
                    ),

                contentScale =
                    ContentScale.Fit
            )
        }


        Spacer(
            modifier =
                Modifier.width(
                    8.dp
                )
        )


        Column(

            modifier =
                Modifier.weight(
                    1f
                )
        ) {

            Text(

                text =
                    title,

                color =
                    Color(0xFF333333),

                fontSize =
                    14.sp
            )


            if (
                !subtitle.isNullOrEmpty()
            ) {

                Spacer(
                    modifier =
                        Modifier.height(
                            2.dp
                        )
                )


                Text(

                    text =
                        subtitle,

                    color =
                        Color(0xFF666666),

                    fontSize =
                        11.sp
                )
            }
        }
    }
}


// =============================================================
// SWITCH ROW
// =============================================================

@Composable
private fun LockSettingSwitchRow(

    iconRes: Int,

    title: String,

    subtitle: String?,

    checked: Boolean,

    onCheckedChange: (Boolean) -> Unit

) {

    Row(

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    start = 10.dp,
                    end = 12.dp,
                    top = 7.dp,
                    bottom = 7.dp
                ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {


        Box(

            modifier =
                Modifier.width(
                    28.dp
                ),

            contentAlignment =
                Alignment.Center
        ) {

            Image(

                painter =
                    painterResource(
                        id =
                            iconRes
                    ),

                contentDescription =
                    title,

                modifier =
                    Modifier.size(
                        19.dp
                    ),

                contentScale =
                    ContentScale.Fit
            )
        }


        Spacer(
            modifier =
                Modifier.width(
                    8.dp
                )
        )


        Column(

            modifier =
                Modifier.weight(
                    1f
                )
        ) {

            Text(

                text =
                    title,

                color =
                    Color(0xFF333333),

                fontSize =
                    14.sp
            )


            if (
                !subtitle.isNullOrEmpty()
            ) {

                Spacer(
                    modifier =
                        Modifier.height(
                            2.dp
                        )
                )


                Text(

                    text =
                        subtitle,

                    color =
                        Color(0xFF666666),

                    fontSize =
                        11.sp
                )
            }
        }


        Switch(

            checked =
                checked,

            onCheckedChange = {

                onCheckedChange(
                    it
                )
            },

            modifier =
                Modifier
                    .size(
                        width = 42.dp,
                        height = 24.dp
                    )
                    .scale(
                        0.56f
                    ),

            colors =
                SwitchDefaults.colors(

                    checkedThumbColor =
                        Color(0xFFF45656),

                    checkedTrackColor =
                        Color(0xFFFFA3A3),

                    uncheckedThumbColor =
                        Color(0xFFAAAAAA),

                    uncheckedTrackColor =
                        Color(0xFFE3E3E3),

                    uncheckedBorderColor =
                        Color.Transparent,

                    checkedBorderColor =
                        Color.Transparent
                )
        )
    }
}


// =============================================================
// DIVIDER
// =============================================================

@Composable
private fun SettingDivider() {

    Divider(

        modifier =
            Modifier.fillMaxWidth(),

        thickness =
            0.6.dp,

        color =
            Color(0xFFE8E8E8)
    )
}