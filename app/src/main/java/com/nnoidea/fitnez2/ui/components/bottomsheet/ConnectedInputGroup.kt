package com.nnoidea.fitnez2.ui.components.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A wrapper component that arranges its children in a Row and passes calculated
 * corner radii to them to create a "connected" look (like Material 3 Expressive Button Groups).
 *
 * @param modifier Modifier for the Row container.
 * @param spacing Spacing between items.
 * @param outerCornerRadius Corner radius for the outermost edges (left of first, right of last).
 * @param innerCornerRadius Corner radius for the adjacent inner edges.
 * @param items List of composable lambdas that receive the calculated corner radii and RowScope.
 */
@Composable
fun ConnectedInputGroup(
    items: List<@Composable RowScope.(topStart: Dp, topEnd: Dp, bottomStart: Dp, bottomEnd: Dp) -> Unit>,
    modifier: Modifier = Modifier,
    spacing: Dp = 4.dp,
    outerCornerRadius: Dp = 24.dp,
    innerCornerRadius: Dp = 8.dp
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
        items.forEachIndexed { index, item ->
            val isFirst = index == 0
            val isLast = index == items.size - 1

            val topStart = if (isFirst) outerCornerRadius else innerCornerRadius
            val bottomStart = if (isFirst) outerCornerRadius else innerCornerRadius
            val topEnd = if (isLast) outerCornerRadius else innerCornerRadius
            val bottomEnd = if (isLast) outerCornerRadius else innerCornerRadius

            item(this, topStart, topEnd, bottomStart, bottomEnd)
        }
    }
}
