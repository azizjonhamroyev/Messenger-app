package uz.pdp.messenger.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import uz.pdp.messenger.R
import uz.pdp.messenger.databinding.CheckBoxUserItemBinding
import uz.pdp.messenger.models.User

class AddGroupRvItemAdapter(var listUsers: List<User>) :
    RecyclerView.Adapter<AddGroupRvItemAdapter.Vh>() {
    var checkedUser: ArrayList<User> = ArrayList()
        get() = field

    inner class Vh(var itemViewBinding: CheckBoxUserItemBinding) :
        RecyclerView.ViewHolder(itemViewBinding.root) {
        fun onBind(user: User) {
            itemViewBinding.userName.text = user.displayName
            Picasso.get().load(user.photoUrl).into(itemViewBinding.profile)
            itemViewBinding.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    var isHave = false
                    for (checkedUid in checkedUser) {
                        if (checkedUid.uid == user.uid) {
                            isHave = true
                            break
                        }
                    }
                    if (!isHave) {
                        checkedUser.add(user)
                    }
                } else {
                    checkedUser.remove(user)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh = Vh(
        CheckBoxUserItemBinding.bind(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.check_box_user_item, parent, false)
        )
    )

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.onBind(listUsers[position])
    }

    override fun getItemCount(): Int = listUsers.size
}