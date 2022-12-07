package com.aaron.compose.ktx.lazylist

import android.os.Parcel
import android.os.Parcelable

/**
 * 可用作列表的 key
 *
 * @author aaronzzxup@gmail.com
 * @since 2022/12/7
 */
data class ParcelableKey(private val index: Int) : Parcelable {

    companion object {
        @Suppress("unused")
        @JvmField
        val CREATOR: Parcelable.Creator<ParcelableKey> =
            object : Parcelable.Creator<ParcelableKey> {
                override fun createFromParcel(parcel: Parcel) =
                    ParcelableKey(parcel.readInt())

                override fun newArray(size: Int) = arrayOfNulls<ParcelableKey?>(size)
            }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(index)
    }

    override fun describeContents(): Int {
        return 0
    }
}