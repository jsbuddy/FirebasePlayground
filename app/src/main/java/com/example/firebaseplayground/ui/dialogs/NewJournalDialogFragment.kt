package com.example.firebaseplayground.ui.dialogs

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.firebaseplayground.databinding.DialogNewJournalBinding
import com.example.firebaseplayground.ui.account.AccountSettingsFragment

class NewJournalDialogFragment : DialogFragment() {
    private lateinit var binding: DialogNewJournalBinding
    private lateinit var listener: NewJournalDialogListener

    interface NewJournalDialogListener {
        fun onDialogPositiveClick(bitmap: Bitmap, name: String)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val inflater = requireActivity().layoutInflater
            binding = DialogNewJournalBinding.inflate(inflater)
            binding.cardImage.setOnClickListener { checkPermissions() }
            val builder = AlertDialog.Builder(it)
            builder.setView(binding.root)
                .setTitle("Add new journal")
                .setPositiveButton("Create Journal") { _, _ ->
                    val bitmap = (binding.imageView.drawable as BitmapDrawable).bitmap
                    val name = binding.inputName.text.toString()
                    listener.onDialogPositiveClick(bitmap, name)
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = targetFragment as NewJournalDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$targetFragment must implement NoticeDialogListener")
        }
    }

    private fun checkPermissions() {
        if (requireContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                AccountSettingsFragment.PERMISSION_CODE
            )
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
            AccountSettingsFragment.PERMISSION_CODE -> {
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
            startActivityForResult(it, AccountSettingsFragment.IMAGE_PICK_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            AccountSettingsFragment.IMAGE_PICK_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    binding.imagePlaceholder.visibility = View.GONE
                    binding.imageView.apply {
                        setImageURI(data?.data)
                        visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}