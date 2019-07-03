package com.example.denys.mysocialtalk

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(){

    companion object {
        val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        login_button_login.setOnClickListener{
            performLogin()
        }

        dont_have_account_textView_login.setOnClickListener(){

            Log.d(TAG,"Try to show log activity")

            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

    }

    private fun performLogin(){
        val email = email_editText_login.text.toString()
        val password = password_editText_login.text.toString()

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter the email",Toast.LENGTH_SHORT).show()
            return
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter the password", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    val intent = Intent(this, LatestMessagesActivity::class.java)
                    startActivity(intent)
                }
                .addOnFailureListener{
                    Toast.makeText(this, "Failed to login, check your email and password",
                            Toast.LENGTH_SHORT).show()
                    return@addOnFailureListener
                }

    }
}