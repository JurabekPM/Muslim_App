package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import uz.turgunboyevjurabek.muslimapp.R
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.daily.Bugungi
import uz.turgunboyevjurabek.muslimapp.feature.presentation.theme.ScheduleCardContent
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.animation.AnimatedValueText
import java.util.Locale

private enum class PrayerArtwork(@param:DrawableRes val image: Int, val alignment: BiasAlignment) {
    Dawn(R.drawable.img_1, BiasAlignment(0f, 0.68f)),
    Sunrise(R.drawable.img_2, BiasAlignment(0f, 0.32f)),
    Noon(R.drawable.img_4, BiasAlignment(0f, 0.75f)),
    Afternoon(R.drawable.prayer_asr, BiasAlignment(0f, 0f)),
    Sunset(R.drawable.img_3, BiasAlignment(0f, 0.55f)),
    Night(R.drawable.img_5, BiasAlignment(0f, -0.12f)),
}

private fun artworkFor(prayer: String?): PrayerArtwork = when (prayer?.trim()?.lowercase(Locale.ROOT)) {
    "bomdod" -> PrayerArtwork.Dawn
    "quyosh" -> PrayerArtwork.Sunrise
    "peshin" -> PrayerArtwork.Noon
    "asr" -> PrayerArtwork.Afternoon
    "shom" -> PrayerArtwork.Sunset
    "xufton", "hufton" -> PrayerArtwork.Night
    else -> PrayerArtwork.Night
}

@Composable
internal fun FeaturedPrayerCard(today: Bugungi, modifier: Modifier = Modifier) {
    val artwork = remember(today.featuredPrayer) { artworkFor(today.featuredPrayer) }
    val cardColor = Color(0xFF142331)
    val scrim = remember(cardColor) {
        Brush.horizontalGradient(
            0f to cardColor,
            0.35f to cardColor,
            0.58f to cardColor.copy(alpha = 0.86f),
            1f to cardColor.copy(alpha = 0.12f),
        )
    }

    Box(
        modifier = modifier.fillMaxWidth().heightIn(min = 136.dp)
            .clip(RoundedCornerShape(20.dp)).background(cardColor),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(Modifier.matchParentSize(), contentAlignment = Alignment.CenterEnd) {
            Crossfade(
                targetState = artwork,
                modifier = Modifier.fillMaxWidth(0.66f).fillMaxHeight(),
                animationSpec = tween(420),
                label = "Prayer card background",
            ) { scene ->
                Image(
                    painter = painterResource(scene.image),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alignment = scene.alignment,
                )
            }
        }
        Box(Modifier.matchParentSize().background(scrim))

        Column(
            modifier = Modifier.fillMaxWidth(0.58f).padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            AnimatedValueText(
                text = today.featuredPrayer?.takeIf { it.isNotBlank() } ?: "Namoz vaqti",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = ScheduleCardContent,
            )
            today.featuredTime?.takeIf { it.isNotBlank() }?.let { time ->
                AnimatedValueText(
                    time,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF2D27C),
                )
            }
        }
    }
}
