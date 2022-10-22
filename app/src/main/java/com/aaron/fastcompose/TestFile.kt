package com.aaron.fastcompose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aaron.compose.component.ViewStateable
import com.aaron.compose.component.ViewStateComponent
import com.aaron.compose.ktx.clipToBackground
import com.aaron.compose.ktx.toDp
import com.aaron.compose.ui.refresh.SmartRefresh
import com.aaron.compose.ui.refresh.SmartRefreshType
import com.aaron.compose.ui.refresh.materialheader.MaterialRefreshIndicator
import com.aaron.compose.ui.refresh.rememberSmartRefreshState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/10/14
 */

@Composable
fun TestComposable(
    viewStateable: ViewStateable,
    data: List<Int>,
    refreshType: SmartRefreshType,
    onRefresh: () -> Unit,
    onIdle: () -> Unit
) {
    SmartRefresh(
        state = rememberSmartRefreshState(type = refreshType),
        onRefresh = onRefresh,
        onIdle = onIdle,
        clipHeaderEnabled = false,
        translateBody = false,
        indicator = { smartRefreshState, triggerPixels, maxDragPixels, height ->
            MaterialRefreshIndicator(
                state = smartRefreshState,
                refreshTriggerDistance = triggerPixels.toDp(),
                contentColor = Color(0xFF4FC3F7)
            )
        }
    ) {
        ViewStateComponent(
            viewStateable = viewStateable,
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color(0xFFF0F0F0))
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(data) { int ->
                    Box(
                        modifier = Modifier
                            .clipToBackground(
                                color = Color.Red.copy(0.1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .fillMaxWidth()
                            .aspectRatio(2f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "$int", color = Color(0xFF333333))
                    }
                }
            }
        }
    }
}

class TestVM : ViewModel(), ViewStateable by ViewStateable() {

    var smartRefreshType: SmartRefreshType by mutableStateOf(SmartRefreshType.Idle)

    val data: StateFlow<List<Int>> get() = _data
    private val _data = MutableStateFlow<List<Int>>(emptyList())

    init {
        initLoad(true)
    }

    fun refresh() {
        smartRefreshType = SmartRefreshType.Refreshing
        initLoad(false)
    }

    fun finishRefresh(success: Boolean) {
        if (smartRefreshType == SmartRefreshType.Refreshing) {
            smartRefreshType = when (success) {
                true -> SmartRefreshType.Success(0)
                else -> SmartRefreshType.Failure(0)
            }
        }
    }

    private fun initLoad(enableLoading: Boolean) {
        viewModelScope.launchWithViewState(enableLoading = enableLoading) {
            delay(2000)
            when (Random(System.currentTimeMillis()).nextInt(0, 4)) {
                1 -> {
                    finishRefresh(false)
                    ViewStateable.Result.Failure(404, "Not Found")
                }
                2 -> {
                    finishRefresh(false)
                    ViewStateable.Result.Error(IllegalStateException("Internal Error"))
                }
                3 -> {
                    finishRefresh(true)
                    _data.emit(emptyList())
                    ViewStateable.Result.Empty
                }
                else -> {
                    finishRefresh(true)
                    val stIndex = Random(System.currentTimeMillis()).nextInt(0, 1000)
                    val range = stIndex..(stIndex + 50)
                    _data.emit(range.toList())
                    ViewStateable.Result.Default
                }
            }
        }
    }

    override fun retry() {
        initLoad(true)
    }
}