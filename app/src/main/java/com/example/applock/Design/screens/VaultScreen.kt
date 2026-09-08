package com.example.applock.Design.screens

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.applock.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

// =============================================================
// VAULT MEDIA ITEM
// =============================================================

data class VaultMediaItem(
    val uri: Uri,
    val name: String,
    val bucketName: String,
    val isVideo: Boolean,
    val isVaultFile: Boolean = false,
    val vaultFilePath: String? = null
)

// =============================================================
// ALL ALBUMS KEY
// =============================================================

private const val ALL_ALBUMS_KEY = "ALL"

// =============================================================
// VAULT SCREEN
// =============================================================

@Composable
fun VaultScreen() {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // =========================================================
    // 0 = PHOTOS
    // 1 = VIDEOS
    // =========================================================

    var selectedTab by remember {
        mutableStateOf(0)
    }

    // =========================================================
    // GALLERY OPEN
    // =========================================================

    var galleryOpen by remember {
        mutableStateOf(false)
    }

    // =========================================================
    // SELECTED MEDIA
    // =========================================================

    var selectedMedia by remember {
        mutableStateOf<Set<Uri>>(emptySet())
    }

    // =========================================================
    // HIDDEN MEDIA
    // =========================================================

    var hiddenMedia by remember {
        mutableStateOf(
            loadHiddenMediaMetadata(context)
        )
    }

    // =========================================================
    // UNHIDE
    // =========================================================

    var mediaToUnhide by remember {
        mutableStateOf<VaultMediaItem?>(null)
    }

    // =========================================================
    // PENDING COPIED ITEMS
    // =========================================================

    var pendingCopiedItems by remember {
        mutableStateOf<List<VaultMediaItem>>(emptyList())
    }

    // =========================================================
    // MEDIA PERMISSION
    // =========================================================

    val mediaPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val granted =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                    val requiredPermission =
                        if (selectedTab == 0) {
                            Manifest.permission.READ_MEDIA_IMAGES
                        } else {
                            Manifest.permission.READ_MEDIA_VIDEO
                        }

                    permissions[requiredPermission] == true ||
                            ContextCompat.checkSelfPermission(
                                context,
                                requiredPermission
                            ) == PackageManager.PERMISSION_GRANTED

                } else {

                    permissions[
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ] == true ||
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            ) == PackageManager.PERMISSION_GRANTED
                }

            if (granted) {
                galleryOpen = true
            }
        }

    // =========================================================
    // CHECK MEDIA PERMISSION
    // =========================================================

    fun hasMediaPermission(): Boolean {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            val permission =
                if (selectedTab == 0) {
                    Manifest.permission.READ_MEDIA_IMAGES
                } else {
                    Manifest.permission.READ_MEDIA_VIDEO
                }

            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED

        } else {

            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    // =========================================================
    // OPEN GALLERY
    // =========================================================

    fun openGallery() {

        if (hasMediaPermission()) {

            galleryOpen = true

        } else {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                val permission =
                    if (selectedTab == 0) {
                        Manifest.permission.READ_MEDIA_IMAGES
                    } else {
                        Manifest.permission.READ_MEDIA_VIDEO
                    }

                mediaPermissionLauncher.launch(
                    arrayOf(permission)
                )

            } else {

                mediaPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                )
            }
        }
    }

    // =========================================================
    // ANDROID MODIFY / WRITE PERMISSION
    //
    // IMPORTANT:
    // createWriteRequest() is used here instead of
    // createDeleteRequest().
    // =========================================================

    val writeLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->

            if (result.resultCode == Activity.RESULT_OK) {

                // =================================================
                // USER ALLOWED MODIFY ACCESS
                // =================================================

                coroutineScope.launch {

                    val itemsToDelete =
                        pendingCopiedItems

                    val deleteSuccess =
                        withContext(Dispatchers.IO) {

                            itemsToDelete.all { copied ->

                                try {

                                    val originalUri =
                                        getOriginalUriFromVaultItem(
                                            copied
                                        )

                                    context.contentResolver.delete(
                                        originalUri,
                                        null,
                                        null
                                    ) > 0

                                } catch (e: Exception) {

                                    e.printStackTrace()

                                    false
                                }
                            }
                        }

                    if (deleteSuccess) {

                        // =============================================
                        // ADD COPIES TO VAULT METADATA
                        // =============================================

                        val newHidden =
                            hiddenMedia.toMutableList()

                        pendingCopiedItems.forEach { copied ->

                            val alreadyExists =
                                newHidden.any {

                                    it.vaultFilePath ==
                                            copied.vaultFilePath
                                }

                            if (!alreadyExists) {
                                newHidden.add(copied)
                            }
                        }

                        hiddenMedia = newHidden

                        saveHiddenMediaMetadata(
                            context = context,
                            mediaList = newHidden
                        )

                    } else {

                        // =============================================
                        // ORIGINAL DELETE FAILED
                        // DELETE COPIED VAULT FILES
                        // =============================================

                        pendingCopiedItems.forEach { copied ->

                            try {

                                copied.vaultFilePath?.let { path ->
                                    File(path).delete()
                                }

                            } catch (e: Exception) {

                                e.printStackTrace()
                            }
                        }
                    }

                    pendingCopiedItems = emptyList()
                    selectedMedia = emptySet()
                    galleryOpen = false
                }

            } else {

                // =================================================
                // USER DENIED MODIFY PERMISSION
                // =================================================

                pendingCopiedItems.forEach { copied ->

                    try {

                        copied.vaultFilePath?.let { path ->
                            File(path).delete()
                        }

                    } catch (e: Exception) {

                        e.printStackTrace()
                    }
                }

                pendingCopiedItems = emptyList()
                selectedMedia = emptySet()
                galleryOpen = false
            }
        }

    // =========================================================
    // GALLERY SCREEN
    // =========================================================

    if (galleryOpen) {

        VaultGalleryScreen(

            context = context,

            isVideo = selectedTab == 1,

            hiddenMedia = hiddenMedia,

            selectedMedia = selectedMedia,

            onSelectionChange = { newSelection ->
                selectedMedia = newSelection
            },

            onBack = {
                galleryOpen = false
                selectedMedia = emptySet()
            },

            onHide = {

                coroutineScope.launch {

                    val allMedia =
                        loadVaultMedia(
                            context = context,
                            isVideo = selectedTab == 1
                        )

                    val itemsToHide =
                        allMedia.filter { media ->

                            selectedMedia.contains(
                                media.uri
                            )
                        }

                    if (itemsToHide.isEmpty()) {

                        selectedMedia = emptySet()
                        galleryOpen = false

                        return@launch
                    }

                    // =================================================
                    // COPY TO PRIVATE VAULT FIRST
                    // =================================================

                    val copiedItems =
                        withContext(Dispatchers.IO) {

                            itemsToHide.mapNotNull { media ->

                                copyMediaToVault(
                                    context = context,
                                    media = media
                                )
                            }
                        }

                    // =================================================
                    // COPY FAILED
                    // =================================================

                    if (
                        copiedItems.size !=
                        itemsToHide.size
                    ) {

                        copiedItems.forEach { copied ->

                            try {

                                copied.vaultFilePath?.let { path ->
                                    File(path).delete()
                                }

                            } catch (e: Exception) {

                                e.printStackTrace()
                            }
                        }

                        selectedMedia = emptySet()

                        return@launch
                    }

                    // =================================================
                    // ANDROID 11+
                    //
                    // SHOW MODIFY / WRITE PERMISSION
                    // =================================================

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                        try {

                            val uris =
                                itemsToHide.map {
                                    it.uri
                                }

                            val pendingIntent =
                                MediaStore.createWriteRequest(
                                    context.contentResolver,
                                    uris
                                )

                            // =================================================
                            // KEEP BOTH ORIGINAL URI + VAULT FILE
                            // =================================================

                            pendingCopiedItems =
                                copiedItems.mapIndexed { index, copied ->

                                    copied.copy(
                                        uri = itemsToHide[index].uri
                                    )
                                }

                            writeLauncher.launch(

                                IntentSenderRequest.Builder(
                                    pendingIntent.intentSender
                                ).build()
                            )

                        } catch (e: Exception) {

                            e.printStackTrace()

                            // =================================================
                            // CLEAN COPIED FILES
                            // =================================================

                            copiedItems.forEach { copied ->

                                try {

                                    copied.vaultFilePath?.let { path ->
                                        File(path).delete()
                                    }

                                } catch (cleanupError: Exception) {

                                    cleanupError.printStackTrace()
                                }
                            }

                            pendingCopiedItems = emptyList()
                            selectedMedia = emptySet()
                            galleryOpen = false
                        }

                    } else {

                        // =================================================
                        // ANDROID 10 AND BELOW
                        // =================================================

                        val deleteSuccess =
                            withContext(Dispatchers.IO) {

                                itemsToHide.all { media ->

                                    try {

                                        context.contentResolver.delete(
                                            media.uri,
                                            null,
                                            null
                                        ) > 0

                                    } catch (e: Exception) {

                                        e.printStackTrace()

                                        false
                                    }
                                }
                            }

                        if (deleteSuccess) {

                            val newHidden =
                                hiddenMedia.toMutableList()

                            copiedItems.forEach { copied ->

                                if (
                                    newHidden.none {

                                        it.vaultFilePath ==
                                                copied.vaultFilePath
                                    }
                                ) {

                                    newHidden.add(copied)
                                }
                            }

                            hiddenMedia = newHidden

                            saveHiddenMediaMetadata(
                                context = context,
                                mediaList = newHidden
                            )

                        } else {

                            copiedItems.forEach { copied ->

                                try {

                                    copied.vaultFilePath?.let { path ->
                                        File(path).delete()
                                    }

                                } catch (e: Exception) {

                                    e.printStackTrace()
                                }
                            }
                        }

                        selectedMedia = emptySet()
                        galleryOpen = false
                    }
                }
            }
        )

        return
    }

    // =============================================================
    // NORMAL VAULT SCREEN
    // =============================================================

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            // =================================================
            // HEADING
            // =================================================

            Text(
                text = stringResource(R.string.vault),
                color = Color.Black,
                fontSize = 20.sp,
                modifier = Modifier.padding(
                    start = 60.dp,
                    top = 10.dp
                )
            )

            // =================================================
            // TABS
            // =================================================

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp)
                    .height(42.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                VaultTab(
                    selected = selectedTab == 0,
                    icon = R.drawable.imageicon,
                    text = stringResource(R.string.photos),
                    onClick = {

                        selectedTab = 0
                        selectedMedia = emptySet()
                    },
                    modifier = Modifier.weight(1f)
                )

                VaultTab(
                    selected = selectedTab == 1,
                    icon = R.drawable.vedioicon,
                    text = stringResource(R.string.videos),
                    onClick = {

                        selectedTab = 1
                        selectedMedia = emptySet()
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // =================================================
            // CONTENT
            // =================================================

            Box(
                modifier = Modifier.fillMaxSize()
            ) {

                val currentHidden =
                    hiddenMedia.filter {

                        it.isVideo ==
                                (selectedTab == 1)
                    }

                if (currentHidden.isEmpty()) {

                    if (selectedTab == 0) {

                        PhotosVaultContent()

                    } else {

                        VideosVaultContent()
                    }

                } else {

                    VaultHiddenMediaGrid(

                        mediaList = currentHidden,

                        onMediaClick = { media ->
                            mediaToUnhide = media
                        }
                    )
                }
            }
        }

        // =====================================================
        // PLUS BUTTON
        // =====================================================

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 15.dp,
                    bottom = 29.dp
                )
                .size(50.dp)
                .clip(CircleShape)
                .clickable {
                    openGallery()
                },
            contentAlignment = Alignment.Center
        ) {

            Image(
                painter = painterResource(
                    id = R.drawable.plus
                ),
                contentDescription = stringResource(
                    R.string.add
                ),
                modifier = Modifier.size(50.dp)
            )
        }

        // =====================================================
        // UNHIDE DIALOG
        // =====================================================

        mediaToUnhide?.let { media ->

            AlertDialog(

                onDismissRequest = {
                    mediaToUnhide = null
                },

                title = {

                    Text(
                        text =
                            if (media.isVideo) {

                                stringResource(
                                    R.string.unhide_video
                                )

                            } else {

                                stringResource(
                                    R.string.unhide_image
                                )
                            }
                    )
                },

                text = {

                    Text(
                        text =
                            if (media.isVideo) {

                                stringResource(
                                    R.string.unhide_video_question
                                )

                            } else {

                                stringResource(
                                    R.string.unhide_image_question
                                )
                            }
                    )
                },

                confirmButton = {

                    TextButton(

                        onClick = {

                            coroutineScope.launch {

                                val success =
                                    withContext(Dispatchers.IO) {

                                        restoreMediaToGallery(
                                            context = context,
                                            media = media
                                        )
                                    }

                                if (success) {

                                    val updated =
                                        hiddenMedia.filterNot {

                                            it.vaultFilePath ==
                                                    media.vaultFilePath
                                        }

                                    hiddenMedia = updated

                                    saveHiddenMediaMetadata(
                                        context = context,
                                        mediaList = updated
                                    )
                                }

                                mediaToUnhide = null
                            }
                        }
                    ) {

                        Text(
                            text = stringResource(
                                R.string.unhide
                            ),
                            color = Color(0xFF0396FF)
                        )
                    }
                },

                dismissButton = {

                    TextButton(

                        onClick = {
                            mediaToUnhide = null
                        }
                    ) {

                        Text(
                            text = stringResource(
                                R.string.cancel
                            ),
                            color = Color(0xFF818181)
                        )
                    }
                }
            )
        }
    }
}

