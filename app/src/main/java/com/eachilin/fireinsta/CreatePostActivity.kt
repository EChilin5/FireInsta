package com.eachilin.fireinsta

import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.eachilin.fireinsta.models.Post
import com.eachilin.fireinsta.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

private const val TAG = "CreatePostActivity"
private const val PICK_PHOTO_CODE = 1234

class CreatePostActivity : AppCompatActivity() {

    private var signedInUser: User? = null
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var storageReference:StorageReference


    private lateinit var btnChooseImage: Button
    private lateinit var ivUpPost: ImageView
    private lateinit var etPostDesc: EditText
    private lateinit var btnSubmitPost: Button
    private var photoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        signedInUser()
        initFindView()
        initContent()

        storageReference = FirebaseStorage.getInstance().reference

    }

    private fun initFindView() {
        btnChooseImage = findViewById(R.id.btnChooseImage)
        ivUpPost = findViewById(R.id.ivUpPost)
        etPostDesc = findViewById(R.id.etPostDesc)
        btnSubmitPost = findViewById(R.id.btnSubmitPost)
    }

    private fun initContent() {

        btnChooseImage.setOnClickListener {
            Log.i(TAG, "open up image picker on device")
            val imagePickerIntent = Intent(Intent.ACTION_GET_CONTENT)
            imagePickerIntent.type = "image/*"
            if (imagePickerIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(imagePickerIntent, PICK_PHOTO_CODE)
            }
        }

        btnSubmitPost.setOnClickListener {
            var desc = etPostDesc.text

            if (photoUri == null) {
                Toast.makeText(this, "no phot selected", Toast.LENGTH_SHORT).show()
            }
            if (desc.isBlank()) {
                Toast.makeText(this, "no desc is add", Toast.LENGTH_SHORT).show()
            }
            if(signedInUser == null){
                Toast.makeText(this, "no signed inn user, please wait" , Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSubmitPost.isEnabled = false
            // taks api
            // upload photo to firebase storage
            val photoReference = storageReference.child("images/${System.currentTimeMillis()}-photo.jp")
            photoReference.putFile(photoUri!!)
                .continueWithTask{ photoUploadTask ->
                    Log.i(TAG, "upload bytes: ${photoUploadTask.result?.bytesTransferred}")
                    // retrieve image url of upload image
                    photoReference.downloadUrl

                }.continueWithTask{ downloadUrlTask ->
                    val post = Post(
                        desc.toString(),
                        downloadUrlTask.result.toString(),
                        System.currentTimeMillis(),
                        signedInUser
                    )
                    firestoreDb.collection("posts").add(post)

                }.addOnCompleteListener { postCreationTask->
                    btnSubmitPost.isEnabled= true

                    if(!postCreationTask.isSuccessful){
                        Toast.makeText(this, "UPLOADED failed", Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "failed", postCreationTask.exception)
                    }
                    etPostDesc.text.clear()
                    ivUpPost.setImageResource(0)
                    val profileIntent = Intent(this, ProfileActivity::class.java)
                    profileIntent.putExtra("EXTRA_USERNAME", signedInUser?.username)
                    startActivity(profileIntent)
                    finish()


                }

            // retrieve image url of the uploaded image
            // create a post object with image url and add that to post collection

        }
    }

    private fun signedInUser() {
        firestoreDb = FirebaseFirestore.getInstance()
        // retrieve current signed in user

        firestoreDb.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid as String)
            .get()
            .addOnSuccessListener { userSnapShot ->
                signedInUser = userSnapShot.toObject((User::class.java))
                Log.i(TAG, "signed in user: $signedInUser")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failure fetching singed in user", exception)
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_PHOTO_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                photoUri = data?.data
                ivUpPost.setImageURI(photoUri)
            } else {
                Toast.makeText(this, "Image is canceled", Toast.LENGTH_SHORT).show()
            }
        }
    }
}