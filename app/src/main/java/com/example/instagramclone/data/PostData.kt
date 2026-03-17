package com.example.instagramclone.data

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.Exclude

data class PostData(
    val postId: String? = null,
    val userId: String? = null,
    val postImage: String? = null,
    val postDescription: String? = null,
    val postLocation: String? = null,
    val time: Long? = null,
    var likes: List<String>? = null,
    val searchTerms: List<String>? = null,
    @Exclude val userName: String = "",
    @Exclude val userImage: String = "",
) : Parcelable {
  constructor(
      parcel: Parcel
  ) : this(
      parcel.readString(),
      parcel.readString(),
      parcel.readString(),
      parcel.readString(),
      parcel.readString(),
      parcel.readValue(Long::class.java.classLoader) as Long,
      parcel.createStringArrayList(),
      parcel.createStringArrayList(),
  )

  override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.writeString(postId)
    parcel.writeString(userId)
    parcel.writeString(postImage)
    parcel.writeString(postDescription)
    parcel.writeString(postLocation)
    parcel.writeValue(time)
    parcel.writeStringList(likes)
    parcel.writeStringList(searchTerms)
  }

  override fun describeContents() = 0

  companion object CREATOR : Parcelable.Creator<PostData> {
    override fun createFromParcel(parcel: Parcel) = PostData(parcel)

    override fun newArray(size: Int): Array<out PostData?> = arrayOfNulls(size)
  }
}