// =============================================================
// GET ORIGINAL URI
// =============================================================

private fun getOriginalUriFromVaultItem(
    media: VaultMediaItem
): Uri {

    return media.uri
}

// =============================================================
// TAB
// =============================================================

@Composable
private fun VaultTab(
    selected: Boolean,
    icon: Int,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val selectedColor = Color(0xFF0396FF)
    val unselectedColor = Color(0xFFBDBDBD)

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable {
                onClick()
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = Modifier
                .height(40.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {

            Image(
                painter = painterResource(id = icon),
                contentDescription = text,
                colorFilter = ColorFilter.tint(
                    if (selected) {
                        selectedColor
                    } else {
                        unselectedColor
                    }
                ),
                modifier = Modifier.size(18.dp)
            )

            Spacer(
                modifier = Modifier.width(5.dp)
            )

            Text(
                text = text,
                color =
                    if (selected) {
                        selectedColor
                    } else {
                        unselectedColor
                    },
                fontSize = 16.sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(
                    if (selected) {
                        selectedColor
                    } else {
                        Color.Transparent
                    }
                )
        )
    }
}

// =============================================================
// EMPTY PHOTOS
// =============================================================

@Composable
private fun PhotosVaultContent() {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(
                    id = R.drawable.imageclick
                ),
                contentDescription = stringResource(
                    R.string.add_image
                ),
                modifier = Modifier.size(80.dp)
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = stringResource(
                    R.string.click_to_add_image
                ),
                color = Color(0xFF9E9E9E),
                fontSize = 14.sp
            )
        }
    }
}

