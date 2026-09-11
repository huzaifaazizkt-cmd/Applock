
package com.example.applock.Design.screens

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.core.content.ContextCompat

import com.example.applock.R
import com.example.applock.data.DataStoreManager

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


// =============================================================
// INTRUDER IMAGE
// =============================================================

private data class IntruderImage(
    val uri: Uri,
    val name: String,
    val dateAdded: Long
)


// =============================================================
// INTRUDER SCREEN
// =============================================================

@Composable
fun IntruderScreen(
    onBackClick: () -> Unit = {}
) {

    val context = LocalContext.current

    val dataStore = remember {
        DataStoreManager(context)
    }

    val scope = rememberCoroutineScope()

    val blueColor =
        Color(0xFF0396FF)

    val backgroundColor =
        Color(0xFFF7F7F7)


    // =========================================================
    // STATES
    // =========================================================

    var intruderEnabled by remember {
        mutableStateOf(false)
    }

    var observationAttempts by remember {
        mutableStateOf(3)
    }

    var selectedObservationAttempts by remember {
        mutableStateOf("3 Attempts")
    }

    var showAttemptsDialog by remember {
        mutableStateOf(false)
    }

    var showPermissionSettingsDialog by remember {
        mutableStateOf(false)
    }


    // =========================================================
    // CAMERA PERMISSION
    // =========================================================

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {

                intruderEnabled = true

                scope.launch {

                    dataStore.saveIntruderEnabled(
                        true
                    )
                }

            } else {

                intruderEnabled = false

                scope.launch {

                    dataStore.saveIntruderEnabled(
                        false
                    )
                }

                showPermissionSettingsDialog =
                    true
            }
        }


    // =========================================================
    // OPEN APP SETTINGS
    // =========================================================

    fun openAppSettings() {

        try {

            val intent =
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse(
                        "package:${context.packageName}"
                    )
                )

            context.startActivity(
                intent
            )

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }


    // =========================================================
    // CAMERA PERMISSION REQUEST
    // =========================================================

    fun requestCameraPermission() {

        val permissionGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) ==
                    PackageManager.PERMISSION_GRANTED

        if (permissionGranted) {

            intruderEnabled = true

            scope.launch {

                dataStore.saveIntruderEnabled(
                    true
                )
            }

            return
        }

        cameraPermissionLauncher.launch(
            Manifest.permission.CAMERA
        )
    }


    // =========================================================
    // LOAD SAVED SETTINGS
    // =========================================================

    LaunchedEffect(Unit) {

        intruderEnabled =
            dataStore
                .getIntruderEnabled()
                .first()


        observationAttempts =
            dataStore
                .getIntruderObservationTime()
                .first()


        selectedObservationAttempts =
            observationAttemptsToText(
                observationAttempts
            )
    }


    // =========================================================
    // INTRUDER IMAGES
    // =========================================================

    var intruderImages by remember {
        mutableStateOf<List<IntruderImage>>(
            emptyList()
        )
    }


    // =========================================================
    // SELECTION
    // =========================================================

    var selectionMode by remember {
        mutableStateOf(false)
    }

    var selectedImages by remember {
        mutableStateOf<Set<Uri>>(
            emptySet()
        )
    }


    // =========================================================
    // PREVIEW
    // =========================================================

    var previewImage by remember {
        mutableStateOf<IntruderImage?>(null)
    }


    // =========================================================
    // LOAD IMAGES
    // =========================================================

    LaunchedEffect(Unit) {

        intruderImages =
            loadIntruderImages(
                context
            )
    }


    // =========================================================
    // RELOAD IMAGES
    // =========================================================

    fun reloadImages() {

        scope.launch {

            intruderImages =
                loadIntruderImages(
                    context
                )
        }
    }


    // =========================================================
    // DELETE SINGLE IMAGE
    // =========================================================

    fun deleteSingleImage(
        image: IntruderImage
    ) {

        scope.launch {

            withContext(
                Dispatchers.IO
            ) {

                try {

                    context.contentResolver.delete(
                        image.uri,
                        null,
                        null
                    )

                } catch (e: Exception) {

                    e.printStackTrace()
                }
            }

            previewImage = null

            intruderImages =
                loadIntruderImages(
                    context
                )
        }
    }


    // =========================================================
    // DELETE SELECTED IMAGES
    // =========================================================

    fun deleteSelectedImages() {

        val imagesToDelete =
            intruderImages.filter {

                selectedImages.contains(
                    it.uri
                )
            }


        scope.launch {

            withContext(
                Dispatchers.IO
            ) {

                imagesToDelete.forEach {

                    try {

                        context.contentResolver.delete(
                            it.uri,
                            null,
                            null
                        )

                    } catch (e: Exception) {

                        e.printStackTrace()
                    }
                }
            }


            selectedImages =
                emptySet()

            selectionMode =
                false

            intruderImages =
                loadIntruderImages(
                    context
                )
        }
    }


    // =========================================================
    // PREVIEW
    // =========================================================

    if (
        previewImage != null
    ) {

        IntruderImagePreviewScreen(

            image =
                previewImage!!,

            onBackClick = {

                previewImage = null

                reloadImages()
            },

            onDelete = {

                deleteSingleImage(
                    previewImage!!
                )
            }
        )

        return
    }


    // =========================================================
    // ROOT
    // =========================================================

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
                Modifier.fillMaxSize()
        ) {


            // =================================================
            // TOP BAR
            // =================================================

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(
                            start = 6.dp,
                            end = 6.dp
                        ),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = {

                        if (
                            selectionMode
                        ) {

                            selectionMode =
                                false

                            selectedImages =
                                emptySet()

                        } else {

                            onBackClick()
                        }
                    }
                ) {

                    if (
                        selectionMode
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.Close,

                            contentDescription =
                                "Close selection",

                            tint =
                                Color(0xFF333333),

                            modifier =
                                Modifier.size(
                                    21.dp
                                )
                        )

                    } else {

                        Image(
                            painter =
                                painterResource(
                                    id =
                                        R.drawable.backarrow
                                ),

                            contentDescription =
                                "Back",

                            modifier =
                                Modifier.size(
                                    30.dp
                                )
                        )
                    }
                }


                Text(
                    text =
                        if (
                            selectionMode
                        ) {

                            "Selected (${selectedImages.size})"

                        } else {

                            "Intruder"
                        },

                    color =
                        Color(0xFF333333),

                    fontSize =
                        22.sp,

                    modifier =
                        Modifier.weight(
                            1f
                        )
                )


                if (
                    selectionMode
                ) {

                    val allSelected =
                        intruderImages.isNotEmpty() &&
                                selectedImages.size ==
                                intruderImages.size


                    Row(
                        modifier =
                            Modifier
                                .clickable {

                                    selectedImages =
                                        if (
                                            allSelected
                                        ) {

                                            emptySet()

                                        } else {

                                            intruderImages
                                                .map {
                                                    it.uri
                                                }
                                                .toSet()
                                        }
                                }
                                .padding(
                                    horizontal = 6.dp
                                ),

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Text(
                            text =
                                "All",

                            color =
                                Color(0xFF444444),

                            fontSize =
                                12.sp
                        )


                        Spacer(
                            modifier =
                                Modifier.width(
                                    3.dp
                                )
                        )


                        Box(
                            modifier =
                                Modifier
                                    .size(
                                        10.dp
                                    )
                                    .background(
                                        if (
                                            allSelected
                                        ) {

                                            blueColor

                                        } else {

                                            Color.Transparent
                                        },

                                        CircleShape
                                    ),

                            contentAlignment =
                                Alignment.Center
                        ) {

                            if (
                                allSelected
                            ) {

                                Icon(
                                    imageVector =
                                        Icons.Default.Check,

                                    contentDescription =
                                        "All selected",

                                    tint =
                                        Color.White,

                                    modifier =
                                        Modifier.size(
                                            8.dp
                                        )
                                )
                            }
                        }
                    }

                } else {

                    // =================================================
                    // SET OBSERVATION ATTEMPTS
                    // =================================================

                    IconButton(
                        onClick = {

                            selectedObservationAttempts =
                                observationAttemptsToText(
                                    observationAttempts
                                )

                            showAttemptsDialog =
                                true
                        }
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.Settings,

                            contentDescription =
                                "Set observation attempts",

                            tint =
                                Color(0xFFBDBDBD),

                            modifier =
                                Modifier.size(
                                    23.dp
                                )
                        )
                    }
                }
            }


            // =================================================
            // INTRUDER CARD
            // =================================================

            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 14.dp,
                            end = 14.dp
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
                            2.dp
                    )
            ) {

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 14.dp,
                                end = 12.dp,
                                top = 14.dp,
                                bottom = 14.dp
                            ),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Image(
                        painter =
                            painterResource(
                                id =
                                    R.drawable.hacker
                            ),

                        contentDescription =
                            "Intruder Camera",

                        modifier =
                            Modifier.size(
                                30.dp
                            )
                    )


                    Spacer(
                        modifier =
                            Modifier.width(
                                14.dp
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
                                "Intruder Camera",

                            color =
                                Color(0xFF333333),

                            fontSize =
                                15.sp
                        )


                        Spacer(
                            modifier =
                                Modifier.height(
                                    3.dp
                                )
                        )


                        Text(
                            text =
                                "Capture photos after wrong password attempts",

                            color =
                                Color(0xFF666666),

                            fontSize =
                                11.sp,

                            lineHeight =
                                16.sp
                        )
                    }


                    // =================================================
                    // SWITCH
                    // =================================================

                    Switch(

                        checked =
                            intruderEnabled,

                        onCheckedChange = { enabled ->

                            if (enabled) {

                                val permissionGranted =
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.CAMERA
                                    ) ==
                                            PackageManager.PERMISSION_GRANTED


                                if (
                                    permissionGranted
                                ) {

                                    intruderEnabled =
                                        true

                                    scope.launch {

                                        dataStore
                                            .saveIntruderEnabled(
                                                true
                                            )
                                    }

                                } else {

                                    requestCameraPermission()
                                }

                            } else {

                                intruderEnabled =
                                    false

                                scope.launch {

                                    dataStore
                                        .saveIntruderEnabled(
                                            false
                                        )
                                }
                            }
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
                                    Color.White,

                                checkedTrackColor =
                                    blueColor,

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


            Spacer(
                modifier =
                    Modifier.height(
                        10.dp
                    )
            )


            // =================================================
            // ATTEMPTS SUMMARY
            // =================================================

            Text(
                text =
                    if (
                        observationAttempts == 0
                    ) {

                        "Photo capture: Never"

                    } else {

                        "Photo capture after $observationAttempts wrong attempts"
                    },

                color =
                    Color(0xFF777777),

                fontSize =
                    12.sp,

                modifier =
                    Modifier.padding(
                        start = 18.dp,
                        top = 2.dp,
                        bottom = 4.dp
                    )
            )


            // =================================================
            // IMAGES
            // =================================================

            if (
                intruderImages.isEmpty()
            ) {

                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(
                                1f
                            ),

                    contentAlignment =
                        Alignment.Center
                ) {

                    Column(
                        horizontalAlignment =
                            Alignment.CenterHorizontally,

                        verticalArrangement =
                            Arrangement.Center
                    ) {

                        Icon(
                            painter =
                                painterResource(
                                    id =
                                        R.drawable.nofound
                                ),

                            contentDescription =
                                "No intruder found",

                            tint =
                                Color(0xFFBDBDBD),

                            modifier =
                                Modifier.size(
                                    120.dp
                                )
                        )


                        Spacer(
                            modifier =
                                Modifier.height(
                                    14.dp
                                )
                        )


                        Text(
                            text =
                                "No Intruder Found",

                            color =
                                Color(0xFF333333),

                            fontSize =
                                19.sp
                        )
                    }
                }

            } else {

                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(
                                1f
                            )
                ) {

                    Text(
                        text =
                            "Today",

                        color =
                            Color(0xFF555555),

                        fontSize =
                            12.sp,

                        modifier =
                            Modifier.padding(
                                start = 18.dp,
                                top = 10.dp,
                                bottom = 7.dp
                            )
                    )


                    LazyVerticalGrid(

                        columns =
                            GridCells.Fixed(
                                3
                            ),

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(
                                    1f
                                )
                                .padding(
                                    start = 8.dp,
                                    end = 8.dp
                                ),

                        horizontalArrangement =
                            Arrangement.spacedBy(
                                6.dp
                            ),

                        verticalArrangement =
                            Arrangement.spacedBy(
                                6.dp
                            ),

                        contentPadding =
                            PaddingValues(
                                bottom = 20.dp
                            )
                    ) {

                        items(

                            items =
                                intruderImages,

                            key = {
                                it.uri.toString()
                            }

                        ) { image ->

                            IntruderImageItem(

                                image =
                                    image,

                                selected =
                                    selectedImages.contains(
                                        image.uri
                                    ),

                                selectionMode =
                                    selectionMode,

                                onClick = {

                                    if (
                                        selectionMode
                                    ) {

                                        selectedImages =
                                            if (
                                                selectedImages.contains(
                                                    image.uri
                                                )
                                            ) {

                                                selectedImages -
                                                        image.uri

                                            } else {

                                                selectedImages +
                                                        image.uri
                                            }

                                    } else {

                                        previewImage =
                                            image
                                    }
                                },

                                onLongClick = {

                                    if (
                                        !selectionMode
                                    ) {

                                        selectionMode =
                                            true

                                        selectedImages =
                                            setOf(
                                                image.uri
                                            )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }


        // =========================================================
        // DELETE
        // =========================================================

        if (
            selectionMode &&
            selectedImages.isNotEmpty()
        ) {

            Button(

                onClick =
                    ::deleteSelectedImages,

                modifier =
                    Modifier
                        .align(
                            Alignment.BottomCenter
                        )
                        .fillMaxWidth()
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 20.dp
                        )
                        .height(
                            47.dp
                        ),

                shape =
                    RoundedCornerShape(
                        7.dp
                    ),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            blueColor,

                        contentColor =
                            Color.White
                    )
            ) {

                Text(
                    text =
                        "Delete",

                    fontSize =
                        15.sp
                )
            }
        }
    }


    // =========================================================
    // SET OBSERVATION ATTEMPTS DIALOG
    // =========================================================

    if (
        showAttemptsDialog
    ) {

        AlertDialog(

            onDismissRequest = {

                showAttemptsDialog =
                    false
            },

            shape =
                RoundedCornerShape(
                    10.dp
                ),

            containerColor =
                Color.White,

            title = {

                Column(
                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Text(
                        text =
                            "Set Observation Attempts",

                        color =
                            Color(0xFF333333),

                        fontSize =
                            16.sp
                    )


                    Spacer(
                        modifier =
                            Modifier.height(
                                2.dp
                            )
                    )


                    Text(
                        text =
                            "Take an intruder photo after wrong password attempts",

                        color =
                            Color(0xFF999999),

                        fontSize =
                            10.sp,

                        lineHeight =
                            14.sp
                    )
                }
            },

            text = {

                Column(
                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    ObservationAttemptsOption(
                        text =
                            "Never",

                        selected =
                            selectedObservationAttempts ==
                                    "Never",

                        blueColor =
                            blueColor,

                        onClick = {

                            selectedObservationAttempts =
                                "Never"
                        }
                    )


                    ObservationAttemptsOption(
                        text =
                            "3 Attempts",

                        selected =
                            selectedObservationAttempts ==
                                    "3 Attempts",

                        blueColor =
                            blueColor,

                        onClick = {

                            selectedObservationAttempts =
                                "3 Attempts"
                        }
                    )


                    ObservationAttemptsOption(
                        text =
                            "5 Attempts",

                        selected =
                            selectedObservationAttempts ==
                                    "5 Attempts",

                        blueColor =
                            blueColor,

                        onClick = {

                            selectedObservationAttempts =
                                "5 Attempts"
                        }
                    )


                    ObservationAttemptsOption(
                        text =
                            "10 Attempts",

                        selected =
                            selectedObservationAttempts ==
                                    "10 Attempts",

                        blueColor =
                            blueColor,

                        onClick = {

                            selectedObservationAttempts =
                                "10 Attempts"
                        }
                    )
                }
            },


            dismissButton = {

                Button(

                    onClick = {

                        selectedObservationAttempts =
                            observationAttemptsToText(
                                observationAttempts
                            )

                        showAttemptsDialog =
                            false
                    },

                    colors =
                        ButtonDefaults.buttonColors(

                            containerColor =
                                Color.Transparent,

                            contentColor =
                                Color(0xFF999999)
                        )
                ) {

                    Text(
                        text =
                            "Cancel",

                        fontSize =
                            14.sp
                    )
                }
            },


            confirmButton = {

                Button(

                    onClick = {

                        val attempts =
                            observationAttemptsFromText(
                                selectedObservationAttempts
                            )


                        observationAttempts =
                            attempts


                        showAttemptsDialog =
                            false


                        scope.launch {

                            dataStore
                                .saveIntruderObservationTime(
                                    attempts
                                )
                        }
                    },

                    colors =
                        ButtonDefaults.buttonColors(

                            containerColor =
                                Color.Transparent,

                            contentColor =
                                blueColor
                        )
                ) {

                    Text(
                        text =
                            "Confirm",

                        fontSize =
                            14.sp
                    )
                }
            }
        )
    }


    // =========================================================
    // CAMERA PERMISSION DIALOG
    // =========================================================

    if (
        showPermissionSettingsDialog
    ) {

        AlertDialog(

            onDismissRequest = {

                showPermissionSettingsDialog =
                    false
            },

            shape =
                RoundedCornerShape(
                    12.dp
                ),

            containerColor =
                Color.White,

            title = {

                Text(
                    text =
                        "Camera Permission",

                    color =
                        Color(0xFF333333),

                    fontSize =
                        18.sp
                )
            },

            text = {

                Text(
                    text =
                        "Camera permission was denied. Please allow Camera permission from App Settings to use Intruder Camera.",

                    color =
                        Color(0xFF666666),

                    fontSize =
                        13.sp,

                    lineHeight =
                        19.sp
                )
            },

            dismissButton = {

                Button(

                    onClick = {

                        showPermissionSettingsDialog =
                            false
                    },

                    colors =
                        ButtonDefaults.buttonColors(

                            containerColor =
                                Color.Transparent,

                            contentColor =
                                Color(0xFF999999)
                        )
                ) {

                    Text(
                        text =
                            "Cancel"
                    )
                }
            },

            confirmButton = {

                Button(

                    onClick = {

                        showPermissionSettingsDialog =
                            false

                        openAppSettings()
                    },

                    colors =
                        ButtonDefaults.buttonColors(

                            containerColor =
                                Color.Transparent,

                            contentColor =
                                blueColor
                        )
                ) {

                    Text(
                        text =
                            "Settings"
                    )
                }
            }
        )
    }
}


// =============================================================
// ATTEMPTS INT -> TEXT
// =============================================================

private fun observationAttemptsToText(
    value: Int
): String {

    return when (value) {

        0 ->
            "Never"

        3 ->
            "3 Attempts"

        5 ->
            "5 Attempts"

        10 ->
            "10 Attempts"

        else ->
            "3 Attempts"
    }
}


// =============================================================
// ATTEMPTS TEXT -> INT
// =============================================================

private fun observationAttemptsFromText(
    value: String
): Int {

    return when (value) {

        "Never" ->
            0

        "3 Attempts" ->
            3

        "5 Attempts" ->
            5

        "10 Attempts" ->
            10

        else ->
            3
    }
}


// =============================================================
// IMAGE ITEM
// =============================================================

@Composable
private fun IntruderImageItem(
    image: IntruderImage,
    selected: Boolean,
    selectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .aspectRatio(
                    1f
                )
                .clip(
                    RoundedCornerShape(
                        12.dp
                    )
                )
                .combinedClickable(
                    onClick =
                        onClick,

                    onLongClick =
                        onLongClick
                )
    ) {

        IntruderThumbnail(
            uri =
                image.uri
        )


        if (
            selectionMode
        ) {

            Box(
                modifier =
                    Modifier
                        .align(
                            Alignment.TopEnd
                        )
                        .padding(
                            5.dp
                        )
                        .size(
                            14.dp
                        )
                        .background(

                            if (
                                selected
                            ) {

                                Color(0xFF0396FF)

                            } else {

                                Color.White.copy(
                                    alpha =
                                        0.75f
                                )
                            },

                            CircleShape
                        ),

                contentAlignment =
                    Alignment.Center
            ) {

                if (
                    selected
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Check,

                        contentDescription =
                            "Selected",

                        tint =
                            Color.White,

                        modifier =
                            Modifier.size(
                                10.dp
                            )
                    )
                }
            }
        }
    }
}


