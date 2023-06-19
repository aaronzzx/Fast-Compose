package com.aaron.fastcompose.ui.test

import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import com.aaron.compose.base.BaseRoute
import com.aaron.compose.base.navTo
import com.aaron.compose.ui.AutoResizeText
import com.aaron.compose.ui.FontSizeRange
import com.aaron.compose.ui.RollPickerDefaults
import com.aaron.compose.ui.RollPickerStyle
import com.aaron.compose.ui.TopBar
import com.aaron.compose.ui.VerticalRollPicker
import com.aaron.compose.ui.clipRollPickerCenter
import com.aaron.compose.ui.rememberRollPickerState
import com.aaron.fastcompose.R
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.VibrateUtils
import com.google.accompanist.navigation.animation.composable
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import kotlin.math.absoluteValue
import kotlin.math.sign

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/12/29
 */

object TestScreen : BaseRoute {

    override val route: String = "test"

    fun navigate(navController: NavController, navOptions: NavOptions? = null) {
        navController.navTo(
            route = TestScreen,
            navOptions = navOptions
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.testScreen(navController: NavController) {
    composable(route = TestScreen.route) {
        TestScreen(
            onBack = {
                navController.popBackStack()
            }
        )
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun TestScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colors.background)
            .navigationBarsPadding()
    ) {
        TopBar(
            title = "Hello World",
            startIcon = R.drawable.back,
            contentPadding = WindowInsets.statusBars.asPaddingValues(),
            backgroundColor = Color.Transparent,
            elevation = 0.dp,
            titleStyle = TextStyle(fontSize = 24.sp),
            contentColor = contentColorFor(backgroundColor = MaterialTheme.colors.surface),
            onStartIconClick = {
                onBack()
            }
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
//            Wheels()

            SingleWheel()
        }
    }
}

@Composable
private fun SingleWheel() {
    CompositionLocalProvider(
        LocalTextStyle provides TextStyle.Default.copy(
            color = Color.Gray
        )
    ) {
        val state = rememberRollPickerState()

        LaunchedEffect(key1 = state) {
            snapshotFlow { state.currentIndexOffsetFraction }
                .filter { it.absoluteValue <= 0.1f }
                .distinctUntilChangedBy { state.currentIndex }
                .drop(1)
                .collect {
                    VibrateUtils.vibrate(10)
                }
        }

        VerticalRollPicker(
            modifier = Modifier
                .size(200.dp)
                .clipRollPickerCenter(
                    state = state,
                    color = MaterialTheme.colors.primary,
                    scaleX = 1.1f
                ),
            count = 99,
            state = state,
            visibleCount = 5,
            lineSpacingMultiplier = 1f,
            loop = true,
            onPick = { index ->
                ToastUtils.showShort("${index.value}")
            },
            style = RollPickerStyle.Wheel(),
//            absMaxVelocity = 10000f,
            onFling = RollPickerDefaults.slowInFling(state = state)
        ) { index ->
            AutoResizeText(
                text = formatInt(index.value),
                fontSizeRange = FontSizeRange(8.sp, state.itemFontSize),
                fitCenter = true,
                style = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
            )
        }
    }
}

@Composable
private fun Wheels() {
    Box(
        modifier = Modifier
            .background(color = Color(0xFFF0F0F0))
            .fillMaxWidth()
            .height(250.dp)
    ) {
        var itemSize by remember {
            mutableStateOf(0.dp)
        }
        Box(
            modifier = Modifier
                .alpha(0f)
                .align(Alignment.Center)
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .height(itemSize)
                .background(
                    color = Color(0xFFFCE1E3),
                    shape = RoundedCornerShape(8.dp)
                )
        )
        Row(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxSize(),
//                    horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val year = (1970..2023).map { "$it" }.toPersistentList()
            val month = (1..12).map { formatInt(it) }.toPersistentList()
            val day = (1..30).map { formatInt(it) }.toPersistentList()
            MyWheel(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                label = "年",
                data = year,
                onSelected = {
                    ToastUtils.showShort(it)
                },
                onItemSizeChange = {
                    itemSize = it
                }
            )
            MyWheel(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                label = "月",
                data = month,
                onSelected = {
                    ToastUtils.showShort(it)
                }
            )
            MyWheel(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                label = "日",
                data = day,
                onSelected = {
                    ToastUtils.showShort(it)
                }
            )
        }
    }
}

@Composable
private fun MyWheel(
    label: String,
    data: PersistentList<String>,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    onItemSizeChange: ((Dp) -> Unit)? = null
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val textStyle = LocalTextStyle.current.copy(
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF7C7C7C)
        )
        val maxLines = 1
        val overflow = TextOverflow.Ellipsis
        val fontSizeRange = FontSizeRange(min = 8.sp, max = 26.sp)

        val rollPickerState = rememberRollPickerState()
        val curOnItemSizeChange by rememberUpdatedState(newValue = onItemSizeChange)
        LaunchedEffect(key1 = rollPickerState) {
            launch {
                snapshotFlow { rollPickerState.isScrollInProgress }
                    .drop(1)
                    .collect {
                        Log.d("zzx", "curIndex: ${rollPickerState.currentIndex.value}, scroll: $it")
                    }
            }

            launch {
                snapshotFlow { rollPickerState.itemSize }
                    .drop(1)
                    .collect {
                        curOnItemSizeChange?.invoke(it)
                    }
            }
        }

        VerticalRollPicker(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f),
            count = data.size,
            state = rollPickerState,
            visibleCount = 9,
            loop = true,
            lineSpacingMultiplier = 1f,
            absMaxVelocity = 6000f,
            onPick = { index ->
                onSelected(data[index.value])
            }
        ) { index ->
            AutoResizeText(
                text = data[index.value],
                style = textStyle.copy(fontFamily = FontFamily.Monospace),
                maxLines = maxLines,
                overflow = overflow,
                fontSizeRange = fontSizeRange
            )
        }

        Text(text = label)
    }
}

private fun formatInt(index: Int): String {
    return if (index > 9) "$index" else "0$index"
}

private fun formatOffset(offset: Float): String {
    return when (offset.sign) {
        -1f -> decimalFormat.format(offset)
        else -> "+${decimalFormat.format(offset)}"
    }
}

private fun formatText(index: String, offset: String): String {
    return "$index@HAUIHSDIAUHS$offset"
}

private val decimalFormat = DecimalFormat("0.00")