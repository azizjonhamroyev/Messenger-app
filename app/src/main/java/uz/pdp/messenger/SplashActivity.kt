package uz.pdp.messenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.navigation.findNavController
import uz.pdp.messenger.databinding.ActivitySplashBinding
import uz.pdp.messenger.utils.ConnectHelper

class SplashActivity : AppCompatActivity() {
    lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        val connectHelper = ConnectHelper(this)
        if (!connectHelper.isNetworkConnected()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_2)
        return navController.navigateUp()
    }

    override fun onBackPressed() {
        val navController = binding.navHostFragment2.findNavController()
        when (navController.currentDestination?.id) {
            R.id.signInFragment -> finish()
            else -> super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            val navController = findNavController(R.id.nav_host_fragment_2)
            navController.navigate(R.id.sign_in)
        }
    }

}