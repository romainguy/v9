package dev.romainguy.graphics.v9.demo.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

val Typography= Typography(
    h1 = TextStyle(
        fontWeight = FontWeight.W100,
        fontSize = 76.sp,
    ),
    h2 = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 44.sp,
        letterSpacing = 1.5.sp
    ),
    h3 = TextStyle(
        fontWeight = FontWeight.W400,
        fontSize = 18.sp
    ),
    h4 = TextStyle(
        fontWeight = FontWeight.W700,
        fontSize = 18.sp
    ),
    h5 = TextStyle(
        fontWeight = FontWeight.W400,
        fontSize = 18.sp
    ),
    h6 = TextStyle(
        fontWeight = FontWeight.W400,
        fontSize = 15.sp
    ),
    subtitle1 = TextStyle(
        fontWeight = FontWeight.Light,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 3.sp
    ),
    subtitle2 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        letterSpacing = 0.02.em
    ),
    body1 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = 0.02.em
    ),
    body2 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.02.em
    ),
    button = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.1.em
    ),
    caption = TextStyle(
        fontWeight = FontWeight.W500,
        fontSize = 12.sp
    ),
    overline = TextStyle(
        fontWeight = FontWeight.W500,
        fontSize = 10.sp
    )
)
