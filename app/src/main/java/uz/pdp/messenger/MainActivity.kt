package uz.pdp.messenger

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Exclude
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import uz.pdp.messenger.databinding.ActivityMainBinding
import uz.pdp.messenger.models.User
import uz.pdp.messenger.utils.ConnectHelper

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var database: DatabaseReference
    lateinit var currentUser: FirebaseUser
    private val CHANNEL_ID = "123"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        createNotificationChannel()
        supportActionBar?.hide()
        firebaseAuth = Firebase.auth
        database = Firebase.database.reference
        currentUser = firebaseAuth.currentUser!!
        val connectHelper = ConnectHelper(this)
        if (!connectHelper.isNetworkConnected()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
        }

        val friendId = intent.extras?.getString("userid")
        if (friendId != null) {
            database.child("users/$friendId/token").get().addOnSuccessListener {
                val friendToken = it.getValue<String>()
                val bundle = Bundle()
                bundle.putString("uid", friendId)
                bundle.putString("token", friendToken)
                val navController = findNavController(R.id.nav_host_fragment)
                navController.navigate(R.id.action_mainFragment_to_chatPersonalFragment, bundle)
            }
        }

        val gid = intent.extras?.getString("gid")
        val groupName = intent.extras?.getString("groupName")

        if (gid != null && groupName != null) {
            val bundle = Bundle()
            bundle.putString("gid", gid)
            bundle.putString("groupName", groupName)
            val navController = findNavController(R.id.nav_host_fragment)
            navController.navigate(R.id.action_mainFragment_to_chatGroupFragment, bundle)
        }


    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            mChannel.description = descriptionText
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp()
    }

    override fun onStart() {
        val hashMapOf = hashMapOf<String, Any>(
            "users/${currentUser.uid}/online" to true
        )
        database.updateChildren(hashMapOf)
        super.onStart()
    }


    override fun onStop() {
        val hashMapOf = hashMapOf<String, Any>(
            "users/${currentUser.uid}/online" to false
        )
        database.updateChildren(hashMapOf)
        super.onStop()
    }


}