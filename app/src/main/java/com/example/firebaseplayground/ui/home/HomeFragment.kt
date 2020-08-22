package com.example.firebaseplayground.ui.home

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebaseplayground.JournalListAdapter
import com.example.firebaseplayground.R
import com.example.firebaseplayground.databinding.HomeFragmentBinding
import com.example.firebaseplayground.models.Journal
import com.example.firebaseplayground.ui.dialogs.NewJournalDialogFragment
import com.example.firebaseplayground.utils.Image
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class HomeFragment : Fragment(), NewJournalDialogFragment.NewJournalDialogListener {
    private lateinit var viewModel: HomeViewModel
    private val auth = Firebase.auth
    private lateinit var binding: HomeFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = HomeFragmentBinding.inflate(inflater, container, false)
        setupToolbar()
        binding.buttonAdd.setOnClickListener { addNewJournal() }
        binding.fabAdd.setOnClickListener { addNewJournal() }
        binding.listJournals.layoutManager = LinearLayoutManager(requireContext())
        getJournals()
        return binding.root
    }

    private fun setupToolbar() {
        binding.toolbar.title = "Dashboard"
        binding.toolbar.inflateMenu(R.menu.menu_home)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.item_logout -> {
                    auth.signOut()
                    findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
                    true
                }
                R.id.item_account_settings -> {
                    findNavController().navigate(R.id.action_homeFragment_to_accountSettingsFragment)
                    true
                }
                else -> false
            }
        }
    }

    private fun getJournals() {
        binding.progressBar.visibility = View.VISIBLE
        val databaseReference = Firebase.database.reference
            .child(getString(R.string.dbnode_users))
            .child(auth.currentUser!!.uid)
            .child(getString(R.string.dbnode_journals))
            .orderByKey()

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.map { s ->
                    s.getValue<Journal>()!!
                }
                if (list.isNotEmpty()) {
                    binding.listJournals.adapter = JournalListAdapter(list)
                    binding.listJournals.visibility = View.VISIBLE
                    binding.fabAdd.visibility = View.VISIBLE
                    binding.emptyState.visibility = View.GONE
                    binding.progressBar.visibility = View.GONE
                } else {
                    binding.fabAdd.visibility = View.GONE
                    binding.listJournals.visibility = View.GONE
                    binding.emptyState.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        // TODO: Use the ViewModel
    }

    private fun addNewJournal() {
        val dialog = NewJournalDialogFragment()
        dialog.setTargetFragment(this, 0)
        dialog.show(parentFragmentManager, "AddJournalDialogFragment")
    }

    override fun onDialogPositiveClick(bitmap: Bitmap, name: String) {
        binding.progressBar.visibility = View.VISIBLE
        val databaseReference = Firebase.database.reference
            .child(getString(R.string.dbnode_users))
            .child(auth.currentUser!!.uid)
            .child(getString(R.string.dbnode_journals))
        val key = databaseReference.push().key!!

        val storageReference = Firebase.storage.reference
            .child("/images/users/${auth.currentUser!!.uid}/journal$key")
        Image().upload(requireContext(), storageReference, bitmap) { url ->
            val journal = Journal(name = name, image = url)
            databaseReference.child(key).setValue(journal)
                .addOnCompleteListener { task ->
                    binding.progressBar.visibility = View.GONE
                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(), "Success", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Failure", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}