package com.example.ourapp.AccountRelated

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.ourapp.HomePageUser.UserHome
import com.example.ourapp.R

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.mikhaellopez.circularimageview.CircularImageView
import es.dmoral.toasty.Toasty
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class CreateAccountActivity : AppCompatActivity(),View.OnClickListener {
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private var myRef = FirebaseDatabase.getInstance().getReference("UserProfile")
    private lateinit var signin_button: Button;
    private lateinit var userFirstName: EditText;
    private lateinit var userLastName: EditText;
    private lateinit var image_upload_text: TextView;
    private lateinit var userImage: CircularImageView;
    private lateinit var username: EditText;
    private lateinit var password: EditText;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)
        signin_button = findViewById(R.id.signin_button)
        username = findViewById(R.id.user_name)
        password = findViewById(R.id.password)
        userFirstName = findViewById(R.id.user_firstname)
        userLastName = findViewById(R.id.user_lastname)
        userImage = findViewById(R.id.userProfileImage)
        image_upload_text = findViewById(R.id.image_upload_text)
        signin_button.setOnClickListener(this)
        userImage.setOnClickListener(this)
    }


    public override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.signin_button -> {
                    var email = username.text.toString()
                    var password = password.text.toString()
                    var user_fname = userFirstName.text.toString()
                    var user_lname = userLastName.text.toString()

                    if (password.isEmpty() || email.isEmpty() || user_fname.isEmpty() || user_lname.isEmpty()) {
                        Toasty.info(this,"Insufficient  Details",Toasty.LENGTH_SHORT).show()
                        return;
                    }

                    mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("info", "createUserWithEmail:success")
                                val user = mAuth.currentUser
                                if (user != null) {

                                    var profileImageURl: String? = null;

                                    val storage =
                                        FirebaseStorage.getInstance()
                                            .getReferenceFromUrl("gs://my-application-bc31e.appspot.com/")
                                    val df = SimpleDateFormat("ddMMyyHHmmss")
                                    val dataobj = Date()
                                    val imagePath =
                                        user_fname + user_lname + df.format(dataobj) + ".jpg"
                                    val storageRef = storage.child("images/" + imagePath)
                                    userImage.isDrawingCacheEnabled = true
                                    userImage.buildDrawingCache()
                                    val drawable = userImage.drawable as BitmapDrawable
                                    val bitmap = drawable.bitmap
                                    val baos = ByteArrayOutputStream()
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                                    val data = baos.toByteArray()
                                    val upload = storageRef.putBytes(data)
                                    val urlTask = upload.continueWithTask { task ->
                                        if (!task.isSuccessful) {
                                            task.exception?.let {
                                                throw it
                                            }
                                        }
                                        storageRef.downloadUrl
                                    }.addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val downloadUri = task.result
                                            profileImageURl = downloadUri.toString()
                                            var model = UserInfoModel(
                                                user_fname,
                                                user_lname,
                                                email,
                                                profileImageURl
                                            )
                                            myRef.child(user.uid).setValue(model)


                                        } else {
                                            Toast.makeText(
                                                this, "Not able to upload image error occured !",
                                                Toast.LENGTH_LONG
                                            ).show()

                                        }
                                    }


                                    var intent = Intent(this, UserHome::class.java)
                                    startActivity(intent)
                                    finish()
                                }

                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("Info", "createUserWithEmail:failure", task.exception)
                                Toasty.info(
                                    baseContext, "Email Already Exists",
                                    Toasty.LENGTH_SHORT
                                ).show()

                            }

                            // ...
                        }
                }


                R.id.userProfileImage -> {
                    if (checkPermission()) {
                        chooseImageGallery()
                    }
                }

            }
        }
    }

    private fun chooseImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_CHOOSE)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int, data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)


        if (data != null && resultCode == Activity.RESULT_OK) {
            image_upload_text.text = ""
            userImage.setImageURI(data?.data)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    chooseImageGallery()
                } else {
                    Toasty.info(
                        this,
                        "Permission denied",
                        Toasty.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


    fun checkPermission(): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED
            ) {
                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                requestPermissions(permissions, PERMISSION_CODE)
            } else {
                return true
            }
        } else {
            return true
        }
        return false
    }

    companion object {
        private val IMAGE_CHOOSE = 1000;
        private val PERMISSION_CODE = 1001;
    }


}

