package com.example.ourapp.HomePageUser

import android.app.Dialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.ourapp.AccountRelated.MainActivity
import com.example.ourapp.R
import com.example.ourapp.UploadDataToFireBase.ContentUploaderClass
import com.example.ourapp.showDataToUser.UserUploadContent
import com.example.ourapp.showDataToUser.User_Fav_Content_Adap
import com.example.ourapp.showDataToUser.User_content_Adapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Exception
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class UserHome : AppCompatActivity() ,BottomNavigationView.OnNavigationItemSelectedListener,View.OnClickListener{
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance();
    private var user = mAuth.currentUser
    var containerList_show=ArrayList<UserUploadContent>()
    var containerList_fav = ArrayList<UserUploadContent>()
    var myDeletePostRef = FirebaseDatabase.getInstance().getReference("DeletedPostByUsers")
    private var myRef = FirebaseDatabase.getInstance().getReference("UsersImportantMaterials")
    private var users_data_show_hashMap:HashMap<String,Boolean> = HashMap()
    private var users_data_favourate_hashMap:HashMap<String,Boolean> = HashMap()
    private var user_deleted_post:HashMap<String,Boolean> = HashMap()

    lateinit var tap_to_upload:Button;
    var containerAdapter_show: RecyclerView.Adapter<User_content_Adapter.ViewHolder>? =null;
    var containerAdapter_fav: RecyclerView.Adapter<User_Fav_Content_Adap.ViewHolder>? =null;

    var fragment: Fragment = home_fragment()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_home)
        tap_to_upload = findViewById(R.id.tab_for_upload)
        loadDeletedData()
        loadFavData()
        var bnv: BottomNavigationView = findViewById(R.id.bottom_navigation_bar)
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment)
            .commit()

        bnv.setOnNavigationItemSelectedListener(this)
        tap_to_upload.setOnClickListener(this)
    }



    override fun onNavigationItemSelected(it: MenuItem): Boolean {
        when(it.itemId){
            R.id.home_navigation -> {
                fragment = home_fragment()

            }
            R.id.users_content->{
                loadShowData()

                containerAdapter_show = User_content_Adapter(this, containerList_show,containerList_fav,users_data_favourate_hashMap,users_data_show_hashMap)
                fragment = fragment_containt_avialble_for_user(containerList_show,containerAdapter_show)
            }
            R.id.favourate_content -> {
                containerAdapter_fav = User_Fav_Content_Adap(this, containerList_show, containerList_fav, users_data_favourate_hashMap,users_data_show_hashMap,containerAdapter_show)
                fragment = favourate_content_fragment(containerList_fav,containerAdapter_fav)
            }
        }

        supportFragmentManager.beginTransaction().replace(R.id.fragment_container,fragment).commit()
        return super.onOptionsItemSelected(it)
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when(v.id){
                R.id.tab_for_upload->{
                    val intent = Intent(this,ContentUploaderClass::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }



     private fun uploadFile(view: View?) {
        var dialog = Dialog(this)
        dialog.setContentView(R.layout.upload_file_dialog)
        var dialog_yes_button = dialog.findViewById<Button>(R.id.yesUpload)
        var dialog_no_button = dialog.findViewById<Button>(R.id.noUpload)
        dialog_yes_button.setOnClickListener(View.OnClickListener {

            var intent = Intent(this, ContentUploaderClass::class.java)

            startActivity(intent)
            finish()
            dialog.dismiss()
        })
        dialog_no_button.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
        })

        dialog.show()
    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.user__login__page, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.signout -> {
                mAuth.signOut();
                var intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }



        }
        return super.onOptionsItemSelected(item);
    }


    private fun loadDeletedData() {
        myDeletePostRef.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                try{
                    var td = snapshot!!.value as HashMap<String, Any>
                    var user_deleted = td[user?.uid] as HashMap<String,String>
                    for(k in user_deleted.keys){
                        user_deleted_post.put(k,true)
                    }
                    Log.i("info","user deleted "+user_deleted.toString())
                }catch (ex:Exception){
                    //this is it.
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


        })
    }
    fun loadShowData(){

        myRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    var td = snapshot!!.value as HashMap<String, Any>

                    for (key in td.keys) {
                        var userInfo = td[key] as HashMap<String, String>
                        var unique_user_id = userInfo["user_id_unique"]

                        if (!users_data_show_hashMap.containsKey(unique_user_id) && !users_data_favourate_hashMap.contains(unique_user_id) && !user_deleted_post.containsKey(unique_user_id)) {

                            var user_email = userInfo["user_email"]

                            var pdf_url = userInfo["pdf_url"]
                            var pdf_des = userInfo["pdf_des"]
                            var date = userInfo["date"]
                            containerList_show.add(UserUploadContent(unique_user_id!!,user_email!!, pdf_url!!, pdf_des!!, date!!))
                            users_data_show_hashMap.put(unique_user_id, true)
                        }
                    }


                }catch (ex: Exception){
                    //catch it
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


        })
        containerAdapter_show?.notifyDataSetChanged()

    }


    fun loadFavData(){
        var myFavRef = FirebaseDatabase.getInstance().getReference("UserFavourateContent")
        myFavRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    var td = snapshot!!.value as HashMap<String, Any>
                    var user_data = td[user?.uid] as HashMap<String,Any>


                    for (key in user_data.keys) {
                        var userInfo = user_data[key] as HashMap<String, String>
                        var user_unique_id = userInfo["user_id_unique"]
                        if (!user_deleted_post.containsKey(user_unique_id)) {
                            if (!users_data_favourate_hashMap.containsKey(user_unique_id)) {
                                var user_email = userInfo["user_email"]

                                var pdf_url = userInfo["pdf_url"]

                                var pdf_des = userInfo["pdf_des"]
                                var date = userInfo["date"]
                                containerList_fav.add(UserUploadContent(user_unique_id!!, user_email!!, pdf_url!!, pdf_des!!, date!!))
                                users_data_favourate_hashMap[user_unique_id] = true
                            }
                        }
                    }


                }catch (ex: Exception){
                    //catch it
                    Log.i("info","error Occured !!")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


        })
        containerAdapter_fav?.notifyDataSetChanged()

    }
}