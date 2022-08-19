package uz.pdp.messenger.adapters

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.amulyakhare.textdrawable.TextDrawable
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import uz.pdp.messenger.R
import uz.pdp.messenger.databinding.GroupItemBinding
import uz.pdp.messenger.models.Group
import uz.pdp.messenger.models.Message
import java.io.ByteArrayInputStream

class GroupRvItemAdapter(var list: MutableList<Group>, var navController: NavController) :
    RecyclerView.Adapter<GroupRvItemAdapter.Vh>() {
    lateinit var database: DatabaseReference
    lateinit var currentUser: FirebaseUser

    inner class Vh(var itemViewBinding: GroupItemBinding) :
        RecyclerView.ViewHolder(itemViewBinding.root) {
        fun onBind(group: Group) {
            itemViewBinding.groupName.text = group.name
            if (group.groupImage == null) {
                val buildRect = TextDrawable.builder()
                    .buildRect(group.name?.substring(0, 1), group.color!!)
                itemViewBinding.profile.setImageDrawable(buildRect)
            } else {
                val substring = group.groupImage?.substring(group.groupImage!!.indexOf(",") + 1)
                val byteArrayInputStream =
                    ByteArrayInputStream(
                        Base64.decode(
                            substring?.encodeToByteArray(),
                            Base64.DEFAULT
                        )
                    )
                val decodeStream = BitmapFactory.decodeStream(byteArrayInputStream)
                itemViewBinding.profile.setImageBitmap(decodeStream)
            }
            database = Firebase.database.reference
            currentUser = Firebase.auth.currentUser!!
            database.child("groups").child(group.gid!!).child("users").child(currentUser.uid)
                .child("messages").child("lastCount").get().addOnSuccessListener {
                    val value = it.getValue<Int>()
                    if (value == null || value == 0) {
                        itemViewBinding.newMessageCount.visibility = View.INVISIBLE
                    } else {
                        itemViewBinding.newMessageCount.visibility = View.VISIBLE
                        itemViewBinding.newMessageCount.text = value.toString()
                    }
                }
            database.child("groups").child(group.gid!!).child("users").child(currentUser.uid)
                .child("messages").child("allMessages").addValueEventListener(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            try {
                                val value1 = snapshot.children.last().getValue(Message::class.java)
                                if (value1?.message != null) {

                                    if (value1.fromUid == currentUser.uid) {
                                        itemViewBinding.lastMessage.text =
                                            if (value1.message!!.length <= 15)
                                                "You: ${value1.message}" else "You: ${
                                                value1.message!!.substring(
                                                    0,
                                                    15
                                                )
                                            }..."
                                    } else {
                                        database.child("users").child(value1.fromUid!!)
                                            .child("displayName").addListenerForSingleValueEvent(
                                                object : ValueEventListener {
                                                    override fun onDataChange(snapshot: DataSnapshot) {
                                                        val value2 =
                                                            snapshot.getValue(String::class.java)
                                                        itemViewBinding.lastMessage.text =
                                                            if (value1.message!!.length <= 15)
                                                                "${
                                                                    value2?.substring(
                                                                        0,
                                                                        value2.indexOf(" ")
                                                                    )
                                                                }: ${value1.message}" else "${
                                                                value2?.substring(
                                                                    0,
                                                                    value2.indexOf(" ")
                                                                )
                                                            }: ${
                                                                value1.message!!.substring(
                                                                    0,
                                                                    15
                                                                )
                                                            }..."
                                                    }

                                                    override fun onCancelled(error: DatabaseError) {

                                                    }

                                                }
                                            )
                                    }
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
            itemViewBinding.root.setOnClickListener {
                val bundle = Bundle()
                bundle.putString("gid", group.gid)
                bundle.putString("groupName", group.name)
                navController.navigate(R.id.action_mainFragment_to_chatGroupFragment, bundle)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh = Vh(
        GroupItemBinding.bind(
            LayoutInflater.from(parent.context).inflate(
                R.layout.group_item, parent, false
            )
        )
    )

    override fun onBindViewHolder(holder: Vh, position: Int) = holder.onBind(list[position])

    override fun getItemCount(): Int = list.size
}