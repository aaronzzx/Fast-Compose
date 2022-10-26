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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aaron.compose.base.SafeState
import com.aaron.compose.base.safeStateOf
import com.aaron.compose.component.RefreshComponent
import com.aaron.compose.component.StateComponent
import com.aaron.compose.component.stateComponent
import com.aaron.compose.ktx.clipToBackground
import com.aaron.compose.ui.refresh.SmartRefreshType
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
    refreshComponent: RefreshComponent,
    stateComponent: StateComponent,
    data: List<Int>
) {
    RefreshComponent(component = refreshComponent) {
        StateComponent(
            component = stateComponent,
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

class TestVM : ViewModel(), RefreshComponent, StateComponent by stateComponent() {

    override val smartRefreshType: SafeState<SmartRefreshType> = safeStateOf(SmartRefreshType.Idle)

    val data: StateFlow<List<Int>> get() = _data
    private val _data = MutableStateFlow<List<Int>>(emptyList())

    init {
        initLoad(true)
    }

    override fun refreshIgnoreAnimation() {
        initLoad(false)
    }

    private fun finishRefresh(success: Boolean) {
        finishRefresh(success, 0)
    }

    private fun initLoad(enableLoading: Boolean) {
        viewModelScope.launchWithViewState(enableLoading = enableLoading) {
            delay(2000)
            when (Random(System.currentTimeMillis()).nextInt(0, 4)) {
                1 -> {
                    finishRefresh(false)
                    StateComponent.ViewState.Failure(404, "Not Found")
                }
                2 -> {
                    finishRefresh(false)
                    StateComponent.ViewState.Error(IllegalStateException("Internal Error"))
                }
                3 -> {
                    finishRefresh(true)
                    _data.emit(emptyList())
                    StateComponent.ViewState.Empty
                }
                else -> {
                    finishRefresh(true)
                    val stIndex = Random(System.currentTimeMillis()).nextInt(0, 1000)
                    val range = stIndex..(stIndex + 50)
                    _data.emit(range.toList())
                    StateComponent.ViewState.Idle
                }
            }
        }
    }

    override fun retry() {
        initLoad(true)
    }
}