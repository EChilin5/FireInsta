package com.eachilin.fireinsta

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.eachilin.fireinsta.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding:ActivityLoginBinding
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val auth = FirebaseAuth.getInstance()
        if(auth.currentUser !=null){
            getPostActivity()
        }

        binding.btnLogin.setOnClickListener {
            binding.btnLogin.isEnabled = false
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if(email.isBlank() || password.isBlank()){
                binding.btnLogin.isEnabled = true
                Toast.makeText(this, "Missing information", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firebase authentication check
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task->
                binding.btnLogin.isEnabled = true
                if(task.isSuccessful){
                    Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show()
                    getPostActivity()
                }else{
                    Log.i(TAG, "Login Failed", task.exception)
                    Toast.makeText(this, "Authentication Faile", Toast.LENGTH_SHORT).show()

                }
            }
        }


    }

    private fun getPostActivity() {
        Log.i(TAG, "fetching post")
        val intent = Intent(this, PostActivity::class.java)
        startActivity(intent)
        finish()
    }
}