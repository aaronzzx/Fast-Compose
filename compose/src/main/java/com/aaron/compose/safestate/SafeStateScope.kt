package com.aaron.compose.safestate

/**
 * 提供 SafeState 系列扩展
 *
 * @author aaronzzxup@gmail.com
 * @since 2022/11/8
 */
interface SafeStateScope {

    //region State
    fun <T> SafeState<T>.setValue(value: T) {
        setValueInternal(value)
    }

    fun <T> SafeState<T>.postValue(block: (value: T) -> T) {
        val pendingValue = block(value)
        setValue(pendingValue)
    }
    //endregion

    //region SnapshotStateList
    fun <E> SafeStateList<E>.edit(): MutableList<E> {
        return editInternal()
    }

    fun <E> SafeStateList<E>.withEdit(block: MutableList<E>.() -> Unit) {
        edit().block()
    }
    //endregion

    //region SnapshotStateMap
    fun <K, V> SafeStateMap<K, V>.edit(): MutableMap<K, V> {
        return editInternal()
    }

    fun <K, V> SafeStateMap<K, V>.withEdit(block: MutableMap<K, V>.() -> Unit) {
        edit().block()
    }
    //endregion

    //region StateFlow
    suspend fun <T> SafeStateFlow<T>.emit(value: T) {
        emitInternal(value)
    }

    fun <T> SafeStateFlow<T>.tryEmit(value: T) {
        tryEmitInternal(value)
    }
    //endregion
}