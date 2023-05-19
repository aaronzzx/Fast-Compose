package com.aaron.compose.paging

import com.aaron.compose.base.BasePagingResult
import com.aaron.compose.component.LazyPagingComponent
import com.aaron.compose.defaults.Defaults
import com.aaron.compose.ktx.toLazyPagingComponent
import kotlinx.coroutines.CoroutineScope

/**
 * 拥有请求分页能力的作用域，直接继承接口即可，无需实现函数。
 *
 * @author aaronzzxup@gmail.com
 * @since 2023/5/11
 */
interface LazyPagingScope : PagingScope {

    /**
     * 分页，loadKey 一般和 initialKey 相同类型，根据需要进行强转
     *
     * @param initialKey 初始值
     * @param successCode 成功状态码
     * @param pageConfig 分页详细配置
     * @param invokeCompletion 请求完成回调
     * @param onNextKey 获取下一个 loadKey ，决定是否还有下一页
     * @param onRequest 请求块，loadKey 一般和 initialKey 相同类型，根据需要进行强转
     */
    fun <V> CoroutineScope.buildLazyPagingComponent(
        initialKey: Any?,
        successCode: Int = Defaults.SuccessCode,
        pageConfig: PageConfig = PageConfig(),
        invokeCompletion: (suspend LazyPagingComponent<Any?, V>.(LoadResult<Any?, V>) -> Unit)? = null,
        onNextKey: suspend (BasePagingResult<V>, LoadParams<Any?>) -> Any? = { resp, params ->
            getNextLoadKey(initialKey, params)
        },
        onRequest: suspend LazyPagingComponent<Any?, V>.(loadKey: Any?, pageSize: Int) -> BasePagingResult<V>
    ): LazyPagingComponent<Any?, V> = buildMappingLazyPagingComponent(
        initialKey = initialKey,
        successCode = successCode,
        pageConfig = pageConfig,
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
     * @param invokeCompletion 请求完成回调
     * @param onNextKey 获取下一个 loadKey ，决定是否还有下一页
     * @param onMapping 转换数据
     * @param onRequest 请求块，loadKey 一般和 initialKey 相同类型，根据需要进行强转
     */
    fun <V, R> CoroutineScope.buildMappingLazyPagingComponent(
        initialKey: Any?,
        successCode: Int = Defaults.SuccessCode,
        pageConfig: PageConfig = PageConfig(),
        invokeCompletion: (suspend LazyPagingComponent<Any?, R>.(LoadResult<Any?, R>) -> Unit)? = null,
        onNextKey: suspend (BasePagingResult<V>, LoadParams<Any?>) -> Any? = { resp, params ->
            getNextLoadKey(initialKey, params)
        },
        onMapping: suspend (data: List<V>) -> List<R>,
        onRequest: suspend LazyPagingComponent<Any?, R>.(loadKey: Any?, pageSize: Int) -> BasePagingResult<V>
    ): LazyPagingComponent<Any?, R> {
        var pagingComponent: LazyPagingComponent<Any?, R>? = null
        pagingComponent = buildMappingPageData(
            initialKey = initialKey,
            successCode = successCode,
            pageConfig = pageConfig,
            lazyLoad = true,
            invokeCompletion = {
                if (invokeCompletion != null) {
                    pagingComponent!!.invokeCompletion(it)
                }
            },
            onNextKey = onNextKey,
            onMapping = onMapping,
            onRequest = { loadKey, pageSize ->
                pagingComponent!!.onRequest(loadKey, pageSize)
            }
        ).toLazyPagingComponent()
        return pagingComponent
    }
}

/**
 * 拥有请求分页能力的作用域，直接继承接口即可，无需实现函数。
 *
 * @author aaronzzxup@gmail.com
 * @since 2022/11/17
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
     * @param onNextKey 获取下一个 loadKey ，决定是否还有下一页
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
     * @param onNextKey 获取下一个 loadKey ，决定是否还有下一页
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
            val nextKey = onNextKey(resp, params)
            val mapped = onMapping(dataList)
            LoadResult.Page(mapped, nextKey)
        } else {
            LoadResult.Error(PageException(resp.code, resp.msg))
        }
    }
}

internal fun getNextLoadKey(
    initialKey: Any?,
    params: LoadParams<Any?>
): Any? {
    val curLoadKey = when (params) {
        is LoadParams.Refresh -> initialKey
        else -> params.key
    }
    return if (curLoadKey !is Int) null else {
        val initialSize = params.initialSize
        val pageSize = params.pageSize
        when (curLoadKey == initialKey) {
            true -> (initialSize / pageSize).plus(1)
            else -> curLoadKey.plus(1)
        }
    }
}