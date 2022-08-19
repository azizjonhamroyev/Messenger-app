package uz.pdp.messenger.notification.models

class Sender {
    var data: Data? = null
    var to: String? = null

    constructor(data: Data?, to: String?) {
        this.data = data
        this.to = to
    }
}