// =============================================================
// LOAD ROTATED BITMAP
// =============================================================

private suspend fun loadCorrectlyRotatedBitmap(
    context: Context,
    uri: Uri
): Bitmap? {

    return withContext(
        Dispatchers.IO
    ) {

        try {

            val bitmap =
                context.contentResolver
                    .openInputStream(
                        uri
                    )
                    ?.use { input ->

                        BitmapFactory
                            .decodeStream(
                                input
                            )
                    }
                    ?: return@withContext null


            val orientation =
                context.contentResolver
                    .openInputStream(
                        uri
                    )
                    ?.use { input ->

                        ExifInterface(
                            input
                        ).getAttributeInt(

                            ExifInterface.TAG_ORIENTATION,

                            ExifInterface.ORIENTATION_NORMAL
                        )
                    }
                    ?: ExifInterface.ORIENTATION_NORMAL


            val matrix =
                Matrix()


            when (
                orientation
            ) {

                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> {

                    matrix.setScale(
                        -1f,
                        1f
                    )
                }


                ExifInterface.ORIENTATION_ROTATE_180 -> {

                    matrix.setRotate(
                        180f
                    )
                }


                ExifInterface.ORIENTATION_FLIP_VERTICAL -> {

                    matrix.setScale(
                        1f,
                        -1f
                    )
                }


                ExifInterface.ORIENTATION_TRANSPOSE -> {

                    matrix.setRotate(
                        90f
                    )

                    matrix.postScale(
                        -1f,
                        1f
                    )
                }


                ExifInterface.ORIENTATION_ROTATE_90 -> {

                    matrix.setRotate(
                        90f
                    )
                }


                ExifInterface.ORIENTATION_TRANSVERSE -> {

                    matrix.setRotate(
                        -90f
                    )

                    matrix.postScale(
                        -1f,
                        1f
                    )
                }


                ExifInterface.ORIENTATION_ROTATE_270 -> {

                    matrix.setRotate(
                        270f
                    )
                }
            }


            if (
                matrix.isIdentity
            ) {

                return@withContext bitmap
            }


            Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width,
                bitmap.height,
                matrix,
                true
            )

        } catch (
            e: Exception
        ) {

            e.printStackTrace()

            null
        }
    }
}


