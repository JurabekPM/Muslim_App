package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components

import androidx.compose.ui.res.stringResource
import androidx.annotation.DrawableRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import uz.turgunboyevjurabek.muslimapp.R
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.daily.Bugungi
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.PrayerInterval
import uz.turgunboyevjurabek.muslimapp.feature.presentation.theme.ScheduleCardContent
import uz.turgunboyevjurabek.muslimapp.feature.presentation.theme.ScheduleCardColor
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.animation.AnimatedValueText
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private val featuredCardShape = RoundedCornerShape(20.dp)
private val progressGold = Color(0xFFF2D27C)

private data class ProgressSparkle(
    val position: Float,
    val height: Float,
    val radius: Float,
    val phase: Float,
)

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
internal fun FeaturedPrayerCard(
    today: Bugungi,
    interval: PrayerInterval?,
    nextName: String?,
    modifier: Modifier = Modifier,
) {
    val artwork = remember(today.featuredPrayer) { artworkFor(today.featuredPrayer) }
    val cardColor = Color(0xFF142331)
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.3f
    val shadowColor = if (dark) Color(0xFF597CA4) else ScheduleCardColor
    val footerScrim = remember(cardColor) {
        Brush.verticalGradient(
            0.52f to Color.Transparent,
            1f to cardColor.copy(alpha = 0.88f),
        )
    }
    val scrim = remember(cardColor) {
        Brush.horizontalGradient(
            0f to cardColor,
            0.35f to cardColor,
            0.58f to cardColor.copy(alpha = 0.86f),
            1f to cardColor.copy(alpha = 0.12f),
        )
    }

    Box(
        modifier = modifier.fillMaxWidth().heightIn(min = 150.dp)
            .dropShadow(
                shape = featuredCardShape,
                shadow = Shadow(
                    radius = 10.dp,
                    color = shadowColor.copy(alpha = if (dark) 0.46f else 0.28f),
                    offset = DpOffset(0.dp, 5.dp),
                ),
            )
            .clip(featuredCardShape)
            .background(cardColor),
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
        if (interval != null && nextName != null) {
            Box(Modifier.matchParentSize().background(footerScrim))
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            AnimatedValueText(
                text = today.featuredPrayer?.takeIf { it.isNotBlank() }?.let { prayerName(it) }
                    ?: stringResource(R.string.prayer_time),
                modifier = Modifier.fillMaxWidth(0.58f),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = ScheduleCardContent,
            )
            today.featuredTime?.takeIf { it.isNotBlank() }?.let { time ->
                AnimatedValueText(
                    time,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = progressGold,
                )
            }
            if (interval != null && nextName != null) {
                Spacer(Modifier.height(5.dp))
                AnimatedValueText(
                    interval.remainingCaption(nextName),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFFE4A5),
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        if (interval != null && nextName != null) {
            PrayerCardProgress(interval, Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable
private fun PrayerCardProgress(interval: PrayerInterval, modifier: Modifier = Modifier) {
    val fraction = animateFloatAsState(interval.fraction, tween(650), label = "Prayer progress")
    val duration = 950 + interval.minutesLeft.coerceIn(1L, 180L).toInt() * 15
    val motion = rememberInfiniteTransition(label = "Prayer progress sparkles").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(duration, easing = LinearEasing)),
        label = "Sparkle phase",
    )
    Box(
        modifier.fillMaxWidth().height(12.dp).clipToBounds()
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(interval.fraction, 0f..1f) }
            .drawWithCache {
                val trackColor = Color(0xFF46505C)
                val fill = Brush.verticalGradient(
                    listOf(progressGold, Color(0xFFDEAA4B), Color(0xFFBA812F)),
                    endY = size.height,
                )
                val lightColor = Color(0xFFFFF9E8)
                val glowRadius = size.height * 0.6f
                val glow = Brush.radialGradient(
                    listOf(lightColor, progressGold.copy(alpha = 0.72f), Color.Transparent),
                    center = Offset.Zero,
                    radius = glowRadius,
                )
                val random = Random(23)
                val sparkles = List(26) { index ->
                    ProgressSparkle(
                        position = (index + random.nextFloat()) / 26f,
                        height = 0.2f + random.nextFloat() * 0.6f,
                        radius = (0.45f + random.nextFloat() * 0.65f).dp.toPx(),
                        phase = random.nextFloat(),
                    )
                }
                val drift = 17.dp.toPx()
                onDrawBehind {
                    val phase = motion.value
                    drawRect(trackColor)
                    val filled = size.width * fraction.value
                    val rtl = layoutDirection == LayoutDirection.Rtl
                    val tipRadius = minOf(size.height / 2f, filled)
                    val left = if (rtl) size.width - filled else -tipRadius
                    val fillSize = Size(filled + tipRadius, size.height)
                    if (filled > 0f) {
                        drawRoundRect(fill, Offset(left, 0f), fillSize, CornerRadius(tipRadius))
                    }
                    for (sparkle in sparkles) {
                        val age = (phase + sparkle.phase) % 1f
                        val distance = (sparkle.position * size.width + age * drift) % size.width
                        val twinkle = sin(PI * age).toFloat()
                        val insideFill = distance < filled - tipRadius
                        val x = if (rtl) size.width - distance else distance
                        drawCircle(
                            color = if (insideFill) lightColor else progressGold,
                            radius = sparkle.radius,
                            center = Offset(x, size.height * sparkle.height),
                            alpha = twinkle * if (insideFill) 0.85f else 0.42f,
                        )
                    }
                    if (filled >= size.height) {
                        val tip = if (rtl) left + tipRadius else filled - tipRadius
                        val pulse = 0.85f + 0.15f * sin(2 * PI * phase).toFloat()
                        translate(tip, size.height / 2f) {
                            drawRoundRect(
                                brush = glow,
                                topLeft = Offset(left - tip, -size.height / 2f),
                                size = fillSize,
                                cornerRadius = CornerRadius(tipRadius),
                                alpha = pulse,
                            )
                        }
                    }
                }
            },
    )
}
