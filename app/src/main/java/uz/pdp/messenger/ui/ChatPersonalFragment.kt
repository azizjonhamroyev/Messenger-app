package uz.pdp.messenger.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.messaging.ktx.remoteMessage
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import uz.pdp.messenger.R
import uz.pdp.messenger.adapters.MessagePersonalAdapter
import uz.pdp.messenger.databinding.FragmentChatPersonalBinding
import uz.pdp.messenger.models.Message
import uz.pdp.messenger.models.User
import uz.pdp.messenger.notification.FirebaseMessageService
import uz.pdp.messenger.notification.models.Data
import uz.pdp.messenger.notification.models.MyResponse
import uz.pdp.messenger.notification.models.Sender
import uz.pdp.messenger.notification.retrofit.Common
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatPersonalFragment : Fragment() {

    private lateinit var binding: FragmentChatPersonalBinding
    lateinit var messagePersonalAdapter: MessagePersonalAdapter
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var database: DatabaseReference
    lateinit var messages: ArrayList<Message>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatPersonalBinding.inflate(inflater, container, false)
        database = Firebase.database.reference
        firebaseAuth = Firebase.auth
        val currentUser = firebaseAuth.currentUser
        val myUid = currentUser?.uid
        val youUid = arguments?.getString("uid")
        val friendToken = arguments?.getString("token")
        binding.backB.setOnClickListener {
            findNavController().popBackStack()
        }
        if (youUid != null) {
            database.child("users").child(youUid).addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val value = snapshot.getValue<User>()
                        Picasso.get().load(value?.photoUrl).into(binding.profile)
                        binding.userName.text = value?.displayName
                        if (value?.isOnline!!) {
                            binding.isOnline.text = "Online"
                            binding.isOnline.setTextColor(Color.parseColor("#2675EC"))
                        } else {
                            binding.isOnline.text = "last seen recently"
                            binding.isOnline.setTextColor(Color.GRAY)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                }
            )
        }
        binding.sendB.setOnClickListener {
            if (binding.yourMessage.checkIsFill()) {

                val key = database.push().key
                val date = Date()

                val message = Message(
                    SimpleDateFormat("dd.MM.yyyy HH:mm").format(date),
                    myUid!!,
                    binding.yourMessage.text.toString(),
                    youUid
                )
                Common.retrofitServices.sendNotification(
                    Sender(
                        Data(
                            myUid,
                            R.mipmap.ic_launcher_foreground,
                            message.message,
                            "New message",
                            youUid,
                            Firebase.auth.currentUser?.displayName,
                            true
                        ),
                        friendToken
                    )
                ).enqueue(object : Callback<MyResponse> {
                    override fun onResponse(
                        call: Call<MyResponse>,
                        response: Response<MyResponse>
                    ) {
                        if (response.code() == 200) {
                            if (response.body()?.success != 1) {
                                Toast.makeText(requireActivity(), "Failed!", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }

                    override fun onFailure(call: Call<MyResponse>, t: Throwable) {

                    }

                })

                database.child("users/$youUid")
                if (youUid != null) {
                    database.child("users").child(myUid).child("messages").child(youUid)
                        .child("allMessages")
                        .child(key!!)
                        .setValue(message)
                    val hashMapOf1 = hashMapOf<String, Any>(
                        "users/$myUid/last" to date.time,
                        "users/$youUid/last" to date.time
                    )
                    database.updateChildren(hashMapOf1)
                    database.child("users").child(myUid).child("messages").child(youUid)
                        .child("last").get().addOnSuccessListener {
                            val value = it.getValue<Long>()
                            if (value == null) {
                                database.child("users/$myUid/messages/$youUid/last")
                                    .setValue(date.time)
                            } else {
                                val hashMapOf = hashMapOf<String, Any>(
                                    "users/$myUid/messages/$youUid/last" to date.time
                                )
                                database.updateChildren(hashMapOf)
                            }
                        }
                    database.child("users").child(youUid).child("messages").child(myUid)
                        .addListenerForSingleValueEvent(
                            object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val value = snapshot.child("lastCount").getValue<Int>()
                                    if (value == null) {
                                        database.child("users").child(youUid).child("messages")
                                            .child(myUid).child("lastCount").setValue(1)
                                    } else {
                                        val hashMapOf = hashMapOf<String, Any>(
                                            "users/$youUid/messages/$myUid/lastCount" to value + 1,
                                        )
                                        database.updateChildren(hashMapOf)
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {

                                }

                            }
                        )
                }
                if (youUid != null) {
                    if (key != null) {
                        database.child("users").child(youUid).child("messages").child(myUid)
                            .child("allMessages")
                            .child(key)
                            .setValue(message)
                        database.child("users").child(youUid).child("messages").child(myUid)
                            .child("last").get().addOnSuccessListener {
                                val value = it.getValue<Long>()
                                if (value == null) {
                                    database.child("users/$youUid/messages/$myUid/last")
                                        .setValue(date.time)
                                } else {
                                    val hashMapOf = hashMapOf<String, Any>(
                                        "users/$youUid/messages/$myUid/last" to date.time
                                    )
                                    database.updateChildren(hashMapOf)
                                }
                            }
                    }
                }
            }
            binding.yourMessage.text.clear()
        }

        database.child("users").child(myUid!!).child("messages").child(youUid!!)
            .child("allMessages")
            .addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        messages = ArrayList()
                        for (child in snapshot.children) {
                            val value = child.getValue<Message>()
                            if (value?.date != null) {
                                messages.add(value)
                            }
                        }
                        messagePersonalAdapter = MessagePersonalAdapter(messages, myUid)
                        messagePersonalAdapter.notifyDataSetChanged()
                        binding.rv.adapter = messagePersonalAdapter
                        if (binding.rv.adapter?.itemCount != 0)
                            binding.rv.scrollToPosition(binding.rv.adapter?.itemCount!! - 1)
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                }
            )
        return binding.root
    }

    override fun onStop() {
        val gid = arguments?.getString("uid")
        val uid = firebaseAuth.currentUser?.uid
        val hashMapOf = hashMapOf<String, Any>(
            "users/$uid/messages/$gid/lastCount" to 0
        )
        database.updateChildren(hashMapOf)
        super.onStop()
    }

    private fun EditText.checkIsFill(): Boolean {
        val toString = this.text.toString()
        var isFill = false
        if (toString == "") return false
        for (c in toString) {
            if (c != ' ' && c != '\n') {
                isFill = true
                break
            }
        }
        return isFill
    }
}