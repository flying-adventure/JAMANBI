package com.example.jamanbi

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class PostListAdapter(
    private val context: Context,
    private val posts: List<Post>
) : RecyclerView.Adapter<PostListAdapter.ViewHolder>() {

    private val firestore = FirebaseFirestore.getInstance()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleView: TextView = view.findViewById(R.id.titleView)
        val contentView: TextView = view.findViewById(R.id.contentView)
        val likesView: TextView = view.findViewById(R.id.likesView)
        val timeView: TextView = view.findViewById(R.id.timeView)
        val commentCountView: TextView = view.findViewById(R.id.commentCountView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_post_list, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = posts.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = posts[position]

        holder.titleView.text = post.title
        holder.contentView.text = post.content
        holder.likesView.text = "❤️ ${post.likes}"

        val sdf = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
        val date = Date(post.timestamp)
        holder.timeView.text = sdf.format(date)

        // 댓글 수 가져오기
        post.id?.let { postId ->
            firestore.collection("posts").document(postId)
                .collection("comments")
                .get()
                .addOnSuccessListener { result ->
                    holder.commentCountView.text = result.size().toString()
                }
                .addOnFailureListener {
                    holder.commentCountView.text = "0"
                }
        } ?: run {
            holder.commentCountView.text = "0"
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, PostViewActivity::class.java)
            intent.putExtra("title", post.title)
            intent.putExtra("content", post.content)
            intent.putExtra("postId", post.id)
            context.startActivity(intent)
        }
    }
}
