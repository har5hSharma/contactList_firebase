package com.example.firebasestorage

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.firebasestorage.adapter.RvContactsAdapter
import com.example.firebasestorage.databinding.FragmentHomeBinding
import com.example.firebasestorage.databinding.FragmentUpdateBinding
import com.example.firebasestorage.module.Contacts
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso


class UpdateFragment : Fragment() {

    private var _binding: FragmentUpdateBinding?= null
    private val binding get() = _binding!!

    private val args: UpdateFragmentArgs by navArgs()
    private lateinit var fireBaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference

    var uri : Uri?=null
    var imageUrl: String?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUpdateBinding.inflate(inflater, container, false)
        fireBaseReference = FirebaseDatabase.getInstance().getReference("contacts")
        storageReference = FirebaseStorage.getInstance().getReference("Images")
        imageUrl = args.imageUri

        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()){
            binding.ivUpdateImage.setImageURI(it)
            binding.ivUpdateImage.cropToPadding
            if(it != null){
                uri=it
            }
        }

        binding.apply {
            etname.setText(args.name)
            etphone.setText(args.phoneNumber)
            Picasso.get().load(imageUrl).into(ivUpdateImage)

            btUpdatedata.setOnClickListener{
                updateData()
                findNavController().navigate(R.id.action_updateFragment_to_homeFragment)
            }
            ivUpdateImage.setOnClickListener {
                context.let {context ->
                    MaterialAlertDialogBuilder(context!!)
                        .setTitle("Update the image")
                        .setMessage("Do you want to update the image?")
                        .setPositiveButton("Update"){_,_->
                            pickImage.launch("image/*")
                        }
                        .setNegativeButton("delete"){_,_->
                            imageUrl = null
                            ivUpdateImage.setImageResource(R.drawable.ic_launcher_foreground)
                        }
                        .setNeutralButton("cancel"){_,_ ->

                        }
                        .show()
                }
            }
        }

        return binding.root
    }

    private fun updateData() {
        val name = binding.etname.text.toString()
        val phone = binding.etphone.text.toString()

        if(name.isEmpty()) binding.etname.error = "write name"
        if(phone.isEmpty()) binding.etphone.error = "write Phone Number"

        val contactId = args.id
        var contacts : Contacts

        if(uri != null){
            storageReference.child(contactId).putFile(uri!!)
                .addOnSuccessListener { task->
                    task.metadata!!.reference!!.downloadUrl
                        .addOnCompleteListener{ url->
                            val imgeUrl = url.toString()
                            contacts = Contacts(contactId, name, phone, imgeUrl)
                            fireBaseReference.child(contactId).setValue(contacts)
                                .addOnCompleteListener{
                                    Toast.makeText(context, "Data updated successfully", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener{
                                    Toast.makeText(context, "error ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                        } }
                }
        if (uri == null){
            contacts = Contacts(contactId, name, phone, imageUrl)
            fireBaseReference.child(contactId).setValue(contacts)
                .addOnCompleteListener{
                    Toast.makeText(context, "Data updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener{
                    Toast.makeText(context, "error ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
        if(imageUrl == null) storageReference.child(contactId).delete()
    }
}