package uz.pdp.messenger.notification.models

class Data {
    private var user: String? = null
        get() = field
        set(value) {
            field = value
        }

    private var icon: Int? = null
        get() = field
        set(value) {
            field = value
        }

    private var body: String? = null
        get() = field
        set(value) {
            field = value
        }

    private var title: String? = null
        get() = field
        set(value) {
            field = value
        }

    private var sented: String? = null
        get() = field
        set(value) {
            field = value
        }

    private var username: String? = null
        get() = field
        set(value) {
            field = value
        }

    private var isPersonal: Boolean? = null
        get() = field
        set(value) {
            field = value
        }

    constructor()
    constructor(
        user: String?,
        icon: Int?,
        body: String?,
        title: String?,
        sented: String?,
        username: String?,
        isPersonal: Boolean?
    ) {
        this.user = user
        this.icon = icon
        this.body = body
        this.title = title
        this.sented = sented
        this.username = username
        this.isPersonal = isPersonal
    }


}