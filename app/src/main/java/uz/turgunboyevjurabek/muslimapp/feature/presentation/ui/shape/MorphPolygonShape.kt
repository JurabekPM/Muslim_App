package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.shape

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.toPath

class MorphPolygonShape(
    private val morph: Morph,
    private val percentage: Float,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline = Outline.Generic(outlinePath(size))

    internal fun outlinePath(size: Size, inset: Float = 0f): Path {
        // The source polygons have a radius of 1 and are centered at the origin.
        val matrix = Matrix()
        matrix.scale(size.width / 2f - inset, size.height / 2f - inset)
        matrix.translate(1f, 1f)

        val path = morph.toPath(progress = percentage).asComposePath()
        path.transform(matrix)
        path.translate(Offset(inset, inset))
        return path
    }
}
