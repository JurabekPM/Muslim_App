package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.shimmers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import uz.turgunboyevjurabek.muslimapp.core.utils.shimmerEffectAsCard

@Composable
internal fun ShimmerPlaceholder(
    modifier: Modifier,
    cornerRadius: Dp = 18.dp,
    description: String? = null,
) {
    Box(
        modifier
            .then(if (description == null) Modifier else Modifier.semantics { contentDescription = description })
            .clip(RoundedCornerShape(cornerRadius))
            .shimmerEffectAsCard(),
    )
}
