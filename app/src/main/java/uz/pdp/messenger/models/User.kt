package uz.pdp.messenger.models

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
class User {
    var uid: String? = null
    var displayName: String? = null
    var photoUrl: String? = null
    var isOnline: Boolean? = null
    var last: Long = Long.MIN_VALUE
    var token: String? = null

    constructor()

    constructor(uid: String?, displayName: String?, photoUrl: String?, isOnline: Boolean?) {
        this.uid = uid
        this.displayName = displayName
        this.photoUrl = photoUrl
        this.isOnline = isOnline
    }

}