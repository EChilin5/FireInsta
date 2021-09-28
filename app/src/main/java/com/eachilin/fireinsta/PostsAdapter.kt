package com.eachilin.fireinsta

import android.content.Context
import android.text.Layout
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.eachilin.fireinsta.models.Post


class  PostsAdapter (val context: Context, val posts: List<Post>) :
    RecyclerView.Adapter<PostsAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val  view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        fun bind(post: Post) {
            var tvUserName: TextView = itemView.findViewById<TextView>(R.id.etUserName)
            var etTime:TextView = itemView.findViewById(R.id.etTime)
            val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
            val ivPost: ImageView = itemView.findViewById<ImageView>(R.id.ivPost)

            tvUserName.text = post.user?.username
            etTime.text = DateUtils.getRelativeTimeSpanString(post.creationTimeMs)
            tvDescription.text = post.description

            Glide.with(context).load(post.imageUrl).into(ivPost)
        }

    }
}