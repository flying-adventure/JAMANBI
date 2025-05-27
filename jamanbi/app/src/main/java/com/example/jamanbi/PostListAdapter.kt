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
        val naverBadge: TextView = view.findViewById(R.id.naverBadge) // ✅ 네이버 뱃지 추가
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

        // ✅ 댓글 수 표시: 내부 글만
        if (!post.isExternal && post.id != null) {
            firestore.collection("posts").document(post.id!!)
                .collection("comments")
                .get()
                .addOnSuccessListener { result ->
                    holder.commentCountView.text = result.size().toString()
                }
                .addOnFailureListener {
                    holder.commentCountView.text = "0"
                }
        } else {
            holder.commentCountView.text = ""
        }

        // ✅ 외부 글이면 Naver 뱃지 표시
        holder.naverBadge.visibility = if (post.isExternal) View.VISIBLE else View.GONE

        // ✅ 클릭 시 상세 페이지로 이동 (외부 여부 포함)
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PostViewActivity::class.java)
            intent.putExtra("title", post.title)
            intent.putExtra("content", post.content)
            intent.putExtra("postId", post.id ?: "")
            intent.putExtra("isExternal", post.isExternal)
            intent.putExtra("externalUrl", post.externalUrl)
            context.startActivity(intent)
        }
    }
}
