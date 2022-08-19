package uz.pdp.messenger.notification.retrofit

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import uz.pdp.messenger.notification.models.MyResponse
import uz.pdp.messenger.notification.models.Sender

interface RetrofitServices {

    @Headers(
        "Content-type:application/json",
        "Authorization:key=AAAASMxo9uI:APA91bFECbGwutbdl7AGL95Nmuoqb50PhM-Olg9NFGEtM9TtkRyGjKn44xy2gl1l0-LLQ5LzVsCPl61xnrAjlJ8DSfbdLX8p0mwDjcE_uZhzt2zgaBNYlN7WZu0j1JLfTNBoun6lV_L0"
    )

    @POST("fcm/send")
    fun sendNotification(@Body sender: Sender): Call<MyResponse>

}