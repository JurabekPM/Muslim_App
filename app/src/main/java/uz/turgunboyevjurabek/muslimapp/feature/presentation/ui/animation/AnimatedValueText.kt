package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.animation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit

internal const val screenFadeOutMillis = 65
internal const val screenFadeInMillis = 135

internal fun AnimatedContentTransitionScope<*>.textTransition(): ContentTransform =
    (fadeIn(tween(180)) togetherWith fadeOut(tween(120)))
        .using(SizeTransform(clip = false) { _, _ -> tween(180) })

@Composable
internal fun AnimatedValueText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    textAlign: TextAlign? = null,
    style: TextStyle = LocalTextStyle.current,
    contentAlignment: Alignment = Alignment.CenterStart,
) {
    AnimatedContent(
        targetState = text,
        modifier = modifier,
        contentAlignment = contentAlignment,
        transitionSpec = { textTransition() },
        label = "Text update",
    ) { value ->
        Text(
            text = value,
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            textAlign = textAlign,
            style = style,
        )
    }
}
