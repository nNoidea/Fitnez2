package com.nnoidea.fitnez2.ui.components.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Material 3 Expressive Connected Shape Helper.
 * Computes corner radii for connected horizontal items (leading, middle, trailing, or single).
 */
object ConnectedShapeDefaults {
    val OuterCornerRadius: Dp = 24.dp
    val InnerCornerRadius: Dp = 8.dp

    fun shape(
        index: Int,
        count: Int,
        outerRadius: Dp = OuterCornerRadius,
        innerRadius: Dp = InnerCornerRadius
    ): Shape {
        val isFirst = index == 0
        val isLast = index == count - 1
        return when {
            isFirst && isLast -> RoundedCornerShape(outerRadius)
            isFirst -> RoundedCornerShape(topStart = outerRadius, bottomStart = outerRadius, topEnd = innerRadius, bottomEnd = innerRadius)
            isLast -> RoundedCornerShape(topStart = innerRadius, bottomStart = innerRadius, topEnd = outerRadius, bottomEnd = outerRadius)
            else -> RoundedCornerShape(innerRadius)
        }
    }
}

/**
 * A wrapper component that arranges its children in a Row and passes calculated
 * corner radii to them to create a Material 3 Expressive connected layout.
 */
@Composable
fun ConnectedInputGroup(
    items: List<@Composable RowScope.(topStart: Dp, topEnd: Dp, bottomStart: Dp, bottomEnd: Dp) -> Unit>,
    modifier: Modifier = Modifier,
    spacing: Dp = 4.dp,
    outerCornerRadius: Dp = ConnectedShapeDefaults.OuterCornerRadius,
    innerCornerRadius: Dp = ConnectedShapeDefaults.InnerCornerRadius
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
        val count = items.size
        items.forEachIndexed { index, item ->
            val isFirst = index == 0
            val isLast = index == count - 1

            val topStart = if (isFirst) outerCornerRadius else innerCornerRadius
            val bottomStart = if (isFirst) outerCornerRadius else innerCornerRadius
            val topEnd = if (isLast) outerCornerRadius else innerCornerRadius
            val bottomEnd = if (isLast) outerCornerRadius else innerCornerRadius

            item(this, topStart, topEnd, bottomStart, bottomEnd)
        }
    }
}
