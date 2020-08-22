package com.example.firebaseplayground

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.firebaseplayground.models.Journal
import kotlinx.android.synthetic.main.list_journals.view.*

class JournalListAdapter(private val items: List<Journal>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return JournalListViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.list_journals, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is JournalListViewHolder -> holder.bind(items[position])
        }
    }

    override fun getItemCount(): Int = items.size

    class JournalListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val image: ImageView = itemView.image
        private val name: TextView = itemView.text_name

        fun bind(journal: Journal) {
            val options = RequestOptions().placeholder(R.drawable.avatar)
            Glide.with(itemView.context)
                .applyDefaultRequestOptions(options)
                .load(journal.image)
                .into(image)
            name.text = journal.name
        }
    }
}