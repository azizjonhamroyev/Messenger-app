package uz.pdp.messenger.adapters

import android.content.Context
import android.os.Bundle
import android.provider.Settings.Global.getString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import uz.pdp.messenger.R
import uz.pdp.messenger.databinding.LogOutItemBinding
import uz.pdp.messenger.databinding.UserItemBinding
import uz.pdp.messenger.models.Message
import uz.pdp.messenger.models.User

class UserRvItemAdapter(
    var list: MutableList<User>,
    var navController: NavController,
    var context: Context
) :
    RecyclerView.Adapter<UserRvItemAdapter.Vh>() {
    lateinit var currentUser: FirebaseUser

    inner class Vh(var itemViewBinding: UserItemBinding) :
        RecyclerView.ViewHolder(itemViewBinding.root) {
        fun onBind(user: User) {
            currentUser = Firebase.auth.currentUser!!
            val database = Firebase.database.reference
            itemViewBinding.userName.text = user.displayName
            database.child("users/${user.uid}/online").addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val value = snapshot.getValue<Boolean>()
                        if (value != null && value) {
                            itemViewBinding.isOnline.visibility = View.VISIBLE
                        } else {
                            itemViewBinding.isOnline.visibility = View.INVISIBLE
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                }
            )

            if (user.uid != null)
                database.child("users").child(currentUser.uid).child("messages").child(user.uid!!)
                    .child("lastCount").get().addOnSuccessListener {
                        val value = it.getValue<Int>()
                        if (value == null || value == 0) {
                            itemViewBinding.newMessageCount.visibility = View.INVISIBLE
                        } else {
                            itemViewBinding.newMessageCount.visibility = View.VISIBLE
                            itemViewBinding.newMessageCount.text = value.toString()
                        }
                    }
            if (user.uid != null)
                database.child("users").child(currentUser.uid).child("messages").child(user.uid!!)
                    .child("allMessages")
                    .addValueEventListener(
                        object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                try {
                                    val value1 =
                                        snapshot.children.last().getValue(Message::class.java)
                                    if (value1?.message != null) {
                                        if (value1.fromUid == currentUser.uid)
                                            itemViewBinding.lastMessage.text =
                                                if (value1.message!!.length <= 15)
                                                    "You: ${value1.message}" else "You: ${
                                                    value1.message!!.substring(
                                                        0,
                                                        15
                                                    )
                                                }..."
                                        else
                                            itemViewBinding.lastMessage.text =
                                                if (value1.message!!.length <= 18) value1.message else value1.message!!.substring(
                                                    0,18
                                                ) + "..."
                                        itemViewBinding.lastMessageTime.text = value1.date
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }

                            }

                            override fun onCancelled(error: DatabaseError) {

                            }

                        }
                    )

            Picasso.get().load(user.photoUrl).into(itemViewBinding.profile)
            itemViewBinding.root.setOnClickListener {
                val bundle = Bundle()
                bundle.putString("uid", user.uid)
                bundle.putString("token",user.token)
                navController.navigate(R.id.action_mainFragment_to_chatPersonalFragment, bundle)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        return Vh(
            UserItemBinding.bind(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.user_item, parent, false
                )
            )
        )

    }


    override fun onBindViewHolder(holder: UserRvItemAdapter.Vh, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int = list.size

}
