package com.aaron.compose.paging

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/9/20
 */
@Stable
sealed class LoadState(val noMoreData: Boolean) {

    class Idle(noMoreData: Boolean, val loadCompleted: Boolean) : LoadState(noMoreData) {
        override fun toString(): String {
            return "Idle(noMoreData=$noMoreData)"
        }

        override fun equals(other: Any?): Boolean {
            return other is Idle &&
                    noMoreData == other.noMoreData
        }

        override fun hashCode(): Int {
            return noMoreData.hashCode()
        }
    }

    object Loading : LoadState(false) {
        override fun toString(): String {
            return "Loading"
        }
    }

    class Error(val throwable: Throwable) : LoadState(false) {
        override fun equals(other: Any?): Boolean {
            return other is Error &&
                    noMoreData == other.noMoreData &&
                    throwable == other.throwable
        }

        override fun hashCode(): Int {
            return noMoreData.hashCode() + throwable.hashCode()
        }

        override fun toString(): String {
            return "Error(noMoreData=$noMoreData, throwable=$throwable)"
        }
    }

    object Waiting : LoadState(false) {
        override fun toString(): String {
            return "Waiting"
        }
    }
}

@Stable
class CombinedLoadState(
    refresh: LoadState = LoadState.Idle(false, false),
    loadMore: LoadState = LoadState.Idle(false, false)
) {

    var refresh: LoadState by mutableStateOf(refresh)
        internal set

    var loadMore: LoadState by mutableStateOf(loadMore)
        internal set
}