package com.example.fertilizerapp.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import kotlin.math.min

data class TabItem(
    val title: String,
    val icon: ImageVector
)

private const val LABEL_MAX_SP = 14f
private const val LABEL_MIN_SP = 12f
private val LabelHorizontalPadding = 4.dp

@Composable
fun AppTabRow(
    tabs: List<TabItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    /** Fractional page position (e.g. 1.4 while swiping from tab 1 to 2); read only during layout. */
    scrollPosition: () -> Float = { selectedIndex.toFloat() }
) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val maxLabelWidth = maxWidth / tabs.size - LabelHorizontalPadding * 2
        val labels = rememberTabLabels(titles = tabs.map { it.title }, maxLabelWidth = maxLabelWidth)

        PrimaryTabRow(
            selectedTabIndex = selectedIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = {
                TabRowDefaults.PrimaryIndicator(
                    modifier = Modifier.tabIndicatorLayout { measurable, constraints, tabPositions ->
                        // Interpolate between the two tabs the pager is currently between,
                        // so the indicator moves with the finger instead of jumping at 50 %
                        val last = tabPositions.lastIndex
                        val pos = scrollPosition().coerceIn(0f, last.toFloat())
                        val from = tabPositions[pos.toInt()]
                        val to = tabPositions[min(pos.toInt() + 1, last)]
                        val fraction = pos - pos.toInt()
                        // Underline matches the label width (TabPosition.contentWidth assumes 16dp padding)
                        val width = lerp(labels.widths[pos.toInt()], labels.widths[min(pos.toInt() + 1, last)], fraction)
                        val center = lerp(from.left + from.width / 2, to.left + to.width / 2, fraction)
                        val widthPx = width.roundToPx()
                        val placeable = measurable.measure(constraints.copy(minWidth = widthPx, maxWidth = widthPx))
                        layout(constraints.maxWidth, placeable.height) {
                            placeable.place(center.roundToPx() - widthPx / 2, 0)
                        }
                    },
                    width = Dp.Unspecified
                )
            },
            divider = {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        ) {
            tabs.forEachIndexed { index, tab ->
                val selected = index == selectedIndex
                // Content overload: no fixed 16dp text padding, so long labels get more room
                Tab(
                    selected = selected,
                    onClick = { onSelect(index) },
                    modifier = Modifier.height(64.dp),
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = tab.title,
                        style = labels.style,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = LabelHorizontalPadding)
                    )
                }
            }
        }
    }
}

private class TabLabels(val style: TextStyle, val widths: List<Dp>)

/**
 * Picks the largest font size between [LABEL_MIN_SP] and [LABEL_MAX_SP] at which every
 * label fits on one line, so all tabs share the same size. Also returns each label's
 * width (capped to the available space) for the indicator.
 */
@Composable
private fun rememberTabLabels(titles: List<String>, maxLabelWidth: Dp): TabLabels {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val base = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.sp)

    return remember(titles, maxLabelWidth, base, density) {
        val maxPx = with(density) { maxLabelWidth.roundToPx() }
        // Measure with the selected (widest) weight
        fun widthsAt(size: Float) = titles.map {
            measurer.measure(it, base.copy(fontSize = size.sp, fontWeight = FontWeight.SemiBold), maxLines = 1, softWrap = false).size.width
        }

        var size = LABEL_MAX_SP
        var widths = widthsAt(size)
        while (size > LABEL_MIN_SP && widths.max() > maxPx) {
            size -= 0.5f
            widths = widthsAt(size)
        }
        TabLabels(
            style = base.copy(fontSize = size.sp),
            widths = widths.map { with(density) { min(it, maxPx).toDp() } }
        )
    }
}
