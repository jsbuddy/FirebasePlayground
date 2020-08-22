package com.example.firebaseplayground.ui.account

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.firebaseplayground.R
import com.example.firebaseplayground.databinding.AccountSettingsFragmentBinding
import com.example.firebaseplayground.models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream


class AccountSettingsFragment : Fragment() {
    private lateinit var viewModel: AccountSettingsViewModel
    private lateinit var binding: AccountSettingsFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = AccountSettingsFragmentBinding.inflate(inflater, container, false)
        setupToolbar()
        binding.imageAvatar.setOnClickListener { checkPermissions() }
        preFill()
        return binding.root
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        binding.toolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }
        binding.toolbar.title = "Account settings"
        binding.toolbar.inflateMenu(R.menu.menu_account_settings)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.item_save -> {
                    save()
                    true
                }
                else -> false
            }
        }
    }

    private fun checkPermissions() {
        if (requireContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_CODE)
        } else {
            pickImage()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImage()
                } else {
                    Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun pickImage() {
        Intent(Intent.ACTION_PICK).also {
            it.type = "image/*"
            startActivityForResult(it, IMAGE_PICK_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            IMAGE_PICK_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    binding.imageAvatar.setImageURI(data?.data)
                }
            }
        }
    }

    private fun uploadImage() {
        binding.progressBar.visibility = View.VISIBLE
        val user = Firebase.auth.currentUser
        val ref = Firebase.storage.reference
            .child("/images/users/${user!!.uid}/profile_image")
        val imageView = binding.imageAvatar
        imageView.isDrawingCacheEnabled = true
        imageView.buildDrawingCache()
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val uploadTask = ref.putBytes(data)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show()
            }
            ref.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                binding.progressBar.visibility = View.INVISIBLE

                Toast.makeText(requireContext(), "Account updated", Toast.LENGTH_SHORT).show()

                val url = task.result.toString()

                Firebase.database.reference
                    .child(getString(R.string.dbnode_users))
                    .child(Firebase.auth.currentUser!!.uid)
                    .child("photoUrl")
                    .setValue(url)
                Glide.with(requireContext()).load(url).into(imageView)
            }
        }
    }

    private fun preFill() {
        binding.progressBar.visibility = View.VISIBLE

        val database = Firebase.database.reference

        val query = database.child(getString(R.string.dbnode_users)).orderByChild("uid")
            .equalTo(Firebase.auth.currentUser!!.uid)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach() { s ->
                    val data = s.getValue<User>()

                    binding.inputName.setText(data?.name)
                    binding.inputPhone.setText(data?.phone)
                    Glide.with(requireContext()).load(data?.photoUrl).into(binding.imageAvatar)
                    binding.inputEmail.setText(Firebase.auth.currentUser!!.email)
                    binding.progressBar.visibility = View.INVISIBLE
                    binding.groupForm.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    requireContext(),
                    "Could not get account information",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun save() {
        val name = binding.inputName.text.toString()
        val phone = binding.inputPhone.text.toString()

        val database = Firebase.database.reference
        val user = Firebase.auth.currentUser

        val map = hashMapOf<String, Any>(
            "/${getString(R.string.dbnode_users)}/${user!!.uid}/name" to name,
            "/${getString(R.string.dbnode_users)}/${user.uid}/phone" to phone
        )

        database.updateChildren(map).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                uploadImage()
            } else {
                Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AccountSettingsViewModel::class.java)
        // TODO: Use the ViewModel
    }

    companion object {
        const val PERMISSION_CODE = 1001
        const val IMAGE_PICK_CODE = 1001
    }
}