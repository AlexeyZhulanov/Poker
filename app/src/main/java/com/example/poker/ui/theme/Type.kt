package com.example.poker.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.poker.R

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)
val OswaldFontFamily = FontFamily(
    Font(R.font.oswald_semibold, FontWeight.SemiBold),
    Font(R.font.oswald_regular, FontWeight.Normal)
)
val MerriWeatherFontFamily = FontFamily(
    Font(R.font.merriweather_24pt_semicondensed_semibold, FontWeight.SemiBold)
)