// =============================================================
// THUMBNAIL
// =============================================================

@Composable
private fun IntruderThumbnail(
    uri: Uri
) {

    val context =
        LocalContext.current

    var bitmap by remember(uri) {
        mutableStateOf<Bitmap?>(null)
    }


    LaunchedEffect(uri) {

        bitmap =
            loadCorrectlyRotatedBitmap(
                context,
                uri
            )
    }


    if (
        bitmap != null
    ) {

        Image(
            bitmap =
                bitmap!!
                    .asImageBitmap(),

            contentDescription =
                "Intruder photo",

            modifier =
                Modifier.fillMaxSize(),

            contentScale =
                ContentScale.Crop
        )

    } else {

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Color(0xFFE8E8E8)
                    ),

            contentAlignment =
                Alignment.Center
        ) {

            Text(
                text =
                    "Loading...",

                color =
                    Color(0xFF999999),

                fontSize =
                    9.sp
            )
        }
    }
}


// =============================================================
// PREVIEW
// =============================================================

@Composable
private fun IntruderImagePreviewScreen(
    image: IntruderImage,
    onBackClick: () -> Unit,
    onDelete: () -> Unit
) {

    val context =
        LocalContext.current

    var bitmap by remember(image.uri) {
        mutableStateOf<Bitmap?>(null)
    }


    LaunchedEffect(
        image.uri
    ) {

        bitmap =
            loadCorrectlyRotatedBitmap(
                context,
                image.uri
            )
    }


    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Color.White
                )
    ) {

        Column(
            modifier =
                Modifier.fillMaxSize()
        ) {

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            64.dp
                        )
                        .padding(
                            start = 6.dp,
                            end = 6.dp
                        ),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                IconButton(
                    onClick =
                        onBackClick
                ) {

                    Image(
                        painter =
                            painterResource(
                                id =
                                    R.drawable.backarrow
                            ),

                        contentDescription =
                            "Back",

                        modifier =
                            Modifier.size(
                                30.dp
                            )
                    )
                }


                Text(
                    text =
                        image.name,

                    color =
                        Color(0xFF333333),

                    fontSize =
                        16.sp,

                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    maxLines =
                        1
                )


                Icon(
                    imageVector =
                        Icons.Default.Settings,

                    contentDescription =
                        "Settings",

                    tint =
                        Color(0xFFBDBDBD),

                    modifier =
                        Modifier
                            .padding(
                                end = 6.dp
                            )
                            .size(
                                23.dp
                            )
                )
            }


            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(
                            1f
                        )
                        .padding(
                            start = 6.dp,
                            end = 6.dp,
                            top = 15.dp,
                            bottom = 15.dp
                        )
                        .clip(
                            RoundedCornerShape(
                                8.dp
                            )
                        )
                        .background(
                            Color(0xFFF0EEEE)
                        ),

                contentAlignment =
                    Alignment.Center
            ) {

                if (
                    bitmap != null
                ) {

                    Image(
                        bitmap =
                            bitmap!!
                                .asImageBitmap(),

                        contentDescription =
                            "Intruder photo preview",

                        modifier =
                            Modifier.fillMaxSize(),

                        contentScale =
                            ContentScale.Fit
                    )
                }
            }


            Button(
                onClick =
                    onDelete,

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 10.dp,
                            end = 10.dp,
                            bottom = 54.dp
                        )
                        .height(
                            46.dp
                        ),

                shape =
                    RoundedCornerShape(
                        7.dp
                    ),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            Color(0xFF2196F3),

                        contentColor =
                            Color.White
                    )
            ) {

                Text(
                    text =
                        "Delete",

                    fontSize =
                        15.sp
                )
            }
        }
    }
}


