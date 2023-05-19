package com.aaron.compose.safestate

/**
 * 限制 [SafeState] 、[SafeStateFlow] 、[SafeStateList] 、[SafeStateMap] 等类的值修改权限，
 * 仅在实现了此接口的作用域内才能修改值。
 *
 * @author aaronzzxup@gmail.com
 * @since 2022/11/8
 */
interface SafeStateScope {

    //region State
    fun <T> SafeState<T>.setValue(value: T) {
        setValueInternal(value)
    }
    //endregion

    //region SnapshotStateList
    fun <E> SafeStateList<E>.edit(): MutableList<E> {
        return editInternal()
    }
    //endregion

    //region SnapshotStateMap
    fun <K, V> SafeStateMap<K, V>.edit(): MutableMap<K, V> {
        return editInternal()
    }
    //endregion

    //region StateFlow
    suspend fun <T> SafeStateFlow<T>.emit(value: T) {
        emitInternal(value)
    }

    fun <T> SafeStateFlow<T>.tryEmit(value: T): Boolean {
        return tryEmitInternal(value)
    }
    //endregion
}