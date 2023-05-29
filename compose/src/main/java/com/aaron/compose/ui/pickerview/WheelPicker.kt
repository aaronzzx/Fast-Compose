package com.aaron.compose.ui.pickerview

import android.graphics.Typeface
import android.view.Gravity
import androidx.compose.foundation.layout.Box
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.aaron.compose.ui.pickerview.adapter.WheelAdapter
import com.aaron.compose.ui.pickerview.view.WheelView
import kotlinx.collections.immutable.PersistentList

/**
 * @author aaronzzxup@gmail.com
 * @since 2023/5/29
 */

@Composable
fun WheelPicker(
    data: PersistentList<String>,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    alphaGradientEnabled: Boolean = false,
    cyclicEnabled: Boolean = false,
    dividerColor: Color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
    dividerType: WheelPickerDividerType = WheelPickerDividerType.Wrap,
    dividerThickness: Dp = 1.dp,
    gravity: WheelPickerGravity = WheelPickerGravity.Center,
    itemsVisibleCount: Int = 3,
    lineSpacingMultiplier: Float = 1.6f,
    centerFontColor: Color = MaterialTheme.colors.primary,
    outFontColor: Color = centerFontColor.copy(alpha = 0.35f),
    fontSize: TextUnit = 20.sp,
    fontXOffset: Dp = 0.dp,
    typeface: Typeface = Typeface.MONOSPACE
) {
    Box(modifier = modifier.clip(RectangleShape)) {
        val density = LocalDensity.current
        AndroidView(
            factory = { context ->
                WheelView(context)
            },
            update = { view ->
                view.setAlphaGradient(alphaGradientEnabled)
                view.setCyclic(cyclicEnabled)
                view.setDividerColor(dividerColor.toArgb())
                view.setDividerWidth(density.run { dividerThickness.roundToPx() })
                view.setDividerType(
                    when (dividerType) {
                        WheelPickerDividerType.Fill -> WheelView.DividerType.FILL
                        WheelPickerDividerType.Wrap -> WheelView.DividerType.WRAP
                        WheelPickerDividerType.Circle -> WheelView.DividerType.CIRCLE
                    }
                )
                view.setGravity(
                    when (gravity) {
                        WheelPickerGravity.Center -> Gravity.CENTER
                        WheelPickerGravity.Left -> Gravity.LEFT
                        WheelPickerGravity.Right -> Gravity.RIGHT
                    }
                )
                view.setLabel(null)
                view.setIsOptions(false)
                view.setItemsVisibleCount(itemsVisibleCount)
                view.setLineSpacingMultiplier(lineSpacingMultiplier)
                view.setTextColorCenter(centerFontColor.toArgb())
                view.setTextColorOut(outFontColor.toArgb())
                view.setTextSizePixels(density.run { fontSize.roundToPx() })
                view.setTextXOffset(density.run { fontXOffset.roundToPx() })
                view.setTypeface(typeface)
                view.setOnItemSelectedListener {
                    val selected = data[it]
                    onItemSelected(selected)
                }
                view.adapter = object : WheelAdapter<String> {
                    override fun getItemsCount(): Int {
                        return data.size
                    }

                    override fun getItem(index: Int): String {
                        return data[index]
                    }

                    override fun indexOf(o: String?): Int {
                        return data.indexOfFirst { it == o }
                    }
                }
            }
        )
    }
}

enum class WheelPickerDividerType {

    Fill, Wrap, Circle
}

enum class WheelPickerGravity {

    Center, Left, Right
}