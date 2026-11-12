package uz.turgunboyevjurabek.muslimapp.feature.presentation.View.components.shimmers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import uz.turgunboyevjurabek.muslimapp.core.utils.shimmerEffectAsCard

@Composable
fun MainCardShimmer() {
    Box(Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(20.dp)).shimmerEffectAsCard())
}
