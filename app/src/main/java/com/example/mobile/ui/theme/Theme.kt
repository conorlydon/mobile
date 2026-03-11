package com.example.mobile.ui.theme

import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = Green80,
    secondary = GreenGrey80,
    tertiary = Mint80
)

private val LightColorScheme = lightColorScheme(
    primary = Green40,
    secondary = GreenGrey40,
    tertiary = Mint40,
    background = GreenBackgroundBottom,
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = GreenHeading,
    onSurface = GreenHeading
)

@Immutable
data class MobileExtendedColors(
    val brandPrimary: Color,
    val brandOnPrimary: Color,
    val screenGradientTop: Color,
    val screenGradientMiddle: Color,
    val screenGradientBottom: Color,
    val heading: Color,
    val cardSurface: Color,
    val mutedText: Color,
    val youthSkill: Color,
    val seniorSkill: Color,
    val intermediateSkill: Color,
    val minorSkill: Color,
    val casualSkill: Color,
    val unknownSkill: Color,
    val locationAccent: Color,
    val dateAccent: Color,
)

@Immutable
data class MobileShapes(
    val actionButton: RoundedCornerShape,
    val badge: RoundedCornerShape,
    val iconContainer: RoundedCornerShape,
)

private val LocalExtendedColors = staticCompositionLocalOf {
    MobileExtendedColors(
        brandPrimary = Green40,
        brandOnPrimary = Color.White,
        screenGradientTop = GreenBackgroundTop,
        screenGradientMiddle = GreenBackgroundMiddle,
        screenGradientBottom = GreenBackgroundBottom,
        heading = GreenHeading,
        cardSurface = Color.White,
        mutedText = MutedText,
        youthSkill = YouthBlue,
        seniorSkill = SeniorRed,
        intermediateSkill = IntermediateOrange,
        minorSkill = MinorGreen,
        casualSkill = CasualPurple,
        unknownSkill = MutedText,
        locationAccent = OrangeAccent,
        dateAccent = PurpleAccent,
    )
}

private val LocalMobileShapes = staticCompositionLocalOf {
    MobileShapes(
        actionButton = RoundedCornerShape(12.dp),
        badge = RoundedCornerShape(20.dp),
        iconContainer = RoundedCornerShape(12.dp),
    )
}

object MobileThemeExtras {
    val colors: MobileExtendedColors
        @Composable get() = LocalExtendedColors.current

    val shapes: MobileShapes
        @Composable get() = LocalMobileShapes.current

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun topAppBarColors(): TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = colors.brandPrimary,
        titleContentColor = colors.brandOnPrimary,
        navigationIconContentColor = colors.brandOnPrimary,
        actionIconContentColor = colors.brandOnPrimary,
    )

    @Composable
    fun screenBackgroundBrush(): Brush = Brush.verticalGradient(
        colors = listOf(
            colors.screenGradientTop,
            colors.screenGradientMiddle,
            colors.screenGradientBottom,
        )
    )

    @Composable
    fun formFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = colors.brandPrimary,
        focusedLabelColor = colors.brandPrimary,
        cursorColor = colors.brandPrimary,
        errorBorderColor = MaterialTheme.colorScheme.error,
        errorLabelColor = MaterialTheme.colorScheme.error,
    )

    @Composable
    fun primaryButtonColors(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = colors.brandPrimary,
        contentColor = colors.brandOnPrimary,
    )

    @Composable
    fun textButtonColors(): ButtonColors = ButtonDefaults.textButtonColors(
        contentColor = colors.brandPrimary,
    )

    @Composable
    fun outlinedButtonColors(): ButtonColors = ButtonDefaults.outlinedButtonColors(
        contentColor = colors.brandPrimary,
    )

    @Composable
    fun surfaceCardColors(): CardColors = CardDefaults.cardColors(
        containerColor = colors.cardSurface,
    )

    @Composable
    fun skillLevelColor(skillLevel: String): Color = when {
        skillLevel.startsWith("U") -> colors.youthSkill
        skillLevel == "Senior" -> colors.seniorSkill
        skillLevel == "Intermediate" -> colors.intermediateSkill
        skillLevel == "Minor" -> colors.minorSkill
        skillLevel == "Casual" -> colors.casualSkill
        else -> colors.unknownSkill
    }
}

@Composable
fun MobileTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val extendedColors = MobileExtendedColors(
        brandPrimary = Green40,
        brandOnPrimary = Color.White,
        screenGradientTop = GreenBackgroundTop,
        screenGradientMiddle = GreenBackgroundMiddle,
        screenGradientBottom = GreenBackgroundBottom,
        heading = GreenHeading,
        cardSurface = Color.White,
        mutedText = MutedText,
        youthSkill = YouthBlue,
        seniorSkill = SeniorRed,
        intermediateSkill = IntermediateOrange,
        minorSkill = MinorGreen,
        casualSkill = CasualPurple,
        unknownSkill = MutedText,
        locationAccent = OrangeAccent,
        dateAccent = PurpleAccent,
    )
    val mobileShapes = MobileShapes(
        actionButton = RoundedCornerShape(12.dp),
        badge = RoundedCornerShape(20.dp),
        iconContainer = RoundedCornerShape(12.dp),
    )
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(
        LocalExtendedColors provides extendedColors,
        LocalMobileShapes provides mobileShapes,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
