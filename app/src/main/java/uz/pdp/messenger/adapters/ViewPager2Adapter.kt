package uz.pdp.messenger.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.iid.FirebaseInstanceIdReceiver
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.messaging
import uz.pdp.messenger.R
import uz.pdp.messenger.databinding.GroupsViewPagerRvItemBinding
import uz.pdp.messenger.databinding.UserViewPagerItemBinding
import uz.pdp.messenger.models.Group
import uz.pdp.messenger.models.User
import java.lang.Exception

class ViewPager2Adapter(
    val navController: NavController,
    var context: Context
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    lateinit var users: ArrayList<User>
    lateinit var groups: ArrayList<Group>
    lateinit var database: DatabaseReference
    lateinit var currentUser: FirebaseUser
    lateinit var groupsList: ArrayList<String>

    inner class VhUser(var userViewPagerItemBinding: UserViewPagerItemBinding) :
        RecyclerView.ViewHolder(userViewPagerItemBinding.root) {
        fun onBind() {
            currentUser = Firebase.auth.currentUser!!
            database = Firebase.database.reference
            database.child("users").addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        users = ArrayList()
                        var isHave = false
                        for (child in snapshot.children) {
                            val value = child.getValue(User::class.java)
                            if (value?.uid == currentUser.uid)
                                isHave = true
                            if (value != null && value.uid != currentUser.uid)
                                users.add(value)
                        }
                        if (!isHave) {
                            Firebase.messaging.token.addOnCompleteListener { t ->
                                if (t.isSuccessful) {
                                    val user = User(
                                        currentUser.uid,
                                        currentUser.displayName,
                                        currentUser.photoUrl.toString(),
                                        true
                                    )
                                    user.token = t.result
                                    database.child("users").child(currentUser.uid)
                                        .setValue(user)
                                }
                            }

                        }
                        database.child("users").child(currentUser.uid).child("messages").get()
                            .addOnSuccessListener {
                                val lasts = ArrayList<Long>()
                                for (i in 0 until users.size) {
                                    if (i != users.size && users[i].uid != null) {
                                        val value =
                                            it.child(users[i].uid!!).child("last")
                                                .getValue(Long::class.java)
                                        if (value != null) {
                                            lasts.add(value)
                                        } else {
                                            lasts.add(Long.MIN_VALUE)
                                        }
                                    }
                                }
                                var swap = true
                                while (swap) {
                                    swap = false
                                    for (i in 0 until lasts.size - 1) {
                                        if (lasts[i] < lasts[i + 1]) {
                                            val temp = lasts[i]
                                            lasts[i] = lasts[i + 1]
                                            lasts[i + 1] = temp
                                            val temp2 = users[i]
                                            users[i] = users[i + 1]
                                            users[i + 1] = temp2
                                            swap = true
                                        }
                                    }
                                }
                                try {
                                    if (userViewPagerItemBinding.rv.adapter != null) {
                                        val userRvItemAdapter =
                                            userViewPagerItemBinding.rv.adapter as UserRvItemAdapter
                                        if (userRvItemAdapter.list[0].last != users[0].last && userRvItemAdapter.list.size == users.size) {
                                            userRvItemAdapter.list.clear()
                                            userRvItemAdapter.list.addAll(users)
                                            userRvItemAdapter.notifyDataSetChanged()
                                        } else if (userRvItemAdapter.list.size < users.size) {
                                            userRvItemAdapter.list.clear()
                                            userRvItemAdapter.list.addAll(users)
                                            userRvItemAdapter.notifyDataSetChanged()
                                        }
                                    } else {
                                        userViewPagerItemBinding.rv.adapter =
                                            UserRvItemAdapter(
                                                users,
                                                navController,
                                                context
                                            )
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                }
            )


        }

    }

    inner class VhGroup(var groupItemBinding: GroupsViewPagerRvItemBinding) :
        RecyclerView.ViewHolder(groupItemBinding.root) {
        fun onBind() {
            groupItemBinding.addGroup.setOnClickListener {
                navController.navigate(R.id.action_mainFragment_to_addGroupFragment)
            }
            currentUser = Firebase.auth.currentUser!!
            database = Firebase.database.reference
            database.child("users").child(currentUser.uid).child("groups")
                .addValueEventListener(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            groupsList = ArrayList()
                            for (child in snapshot.children) {
                                val value = child.getValue(String::class.java)
                                if (value != null) {
                                    groupsList.add(value)
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    }
                )
            database.child("groups").addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        groups = ArrayList()
                        for (s in groupsList) {
                            for (child in snapshot.children) {
                                val value = child.getValue(Group::class.java)
                                if (s == value?.gid) {
                                    groups.add(value)
                                    break
                                }
                            }
                        }
                        val sortedGroups = groups.sortedBy { it.last }.asReversed()
                        groupItemBinding.rv.adapter =
                            GroupRvItemAdapter(sortedGroups.toMutableList(), navController)
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                }
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                VhUser(
                    UserViewPagerItemBinding.bind(
                        LayoutInflater.from(parent.context)
                            .inflate(R.layout.user_view_pager_item, parent, false)
                    )
                )
            }
            else -> {
                VhGroup(
                    GroupsViewPagerRvItemBinding.bind(
                        LayoutInflater.from(parent.context)
                            .inflate(R.layout.groups_view_pager_rv_item, parent, false)
                    )
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (position) {
            0 -> {
                val vhUser = holder as VhUser
                vhUser.onBind()
            }
            1 -> {
                val vhGroup = holder as VhGroup
                vhGroup.onBind()
            }
        }
    }

    override fun getItemCount(): Int = 2

    override fun getItemViewType(position: Int): Int {
        return position
    }
}