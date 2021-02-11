package com.example.ourapp.HomePageUser

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.ourapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mikhaellopez.circularimageview.CircularImageView
import com.squareup.picasso.Picasso
import es.dmoral.toasty.Toasty

class home_fragment: Fragment() {
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance();
    private var user = mAuth.currentUser
    private var myRef = FirebaseDatabase.getInstance().getReference("UserProfile")
    lateinit var image_profile: CircularImageView;

    lateinit var username: TextView;
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        var view = layoutInflater.inflate(R.layout.fragment_home, container, false)

        //here we have to write all the stuff
        image_profile = view.findViewById<CircularImageView>(R.id.userProfileImage);
        username = view.findViewById<TextView>(R.id.username)
        setUpProfile()


        return view
    }
    //this is used for upload file in the firebase

    fun setUpProfile() {

        myRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                try {
                    var td = snapshot!!.value as HashMap<String, Any>
                    var userInfo = td[user!!.uid] as HashMap<String, String>
                    var user_email = userInfo["email"]
                    var user_name = userInfo["fname"] + " " + userInfo["lname"]
                    var user_image = userInfo["image"]
                    image_profile.background = null
                    Picasso.get().load(user_image).into(image_profile)
                    username.text = user_name


                } catch (ex: Exception) {
                    context?.let { Toasty.normal(it, "this is called", Toasty.LENGTH_SHORT).show() }

                    Log.e("error", ex.message.toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                        context,
                        "UploadYour Data Error occured !!!",
                        Toast.LENGTH_SHORT
                ).show()
            }

        })
    }
}