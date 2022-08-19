package uz.pdp.messenger.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
class Message {
    var date: String? = null
    var fromUid: String? = null
    var message: String? = null
    var toUid: String? = null

    constructor(date: String?, fromUid: String?, message: String?, toUid: String?) {
        this.date = date
        this.fromUid = fromUid
        this.message = message
        this.toUid = toUid
    }
    constructor()
}