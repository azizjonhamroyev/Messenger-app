package uz.pdp.messenger.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import uz.pdp.messenger.MainActivity
import uz.pdp.messenger.R
import uz.pdp.messenger.databinding.FragmentSplashBinding
import uz.pdp.messenger.models.User


class SplashFragment : Fragment() {

    lateinit var binding: FragmentSplashBinding
    lateinit var handler: Handler
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSplashBinding.inflate(inflater, container, false)
        handler = Handler(Looper.getMainLooper())
        database = Firebase.database.reference
        firebaseAuth = Firebase.auth
        val uid = firebaseAuth.currentUser?.uid
        if (uid != null) {
            database.child("users").child(uid).addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.getValue(User::class.java)?.uid == null) {
                            handler.postDelayed({
                                activity?.findNavController(R.id.nav_host_fragment_2)
                                    ?.navigate(R.id.action_splashFragment_to_signInFragment)
                            }, 400)
                        } else {
                            handler.postDelayed({
                                activity?.startActivity(
                                    Intent(
                                        requireActivity(),
                                        MainActivity::class.java
                                    )
                                )
                                activity?.finish()
                            }, 400)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                }
            )
        } else {
            handler.postDelayed({
                activity?.findNavController(R.id.nav_host_fragment_2)
                    ?.navigate(R.id.action_splashFragment_to_signInFragment)
            }, 400)
        }

        return binding.root
    }
}