package com.example.denys.mysocialtalk

import android.app.Activity
import android.content.Intent

import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {

    companion object {
        val TAG = "RegisterActivity"
    }

    private var selectedPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        already_have_account_textView_register.setOnClickListener{

            Log.d(TAG,"Try to show log activity")

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        selectphoto_button_register.setOnClickListener{

            Log.d(TAG, "Try to show photo")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        register_button_register.setOnClickListener{
            performRegister()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){

            Log.d(TAG, "Photo was selected")

            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            Log.d(TAG, "Photo was selected ${selectedPhotoUri}")

            selectphoto_imageview_register.setImageBitmap(bitmap)
            selectphoto_button_register.alpha = 0f

        }
    }

    private fun performRegister(){

        val email = email_editText_register.text.toString()
        val password = password_editText_register.text.toString()
        val username = username_editText_register.text.toString()

        if (username.isEmpty() || username.length < 4) {
            Toast.makeText(this, "Please enter the correct username",Toast.LENGTH_SHORT).show()
            return
        }

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter the email",Toast.LENGTH_SHORT).show()
            return
        }

        if (password.isEmpty() || password.length < 6) {
            Toast.makeText(this, "Please enter the correct password", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "Email is  $email")
        Log.d(TAG, "Password: $password")

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (!it.isSuccessful) return@addOnCompleteListener
                    Log.d(TAG, "Successfully create user with uid: ${it.result.user.uid}")
                    uploadImageToFirebaseStorage()
                }
                .addOnFailureListener{
                    Log.d(TAG, "Failed to create user ${it.message}")
                    Toast.makeText(this, "Failed to create user",Toast.LENGTH_SHORT).show()
                }
    }

    private fun uploadImageToFirebaseStorage(){

        if (selectedPhotoUri == null) {
            Toast.makeText(this, "Please, choose some picture",Toast.LENGTH_SHORT).show()
            return
        }

        val picture_name = UUID.randomUUID().toString()
        val reference = FirebaseStorage.getInstance().getReference("/images/$picture_name")

        reference.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    Log.d(TAG, "Successfully upload image ${it.metadata?.path}")
                    reference.downloadUrl.addOnSuccessListener {
                        Log.d(TAG, "File Location $it")
                        saveUserInFirebaseDatabase(it.toString())
                    }
                }
                .addOnFailureListener{
                    Toast.makeText(this, "Failed to create new user",Toast.LENGTH_SHORT).show()
                }
    }

    private fun saveUserInFirebaseDatabase(profileImageUrl: String){

        val uid = FirebaseAuth.getInstance().uid ?: ""
        val reference = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(uid, username_editText_register.text.toString(), profileImageUrl)

        reference.setValue(user)
                .addOnSuccessListener {
                    Log.d(TAG, "Finally save the user to FD")

                    Toast.makeText(this, "Success!",Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LatestMessagesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                .addOnFailureListener{
                    Toast.makeText(this, "Failed to save user",Toast.LENGTH_SHORT).show()
                }
    }
}


