package uz.turgunboyevjurabek.muslimapp.feature.presentation.View.components.shimmers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DayOf7ShimmerList(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ShimmerPlaceholder(Modifier.fillMaxWidth().height(54.dp), cornerRadius = 16.dp)
        ShimmerPlaceholder(Modifier.fillMaxWidth().height(222.dp))
        repeat(3) {
            ShimmerPlaceholder(Modifier.fillMaxWidth().height(58.dp))
        }
    }
}