// =============================================================
// EMPTY VIDEOS
// =============================================================

@Composable
private fun VideosVaultContent() {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(
                    id = R.drawable.vedioclick
                ),
                contentDescription = stringResource(
                    R.string.add_video
                ),
                modifier = Modifier.size(80.dp)
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = stringResource(
                    R.string.click_to_add_video
                ),
                color = Color(0xFF9E9E9E),
                fontSize = 14.sp
            )
        }
    }
}

// =============================================================
// HIDDEN GRID
// =============================================================

@Composable
private fun VaultHiddenMediaGrid(
    mediaList: List<VaultMediaItem>,
    onMediaClick: (VaultMediaItem) -> Unit
) {

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 8.dp,
            end = 8.dp,
            top = 12.dp,
            bottom = 90.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {

        items(
            items = mediaList,
            key = { media ->
                media.vaultFilePath
                    ?: media.uri.toString()
            }
        ) { media ->

            VaultHiddenMediaItem(
                media = media,
                onClick = {
                    onMediaClick(media)
                }
            )
        }
    }
}

// =============================================================
// HIDDEN ITEM
// =============================================================

@Composable
private fun VaultHiddenMediaItem(
    media: VaultMediaItem,
    onClick: () -> Unit
) {

    val context = LocalContext.current

    var bitmap by remember(
        media.uri,
        media.vaultFilePath
    ) {
        mutableStateOf<Bitmap?>(null)
    }

    LaunchedEffect(
        media.uri,
        media.vaultFilePath
    ) {

        bitmap =
            loadVaultThumbnail(
                context = context,
                uri =
                    if (media.isVaultFile) {

                        Uri.fromFile(
                            File(
                                media.vaultFilePath ?: ""
                            )
                        )

                    } else {

                        media.uri
                    },
                isVideo = media.isVideo
            )
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(1.dp))
            .clickable {
                onClick()
            }
    ) {

        bitmap?.let { loadedBitmap ->

            Image(
                bitmap = loadedBitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Image(
            painter = painterResource(
                id = R.drawable.locked
            ),
            contentDescription = stringResource(
                R.string.hidden
            ),
            colorFilter = ColorFilter.tint(
                Color.White
            ),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(5.dp)
                .size(17.dp)
        )
    }
}

// =============================================================
// GALLERY SCREEN
// =============================================================

@Composable
private fun VaultGalleryScreen(
    context: Context,
    isVideo: Boolean,
    hiddenMedia: List<VaultMediaItem>,
    selectedMedia: Set<Uri>,
    onSelectionChange: (Set<Uri>) -> Unit,
    onBack: () -> Unit,
    onHide: () -> Unit
) {

    var mediaList by remember {
        mutableStateOf(
            emptyList<VaultMediaItem>()
        )
    }

    var selectedAlbum by remember {
        mutableStateOf(ALL_ALBUMS_KEY)
    }

    var albumDropdownOpen by remember {
        mutableStateOf(false)
    }

    var showHideDialog by remember {
        mutableStateOf(false)
    }

    // =========================================================
    // LOAD GALLERY MEDIA
    // =========================================================

    LaunchedEffect(
        isVideo,
        hiddenMedia
    ) {

        mediaList =
            loadVaultMedia(
                context = context,
                isVideo = isVideo
            ).filterNot { media ->

                hiddenMedia.any {

                    it.name == media.name &&
                            it.isVideo ==
                            media.isVideo &&
                            it.isVaultFile
                }
            }
    }

    // =========================================================
    // ALBUMS
    // =========================================================

    val albums =
        remember(mediaList) {

            listOf(ALL_ALBUMS_KEY) +
                    mediaList
                        .map {
                            it.bucketName
                        }
                        .filter {
                            it.isNotBlank()
                        }
                        .distinct()
                        .sorted()
        }

    // =========================================================
    // FILTER
    // =========================================================

    val filteredMedia =
        remember(
            mediaList,
            selectedAlbum
        ) {

            if (selectedAlbum == ALL_ALBUMS_KEY) {

                mediaList

            } else {

                mediaList.filter {

                    it.bucketName ==
                            selectedAlbum
                }
            }
        }

    // =========================================================
    // ALL SELECTED
    // =========================================================

    val allSelected =
        filteredMedia.isNotEmpty() &&
                filteredMedia.all {

                    selectedMedia.contains(
                        it.uri
                    )
                }

    // =========================================================
    // SCREEN
    // =========================================================

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            // =================================================
            // TOP BAR
            // =================================================

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .padding(
                        start = 15.dp,
                        end = 15.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Image(
                    painter = painterResource(
                        id = R.drawable.backarrow
                    ),
                    contentDescription = stringResource(
                        R.string.back
                    ),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            onBack()
                        }
                )

                Spacer(
                    modifier = Modifier.width(25.dp)
                )

                Row(
                    modifier = Modifier.clickable {

                        albumDropdownOpen =
                            !albumDropdownOpen

                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text =
                            if (
                                selectedAlbum ==
                                ALL_ALBUMS_KEY
                            ) {

                                stringResource(
                                    R.string.all
                                )

                            } else {

                                selectedAlbum
                            },
                        color = Color(0xFF333333),
                        fontSize = 16.sp
                    )

                    Spacer(
                        modifier = Modifier.width(3.dp)
                    )

                    Image(
                        painter = painterResource(
                            id = R.drawable.dropdown
                        ),
                        contentDescription = stringResource(
                            R.string.albums
                        ),
                        modifier = Modifier.size(14.dp)
                    )
                }

                Spacer(
                    modifier = Modifier.weight(1f)
                )
            }

            // =================================================
            // ALBUM DROPDOWN
            // =================================================

            if (albumDropdownOpen) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 55.dp,
                            end = 55.dp
                        )
                        .background(
                            Color.White,
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0xFFE0E0E0),
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {

                    albums.forEach { album ->

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {

                                    selectedAlbum =
                                        album

                                    albumDropdownOpen =
                                        false
                                }
                                .padding(
                                    horizontal = 14.dp,
                                    vertical = 11.dp
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                text =
                                    if (
                                        album ==
                                        ALL_ALBUMS_KEY
                                    ) {

                                        stringResource(
                                            R.string.all
                                        )

                                    } else {

                                        album
                                    },
                                color =
                                    if (
                                        album ==
                                        selectedAlbum
                                    ) {

                                        Color(0xFF0396FF)

                                    } else {

                                        Color(0xFF333333)
                                    },
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // =================================================
            // GRID
            // =================================================

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(
                    start = 8.dp,
                    end = 8.dp,
                    top = 8.dp,
                    bottom = 8.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {

                items(
                    items = filteredMedia,
                    key = { media ->
                        media.uri.toString()
                    }
                ) { media ->

                    VaultGalleryItem(
                        media = media,
                        selected =
                            selectedMedia.contains(
                                media.uri
                            ),
                        onClick = {

                            val newSet =
                                selectedMedia
                                    .toMutableSet()

                            if (
                                newSet.contains(
                                    media.uri
                                )
                            ) {

                                newSet.remove(
                                    media.uri
                                )

                            } else {

                                newSet.add(
                                    media.uri
                                )
                            }

                            onSelectionChange(
                                newSet
                            )
                        }
                    )
                }
            }

            // =================================================
            // BOTTOM BAR
            // =================================================

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .background(Color.White)
                    .padding(
                        horizontal = 45.dp
                    ),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                // =================================================
                // SELECT ALL
                // =================================================

                Image(
                    painter = painterResource(
                        id = R.drawable.allimage
                    ),
                    contentDescription = stringResource(
                        R.string.select_all
                    ),
                    colorFilter = ColorFilter.tint(

                        if (allSelected) {

                            Color(0xFF0396FF)

                        } else {

                            Color(0xFF818181)
                        }
                    ),
                    modifier = Modifier
                        .size(28.dp)
                        .clickable {

                            val newSet =
                                selectedMedia
                                    .toMutableSet()

                            if (allSelected) {

                                filteredMedia.forEach {

                                    newSet.remove(
                                        it.uri
                                    )
                                }

                            } else {

                                filteredMedia.forEach {

                                    newSet.add(
                                        it.uri
                                    )
                                }
                            }

                            onSelectionChange(
                                newSet
                            )
                        }
                )

                // =================================================
                // HIDE
                // =================================================

                Image(
                    painter = painterResource(
                        id = R.drawable.locked
                    ),
                    contentDescription = stringResource(
                        R.string.hide
                    ),
                    colorFilter = ColorFilter.tint(

                        if (
                            selectedMedia.isNotEmpty()
                        ) {

                            Color(0xFF0396FF)

                        } else {

                            Color(0xFF818181)
                        }
                    ),
                    modifier = Modifier
                        .size(28.dp)
                        .clickable {

                            if (
                                selectedMedia.isNotEmpty()
                            ) {

                                showHideDialog =
                                    true
                            }
                        }
                )
            }
        }

        // =====================================================
        // HIDE CONFIRMATION
        // =====================================================

        if (showHideDialog) {

            AlertDialog(

                onDismissRequest = {

                    showHideDialog = false
                },

                title = {

                    Text(
                        text =
                            if (isVideo) {

                                stringResource(
                                    R.string.hide_video
                                )

                            } else {

                                stringResource(
                                    R.string.hide_image
                                )
                            }
                    )
                },

                text = {

                    Text(
                        text =
                            if (isVideo) {

                                stringResource(
                                    R.string.hide_videos_question
                                )

                            } else {

                                stringResource(
                                    R.string.hide_images_question
                                )
                            }
                    )
                },

                confirmButton = {

                    TextButton(

                        onClick = {

                            showHideDialog = false
                            onHide()
                        }
                    ) {

                        Text(
                            text = stringResource(
                                R.string.hide
                            ),
                            color = Color(0xFF0396FF)
                        )
                    }
                },

                dismissButton = {

                    TextButton(

                        onClick = {

                            showHideDialog = false
                        }
                    ) {

                        Text(
                            text = stringResource(
                                R.string.cancel
                            ),
                            color = Color(0xFF818181)
                        )
                    }
                }
            )
        }
    }
}