// =============================================================
// LOAD IMAGES
// =============================================================

private suspend fun loadIntruderImages(
    context: Context
): List<IntruderImage> {

    return withContext(
        Dispatchers.IO
    ) {

        val result =
            mutableListOf<IntruderImage>()


        val collection =
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI


        val projection =
            arrayOf(

                MediaStore.Images.Media._ID,

                MediaStore.Images.Media.DISPLAY_NAME,

                MediaStore.Images.Media.DATE_ADDED,

                MediaStore.Images.Media.RELATIVE_PATH
            )


        val selection =
            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.Q
            ) {

                "${MediaStore.Images.Media.RELATIVE_PATH}=?"

            } else {

                "${MediaStore.Images.Media.DATA} LIKE ?"
            }


        val selectionArgs =
            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.Q
            ) {

                arrayOf(
                    "Pictures/AppLock/Intruder/"
                )

            } else {

                arrayOf(
                    "%Pictures/AppLock/Intruder/%"
                )
            }


        val sortOrder =
            "${MediaStore.Images.Media.DATE_ADDED} DESC"


        try {

            context.contentResolver.query(

                collection,

                projection,

                selection,

                selectionArgs,

                sortOrder

            )?.use { cursor ->

                val idColumn =
                    cursor.getColumnIndexOrThrow(
                        MediaStore.Images.Media._ID
                    )


                val nameColumn =
                    cursor.getColumnIndexOrThrow(
                        MediaStore.Images.Media.DISPLAY_NAME
                    )


                val dateColumn =
                    cursor.getColumnIndexOrThrow(
                        MediaStore.Images.Media.DATE_ADDED
                    )


                while (
                    cursor.moveToNext()
                ) {

                    val id =
                        cursor.getLong(
                            idColumn
                        )


                    val name =
                        cursor.getString(
                            nameColumn
                        )


                    val date =
                        cursor.getLong(
                            dateColumn
                        )


                    val contentUri =
                        ContentUris.withAppendedId(
                            collection,
                            id
                        )


                    result.add(

                        IntruderImage(

                            uri =
                                contentUri,

                            name =
                                name,

                            dateAdded =
                                date
                        )
                    )
                }
            }

        } catch (
            e: Exception
        ) {

            e.printStackTrace()
        }


        result
    }
}


// =============================================================
// OBSERVATION ATTEMPT OPTION
// =============================================================

@Composable
private fun ObservationAttemptsOption(
    text: String,
    selected: Boolean,
    blueColor: Color,
    onClick: () -> Unit
) {

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
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

            colors =
                RadioButtonDefaults.colors(
                    selectedColor =
                        blueColor,

                    unselectedColor =
                        Color(0xFF555555)
                ),

            modifier =
                Modifier
                    .size(
                        40.dp
                    )
                    .scale(
                        0.75f
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
                text,

            color =
                Color(0xFF444444),

            fontSize =
                14.sp
        )
    }
}

