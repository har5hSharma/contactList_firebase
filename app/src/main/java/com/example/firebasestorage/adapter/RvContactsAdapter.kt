package com.example.firebasestorage.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasestorage.HomeFragmentDirections
import com.example.firebasestorage.databinding.RvContactItemBinding
import com.example.firebasestorage.module.Contacts
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class RvContactsAdapter(private val contactList: ArrayList<Contacts>): RecyclerView.Adapter<RvContactsAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: RvContactItemBinding): RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(RvContactItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = contactList[position]
        holder.apply {
            binding.apply {
                tvItemName.text = currentItem.name
                tvItemPhone.text = currentItem.phoneNumber
                tvItemId.text = currentItem.id
                Picasso.get().load(currentItem.image).into(ivContactList)

                rvContainer.setOnClickListener{
                    val action = HomeFragmentDirections.actionHomeFragmentToUpdateFragment(
                        currentItem.name.toString(),
                        currentItem.phoneNumber.toString(),
                        currentItem.id.toString(),
                        currentItem.image.toString()
                    )
                    findNavController(holder.itemView).navigate(action)
                }
                rvContainer.setOnLongClickListener{
                    MaterialAlertDialogBuilder(holder.itemView.context)
                        .setTitle("Delete item permanently")
                        .setMessage("delete the item permanently?")
                        .setPositiveButton("Yes"){_,_ ->
                            val firebaseRef = FirebaseDatabase.getInstance().getReference("contacts")
                            val storageRef = FirebaseStorage.getInstance().getReference("Images")
                            //storage delete
                            storageRef.child(currentItem.id.toString()).delete()

                            //realtime database
                            firebaseRef.child(currentItem.id.toString()).removeValue()
                                .addOnCompleteListener{
                                    Toast.makeText(holder.itemView.context, "Item removed successfully", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener{error ->
                                    Toast.makeText(holder.itemView.context, "error ${error.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .setNegativeButton("No"){_,_ ->
                            Toast.makeText(holder.itemView.context, "No change made", Toast.LENGTH_SHORT).show()
                        }
                        .show()

                    return@setOnLongClickListener true
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return contactList.size
    }
}