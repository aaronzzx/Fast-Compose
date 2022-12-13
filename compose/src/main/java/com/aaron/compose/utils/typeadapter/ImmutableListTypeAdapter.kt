package com.aaron.compose.utils.typeadapter

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

/**
 * GSON 解析 ImmutableList 。
 *
 * @author aaronzzxup@gmail.com
 * @since 2022/12/13
 */
class ImmutableListTypeAdapter<E>(
    private val delegateAdapter: TypeAdapter<Collection<E>>
) : TypeAdapter<ImmutableList<E>>() {

    override fun write(out: JsonWriter?, value: ImmutableList<E>?) {
        delegateAdapter.write(out, value)
    }

    override fun read(`in`: JsonReader?): ImmutableList<E>? {
        return delegateAdapter.read(`in`)?.toPersistentList()
    }
}