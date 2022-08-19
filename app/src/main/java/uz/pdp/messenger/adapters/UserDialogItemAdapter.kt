package uz.pdp.messenger.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import uz.pdp.messenger.R
import uz.pdp.messenger.databinding.UserDialogItemBinding
import uz.pdp.messenger.models.User

class UserDialogItemAdapter(var list:List<User>) : BaseAdapter(){


    inner class Vh(var itemViewBinding: UserDialogItemBinding) :
        RecyclerView.ViewHolder(itemViewBinding.root) {
        fun onBind(user: User) {
            itemViewBinding.userName.text = user.displayName
            Picasso.get().load(user.photoUrl).into(itemViewBinding.profile)
        }
    }

    override fun getCount(): Int  = list.size

    override fun getItem(position: Int): User = list[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        if (convertView == null) {
            val inflate = LayoutInflater.from(parent?.context)
                .inflate(R.layout.user_dialog_item, parent, false)
            val bind = UserDialogItemBinding.bind(inflate)
            bind.userName.text = list[position].displayName
            Picasso.get().load(list[position].photoUrl).into(bind.profile)
            return bind.root
        } else {
            return convertView
        }
    }

}