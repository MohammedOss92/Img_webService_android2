package com.sarrawi.img

import android.os.Parcel
import android.os.Parcelable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CustomScrollState : Parcelable {
    var scrollPosition: Int = 0

    constructor() {}

    private constructor(parcel: Parcel) {
        scrollPosition = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(scrollPosition)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CustomScrollState> {
        override fun createFromParcel(parcel: Parcel): CustomScrollState {
            return CustomScrollState(parcel)
        }

        override fun newArray(size: Int): Array<CustomScrollState?> {
            return arrayOfNulls(size)
        }
    }
}
