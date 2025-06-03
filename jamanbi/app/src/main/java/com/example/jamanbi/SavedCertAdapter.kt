package com.example.jamanbi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SavedCertAdapter(private val list: List<SavedCertListActivity.SavedItem>) :
    RecyclerView.Adapter<SavedCertAdapter.SavedCertViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedCertViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return SavedCertViewHolder(view)
    }

    override fun onBindViewHolder(holder: SavedCertViewHolder, position: Int) {
        val item = list[position]
        holder.name.text = item.name
        holder.category.text = item.category
    }

    override fun getItemCount(): Int = list.size

    inner class SavedCertViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(android.R.id.text1)
        val category: TextView = view.findViewById(android.R.id.text2)
    }
}
