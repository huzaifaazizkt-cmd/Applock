package com.example.applock.Design.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.applock.AppLanguageManager
import com.example.applock.R
import com.example.applock.data.DataStoreManager
import kotlinx.coroutines.launch


data class LanguageItem(
    val name: String,
    val flagRes: Int,
    val code: String
)


@Composable
fun LanguagesScreen(
    onBackClick: (() -> Unit)? = null,
    onLanguageSelected: (() -> Unit)? = null
) {

    val context = LocalContext.current

    val dataStoreManager =
        remember {
            DataStoreManager(context)
        }

    val scope =
        rememberCoroutineScope()


    // ==========================================
    // LANGUAGES
    // ==========================================

    val languages = listOf(

        LanguageItem(
            name = "English",
            flagRes = R.drawable.english,
            code = "en"
        ),

        LanguageItem(
            name = "Portagual",
            flagRes = R.drawable.portagual,
            code = "pt"
        ),

        LanguageItem(
            name = "France",
            flagRes = R.drawable.france1,
            code = "fr"
        ),

        LanguageItem(
            name = "Spain",
            flagRes = R.drawable.spain1,
            code = "es"
        ),

        LanguageItem(
            name = "Turkey",
            flagRes = R.drawable.turkey,
            code = "tr"
        ),

        LanguageItem(
            name = "Japan",
            flagRes = R.drawable.japan,
            code = "ja"
        ),

        LanguageItem(
            name = "Korean",
            flagRes = R.drawable.korean,
            code = "ko"
        ),

        LanguageItem(
            name = "Indonesia",
            flagRes = R.drawable.indonesia,
            code = "id"
        ),

        LanguageItem(
            name = "India",
            flagRes = R.drawable.india,
            code = "hi"
        ),

        LanguageItem(
            name = "Norway",
            flagRes = R.drawable.norway,
            code = "se"
        )
    )


    // ==========================================
    // SAVED LANGUAGE
    // ==========================================

    val savedLanguageCode by
    dataStoreManager
        .getLanguage()
        .collectAsState(
            initial = "en"
        )


    var selectedLanguageCode by
    remember {

        mutableStateOf(
            savedLanguageCode
        )
    }


    LaunchedEffect(
        savedLanguageCode
    ) {

        selectedLanguageCode =
            savedLanguageCode
    }


    // ==========================================
    // MAIN UI
    // ==========================================

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color(0xFFF5F5F5)
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Color.White
                )
        ) {


            // ==================================
            // TOP BAR
            // ==================================

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
            ) {


                if (onBackClick != null) {

                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Filled.ArrowBack,

                        contentDescription =
                            stringResource(
                                R.string.back
                            ),

                        tint =
                            Color(0xFF444444),

                        modifier = Modifier
                            .padding(
                                start = 18.dp,
                                top = 39.dp
                            )
                            .size(25.dp)
                            .clickable (
                                indication = null,
                                interactionSource = remember {
                                    MutableInteractionSource()
                                }
                                    ){
                                onBackClick()
                            }


                    )
                }


                // ==================================
                // TITLE
                // ==================================

                Text(
                    text =
                        stringResource(
                            R.string.languages
                        ),

                    modifier =
                        Modifier.align(
                            Alignment.Center
                        ),

                    color =
                        Color(0xFF888888),

                    fontSize =
                       24.sp,

                    fontWeight =
                        FontWeight.SemiBold
                )
            }


            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )


            // ==================================
            // LANGUAGE LIST
            // ==================================

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 20.dp,
                        end = 15.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                languages.forEach { language ->

                    LanguageRow(

                        language = language,

                        selected =
                            selectedLanguageCode ==
                                    language.code,

                        onClick = {

                            selectedLanguageCode =
                                language.code
                        }
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.weight(1f)
            )


            // ==================================
            // SELECT BUTTON
            // ==================================

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 48.dp,
                        end = 48.dp,
                        bottom = 62.dp
                    )
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .clip(
                            RoundedCornerShape(20.dp)
                        )
                        .background(
                            color =
                                Color(0xFF2196F3),

                            shape =
                                RoundedCornerShape(
                                    20.dp
                                )
                        )
                        .clickable {

                            val selectedItem =
                                languages.firstOrNull {

                                    it.code ==
                                            selectedLanguageCode
                                }


                            if (
                                selectedItem != null
                            ) {

                                scope.launch {

                                    // Save language
                                    dataStoreManager
                                        .saveLanguage(
                                            selectedItem.code
                                        )


                                    // Apply language
                                    AppLanguageManager
                                        .setLanguage(
                                            selectedItem.code
                                        )


                                    // Continue navigation
                                    onLanguageSelected
                                        ?.invoke()
                                }
                            }
                        },

                    contentAlignment =
                        Alignment.Center
                ) {

                    Text(
                        text =
                            stringResource(
                                R.string.select
                            ),

                        color =
                            Color.White,

                        fontSize =
                            12.sp,

                        fontWeight =
                            FontWeight.Medium
                    )
                }
            }
        }
    }
}


@Composable
private fun LanguageRow(
    language: LanguageItem,
    selected: Boolean,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clickable {

                onClick()
            },

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Image(
            painter =
                painterResource(
                    id = language.flagRes
                ),

            contentDescription =
                language.name,

            modifier =
                Modifier.size(
                    width = 30.dp,
                    height = 30.dp
                ),

            contentScale =
                ContentScale.Fit
        )


        Spacer(
            modifier =
                Modifier.width(20.dp)
        )


        Text(
            text =
                language.name,

            modifier =
                Modifier.weight(1f),

            color =
                if (selected) {

                    Color(0xFF0396FF)

                } else {

                    Color(0xFF777777)
                },

            fontSize =
                20.sp
        )


        RadioButton(
            selected =
                selected,

            onClick = {

                onClick()
            },

            modifier =
                Modifier.size(20.dp),

            colors =
                RadioButtonDefaults.colors(

                    selectedColor =
                        Color(0xFF0396FF),

                    unselectedColor =
                        Color(0xFF777777)
                )
        )
    }
}