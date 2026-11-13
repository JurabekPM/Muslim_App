package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.screens

import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.animation.AnimatedValueText
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.preferences.SelectRegionViewModel
import androidx.compose.animation.AnimatedContent
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.animation.textTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import uz.turgunboyevjurabek.muslimapp.R
import uz.turgunboyevjurabek.muslimapp.core.utils.Status
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.daily.Bugungi
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.PrayerTimesError
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.TimeListCard
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.rememberPrayerTimes
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.rememberPrayerClock
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.ScheduleRefresh
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.SavedScheduleLabel
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.savedLabel
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.scheduleDate
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.PrayerScheduleViewModel
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.ScheduleDay
import java.time.LocalTime
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.shimmers.MainCardShimmer
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.shimmers.TimeListCardShimmer
import uz.turgunboyevjurabek.muslimapp.feature.presentation.theme.ScheduleCardColor
import uz.turgunboyevjurabek.muslimapp.feature.presentation.theme.ScheduleCardContent

private val navyHeroFilter = ColorFilter.colorMatrix(
    ColorMatrix(
        floatArrayOf(
            0.92f, 0.04f, 0f, 0f, 0f,
            0.08f, 0.69f, 0.15f, 0f, 0f,
            0f, 0.37f, 0.65f, 0f, 3f,
            0f, 0f, 0f, 1f, 0f,
        )
    )
)

@Composable
fun MainScreen(
    selectRegionViewModel: SelectRegionViewModel,
    schedule: PrayerScheduleViewModel,
    bottomPadding: Dp,
    topPadding: Dp,
) {
    val selection by selectRegionViewModel.selection.collectAsStateWithLifecycle()
    val region = selection?.region
    val now by rememberPrayerClock()
    val connection by schedule.connection.collectAsStateWithLifecycle()
    val key = region?.let { ScheduleDay(it, now.toLocalDate()) }
    val load = rememberPrayerTimes(schedule.daily, key, connection)
    val requestedPeriod = now.toLocalDate().toString()
    val savedLabel = load.savedLabel(connection, requestedPeriod)
    val refresh: () -> Unit = { key?.let { schedule.daily.load(it, force = true) } }

    ScheduleRefresh(load, refresh, topPadding, bottomPadding) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = topPadding + 12.dp, bottom = 12.dp + bottomPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AnimatedContent(
                targetState = load.result to load.snapshot?.period,
                contentKey = { it.first.status },
                transitionSpec = { textTransition() },
                label = "Daily prayer schedule",
            ) { (current, period) ->
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    when (current.status) {
                        Status.LOADING -> {
                            MainCardShimmer()
                            TimeListCardShimmer()
                        }
                        Status.ERROR -> PrayerTimesError(current.message.orEmpty(), refresh)
                        Status.SUCCESS -> current.data?.let { today ->
                            val currentDay = period == requestedPeriod
                            val featured = remember(today, now, currentDay) {
                                if (currentDay) today.withFeaturedPrayerAt(now.toLocalTime()) else today
                            }
                            Column {
                                FeaturedPrayerCard(featured)
                                SavedScheduleLabel(savedLabel)
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                AnimatedValueText(
                                    if (currentDay) "Bugungi jadval" else "Namoz jadvali · ${scheduleDate(today.date)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                val rows = if (currentDay) featured else today.copy(featuredTime = null)
                                TimeListCard(todayData = rows)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun Bugungi.withFeaturedPrayerAt(now: LocalTime): Bugungi {
    val schedule = times ?: return this
    val slots = listOf(
        "Bomdod" to schedule.tongSaharlik,
        "Quyosh" to schedule.quyosh,
        "Peshin" to schedule.peshin,
        "Asr" to schedule.asr,
        "Shom" to schedule.shomIftor,
        "Xufton" to schedule.hufton,
    ).mapNotNull { (name, time) -> time?.let { name to LocalTime.parse(it) } }
    val current = slots.lastOrNull { (_, time) -> time <= now }
    val (name, time) = current ?: slots.firstOrNull() ?: return this
    return copy(featuredPrayer = name, featuredTime = time.toString(), featuredIsCurrent = current != null)
}

@Composable
private fun FeaturedPrayerCard(today: Bugungi) {
    val cardColor = ScheduleCardColor
    val cardContent = ScheduleCardContent
    Box(
        modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp)
            .clip(RoundedCornerShape(20.dp)).background(cardColor),
        contentAlignment = Alignment.CenterStart,
    ) {
        Image(
            painter = painterResource(R.drawable.img_4),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
            alignment = Alignment.BottomCenter,
            colorFilter = navyHeroFilter,
        )
        Box(
            Modifier.matchParentSize().background(
                Brush.horizontalGradient(
                    0f to cardColor,
                    0.43f to cardColor,
                    0.76f to cardColor.copy(alpha = 0.76f),
                    1f to cardColor.copy(alpha = 0.26f),
                )
            )
        )
        Column(
            modifier = Modifier.fillMaxWidth(0.74f).padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            AnimatedValueText(
                text = today.featuredPrayer?.takeIf { it.isNotBlank() } ?: "Namoz vaqti",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = cardContent,
            )
            today.featuredTime?.takeIf { it.isNotBlank() }?.let { time ->
                AnimatedValueText(time, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
            AnimatedValueText(
                text = listOfNotNull(today.region, today.date).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = cardContent,
            )
        }
    }
}
