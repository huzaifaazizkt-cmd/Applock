package com.example.applock.Design.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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


// =========================================================
// LANGUAGE ITEM
// =========================================================

data class LanguageItem(
    val name: String,
    val flagRes: Int,
    val code: String
)


// =========================================================
// LANGUAGES SCREEN
// =========================================================

@Composable
fun LanguagesScreen(
    onBackClick: () -> Unit
) {

    val context = LocalContext.current

    val dataStoreManager = remember {
        DataStoreManager(context)
    }

    val scope = rememberCoroutineScope()


    // =========================================================
    // LANGUAGE LIST
    // =========================================================

    val languages = listOf(

        LanguageItem(
            name = "English",
            flagRes = R.drawable.english,
            code = "en"
        ),

        LanguageItem(
            name = "Portugal",
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
            code = "se" +
                    "" +
                    ""
        )
    )


    // =========================================================
    // GET SAVED LANGUAGE
    // =========================================================

    val savedLanguageCode by dataStoreManager
        .getLanguage()
        .collectAsState(initial = "en")


    // =========================================================
    // SELECTED LANGUAGE
    //
    // savedLanguageCode se initialize hoga.
    // Is wajah se screen recreate hone par English
    // automatically select nahi hogi.
    // =========================================================

    var selectedLanguageCode by remember {
        mutableStateOf(savedLanguageCode)
    }


    // =========================================================
    // UPDATE SELECTED LANGUAGE WHEN DATASTORE CHANGES
    // =========================================================

    LaunchedEffect(savedLanguageCode) {

        selectedLanguageCode = savedLanguageCode
    }


    // =========================================================
    // UI
    // =========================================================

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
                .background(Color.White)
        ) {


            // =====================================================
            // TOP BAR
            // =====================================================

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
            ) {

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,

                    contentDescription = stringResource(
                        R.string.back
                    ),

                    tint = Color(0xFF444444),

                    modifier = Modifier
                        .padding(
                            start = 18.dp,
                            top = 39.dp
                        )
                        .size(23.dp)
                        .clickable {
                            onBackClick()
                        }
                )


                Text(
                    text = stringResource(
                        R.string.languages
                    ),

                    modifier = Modifier.align(
                        Alignment.Center
                    ),

                    color = Color(0xFF888888),

                    fontSize = 18.sp,

                    fontWeight = FontWeight.SemiBold
                )
            }


            Spacer(
                modifier = Modifier.height(1.dp)
            )


            // =====================================================
            // LANGUAGE LIST
            // =====================================================

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 20.dp,
                        end = 15.dp
                    )
            ) {

                languages.forEach { language ->

                    LanguageRow(
                        language = language,

                        // Radio button selected state
                        selected =
                            selectedLanguageCode ==
                                    language.code,

                        onClick = {

                            // Sirf UI selection change hogi.
                            // App language abhi change nahi hogi.
                            selectedLanguageCode =
                                language.code
                        }
                    )
                }
            }


            Spacer(
                modifier = Modifier.weight(1f)
            )


            // =====================================================
            // SELECT BUTTON
            // =====================================================

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
                        .height(29.dp)
                        .background(
                            color = Color(0xFF2196F3),

                            shape = RoundedCornerShape(
                                20.dp
                            )
                        )
                        .clickable {

                            // =================================================
                            // FIND SELECTED LANGUAGE
                            // =================================================

                            val selectedItem =
                                languages.firstOrNull {

                                    it.code ==
                                            selectedLanguageCode
                                }


                            if (selectedItem != null) {

                                // =================================================
                                // SAVE + CHANGE LANGUAGE
                                // =================================================
                                //
                                // Pehle DataStore mein save hoga.
                                // Uske baad AppLanguageManager chalega.
                                //
                                // Isse recreate ke waqt old "en" value
                                // read nahi hogi.
                                // =================================================

                                scope.launch {

                                    dataStoreManager
                                        .saveLanguage(
                                            selectedItem.code
                                        )


                                    // =================================================
                                    // LANGUAGE CHANGE
                                    // SAVE COMPLETE HONE KE BAAD
                                    // =================================================

                                    AppLanguageManager
                                        .setLanguage(
                                            selectedItem.code
                                        )
                                }
                            }
                        },

                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = stringResource(
                            R.string.select
                        ),

                        color = Color.White,

                        fontSize = 12.sp,

                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}


// =========================================================
// LANGUAGE ROW
// =========================================================

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

        verticalAlignment = Alignment.CenterVertically
    ) {


        // =========================================================
        // FLAG
        // =========================================================

        Image(
            painter = painterResource(
                id = language.flagRes
            ),

            contentDescription = language.name,

            modifier = Modifier.size(
                width = 29.dp,
                height = 29.dp
            ),

            contentScale = ContentScale.Fit
        )


        Spacer(
            modifier = Modifier.width(20.dp)
        )


        // =========================================================
        // LANGUAGE NAME
        // =========================================================

        Text(
            text = language.name,

            modifier = Modifier.weight(1f),

            color =
                if (selected) {
                    Color(0xFF0396FF)
                } else {
                    Color(0xFF777777)
                },

            fontSize = 12.sp
        )


        // =========================================================
        // RADIO BUTTON
        // =========================================================

        RadioButton(
            selected = selected,

            onClick = {
                onClick()
            },

            modifier = Modifier.size(24.dp)
                .clip(CircleShape



                )
            ,

            colors = RadioButtonDefaults.colors(

                selectedColor =
                    Color(0xFF0396FF),

                unselectedColor =
                    Color(0xFF777777)
            )
        )
    }
}