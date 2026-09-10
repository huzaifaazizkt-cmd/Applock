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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.applock.R
import com.example.applock.data.DataStoreManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


// =============================================================
// SETTINGS ITEM DATA
// =============================================================

private data class SettingsItemData(
    val key: String,
    val titleRes: Int,
    val iconRes: Int,
    val descriptionRes: Int?
)


// =============================================================
// SETTINGS SCREEN
// =============================================================

@Composable
fun SettingsScreen(
    onIntruderClick: () -> Unit,
    onLanguageClick: () -> Unit
) {

    val context = LocalContext.current

    val dataStore = remember {
        DataStoreManager(context)
    }

    val scope = rememberCoroutineScope()


    // =========================================================
    // APP PROTECTION
    // =========================================================

    var appProtectionEnabled by remember {
        mutableStateOf(false)
    }


    // =========================================================
    // SETTINGS ITEMS
    // =========================================================

    val settingsItems = listOf(

        SettingsItemData(
            key = "languages",
            titleRes = R.string.languages,
            iconRes = R.drawable.language,
            descriptionRes = R.string.language_description
        ),

        SettingsItemData(
            key = "lock_setting",
            titleRes = R.string.lock_setting,
            iconRes = R.drawable.settingicon,
            descriptionRes = R.string.lock_settings_description
        ),

        SettingsItemData(
            key = "intruder",
            titleRes = R.string.intruder,
            iconRes = R.drawable.intruder,
            descriptionRes = R.string.intruder_settings
        ),

        SettingsItemData(
            key = "hide_settings",
            titleRes = R.string.hide_settings,
            iconRes = R.drawable.hideicon,
            descriptionRes = R.string.hide_settings_description
        ),

        SettingsItemData(
            key = "rate_us",
            titleRes = R.string.rate_us,
            iconRes = R.drawable.rateus,
            descriptionRes = R.string.rate_us_description
        ),

        SettingsItemData(
            key = "share",
            titleRes = R.string.share,
            iconRes = R.drawable.share,
            descriptionRes = null
        ),

        SettingsItemData(
            key = "about",
            titleRes = R.string.about,
            iconRes = R.drawable.feedback,
            descriptionRes = R.string.about_applock
        ),

        SettingsItemData(
            key = "privacy_policy",
            titleRes = R.string.privacy_policy,
            iconRes = R.drawable.privacy,
            descriptionRes = R.string.about_applock
        )
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

    var relockOption by remember {
        mutableStateOf("relock_after_quitting")
    }

    var tempRelockOption by remember {
        mutableStateOf("relock_after_quitting")
    }


    // =========================================================
    // DELAY TO RE-LOCK DIALOG
    // =========================================================

    var showDelayDialog by remember {
        mutableStateOf(false)
    }

    var delayOption by remember {
        mutableStateOf("never")
    }

    var tempDelayOption by remember {
        mutableStateOf("never")
    }


    // =========================================================
    // LOAD SAVED SETTINGS
    // =========================================================

    LaunchedEffect(Unit) {

        // -----------------------------------------------------
        // APP PROTECTION
        // -----------------------------------------------------

        appProtectionEnabled =
            dataStore
                .getAppProtectionEnabled()
                .first()


        // -----------------------------------------------------
        // HIDE FROM RECENTS
        // -----------------------------------------------------

        hideFromRecents =
            dataStore
                .getHideFromRecents()
                .first()


        // -----------------------------------------------------
        // RELOCK OPTION
        // -----------------------------------------------------

        relockOption =
            dataStore
                .getRelockOption()
                .first()

        tempRelockOption =
            relockOption


        // -----------------------------------------------------
        // DELAY OPTION
        // -----------------------------------------------------

        delayOption =
            dataStore
                .getRelockDelay()
                .first()

        tempDelayOption =
            delayOption
    }


    // =========================================================
    // SCROLL
    // =========================================================

    val scrollState = rememberScrollState()


    // =========================================================
    // ROOT
    // =========================================================

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color(0xFFF7F7F7)
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {

            // =================================================
            // TITLE
            // =================================================

            Text(
                text = stringResource(
                    R.string.settings
                ),

                modifier = Modifier.padding(
                    start = 54.dp,
                    top = 22.dp
                ),

                color = Color(0xFF333333),

                fontSize = 22.sp
            )


            Spacer(
                modifier = Modifier.height(22.dp)
            )


            // =================================================
            // SCROLL AREA
            // =================================================

            Column(
                modifier = Modifier
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

                // =================================================
                // APPLOCK ENABLE CARD
                // =================================================

                AppProtectionCard(
                    enabled = appProtectionEnabled,

                    onEnabledChange = { enabled ->

                        appProtectionEnabled =
                            enabled

                        scope.launch {

                            dataStore
                                .saveAppProtectionEnabled(
                                    enabled
                                )
                        }
                    }
                )


                Spacer(
                    modifier = Modifier.height(12.dp)
                )


                // =================================================
                // NORMAL SETTINGS ITEMS
                // =================================================

                settingsItems.forEach { item ->

                    val title =
                        stringResource(
                            item.titleRes
                        )

                    val description =
                        item.descriptionRes?.let {
                            stringResource(it)
                        } ?: ""


                    SettingsItem(

                        itemKey = item.key,

                        title = title,

                        iconRes = item.iconRes,

                        description = description,

                        expanded =
                            expandedItem == item.key,

                        hideFromRecents =
                            hideFromRecents,

                        onHideFromRecentsChange = {

                            hideFromRecents = it

                            scope.launch {

                                dataStore
                                    .saveHideFromRecents(
                                        it
                                    )
                            }
                        },

                        onRelockClick = {

                            tempRelockOption =
                                relockOption

                            showRelockDialog =
                                true
                        },

                        onDelayClick = {

                            tempDelayOption =
                                delayOption

                            showDelayDialog =
                                true
                        },

                        delayOption =
                            delayOption,

                        relockOption =
                            relockOption,

                        onArrowClick = {

                            when (item.key) {

                                "languages" -> {

                                    onLanguageClick()
                                }

                                "intruder" -> {

                                    onIntruderClick()
                                }

                                "lock_setting",
                                "hide_settings" -> {

                                    expandedItem =
                                        if (
                                            expandedItem ==
                                            item.key
                                        ) {

                                            null

                                        } else {

                                            item.key
                                        }
                                }

                                "rate_us" -> {
                                    // Later
                                }

                                "share" -> {
                                    // Later
                                }

                                "about" -> {
                                    // Later
                                }

                                "privacy_policy" -> {
                                    // Later
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
                    tempRelockOption,

                onOptionSelected = {

                    tempRelockOption =
                        it
                },

                onCancel = {

                    tempRelockOption =
                        relockOption

                    showRelockDialog =
                        false
                },

                onConfirm = {

                    relockOption =
                        tempRelockOption

                    showRelockDialog =
                        false

                    scope.launch {

                        dataStore
                            .saveRelockOption(
                                tempRelockOption
                            )
                    }
                }
            )
        }


        // =====================================================
        // DELAY TO RE-LOCK DIALOG
        // =====================================================

        if (showDelayDialog) {

            DelayToRelockDialog(

                selectedOption =
                    tempDelayOption,

                onOptionSelected = {

                    tempDelayOption =
                        it
                },

                onCancel = {

                    tempDelayOption =
                        delayOption

                    showDelayDialog =
                        false
                },

                onConfirm = {

                    delayOption =
                        tempDelayOption

                    showDelayDialog =
                        false

                    scope.launch {

                        dataStore
                            .saveRelockDelay(
                                tempDelayOption
                            )
                    }
                }
            )
        }
    }
}


// =============================================================
// APP PROTECTION CARD
// =============================================================

@Composable
private fun AppProtectionCard(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit
) {

    Card(

        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 5.dp,
                shape = RoundedCornerShape(12.dp),
                clip = false,
                ambientColor =
                    Color.Black.copy(alpha = 0.10f),
                spotColor =
                    Color.Black.copy(alpha = 0.10f)
            )
            .clip(
                RoundedCornerShape(12.dp)
            ),

        shape =
            RoundedCornerShape(12.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp,
                pressedElevation = 1.dp
            )
    ) {

        Row(

            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(
                    start = 8.dp,
                    end = 10.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            // =================================================
            // ENABLE ICON
            // =================================================

            Image(

                painter =
                    painterResource(
                        id = R.drawable.enable
                    ),

                contentDescription =
                    stringResource(
                        R.string.enable_app_protection
                    ),

                modifier =
                    Modifier.size(38.dp),

                contentScale =
                    ContentScale.Fit
            )


            Spacer(
                modifier =
                    Modifier.width(18.dp)
            )


            // =================================================
            // TEXT
            // =================================================

            Column(

                modifier =
                    Modifier.weight(1f)
            ) {

                Text(

                    text =
                        stringResource(
                            R.string.applock
                        ),

                    color =
                        Color(0xFF333333),

                    fontSize =
                        15.sp
                )


                Spacer(
                    modifier =
                        Modifier.height(2.dp)
                )


                Text(

                    text =
                        stringResource(
                            R.string.enable_app_protection
                        ),

                    color =
                        Color(0xFF666666),

                    fontSize =
                        11.sp
                )
            }


            // =================================================
            // SWITCH
            // =================================================

            Switch(

                checked =
                    enabled,

                onCheckedChange = {
                    onEnabledChange(it)
                },

                modifier =
                    Modifier
                        .size(
                            width = 42.dp,
                            height = 24.dp
                        )
                        .scale(0.56f),

                colors =
                    SwitchDefaults.colors(

                        checkedThumbColor =
                            Color(0xFF0396FF),

                        checkedTrackColor =
                            Color(0xFF8DCCF7),

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
// SETTINGS ITEM
// =============================================================

@Composable
private fun SettingsItem(

    itemKey: String,

    title: String,

    iconRes: Int,

    description: String,

    expanded: Boolean,

    hideFromRecents: Boolean,

    onHideFromRecentsChange:
        (Boolean) -> Unit,

    onRelockClick: () -> Unit,

    onDelayClick: () -> Unit,

    delayOption: String,

    relockOption: String,

    onArrowClick: () -> Unit
) {

    Card(

        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 5.dp,
                shape = RoundedCornerShape(12.dp),
                clip = false,
                ambientColor =
                    Color.Black.copy(alpha = 0.10f),
                spotColor =
                    Color.Black.copy(alpha = 0.10f)
            )
            .clip(
                RoundedCornerShape(12.dp)
            ),

        shape =
            RoundedCornerShape(12.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp,
                pressedElevation = 1.dp
            )
    ) {

        Column(
            modifier =
                Modifier.fillMaxWidth()
        ) {

            Row(

                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(
                        start = 8.dp,
                        end = 10.dp
                    ),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Image(

                    painter =
                        painterResource(
                            id = iconRes
                        ),

                    contentDescription =
                        title,

                    modifier =
                        Modifier.size(38.dp),

                    contentScale =
                        ContentScale.FillBounds
                )


                Spacer(
                    modifier =
                        Modifier.width(18.dp)
                )


                Text(

                    text = title,

                    modifier =
                        Modifier.weight(1f),

                    color =
                        Color(0xFF333333),

                    fontSize =
                        15.sp
                )


                Box(

                    modifier =
                        Modifier
                            .size(32.dp)
                            .clickable {
                                onArrowClick()
                            },

                    contentAlignment =
                        Alignment.Center
                ) {

                    val expandable =
                        itemKey ==
                                "lock_setting" ||
                                itemKey ==
                                "hide_settings"


                    Image(

                        painter =
                            painterResource(

                                id =
                                    if (expandable) {

                                        if (expanded) {
                                            R.drawable.uparrow
                                        } else {
                                            R.drawable.downicon
                                        }

                                    } else {

                                        R.drawable.sidearrow
                                    }
                            ),

                        contentDescription =

                            if (expandable) {

                                if (expanded) {

                                    stringResource(
                                        R.string.collapse
                                    )

                                } else {

                                    stringResource(
                                        R.string.expand
                                    )
                                }

                            } else {

                                stringResource(
                                    R.string.open_item,
                                    title
                                )
                            },

                        modifier =
                            Modifier.size(
                                if (expandable) {
                                    12.dp
                                } else {
                                    14.dp
                                }
                            ),

                        contentScale =
                            ContentScale.Fit
                    )
                }
            }


            if (
                expanded &&
                itemKey == "lock_setting"
            ) {

                LockSettingExpandedContent(

                    onRelockClick =
                        onRelockClick,

                    onDelayClick =
                        onDelayClick,

                    delayOption =
                        delayOption,

                    relockOption =
                        relockOption
                )
            }


            if (
                expanded &&
                itemKey == "hide_settings"
            ) {

                HideSettingsExpandedContent(

                    hideFromRecents =
                        hideFromRecents,

                    onHideFromRecentsChange =
                        onHideFromRecentsChange
                )
            }


            if (
                expanded &&
                itemKey != "intruder" &&
                itemKey != "lock_setting" &&
                itemKey != "hide_settings" &&
                itemKey != "languages" &&
                itemKey != "rate_us" &&
                itemKey != "share" &&
                itemKey != "about" &&
                itemKey != "privacy_policy"
            ) {

                Text(

                    text = description,

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
// HIDE SETTINGS
// =============================================================

@Composable
private fun HideSettingsExpandedContent(

    hideFromRecents: Boolean,

    onHideFromRecentsChange:
        (Boolean) -> Unit
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
                    stringResource(
                        R.string.hide_from_recent_screen
                    ),

                modifier =
                    Modifier.size(22.dp),

                contentScale =
                    ContentScale.Fit
            )


            Spacer(
                modifier =
                    Modifier.width(13.dp)
            )


            Text(

                text =
                    stringResource(
                        R.string.apps_hide_from_recent_screen
                    ),

                color =
                    Color(0xFF444444),

                fontSize =
                    14.sp,

                modifier =
                    Modifier.weight(1f)
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
                        .scale(0.56f),

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

    delayOption: String,

    relockOption: String
) {

    val context = LocalContext.current

    val dataStore =
        remember {
            DataStoreManager(context)
        }

    val scope =
        rememberCoroutineScope()


    var fingerprintEnabled by remember {
        mutableStateOf(false)
    }

    var vibrationEnabled by remember {
        mutableStateOf(false)
    }

    var hideTrackEnabled by remember {
        mutableStateOf(false)
    }


    LaunchedEffect(Unit) {

        fingerprintEnabled =
            dataStore
                .getFingerprintEnabled()
                .first()

        vibrationEnabled =
            dataStore
                .getVibrationEnabled()
                .first()

        hideTrackEnabled =
            dataStore
                .getHideTrackEnabled()
                .first()
    }


    Column(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Text(
            text =
                stringResource(
                    R.string.password
                ),
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
                stringResource(
                    R.string.reset_password
                ),
            subtitle =
                stringResource(
                    R.string.pattern
                ),
            onClick = {}
        )


        SettingDivider()


        LockSettingRow(
            iconRes =
                R.drawable.security,
            title =
                stringResource(
                    R.string.security_settings
                ),
            subtitle =
                stringResource(
                    R.string.set_security_email
                ),
            onClick = {}
        )


        SettingDivider()


        LockSettingSwitchRow(
            iconRes =
                R.drawable.fingerprint,
            title =
                stringResource(
                    R.string.fingerprint_lock
                ),
            subtitle =
                stringResource(
                    R.string.use_fingerprint_to_unlock
                ),
            checked =
                fingerprintEnabled,
            onCheckedChange = {

                fingerprintEnabled =
                    it

                scope.launch {

                    dataStore
                        .saveFingerprintEnabled(
                            it
                        )
                }
            }
        )


        Text(
            text =
                stringResource(
                    R.string.unlock
                ),
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
                stringResource(
                    R.string.vibration
                ),
            subtitle = null,
            checked =
                vibrationEnabled,
            onCheckedChange = {

                vibrationEnabled =
                    it

                scope.launch {

                    dataStore
                        .saveVibrationEnabled(
                            it
                        )
                }
            }
        )


        SettingDivider()


        LockSettingSwitchRow(
            iconRes =
                R.drawable.track,
            title =
                stringResource(
                    R.string.hide_track
                ),
            subtitle =
                stringResource(
                    R.string.hide_track_description
                ),
            checked =
                hideTrackEnabled,
            onCheckedChange = {

                hideTrackEnabled =
                    it

                scope.launch {

                    dataStore
                        .saveHideTrackEnabled(
                            it
                        )
                }
            }
        )


        SettingDivider()


        LockSettingRow(
            iconRes =
                R.drawable.relock,
            title =
                stringResource(
                    R.string.relock_option
                ),
            subtitle =
                getRelockText(
                    relockOption
                ),
            onClick =
                onRelockClick
        )


        SettingDivider()


        LockSettingRow(
            iconRes =
                R.drawable.delay,
            title =
                stringResource(
                    R.string.delay_to_relock
                ),
            subtitle =
                getDelayText(
                    delayOption
                ),
            onClick =
                onDelayClick
        )


        Spacer(
            modifier =
                Modifier.height(8.dp)
        )
    }
}


// =============================================================
// RE-LOCK TEXT
// =============================================================

@Composable
private fun getRelockText(
    relockOption: String
): String {

    return when (relockOption) {

        "relock_after_screen_off" ->
            stringResource(
                R.string.relock_after_screen_off
            )

        "relock_after_quitting" ->
            stringResource(
                R.string.relock_after_quitting
            )

        else ->
            stringResource(
                R.string.relock_after_quitting
            )
    }
}


// =============================================================
// DELAY TEXT
// =============================================================

@Composable
private fun getDelayText(
    key: String
): String {

    return when (key) {

        "never" ->
            stringResource(
                R.string.never
            )

        "five_seconds" ->
            stringResource(
                R.string.five_seconds
            )

        "fifteen_seconds" ->
            stringResource(
                R.string.fifteen_seconds
            )

        "thirty_seconds" ->
            stringResource(
                R.string.thirty_seconds
            )

        "one_minute" ->
            stringResource(
                R.string.one_minute
            )

        "two_minutes" ->
            stringResource(
                R.string.two_minutes
            )

        "five_minutes" ->
            stringResource(
                R.string.five_minutes
            )

        else ->
            stringResource(
                R.string.never
            )
    }
}


// =============================================================
// RE-LOCK DIALOG
// =============================================================

@Composable
private fun RelockOptionDialog(

    selectedOption: String,

    onOptionSelected:
        (String) -> Unit,

    onCancel: () -> Unit,

    onConfirm: () -> Unit
) {

    Dialog(

        onDismissRequest =
            onCancel,

        properties =
            DialogProperties(
                usePlatformDefaultWidth =
                    false
            )
    ) {

        Box(

            modifier =
                Modifier
                    .width(291.dp)
                    .height(178.dp)
                    .background(
                        Color.White,
                        RoundedCornerShape(12.dp)
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
                            stringResource(
                                R.string.relock
                            ),

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


                RelockOptionRow(

                    text =
                        stringResource(
                            R.string.relock_after_quitting_option
                        ),

                    selected =
                        selectedOption ==
                                "relock_after_quitting",

                    onClick = {

                        onOptionSelected(
                            "relock_after_quitting"
                        )
                    }
                )


                RelockOptionRow(

                    text =
                        stringResource(
                            R.string.relock_after_screen_off
                        ),

                    selected =
                        selectedOption ==
                                "relock_after_screen_off",

                    onClick = {

                        onOptionSelected(
                            "relock_after_screen_off"
                        )
                    }
                )


                Spacer(
                    modifier =
                        Modifier.weight(1f)
                )


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
                Modifier.size(28.dp),

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
                Modifier.width(5.dp)
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
// DELAY DIALOG
// =============================================================

@Composable
private fun DelayToRelockDialog(

    selectedOption: String,

    onOptionSelected:
        (String) -> Unit,

    onCancel: () -> Unit,

    onConfirm: () -> Unit
) {

    val delayOptions =
        listOf(
            "never",
            "five_seconds",
            "fifteen_seconds",
            "thirty_seconds",
            "one_minute",
            "two_minutes",
            "five_minutes"
        )


    Dialog(

        onDismissRequest =
            onCancel,

        properties =
            DialogProperties(
                usePlatformDefaultWidth =
                    false
            )
    ) {

        Box(

            modifier =
                Modifier
                    .width(291.dp)
                    .height(360.dp)
                    .background(
                        Color.White,
                        RoundedCornerShape(12.dp)
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
                            stringResource(
                                R.string.delay_to_relock
                            ),

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


                delayOptions.forEach { option ->

                    DelayOptionRow(

                        text =
                            getDelayText(
                                option
                            ),

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
                        Modifier.weight(1f)
                )


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
                Modifier.size(28.dp),

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
                Modifier.width(5.dp)
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
                stringResource(
                    R.string.cancel
                ),

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
                Modifier.width(4.dp)
        )


        Text(

            text =
                stringResource(
                    R.string.confirm
                ),

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
                Modifier.width(28.dp),

            contentAlignment =
                Alignment.Center
        ) {

            Image(

                painter =
                    painterResource(
                        id = iconRes
                    ),

                contentDescription =
                    title,

                modifier =
                    Modifier.size(19.dp),

                contentScale =
                    ContentScale.Fit
            )
        }


        Spacer(
            modifier =
                Modifier.width(8.dp)
        )


        Column(

            modifier =
                Modifier.weight(1f)
        ) {

            Text(

                text =
                    title,

                color =
                    Color(0xFF333333),

                fontSize =
                    14.sp
            )


            if (!subtitle.isNullOrEmpty()) {

                Spacer(
                    modifier =
                        Modifier.height(2.dp)
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

    onCheckedChange:
        (Boolean) -> Unit
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
                Modifier.width(28.dp),

            contentAlignment =
                Alignment.Center
        ) {

            Image(

                painter =
                    painterResource(
                        id = iconRes
                    ),

                contentDescription =
                    title,

                modifier =
                    Modifier.size(19.dp),

                contentScale =
                    ContentScale.Fit
            )
        }


        Spacer(
            modifier =
                Modifier.width(8.dp)
        )


        Column(

            modifier =
                Modifier.weight(1f)
        ) {

            Text(

                text =
                    title,

                color =
                    Color(0xFF333333),

                fontSize =
                    14.sp
            )


            if (!subtitle.isNullOrEmpty()) {

                Spacer(
                    modifier =
                        Modifier.height(2.dp)
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
                onCheckedChange(it)
            },

            modifier =
                Modifier
                    .size(
                        width = 42.dp,
                        height = 24.dp
                    )
                    .scale(0.56f),

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