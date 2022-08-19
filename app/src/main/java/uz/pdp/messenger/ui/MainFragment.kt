package uz.pdp.messenger.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import uz.pdp.messenger.R
import uz.pdp.messenger.SplashActivity
import uz.pdp.messenger.adapters.ViewPager2Adapter
import uz.pdp.messenger.databinding.FragmentMainBinding
import uz.pdp.messenger.databinding.TabItemBinding
import uz.pdp.messenger.models.Group
import uz.pdp.messenger.models.User

class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding
    lateinit var database: DatabaseReference
    lateinit var viewPager2Adapter: ViewPager2Adapter
    lateinit var tabItemBinding: TabItemBinding
    lateinit var firebaseAuth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        val categories = arrayOf("Chats", "Groups")
        firebaseAuth = Firebase.auth
        val currentUser = firebaseAuth.currentUser
        binding.userName.text = currentUser?.displayName.apply {
            binding.online.text = "Online"
        }
        Picasso.get().load(currentUser?.photoUrl).into(binding.profile)
        binding.logOut.setOnClickListener {
            firebaseAuth.signOut()
            GoogleSignIn.getClient(requireContext(), GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()).signOut().addOnCompleteListener {
                val intent = Intent(activity, SplashActivity::class.java)
                startActivityForResult(intent,1)
                activity?.finish()
            }
        }
        viewPager2Adapter = ViewPager2Adapter(findNavController(), requireContext())
        binding.viewPager2.adapter = viewPager2Adapter
        mediatorAttach(inflater, categories, container)
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tabItemBinding = TabItemBinding.bind(tab?.customView!!)
                tabItemBinding.category.setTextColor(Color.WHITE)
                tabItemBinding.category.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.tab_selected_b)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tabItemBinding = TabItemBinding.bind(tab?.customView!!)
                tabItemBinding.category.setTextColor(Color.parseColor("#808080"))
                tabItemBinding.category.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.tab_unselected_b)
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

        return binding.root
    }


    private fun mediatorAttach(
        inflater: LayoutInflater,
        categories: Array<String>,
        container: ViewGroup?
    ) {
        TabLayoutMediator(
            binding.tabLayout,
            binding.viewPager2
        ) { tab, position ->
            tabItemBinding =
                TabItemBinding.bind(inflater.inflate(R.layout.tab_item, container, false))
            tab.customView = tabItemBinding.root
            binding.viewPager2.setCurrentItem(tab.position, true)
            if (position == 0) {
                tabItemBinding.category.setTextColor(Color.WHITE)
                tabItemBinding.category.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.tab_selected_b)
            } else {
                tabItemBinding.category.setTextColor(Color.parseColor("#808080"))
                tabItemBinding.category.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.tab_unselected_b)
            }
            tabItemBinding.category.text = categories[position]
        }.attach()
    }

}