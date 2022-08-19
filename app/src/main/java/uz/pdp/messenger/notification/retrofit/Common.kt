package uz.pdp.messenger.notification.retrofit

object Common {
    private const val BASE_URL = "https://fcm.googleapis.com/"
    val retrofitServices: RetrofitServices
        get() = RetrofitClient.getClient(BASE_URL).create(RetrofitServices::class.java)
}