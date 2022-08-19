package uz.pdp.messenger.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import uz.pdp.messenger.R
import uz.pdp.messenger.databinding.FromMeItemBinding
import uz.pdp.messenger.databinding.ToMeItemBinding
import uz.pdp.messenger.models.Message

class   MessagePersonalAdapter(var listMessage: List<Message>, val uid: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class ToVh(private var itemViewBinding: ToMeItemBinding) :
        RecyclerView.ViewHolder(itemViewBinding.root) {
        fun onBind(message: Message) {
            itemViewBinding.message.text = message.message
            itemViewBinding.timeMessage.text = message.date
        }
    }

    inner class FromVh(private var itemViewBinding: FromMeItemBinding) :
        RecyclerView.ViewHolder(itemViewBinding.root) {
        fun onBind(message: Message) {
            itemViewBinding.message.text = message.message
            itemViewBinding.timeMessage.text = message.date
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                FromVh(
                    FromMeItemBinding.bind(
                        LayoutInflater.from(parent.context)
                            .inflate(R.layout.from_me_item, parent, false)
                    )
                )
            }
            else -> {
                ToVh(
                    ToMeItemBinding.bind(
                        LayoutInflater.from(parent.context)
                            .inflate(R.layout.to_me_item, parent, false)
                    )
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            0 -> {
                val fromVh = holder as FromVh
                fromVh.onBind(listMessage[position])
            }
            else -> {
                val toVh = holder as ToVh
                toVh.onBind(listMessage[position])
            }
        }
    }

    override fun getItemCount(): Int = listMessage.size

    override fun getItemViewType(position: Int): Int =
        if (listMessage[position].fromUid == uid) 0 else 1

}
