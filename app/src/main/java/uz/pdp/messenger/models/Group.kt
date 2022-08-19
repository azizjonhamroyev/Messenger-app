package uz.pdp.messenger.models

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
class Group {
    var gid: String? = null
    var name: String? = null
    var groupMembers: Int? = null
    var groupImage: String? = null
    var color:Int? = null
    var last:Long = Long.MIN_VALUE

    constructor()

    constructor(gid: String?, name: String?, groupMembers: Int?, groupImage: String?) {
        this.gid = gid
        this.name = name
        this.groupMembers = groupMembers
        this.groupImage = groupImage
    }


}