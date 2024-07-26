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
import com.example.firebasestorage.databinding.FragmentAddBinding
import com.example.firebasestorage.databinding.FragmentHomeBinding
import com.example.firebasestorage.module.Contacts
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class AddFragment : Fragment() {

    private var _binding: FragmentAddBinding?= null
    private val binding get() = _binding!!
    private lateinit var firebaseRef: DatabaseReference
    private lateinit var storageRef: StorageReference
    var uri: Uri?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        firebaseRef = FirebaseDatabase.getInstance().getReference("contacts")
        storageRef = FirebaseStorage.getInstance().getReference("Images")

        binding.btsenddata.setOnClickListener {
            saveData()
            findNavController().navigate(R.id.action_addFragment_to_homeFragment)
        }

        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()){
            binding.ivaddImage.setImageURI(it)
            binding.ivaddImage.cropToPadding
            if(it != null){
                uri=it
            }
        }
        binding.btaddImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        return binding.root
    }

    private fun saveData(){
        val name = binding.etname.text.toString()
        val phone = binding.etphone.text.toString()
        var imgUrl: String?=null

        if(name.isEmpty()) binding.etname.error = "write name"
        if(phone.isEmpty()) binding.etphone.error = "write Phone Number"

        val contactId = firebaseRef.push().key!!
        var contacts: Contacts
        uri?.let {
            storageRef.child(contactId).putFile(it)
                .addOnSuccessListener {task->
                    task.metadata!!.reference!!.downloadUrl
                        .addOnSuccessListener {url->
                            Toast.makeText(context,"Image stored successfully", Toast.LENGTH_SHORT).show()
                            imgUrl = url.toString()
                            contacts = Contacts(contactId, name, phone, imgUrl)
                            firebaseRef.child(contactId).setValue(contacts)
                                .addOnCompleteListener{
                                    Toast.makeText(context, "Data stored successfully", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener{
                                    Toast.makeText(context, "error ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                }
        }
    }
}