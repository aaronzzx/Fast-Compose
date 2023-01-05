package com.aaron.compose.paging

import com.aaron.compose.base.BasePagingResult
import com.aaron.compose.defaults.Defaults
import kotlinx.coroutines.CoroutineScope

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/11/12
 */
interface PagingScope {

    /**
     * 分页，loadKey 一般和 initialKey 相同类型，根据需要进行强转
     *
     * @param initialKey 初始值
     * @param successCode 成功状态码
     * @param pageConfig 分页详细配置
     * @param lazyLoad 懒加载，需要自己调用刷新
     * @param invokeCompletion 请求完成回调
     * @param onNextKey 获取下一个 loadKey
     * @param onRequest 请求块，loadKey 一般和 initialKey 相同类型，根据需要进行强转
     */
    fun <V> CoroutineScope.buildPageData(
        initialKey: Any?,
        successCode: Int = Defaults.SuccessCode,
        pageConfig: PageConfig = PageConfig(),
        lazyLoad: Boolean = false,
        invokeCompletion: (suspend PageData<Any?, V>.(LoadResult<Any?, V>) -> Unit)? = null,
        onNextKey: suspend (BasePagingResult<V>, LoadParams<Any?>) -> Any? = { resp, params ->
            getNextLoadKey(initialKey, params)
        },
        onRequest: suspend PageData<Any?, V>.(loadKey: Any?, pageSize: Int) -> BasePagingResult<V>
    ): PageData<Any?, V> = buildMappingPageData(
        initialKey = initialKey,
        successCode = successCode,
        pageConfig = pageConfig,
        lazyLoad = lazyLoad,
        invokeCompletion = invokeCompletion,
        onNextKey = onNextKey,
        onMapping = { it },
        onRequest = onRequest
    )

    /**
     * 分页，可以转换数据，loadKey 一般和 initialKey 相同类型，根据需要进行强转
     *
     * @param initialKey 初始值
     * @param successCode 成功状态码
     * @param pageConfig 分页详细配置
     * @param lazyLoad 懒加载，需要自己调用刷新
     * @param invokeCompletion 请求完成回调
     * @param onNextKey 获取下一个 loadKey
     * @param onMapping 转换数据
     * @param onRequest 请求块，loadKey 一般和 initialKey 相同类型，根据需要进行强转
     */
    fun <V, R> CoroutineScope.buildMappingPageData(
        initialKey: Any?,
        successCode: Int = Defaults.SuccessCode,
        pageConfig: PageConfig = PageConfig(),
        lazyLoad: Boolean = false,
        invokeCompletion: (suspend PageData<Any?, R>.(LoadResult<Any?, R>) -> Unit)? = null,
        onNextKey: suspend (BasePagingResult<V>, LoadParams<Any?>) -> Any? = { resp, params ->
            getNextLoadKey(initialKey, params)
        },
        onMapping: suspend (data: List<V>) -> List<R>,
        onRequest: suspend PageData<Any?, R>.(loadKey: Any?, pageSize: Int) -> BasePagingResult<V>
    ): PageData<Any?, R> = PageData(
        coroutineScope = this,
        config = pageConfig,
        lazyLoad = lazyLoad,
        invokeCompletion = invokeCompletion
    ) { params ->
        val pageSize = params.pageSize
        val initialSize = params.initialSize

        val curLoadKey = params.key ?: initialKey
        val curPageSize = if (curLoadKey == initialKey) initialSize else pageSize

        val resp = onRequest(curLoadKey, curPageSize)
        if (resp.code == successCode) {
            val dataList = resp.data
            var nextKey: Any? = onNextKey(resp, params)
            if (dataList.size < pageSize) {
                nextKey = null
            }
            val mapped = onMapping(dataList)
            LoadResult.Page(mapped, nextKey)
        } else {
            LoadResult.Error(PageException(resp.code, resp.msg))
        }
    }
}

private fun getNextLoadKey(
    initialKey: Any?,
    params: LoadParams<Any?>
): Any? {
    val curLoadKey = params.key
    return if (curLoadKey is Int) {
        val initialSize = params.initialSize
        val pageSize = params.pageSize
        when (curLoadKey == initialKey) {
            true -> (initialSize / pageSize).plus(1)
            else -> curLoadKey.plus(1)
        }
    } else {
        null
    }
}