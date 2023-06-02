package com.aaron.compose.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aaron.compose.component.LazyComponent
import com.aaron.compose.component.LoadingComponent
import com.aaron.compose.component.LoadingStateComponent
import com.aaron.compose.component.RefreshComponent
import com.aaron.compose.component.StateComponent
import com.aaron.compose.component.UDFComponent
import com.aaron.compose.component.UiBaseEvent
import com.aaron.compose.component.ViewState
import com.aaron.compose.paging.LazyPagingScope
import com.aaron.compose.paging.PagingScope
import com.aaron.compose.safestate.SafeState
import com.aaron.compose.safestate.SafeStateFlow
import com.aaron.compose.safestate.SafeStateList
import com.aaron.compose.safestate.SafeStateMap
import com.aaron.compose.safestate.SafeStateScope
import com.aaron.compose.safestate.safeStateMapOf
import com.aaron.compose.safestate.safeStateOf
import com.aaron.compose.ui.refresh.SmartRefreshType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * [UiState] 代表视图状态数据，[UiEvent] 代表一次性视图事件，如跳转页面等，没有事件可填 Nothing
 *
 * 视图操作回调函数用 on 开头方便查找与一键补全，如 onShowDialog()
 *
 * @author aaronzzxup@gmail.com
 * @since 2023/5/16
 */
abstract class BaseComposeVM<UiState : Any, UiEvent : Any> : ViewModel(),
    SafeStateScope,
    PagingScope,
    LazyPagingScope {

    //region UDFComponent
    protected abstract val initialState: UiState
    protected val uiState: UiState get() = mutableState.value

    @PublishedApi
    internal val mutableState by lazy { MutableStateFlow(initialState) }
    private val eventChannel by lazy { Channel<UiEvent>(Channel.UNLIMITED) }
    private val baseEventChannel by lazy { Channel<Any>(Channel.UNLIMITED) }

    val udfComponent: UDFComponent<UiState, UiEvent> by lazy {
        object : UDFComponent<UiState, UiEvent> {
            override val state: StateFlow<UiState> = mutableState.asStateFlow()
            override val event: Flow<UiEvent> = eventChannel.receiveAsFlow()
            override val baseEvent: Flow<Any> = baseEventChannel.receiveAsFlow()
        }
    }

    protected inline fun updateUiState(crossinline block: (UiState) -> UiState) {
        viewModelScope.launch(Dispatchers.Main.immediate) {
            mutableState.update(block)
        }
    }

    protected fun sendUiEvent(event: UiEvent) {
        viewModelScope.launch(Dispatchers.Main.immediate) {
            eventChannel.trySend(event)
        }
    }

    //region 预置视图事件
    protected val unsafeCall by lazy { UnsafeCall() }

    protected inner class UnsafeCall internal constructor() {
        /**
         * 这个应该作为设计预置事件时才调用的函数，不应该直接在业务中调用，平常请使用类型安全的 [sendUiEvent]
         *
         * 例如要加一个跳转登录的事件，应该在 ViewModel 下写一个跳登录的函数，里面才用到 [sendUiBaseEvent] ，
         * 接着需要重写一个基于 [UDFComponent] 的可组合函数，在其中处理这个全局共用的事件。
         */
        fun sendUiBaseEvent(event: Any) {
            viewModelScope.launch(Dispatchers.Main.immediate) {
                baseEventChannel.trySend(event)
            }
        }
    }

    protected fun toast(resId: Int) {
        unsafeCall.sendUiBaseEvent(UiBaseEvent.ResToast(resId))
    }

    protected fun toast(text: String) {
        unsafeCall.sendUiBaseEvent(UiBaseEvent.StringToast(text))
    }

    protected fun finish() {
        unsafeCall.sendUiBaseEvent(UiBaseEvent.Finish)
    }
    //endregion
    //endregion

    //region LazyComponent
    val lazyComponent: LazyComponent by lazy {
        object : LazyComponent {
            override val initialized: SafeState<Boolean> = safeStateOf(false)

            override fun initialize() {
                this@BaseComposeVM.initialize()
            }
        }
    }

    protected open fun initialize() {
    }
    //endregion

    //region LoadingComponent
    val loadingComponent: LoadingComponent by lazy {
        object : LoadingComponent {
            override val loading: SafeState<Boolean> = safeStateOf(false)
            override val loadingJobs: SafeStateMap<UUID, Job> = safeStateMapOf()
        }
    }

    protected fun CoroutineScope.launchWithLoading(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        cancelable: Boolean = true,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return loadingComponent.run {
            launchWithLoading(context, start, cancelable, block)
        }
    }

    protected fun showLoading(show: Boolean) {
        loadingComponent.showLoading(show)
    }

    protected fun cancelLoading() {
        loadingComponent.cancelLoading()
    }
    //endregion

    //region StateComponent
    val stateComponent: StateComponent by lazy {
        object : StateComponent {
            override val viewState: SafeState<ViewState> = safeStateOf(ViewState.Idle)

            override fun retry() {
                this@BaseComposeVM.retry()
            }
        }
    }

    val loadingStateComponent: LoadingStateComponent by lazy {
        object : LoadingStateComponent,
            StateComponent by stateComponent,
            LoadingComponent by loadingComponent {}
    }

    protected fun showViewState(viewState: ViewState) {
        stateComponent.showViewState(viewState)
    }

    protected open fun retry() {
    }
    //endregion

    //region RefreshComponent
    val refreshComponent: RefreshComponent by lazy {
        object : RefreshComponent {
            override val smartRefreshType: SafeState<SmartRefreshType> =
                safeStateOf(SmartRefreshType.Idle)

            override fun refreshIgnoreAnimation() {
                this@BaseComposeVM.refreshIgnoreAnimation()
            }
        }
    }

    protected fun refresh() {
        refreshComponent.refresh()
    }

    protected fun finishRefresh(success: Boolean) {
        refreshComponent.finishRefresh(success)
    }

    protected open fun refreshIgnoreAnimation() {
    }
    //endregion

    //region SafeStateExt
    protected inline fun <T> SafeState<T>.update(block: (T) -> T) {
        setValue(block(value))
    }

    protected inline fun <E> SafeStateList<E>.update(block: MutableList<E>.() -> Unit) {
        edit().block()
    }

    protected inline fun <K, V> SafeStateMap<K, V>.update(block: MutableMap<K, V>.() -> Unit) {
        edit().block()
    }

    protected inline fun <T> SafeStateFlow<T>.update(block: (T) -> T) {
        tryEmit(block(value))
    }
    //endregion
}