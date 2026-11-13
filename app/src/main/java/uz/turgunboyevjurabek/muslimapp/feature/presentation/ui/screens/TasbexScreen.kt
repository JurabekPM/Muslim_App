package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.screens

import androidx.compose.ui.res.stringResource
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.animation.AnimatedValueText
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.pressScale
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.rememberPressScale
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.PressIconButton
import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.circle
import kotlinx.coroutines.delay
import uz.turgunboyevjurabek.muslimapp.R
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.animation.AnimatedCounter
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.shape.MorphPolygonShape
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.preferences.CounterViewModel
import uz.turgunboyevjurabek.muslimapp.core.utils.coloredShadow
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.shimmers.ShimmerPlaceholder

@OptIn(ExperimentalComposeUiApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TasbexScreen(viewModel: CounterViewModel) {
    val savedCount by viewModel.counter.collectAsStateWithLifecycle()
    val initialCount = savedCount
    if (initialCount == null) {
        Box(Modifier.fillMaxSize().padding(top = 50.dp), contentAlignment = Alignment.TopCenter) {
            ShimmerPlaceholder(Modifier.size(width = 88.dp, height = 56.dp), description = stringResource(R.string.loading_tasbih))
        }
        return
    }
    val context = LocalContext.current
    var count by rememberSaveable {
        mutableIntStateOf(initialCount)
    }
    ConstraintLayout(
        modifier = Modifier.fillMaxSize(),
    ) {
        val (animatedText, messageText,columnItems, shapeCount) = createRefs()

        AnimatedCounter(
            modifier = Modifier
                .constrainAs(animatedText) {
                    top.linkTo(parent.top, margin=50.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            count = count, style = TextStyle(
                fontFamily = FontFamily.Serif, fontSize = 55.sp, fontWeight = FontWeight.ExtraBold
            )
        )
        MessageText(
            viewModel = viewModel,
            modifier = Modifier
                .constrainAs(messageText){
                    top.linkTo(animatedText.bottom)
                    start.linkTo(animatedText.start)
                    end.linkTo(animatedText.end)
                }
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(columnItems) {
                    bottom.linkTo(shapeCount.top, margin = 30.dp)
                    start.linkTo(shapeCount.start)
                    end.linkTo(shapeCount.end)
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PressIconButton(onClick = {
                    Toast.makeText(context, R.string.no_dhikr_history, Toast.LENGTH_SHORT).show()
                }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_tasbex_history),
                        contentDescription = stringResource(R.string.dhikr_history),
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                PressIconButton(onClick = {
                    count=0
                    viewModel.clearCounter()
                }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_tasbex_reset),
                        contentDescription = stringResource(R.string.reset_count),
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

            }

        }
        ShapeCount(
            onClick = {
                count++
                viewModel.incrementCounter()
            },
            count = count,
            modifier = Modifier
                .constrainAs(shapeCount){
                    bottom.linkTo(parent.bottom, margin = 30.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )
    }
}

@Composable
fun MessageText(viewModel: CounterViewModel, modifier: Modifier) {
    val counterViewModel = viewModel.counter.collectAsStateWithLifecycle()

    var showText by remember { mutableStateOf(true) }

    // LaunchedEffect bilan 8 soniyadan keyin matnni yo'q qilamiz
    LaunchedEffect(Unit) {
        delay(8000L) // 8 soniya kutish
        showText = false
    }
    AnimatedVisibility(
        modifier = modifier,
        visible = showText, // showText true bo'lsa matn ko'rsatiladi
        enter = slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth }, animationSpec = tween(durationMillis = 3000)
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { fullWidth -> fullWidth }, animationSpec = tween(durationMillis = 3000)
        )
    ) {
        AnimatedValueText(
            text = if (counterViewModel.value == 0) stringResource(R.string.new_dhikr) else stringResource(R.string.continue_dhikr),
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Serif,
            modifier = modifier
                .fillMaxWidth()
                .coloredShadow(
                    MaterialTheme.colorScheme.primary,
                    borderRadius = 20.dp,
                    shadowRadius = 30.dp,
                )
        )
    }

}

@Composable
fun ShapeCount(
    onClick: () -> Unit, count: Int,
    modifier: Modifier
) {

    var alphaForShadow by rememberSaveable {
        mutableFloatStateOf(0.01f)
    }
    alphaForShadow = when (count) {
        in 0..9 -> 0.01f
        in 10..19 -> 0.05f
        in 20..29 -> 0.1f
        in 30..39 -> 0.15f
        in 40..49 -> 0.2f
        in 50..59 -> 0.25f
        in 60..69 -> 0.3f
        in 70..79 -> 0.35f
        in 80..89 -> 0.4f
        in 90..99 -> 0.45f
        else -> 0.5f
    }
    val shapeA = remember {
        RoundedPolygon(
            12, rounding = CornerRounding(0.2f)
        )
    }
    val shapeB = remember {
        RoundedPolygon.circle(
            15
        )
    }
    val morph = remember {
        Morph(shapeA, shapeB)
    }
    val interactionSource = remember {
        MutableInteractionSource()
    }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedProgress = animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        label = "progress",
        animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium)
    )
    Card(
        modifier = modifier
            .coloredShadow(
                MaterialTheme.colorScheme.primary,
                borderRadius = 150.dp,
                alpha = alphaForShadow,
                shadowRadius = 50.dp,
                offsetX = 0.dp,
                offsetY = 0.dp
            )
            .size(350.dp)
            .padding()
            .clip(MorphPolygonShape(morph, animatedProgress.value))
            .size(200.dp)
            .clickable(interactionSource = interactionSource, indication = null) {
                onClick()
            }
            .pressScale(rememberPressScale(interactionSource)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                stringResource(R.string.dhikr),
                fontSize = 25.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.ExtraBold
            )
        }

    }
}