// =============================================================
// GALLERY ITEM
// =============================================================

@Composable
private fun VaultGalleryItem(
    media: VaultMediaItem,
    selected: Boolean,
    onClick: () -> Unit
) {

    val context = LocalContext.current

    var bitmap by remember(
        media.uri
    ) {
        mutableStateOf<Bitmap?>(null)
    }

    LaunchedEffect(
        media.uri
    ) {

        bitmap =
            loadVaultThumbnail(
                context = context,
                uri = media.uri,
                isVideo = media.isVideo
            )
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(1.dp))
            .clickable {
                onClick()
            }
    ) {

        bitmap?.let { loadedBitmap ->

            Image(
                bitmap = loadedBitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(5.dp)
                .size(16.dp)
                .clip(CircleShape)
                .background(

                    if (selected) {

                        Color(0xFF0396FF)

                    } else {

                        Color.Transparent
                    }
                )
                .border(
                    width = 1.dp,
                    color =
                        if (selected) {

                            Color(0xFF0396FF)

                        } else {

                            Color(0xFF818181)
                        },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {

            if (selected) {

                Text(
                    text = "✓",
                    color = Color.White,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// =============================================================
// LOAD MEDIA
// =============================================================

private suspend fun loadVaultMedia(
    context: Context,
    isVideo: Boolean
): List<VaultMediaItem> {

    return withContext(Dispatchers.IO) {

        val result =
            mutableListOf<VaultMediaItem>()

        val collection =
            if (isVideo) {

                MediaStore.Video.Media
                    .EXTERNAL_CONTENT_URI

            } else {

                MediaStore.Images.Media
                    .EXTERNAL_CONTENT_URI
            }

        val projection =
            arrayOf(
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.BUCKET_DISPLAY_NAME
            )

        val sortOrder =
            "${MediaStore.MediaColumns.DATE_ADDED} DESC"

        try {

            context.contentResolver.query(
                collection,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->

                val idIndex =
                    cursor.getColumnIndexOrThrow(
                        MediaStore.MediaColumns._ID
                    )

                val nameIndex =
                    cursor.getColumnIndexOrThrow(
                        MediaStore.MediaColumns.DISPLAY_NAME
                    )

                val bucketIndex =
                    cursor.getColumnIndex(
                        MediaStore.MediaColumns.BUCKET_DISPLAY_NAME
                    )

                while (cursor.moveToNext()) {

                    val id =
                        cursor.getLong(
                            idIndex
                        )

                    val name =
                        cursor.getString(
                            nameIndex
                        ) ?: ""

                    val bucket =
                        if (bucketIndex >= 0) {

                            cursor.getString(
                                bucketIndex
                            ) ?: "Unknown"

                        } else {

                            "Unknown"
                        }

                    val uri =
                        Uri.withAppendedPath(
                            collection,
                            id.toString()
                        )

                    result.add(
                        VaultMediaItem(
                            uri = uri,
                            name = name,
                            bucketName = bucket,
                            isVideo = isVideo
                        )
                    )
                }
            }

        } catch (e: Exception) {

            e.printStackTrace()
        }

        result
    }
}

// =============================================================
// COPY TO PRIVATE VAULT
// =============================================================

private fun copyMediaToVault(
    context: Context,
    media: VaultMediaItem
): VaultMediaItem? {

    return try {

        val vaultDir =
            File(
                context.filesDir,
                if (media.isVideo) {
                    "vault_videos"
                } else {
                    "vault_photos"
                }
            )

        if (!vaultDir.exists()) {
            vaultDir.mkdirs()
        }

        val extension =
            media.name.substringAfterLast(
                ".",
                if (media.isVideo) {
                    "mp4"
                } else {
                    "jpg"
                }
            )

        val safeName =
            "${System.currentTimeMillis()}_" +
                    "${media.uri.lastPathSegment ?: media.name.hashCode()}." +
                    extension

        val vaultFile =
            File(
                vaultDir,
                safeName
            )

        context.contentResolver
            .openInputStream(media.uri)
            ?.use { input ->

                vaultFile.outputStream()
                    .use { output ->

                        input.copyTo(output)
                    }

            }
            ?: return null

        VaultMediaItem(

            // IMPORTANT:
            // Original MediaStore URI is kept here.
            // This lets the permission callback know
            // which original item should be removed.
            uri = media.uri,

            name = media.name,

            bucketName = media.bucketName,

            isVideo = media.isVideo,

            isVaultFile = true,

            vaultFilePath =
                vaultFile.absolutePath
        )

    } catch (e: Exception) {

        e.printStackTrace()

        null
    }
}

// =============================================================
// SAVE HIDDEN MEDIA
// =============================================================

private fun saveHiddenMediaMetadata(
    context: Context,
    mediaList: List<VaultMediaItem>
) {

    val prefs =
        context.getSharedPreferences(
            "vault_hidden_media",
            Context.MODE_PRIVATE
        )

    val editor =
        prefs.edit()

    editor.clear()

    mediaList.forEachIndexed { index, media ->

        editor.putString(
            "uri_$index",
            media.uri.toString()
        )

        editor.putString(
            "name_$index",
            media.name
        )

        editor.putString(
            "bucket_$index",
            media.bucketName
        )

        editor.putBoolean(
            "video_$index",
            media.isVideo
        )

        editor.putBoolean(
            "vault_$index",
            media.isVaultFile
        )

        editor.putString(
            "path_$index",
            media.vaultFilePath
        )
    }

    editor.putInt(
        "count",
        mediaList.size
    )

    editor.apply()
}

// =============================================================
// LOAD HIDDEN MEDIA
// =============================================================

private fun loadHiddenMediaMetadata(
    context: Context
): List<VaultMediaItem> {

    val prefs =
        context.getSharedPreferences(
            "vault_hidden_media",
            Context.MODE_PRIVATE
        )

    val count =
        prefs.getInt(
            "count",
            0
        )

    val result =
        mutableListOf<VaultMediaItem>()

    for (index in 0 until count) {

        val uriString =
            prefs.getString(
                "uri_$index",
                null
            )

        if (uriString != null) {

            val isVideo =
                prefs.getBoolean(
                    "video_$index",
                    false
                )

            val isVault =
                prefs.getBoolean(
                    "vault_$index",
                    true
                )

            val path =
                prefs.getString(
                    "path_$index",
                    null
                )

            if (
                isVault &&
                path != null &&
                !File(path).exists()
            ) {

                continue
            }

            result.add(
                VaultMediaItem(
                    uri = Uri.parse(uriString),
                    name =
                        prefs.getString(
                            "name_$index",
                            "Media"
                        ) ?: "Media",
                    bucketName =
                        prefs.getString(
                            "bucket_$index",
                            "Pictures"
                        ) ?: "Pictures",
                    isVideo = isVideo,
                    isVaultFile = isVault,
                    vaultFilePath = path
                )
            )
        }
    }

    return result
}

// =============================================================
// RESTORE MEDIA TO GALLERY
// =============================================================

private fun restoreMediaToGallery(
    context: Context,
    media: VaultMediaItem
): Boolean {

    return try {

        val file =
            File(
                media.vaultFilePath
                    ?: return false
            )

        if (!file.exists()) {
            return false
        }

        val collection =
            if (media.isVideo) {

                MediaStore.Video.Media
                    .EXTERNAL_CONTENT_URI

            } else {

                MediaStore.Images.Media
                    .EXTERNAL_CONTENT_URI
            }

        val mimeType =
            if (media.isVideo) {

                getVideoMimeType(
                    media.name
                )

            } else {

                getImageMimeType(
                    media.name
                )
            }

        val originalAlbum =
            media.bucketName
                .trim()
                .ifBlank {

                    if (media.isVideo) {
                        "Movies"
                    } else {
                        "Pictures"
                    }
                }

        val safeAlbum =
            originalAlbum
                .replace("/", "_")
                .replace("\\", "_")
                .trim()
                .ifBlank {

                    if (media.isVideo) {
                        "Movies"
                    } else {
                        "Pictures"
                    }
                }

        val relativePath =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                if (media.isVideo) {

                    "Movies/$safeAlbum"

                } else {

                    "Pictures/$safeAlbum"
                }

            } else {

                null
            }

        val values =
            ContentValues().apply {

                put(
                    MediaStore.MediaColumns.DISPLAY_NAME,
                    media.name
                )

                put(
                    MediaStore.MediaColumns.MIME_TYPE,
                    mimeType
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        relativePath
                    )

                    put(
                        MediaStore.MediaColumns.IS_PENDING,
                        1
                    )
                }
            }

        val newUri =
            context.contentResolver.insert(
                collection,
                values
            ) ?: return false

        try {

            context.contentResolver
                .openOutputStream(newUri)
                ?.use { output ->

                    file.inputStream()
                        .use { input ->

                            input.copyTo(output)
                        }
                }
                ?: throw Exception(
                    "Unable to open gallery output stream"
                )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                val completeValues =
                    ContentValues().apply {

                        put(
                            MediaStore.MediaColumns.IS_PENDING,
                            0
                        )
                    }

                context.contentResolver.update(
                    newUri,
                    completeValues,
                    null,
                    null
                )
            }

            if (file.exists()) {
                file.delete()
            }

            true

        } catch (e: Exception) {

            e.printStackTrace()

            try {

                context.contentResolver.delete(
                    newUri,
                    null,
                    null
                )

            } catch (deleteError: Exception) {

                deleteError.printStackTrace()
            }

            false
        }

    } catch (e: Exception) {

        e.printStackTrace()

        false
    }
}

// =============================================================
// IMAGE MIME
// =============================================================

private fun getImageMimeType(
    fileName: String
): String {

    return when (
        fileName
            .substringAfterLast(
                ".",
                ""
            )
            .lowercase()
    ) {

        "png" ->
            "image/png"

        "webp" ->
            "image/webp"

        "gif" ->
            "image/gif"

        "heic" ->
            "image/heic"

        "heif" ->
            "image/heif"

        else ->
            "image/jpeg"
    }
}

// =============================================================
// VIDEO MIME
// =============================================================

private fun getVideoMimeType(
    fileName: String
): String {

    return when (
        fileName
            .substringAfterLast(
                ".",
                ""
            )
            .lowercase()
    ) {

        "mkv" ->
            "video/x-matroska"

        "webm" ->
            "video/webm"

        "3gp" ->
            "video/3gpp"

        "avi" ->
            "video/x-msvideo"

        else ->
            "video/mp4"
    }
}

// =============================================================
// THUMBNAIL
// =============================================================

private suspend fun loadVaultThumbnail(
    context: Context,
    uri: Uri,
    isVideo: Boolean
): Bitmap? {

    return withContext(Dispatchers.IO) {

        try {

            if (isVideo) {

                val retriever =
                    MediaMetadataRetriever()

                try {

                    if (uri.scheme == "file") {

                        retriever.setDataSource(
                            uri.path
                        )

                    } else if (uri.scheme == "vault") {

                        retriever.setDataSource(
                            uri.schemeSpecificPart
                        )

                    } else {

                        retriever.setDataSource(
                            context,
                            uri
                        )
                    }

                    retriever.getFrameAtTime(
                        0L,
                        MediaMetadataRetriever
                            .OPTION_CLOSEST_SYNC
                    )

                } finally {

                    retriever.release()
                }

            } else {

                if (uri.scheme == "file") {

                    android.graphics.BitmapFactory
                        .decodeFile(
                            uri.path
                        )

                } else if (uri.scheme == "vault") {

                    android.graphics.BitmapFactory
                        .decodeFile(
                            uri.schemeSpecificPart
                        )

                } else if (
                    Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.Q
                ) {

                    context.contentResolver
                        .loadThumbnail(
                            uri,
                            android.util.Size(
                                300,
                                300
                            ),
                            null
                        )

                } else {

                    context.contentResolver
                        .openInputStream(uri)
                        ?.use { inputStream ->

                            android.graphics.BitmapFactory
                                .decodeStream(
                                    inputStream
                                )
                        }
                }
            }

        } catch (e: Exception) {

            e.printStackTrace()

            null
        }
    }
}