package com.example.applock.Design.screens

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
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
import com.example.applock.R
import com.example.applock.data.DataStoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


// =============================================================
// INTRUDER IMAGE DATA
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

    val context =
        LocalContext.current

    val dataStore =
        remember {
            DataStoreManager(context)
        }

    val scope =
        rememberCoroutineScope()


    // =========================================================
    // COLORS
    // =========================================================

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


    var observationTime by remember {
        mutableStateOf(10)
    }


    var showTimeDialog by remember {
        mutableStateOf(false)
    }


    var selectedObservationTime by remember {
        mutableStateOf("5 Seconds")
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
    // SELECTION MODE
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
    // LOAD SAVED SETTINGS
    // =========================================================

    LaunchedEffect(Unit) {

        /*
         * IMPORTANT:
         *
         * Intruder switch is controlled only by the value
         * saved when the user manually changes the switch.
         *
         * DataStore default is false.
         */

        intruderEnabled =
            dataStore
                .getIntruderEnabled()
                .first()


        observationTime =
            dataStore
                .getIntruderObservationTime()
                .first()


        selectedObservationTime =
            observationTimeToText(
                observationTime
            )


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

                } catch (
                    e: Exception
                ) {

                    e.printStackTrace()
                }
            }


            previewImage =
                null


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

                    } catch (
                        e: Exception
                    ) {

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
    // PREVIEW SCREEN
    // =========================================================

    if (
        previewImage != null
    ) {

        IntruderImagePreviewScreen(

            image =
                previewImage!!,

            onBackClick = {

                previewImage =
                    null

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


                // =================================================
                // BACK / CLOSE
                // =================================================

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
                                Modifier.size(21.dp)
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
                                Modifier.size(30.dp)
                        )
                    }
                }


                // =================================================
                // TITLE
                // =================================================

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
                        Modifier.weight(1f)
                )


                // =================================================
                // SETTINGS / ALL
                // =================================================

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
                                Modifier.width(3.dp)
                        )


                        Box(

                            modifier =
                                Modifier
                                    .size(10.dp)
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
                                        Modifier.size(8.dp)
                                )
                            }
                        }
                    }

                } else {

                    IconButton(

                        onClick = {

                            selectedObservationTime =
                                observationTimeToText(
                                    observationTime
                                )

                            showTimeDialog =
                                true
                        }
                    ) {

                        Icon(

                            imageVector =
                                Icons.Default.Settings,

                            contentDescription =
                                "Intruder settings",

                            tint =
                                Color(0xFFBDBDBD),

                            modifier =
                                Modifier.size(23.dp)
                        )
                    }
                }
            }


            // =================================================
            // INTRUDER CAMERA CARD
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
                    RoundedCornerShape(12.dp),

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


                    // =================================================
                    // CAMERA ICON
                    // =================================================

                    Image(

                        painter =
                            painterResource(
                                id =
                                    R.drawable.hacker
                            ),

                        contentDescription =
                            "Intruder Camera",

                        modifier =
                            Modifier.size(30.dp)
                    )


                    Spacer(
                        modifier =
                            Modifier.width(14.dp)
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
                                "Intruder Camera",

                            color =
                                Color(0xFF333333),

                            fontSize =
                                15.sp
                        )


                        Spacer(
                            modifier =
                                Modifier.height(3.dp)
                        )


                        Text(

                            text =
                                "Capture photos of anyone who enters\n" +
                                        "the wrong password",

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

                            /*
                             * IMPORTANT:
                             *
                             * Only the user's switch action changes
                             * the Intruder Camera state.
                             */

                            intruderEnabled =
                                enabled

                            scope.launch {

                                dataStore
                                    .saveIntruderEnabled(
                                        enabled
                                    )
                            }
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


            // =================================================
            // IMAGES OR EMPTY AREA
            // =================================================

            if (
                intruderImages.isEmpty()
            ) {

                Box(

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),

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
                                Modifier.size(120.dp)
                        )


                        Spacer(
                            modifier =
                                Modifier.height(14.dp)
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
                            .weight(1f)
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
                            GridCells.Fixed(3),

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(1f)
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
                                                selectedImages
                                                    .contains(
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
        // DELETE BUTTON
        // =========================================================

        if (
            selectionMode &&
            selectedImages.isNotEmpty()
        ) {

            Button(

                onClick = {

                    deleteSelectedImages()
                },

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
                        .height(47.dp),

                shape =
                    RoundedCornerShape(7.dp),

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
    // OBSERVATION TIME DIALOG
    // =========================================================

    if (
        showTimeDialog
    ) {

        AlertDialog(

            onDismissRequest = {

                showTimeDialog =
                    false
            },

            shape =
                RoundedCornerShape(10.dp),

            containerColor =
                Color.White,

            title = {

                Column(
                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Text(

                        text =
                            "Set Observation Time",

                        color =
                            Color(0xFF333333),

                        fontSize =
                            16.sp
                    )


                    Text(

                        text =
                            "Number of times failed to unblocked",

                        color =
                            Color(0xFF999999),

                        fontSize =
                            10.sp
                    )
                }
            },

            text = {

                Column(
                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    ObservationTimeOption(
                        text = "Never",
                        selected =
                            selectedObservationTime ==
                                    "Never",
                        blueColor =
                            blueColor,
                        onClick = {
                            selectedObservationTime =
                                "Never"
                        }
                    )


                    ObservationTimeOption(
                        text = "5 Seconds",
                        selected =
                            selectedObservationTime ==
                                    "5 Seconds",
                        blueColor =
                            blueColor,
                        onClick = {
                            selectedObservationTime =
                                "5 Seconds"
                        }
                    )


                    ObservationTimeOption(
                        text = "15 Seconds",
                        selected =
                            selectedObservationTime ==
                                    "15 Seconds",
                        blueColor =
                            blueColor,
                        onClick = {
                            selectedObservationTime =
                                "15 Seconds"
                        }
                    )


                    ObservationTimeOption(
                        text = "30 Seconds",
                        selected =
                            selectedObservationTime ==
                                    "30 Seconds",
                        blueColor =
                            blueColor,
                        onClick = {
                            selectedObservationTime =
                                "30 Seconds"
                        }
                    )
                }
            },


            // =====================================================
            // CANCEL
            // =====================================================

            dismissButton = {

                Button(

                    onClick = {

                        showTimeDialog =
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


            // =====================================================
            // CONFIRM
            // =====================================================

            confirmButton = {

                Button(

                    onClick = {

                        val timeInSeconds =
                            observationTimeFromText(
                                selectedObservationTime
                            )


                        observationTime =
                            timeInSeconds


                        showTimeDialog =
                            false


                        scope.launch {

                            dataStore
                                .saveIntruderObservationTime(
                                    timeInSeconds
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
}


// =============================================================
// CONVERT INT -> UI TEXT
// =============================================================

private fun observationTimeToText(
    value: Int
): String {

    return when (value) {

        0 ->
            "Never"

        5 ->
            "5 Seconds"

        15 ->
            "15 Seconds"

        30 ->
            "30 Seconds"

        else ->
            "5 Seconds"
    }
}


// =============================================================
// CONVERT UI TEXT -> INT
// =============================================================

private fun observationTimeFromText(
    value: String
): Int {

    return when (value) {

        "Never" ->
            0

        "5 Seconds" ->
            5

        "15 Seconds" ->
            15

        "30 Seconds" ->
            30

        else ->
            5
    }
}


// =============================================================
// INTRUDER IMAGE ITEM
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
                .aspectRatio(1f)
                .clip(
                    RoundedCornerShape(12.dp)
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
                        .size(14.dp)
                        .background(

                            if (
                                selected
                            ) {

                                Color(0xFF0396FF)

                            } else {

                                Color.White.copy(
                                    alpha = 0.75f
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
                            Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}


// =============================================================
// LOAD BITMAP + FIX EXIF ROTATION
// =============================================================

private suspend fun loadCorrectlyRotatedBitmap(
    context: Context,
    uri: Uri
): Bitmap? {

    return withContext(
        Dispatchers.IO
    ) {

        try {

            // =================================================
            // FIRST: DECODE BITMAP
            // =================================================

            val bitmap =
                context.contentResolver
                    .openInputStream(uri)
                    ?.use { input ->

                        BitmapFactory
                            .decodeStream(input)
                    }
                    ?: return@withContext null


            // =================================================
            // SECOND: READ EXIF ORIENTATION
            // =================================================

            val orientation =
                context.contentResolver
                    .openInputStream(uri)
                    ?.use { input ->

                        ExifInterface(input)
                            .getAttributeInt(
                                ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_NORMAL
                            )
                    }
                    ?: ExifInterface.ORIENTATION_NORMAL


            // =================================================
            // CREATE ROTATION MATRIX
            // =================================================

            val matrix =
                Matrix()


            when (orientation) {

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


                ExifInterface.ORIENTATION_NORMAL -> {
                    // No rotation required
                }
            }


            // =================================================
            // IF NO ROTATION
            // =================================================

            if (
                matrix.isIdentity
            ) {

                return@withContext bitmap
            }


            // =================================================
            // APPLY ROTATION
            // =================================================

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

        mutableStateOf<Bitmap?>(
            null
        )
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
// FULL IMAGE PREVIEW SCREEN
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

        mutableStateOf<Bitmap?>(
            null
        )
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
                            Modifier.size(30.dp)
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
                        Modifier.weight(1f),

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
                            .size(23.dp)
                )
            }


            // =================================================
            // IMAGE
            // =================================================

            Box(

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
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


            // =================================================
            // DELETE BUTTON
            // =================================================

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
                        .height(46.dp),

                shape =
                    RoundedCornerShape(7.dp),

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
// LOAD INTRUDER IMAGES
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
// OBSERVATION TIME OPTION
// =============================================================

@Composable
private fun ObservationTimeOption(

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
                }
                .padding(
                    vertical = 0.dp
                ),

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
                    .size(40.dp)
                    .scale(0.75f)
        )


        Spacer(
            modifier =
                Modifier.width(4.dp)
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