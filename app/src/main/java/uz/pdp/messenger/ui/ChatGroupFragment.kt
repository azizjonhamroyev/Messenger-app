package uz.pdp.messenger.ui

import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import com.amulyakhare.textdrawable.TextDrawable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import uz.pdp.messenger.R
import uz.pdp.messenger.adapters.MessageGroupAdapter
import uz.pdp.messenger.adapters.UserDialogItemAdapter
import uz.pdp.messenger.databinding.FragmentChatGroupBinding
import uz.pdp.messenger.models.Group
import uz.pdp.messenger.models.Message
import uz.pdp.messenger.models.User
import uz.pdp.messenger.notification.models.Data
import uz.pdp.messenger.notification.models.MyResponse
import uz.pdp.messenger.notification.models.Sender
import uz.pdp.messenger.notification.retrofit.Common
import java.io.ByteArrayInputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatGroupFragment : Fragment() {

    lateinit var binding: FragmentChatGroupBinding
    lateinit var adapter: MessageGroupAdapter
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var database: DatabaseReference
    lateinit var messages: ArrayList<Message>
    lateinit var groupUsers: ArrayList<User>
    lateinit var users: ArrayList<User>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatGroupBinding.inflate(inflater, container, false)
        val gid = arguments?.getString("gid")
        val groupName = arguments?.getString("groupName")
        firebaseAuth = Firebase.auth
        database = Firebase.database.reference
        val uid = firebaseAuth.currentUser?.uid
        binding.backB.setOnClickListener {
            findNavController().popBackStack()
        }
        database.child("groups").child(gid!!).child("users").child(uid!!).child("messages")
            .child("allMessages")
            .addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        messages = ArrayList()
                        for (child in snapshot.children) {
                            val m = child.getValue(Message::class.java)
                            if (m != null) {
                                messages.add(m)
                            }
                        }
                        adapter = MessageGroupAdapter(messages.toList(), uid)
                        adapter.notifyDataSetChanged()
                        binding.rv.adapter = adapter
                        if (binding.rv.adapter?.itemCount != 0)
                            binding.rv.scrollToPosition(adapter.itemCount - 1)
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                }
            )
        binding.sendB.setOnClickListener {
            if (binding.yourMessage.checkIsFill()) {
                val date = Date()
                val hashMapOf1 = hashMapOf<String, Any>(
                    "groups/$gid/last" to date.time
                )
                database.updateChildren(hashMapOf1)
                database.child("groups").child(gid).addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val value = snapshot.getValue(Group::class.java)
                            groupUsers = ArrayList()
                            for (child in snapshot.child("users").children) {
                                val value1 = child.getValue(User::class.java)
                                if (value1 != null) {
                                    groupUsers.add(value1)
                                }
                            }
                            for (user in groupUsers) {
                                val message = Message(
                                    SimpleDateFormat("dd.MM.yyyy HH:mm").format(date),
                                    uid,
                                    binding.yourMessage.text.toString(),
                                    user.uid
                                )
                                if (user.uid != uid)
                                    Common.retrofitServices.sendNotification(
                                        Sender(
                                            Data(
                                                gid,
                                                R.mipmap.ic_launcher_foreground,
                                                message.message,
                                                groupName,
                                                user.uid,
                                                firebaseAuth.currentUser?.displayName,
                                                false
                                            ),
                                            user.token
                                        )
                                    ).enqueue(object : Callback<MyResponse> {
                                        override fun onResponse(
                                            call: Call<MyResponse>,
                                            response: Response<MyResponse>
                                        ) {
                                            if (response.code() == 200) {
                                                if (response.body()?.success != 1) {
                                                    Toast.makeText(
                                                        requireActivity(),
                                                        "Failed!",
                                                        Toast.LENGTH_SHORT
                                                    )
                                                        .show()
                                                }
                                            }
                                        }

                                        override fun onFailure(
                                            call: Call<MyResponse>,
                                            t: Throwable
                                        ) {

                                        }

                                    })
                                val key = database.push().key
                                database.child("groups").child(gid).child("users").child(user.uid!!)
                                    .child("messages").child("allMessages")
                                    .child(key!!).setValue(message)

                                database.child("groups").child(gid).child("users").child(user.uid!!)
                                    .child("messages").child("lastCount")
                                    .addListenerForSingleValueEvent(
                                        object : ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                val value1 = snapshot.getValue(Int::class.java)
                                                if (value1 == null && user.uid != uid) {
                                                    database.child("groups").child(gid)
                                                        .child("users").child(user.uid!!)
                                                        .child("messages").child("lastCount")
                                                        .setValue(1)
                                                } else if (user.uid != uid) {
                                                    val hashMapOf = hashMapOf<String, Any>(
                                                        "groups/$gid/users/${user.uid}/messages/lastCount" to value1!! + 1
                                                    )
                                                    database.updateChildren(hashMapOf)
                                                }
                                            }

                                            override fun onCancelled(error: DatabaseError) {

                                            }

                                        }
                                    )
                            }
                            binding.yourMessage.text.clear()
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    }
                )
            }
        }

        binding.listUsers.setOnClickListener {
            database.child("groups").child(gid).child("users").addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        users = ArrayList()
                        for (child in snapshot.children) {
                            val value = child.getValue<User>()
                            users.add(value!!)
                        }
                        val builder = AlertDialog.Builder(requireContext())
                        val userDialogItemAdapter = UserDialogItemAdapter(users.toList())
                        builder.setAdapter(
                            userDialogItemAdapter
                        ) { _, _ -> }
                        builder.setTitle("Group members")
                        builder.setPositiveButton(
                            "Cancel"
                        ) { dialog, _ ->
                            dialog.dismiss()
                        }
                        builder.show()
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                }
            )
        }

        database.child("groups").child(gid).addValueEventListener(
            object : ValueEventListener {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onDataChange(snapshot: DataSnapshot) {
                    val value = snapshot.getValue(Group::class.java)
                    binding.subscribers.text = "${value?.groupMembers!! + 1} members"
                    binding.groupName.text = value.name
                    if (value.groupImage == null) {
                        val buildRect = TextDrawable.builder()
                            .buildRect(value.name?.substring(0, 1), value.color!!)
                        binding.profile.setImageDrawable(buildRect)
                    } else {
                        val substring =
                            value.groupImage?.substring(value.groupImage!!.indexOf(",") + 1)
                        val byteArrayInputStream =
                            ByteArrayInputStream(
                                android.util.Base64.decode(
                                    substring?.encodeToByteArray(),
                                    android.util.Base64.DEFAULT
                                )
                            )
                        val decodeStream = BitmapFactory.decodeStream(byteArrayInputStream)
                        binding.profile.setImageBitmap(decodeStream)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            }
        )
        return binding.root
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

    override fun onStop() {
        val gid = arguments?.getString("gid")
        val uid = firebaseAuth.currentUser?.uid
        val hashMapOf = hashMapOf<String, Any>(
            "groups/$gid/users/$uid/messages/lastCount" to 0
        )
        database.updateChildren(hashMapOf)
        super.onStop()
    }
}