package com.eachilin.fireinsta

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eachilin.fireinsta.models.Post
import com.eachilin.fireinsta.models.User
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

private const val TAG = "PostActivity"
private const val EXTRA_USERNAME = "EXTRA_USERNAME"

open class PostActivity : AppCompatActivity() {

    private var signedInUser : User?  =null
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var post:MutableList<Post>
    private lateinit var adapter:PostsAdapter
    private lateinit var rvPost:RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        setUpRecylcer()
        fetchPost()
        createPost()

    }

    private fun createPost() {
        var fabCreate : FloatingActionButton = findViewById(R.id.fabCreate)
        fabCreate.setOnClickListener {
            val intent = Intent(this, CreatePostActivity::class.java)
            startActivity(intent )
        }
    }

    private fun setUpRecylcer() {
        // Create the layout file which represents one post - Done
        // Create the data source - Done
        post = mutableListOf()
        // Create the adapter
        adapter = PostsAdapter(this, post)
        rvPost = findViewById(R.id.rvPost)
        rvPost.adapter = adapter
        rvPost.layoutManager = LinearLayoutManager(this)
        //bind the adpater and layout manger to rv

    }

    private fun fetchPost() {
        firestoreDb = FirebaseFirestore.getInstance()
        // retrieve current signed in user

        firestoreDb.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid as String)
            .get()
            .addOnSuccessListener { userSnapShot->
                signedInUser = userSnapShot.toObject((User::class.java))
                Log.i(TAG, "signed in user: $signedInUser")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failure fetching singed in user", exception)
            }

        var postReference = firestoreDb
            .collection("posts")
            .limit(20)
            .orderBy("creation_time_ms", Query.Direction.DESCENDING)

        val username = intent.getStringExtra(EXTRA_USERNAME)
        if( username != null){
            //user.username is a field path
                supportActionBar?.title = username
            postReference = postReference.whereEqualTo("user.username", username)
        }

        postReference.addSnapshotListener {  snapshot, error ->
            if(error != null || snapshot == null){
                Log.e(TAG, "Exception when querying posts")
                return@addSnapshotListener
            }
            val postList = snapshot.toObjects(Post::class.java)
            post.clear()
            post.addAll(postList)
            adapter.notifyDataSetChanged()
            for(post in postList){
                Log.i(TAG, "Post ${post}")
            }


        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_posts, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.menu_profile){
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra(EXTRA_USERNAME,signedInUser?.username)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}