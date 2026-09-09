
package com.example.applock.Design.screens
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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


// =============================================================
// LANGUAGE DATA
// =============================================================

data class LanguageItem(
    val name: String,
    val flagRes: Int,
    val code: String
)


// =============================================================
// LANGUAGES SCREEN
// =============================================================

@Composable
fun LanguagesScreen(
    onBackClick: (() -> Unit)? = null,
    onLanguageSelected: (() -> Unit)? = null
) {

    val context = LocalContext.current

    val dataStoreManager = remember {
        DataStoreManager(context)
    }

    val scope = rememberCoroutineScope()


    // =========================================================
    // LANGUAGES LIST
    // =========================================================

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
        ),

        LanguageItem(
            name = "Arabic",
            flagRes = R.drawable.sudia,
            code = "ar"
        )

    )


    // =========================================================
    // GET SAVED LANGUAGE
    // =========================================================

    val savedLanguageCode by
    dataStoreManager
        .getLanguage()
        .collectAsState(
            initial = "en"
        )


    // =========================================================
    // SELECTED LANGUAGE
    // =========================================================

    var selectedLanguageCode by
    remember {

        mutableStateOf(
            savedLanguageCode
        )
    }


    // =========================================================
    // UPDATE SELECTED LANGUAGE
    // =========================================================

    LaunchedEffect(
        savedLanguageCode
    ) {

        selectedLanguageCode =
            savedLanguageCode
    }


    // =========================================================
    // SCROLL STATE
    // =========================================================

    val scrollState =
        rememberScrollState()


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
            // TOP BAR
            // =================================================

            Box(

                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
            ) {


                // =================================================
                // BACK BUTTON
                // =================================================

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

                        modifier =
                            Modifier
                                .padding(
                                    start = 18.dp,
                                    top = 39.dp
                                )
                                .size(25.dp)
                                .clickable(

                                    indication = null,

                                    interactionSource =
                                        remember {
                                            MutableInteractionSource()
                                        }

                                ) {

                                    onBackClick()
                                }
                    )
                }


                // =================================================
                // TITLE
                // =================================================

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
                        Color(0xFF333333),

                    fontSize =
                        22.sp,

                    fontWeight =
                        FontWeight.SemiBold
                )
            }


            // =================================================
            // LANGUAGE LIST
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
                        bottom = 20.dp
                    ),



                verticalArrangement =
                    Arrangement.spacedBy(
                        13.dp
                    )
            ) {


                // =================================================
                // EACH LANGUAGE CARD
                // =================================================

                languages.forEach { language ->

                    LanguageCard(

                        language =
                            language,

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


            // =================================================
            // SELECT BUTTON
            // =================================================
            Button(
                onClick = {

                    // ---------------------------------
                    // FIND SELECTED LANGUAGE
                    // ---------------------------------

                    val selectedItem =
                        languages.firstOrNull {

                            it.code ==
                                    selectedLanguageCode
                        }

                    if (selectedItem != null) {

                        scope.launch {

                            // ---------------------------------
                            // SAVE LANGUAGE
                            // ---------------------------------

                            dataStoreManager
                                .saveLanguage(
                                    selectedItem.code
                                )

                            // ---------------------------------
                            // APPLY LANGUAGE
                            // ---------------------------------

                            AppLanguageManager
                                .setLanguage(
                                    selectedItem.code
                                )

                            // ---------------------------------
                            // CONTINUE NAVIGATION
                            // ---------------------------------

                            onLanguageSelected
                                ?.invoke()
                        }
                    }
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 14.dp,
                        end = 14.dp,
                        bottom = 10.dp
                    )
                    .height(51.dp),

                shape =
                    RoundedCornerShape(14.dp),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            Color(0xFF2196F3)
                    )
            ) {

                Text(
                    text =
                        stringResource(
                            R.string.select
                        ),

                    fontSize = 17.sp,

                    fontWeight =
                        FontWeight.SemiBold,

                    color = Color.White
                )
            }
        }
    }
}


// =============================================================
// LANGUAGE CARD
// =============================================================

@Composable
private fun LanguageCard(

    language: LanguageItem,

    selected: Boolean,

    onClick: () -> Unit

) {

    Card(

        modifier = Modifier
            .fillMaxWidth()


            .shadow(

                elevation = 5.dp,

                shape =
                    RoundedCornerShape(
                        12.dp
                    ),

                clip = false,

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
            )

            .clickable {

                onClick()
            },

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
                defaultElevation = 2.dp,

                pressedElevation = 1.dp
            )
    ) {

        // =================================================
        // LANGUAGE ROW
        // =================================================

        Row(

            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .padding(
                    start = 12.dp,
                    end = 10.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {


            // =================================================
            // FLAG
            // =================================================

            Image(

                painter =
                    painterResource(
                        id = language.flagRes
                    ),

                contentDescription =
                    language.name,

                modifier =
                    Modifier.size(
                        width = 32.dp,
                        height = 32.dp
                    ),

                contentScale =
                    ContentScale.Fit
            )


            // =================================================
            // SPACE
            // =================================================

            Spacer(
                modifier =
                    Modifier.width(18.dp)
            )


            // =================================================
            // LANGUAGE NAME
            // =================================================

            Text(

                text =
                    language.name,

                modifier =
                    Modifier.weight(1f),

                color =
                    if (selected) {

                        Color(0xFF0396FF)

                    } else {

                        Color(0xFF555555)
                    },

                fontSize =
                    15.sp,

                fontWeight =
                    if (selected) {

                        FontWeight.Medium

                    } else {

                        FontWeight.Normal
                    }
            )


            // =================================================
            // RADIO BUTTON
            // =================================================

            RadioButton(

                selected =
                    selected,

                onClick =
                    onClick,

                modifier =
                    Modifier.size(30.dp),

                colors =
                    RadioButtonDefaults.colors(

                        selectedColor =
                            Color(0xFF0396FF),

                        unselectedColor =
                            Color(0xFFBDBDBD)
                    )
            )
        }
    }
}

