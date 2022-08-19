package uz.pdp.messenger.ui

import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.setPadding
import androidx.navigation.fragment.findNavController
import com.amulyakhare.textdrawable.TextDrawable
import com.google.android.material.internal.TextDrawableHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.permissionx.guolindev.PermissionCollection
import com.permissionx.guolindev.PermissionX
import uz.pdp.messenger.R
import uz.pdp.messenger.adapters.AddGroupRvItemAdapter
import uz.pdp.messenger.databinding.FragmentAddGroupBinding
import uz.pdp.messenger.models.Group
import uz.pdp.messenger.models.User
import java.io.ByteArrayOutputStream
import kotlin.random.Random

class AddGroupFragment : Fragment() {

    lateinit var binding: FragmentAddGroupBinding
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var database: DatabaseReference
    lateinit var users: ArrayList<User>
    lateinit var adapter: AddGroupRvItemAdapter
    lateinit var byteArrayString: String
    private val REQUEST_IMAGE_CAPTURE = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddGroupBinding.inflate(inflater, container, false)
        firebaseAuth = Firebase.auth
        val currentUser = firebaseAuth.currentUser
        database = Firebase.database.reference
        database.child("users").addListenerForSingleValueEvent(

            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    users = ArrayList()
                    for (child in snapshot.children) {
                        val value = child.getValue<User>()
                        if (value != null && value.uid != currentUser?.uid)
                            users.add(value)
                    }
                    adapter = AddGroupRvItemAdapter(users.toList())
                    binding.rv.adapter = adapter
                }

                override fun onCancelled(error: DatabaseError) {

                }
            }
        )
        binding.createBtn.setOnClickListener {
            if (::adapter.isInitialized && adapter.checkedUser.size != 0 && binding.groupName.checkIsFill()) {
                val key = database.push().key
                val group = Group(
                    key!!,
                    binding.groupName.text.toString(),
                    adapter.checkedUser.size,
                    if (::byteArrayString.isInitialized) byteArrayString else null
                )
                group.color = getRandomColor()
                database.child("groups").child(key).setValue(group)
                database.child("users/${currentUser?.uid}").get().addOnSuccessListener {
                    val cUser = it.getValue<User>()
                    database.child("groups").child(key).child("users").child(currentUser?.uid!!)
                        .setValue(cUser)
                    database.child("users").child(cUser?.uid!!).child("groups").child(key)
                        .setValue(key)
                }
                for (user in adapter.checkedUser) {
                    database.child("groups").child(key).child("users").child(user.uid!!)
                        .setValue(user)
                    database.child("users").child(user.uid!!).child("groups").child(key)
                        .setValue(key)
                }
                findNavController().popBackStack()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please select group members and name the group",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.profile.setOnClickListener {
            getImage()
        }

        return binding.root
    }

    private fun EditText.checkIsFill(): Boolean {
        val toString = this.text.toString()
        var isFill = false
        if (toString == "") return false
        for (c in toString) {
            if (c != ' ' && c != '\n') {
                isFill = true
                break
            }
        }
        return isFill
    }

    private fun requestPermission() {
        if (PermissionX.isGranted(requireContext(), android.Manifest.permission.CAMERA)) {
            dispatchTakePictureIntent()
        } else {
            PermissionX.init(requireActivity()).permissions(android.Manifest.permission.CAMERA)
                .request { allGranted, grantedList, deniedList ->
                    if (allGranted) {
                        dispatchTakePictureIntent()
                    } else {
                        requestPermission()
                    }
                }
        }
    }


    private fun getImage() {
        val popupMenu = PopupMenu(requireContext(), binding.profile)
        popupMenu.inflate(R.menu.popup_menu)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.upload_from_gallery -> {
                    getImageGallery.launch("image/*")
                }
                R.id.upload_from_camera -> {
                    requestPermission()
                }
            }
            true
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupMenu.setForceShowIcon(true)
        }
        popupMenu.show()
    }


    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            binding.profile.setPadding(0)
            binding.profile.scaleType = ImageView.ScaleType.CENTER
            binding.profile.setImageBitmap(imageBitmap)
            val byteArrayOutputStream = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 40, byteArrayOutputStream)
            byteArrayString =
                Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT)
        }
    }

    private val getImageGallery =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                binding.profile.setPadding(0)
                binding.profile.scaleType = ImageView.ScaleType.CENTER
                binding.profile.setImageURI(uri)
                val bitmap =
                    MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 40, stream)
                val toByteArray = stream.toByteArray()
                byteArrayString = Base64.encodeToString(toByteArray, Base64.DEFAULT)
            }
        }

    private fun getRandomColor(): Int {
        val arrayOf = arrayOf(
            Color.BLACK,
            Color.GRAY,
            Color.BLUE,
            Color.CYAN,
            Color.DKGRAY,
            Color.GREEN,
            Color.LTGRAY,
            Color.MAGENTA,
            Color.RED,
            Color.YELLOW
        )
        return arrayOf[Random.nextInt(0, arrayOf.size)]
    }
}