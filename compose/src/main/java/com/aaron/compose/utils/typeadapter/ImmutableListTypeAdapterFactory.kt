package com.aaron.compose.utils.typeadapter

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import kotlinx.collections.immutable.ImmutableList

/**
 * GSON 解析 ImmutableList 。需要使用到 CollectionTypeAdapterFactory ，或者能解析 Collection 的自定义 Factory 。
 *
 * @author aaronzzxup@gmail.com
 * @since 2022/12/13
 */
class ImmutableListTypeAdapterFactory(
    private val delegateFactory: TypeAdapterFactory
) : TypeAdapterFactory {

    override fun <T : Any?> create(gson: Gson?, type: TypeToken<T>?): TypeAdapter<T>? {
        type ?: return null
        if (!ImmutableList::class.java.isAssignableFrom(type.rawType)) {
            return null
        }
        val delegateAdapter =
            delegateFactory.create(gson, type) as? TypeAdapter<Collection<T>> ?: return null
        return ImmutableListTypeAdapter(delegateAdapter) as? TypeAdapter<T>
    }
}