package uz.pdp.messenger.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import uz.pdp.messenger.R
import uz.pdp.messenger.databinding.FromMeItemBinding
import uz.pdp.messenger.databinding.ToMeItemGroupBinding
import uz.pdp.messenger.models.Message
import uz.pdp.messenger.models.User

class MessageGroupAdapter(private var list: List<Message>, var uid: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class VhTo(private var itemViewBinding: ToMeItemGroupBinding) :
        RecyclerView.ViewHolder(itemViewBinding.root) {
        fun onBind(message: Message) {
            itemViewBinding.message.text = message.message
            Firebase.database.reference.child("users").child(message.fromUid!!).addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val user = snapshot.getValue(User::class.java)
                        itemViewBinding.userName.text = user?.displayName
                        itemViewBinding.timeMessage.text = message.date
                        Picasso.get().load(user?.photoUrl).into(itemViewBinding.profile)
                        if (user?.isOnline!!) {
                            itemViewBinding.isOnline.visibility = View.VISIBLE
                        } else {
                            itemViewBinding.isOnline.visibility = View.INVISIBLE
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                }
            )

        }
    }


    inner class VhFrom(private var itemViewBinding: FromMeItemBinding) :
        RecyclerView.ViewHolder(itemViewBinding.root) {
        fun onBind(message: Message) {
            itemViewBinding.message.text = message.message
            itemViewBinding.timeMessage.text = message.date
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            1 -> {
                return VhFrom(
                    FromMeItemBinding.bind(
                        LayoutInflater.from(parent.context)
                            .inflate(R.layout.from_me_item, parent, false)
                    )
                )
            }
            else -> {
                return VhTo(
                    ToMeItemGroupBinding.bind(
                        LayoutInflater.from(parent.context)
                            .inflate(R.layout.to_me_item_group, parent, false)
                    )
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            1 -> {
                holder as VhFrom
                holder.onBind(list[position])
            }
            2 -> {
                holder as VhTo
                holder.onBind(list[position])
            }
        }
    }

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int = if (list[position].fromUid == uid) 1 else 2


}