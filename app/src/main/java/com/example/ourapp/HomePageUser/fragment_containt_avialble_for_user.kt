package com.example.ourapp.HomePageUser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.ourapp.R
import com.example.ourapp.showDataToUser.UserUploadContent
import com.example.ourapp.showDataToUser.User_content_Adapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class fragment_containt_avialble_for_user(containerListHome: ArrayList<UserUploadContent>, var containerAdapter: RecyclerView.Adapter<User_content_Adapter.ViewHolder>?) : Fragment() {
    //this is for firebase
    private var myRef = FirebaseDatabase.getInstance().getReference("UsersImportantMaterials")
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val user = mAuth.currentUser

    //this is for this view
    lateinit var uploaded_fileshow_recycler: RecyclerView;
     var containerList=containerListHome

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //creat a view and return it
        var view = layoutInflater.inflate(R.layout.content_avilable_user,container,false)

        uploaded_fileshow_recycler = view.findViewById(R.id.show_uploaded_items_recycler_View)

        uploaded_fileshow_recycler.adapter = containerAdapter
        uploaded_fileshow_recycler.layoutManager = StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.VERTICAL)
        uploaded_fileshow_recycler.adapter?.notifyDataSetChanged()

        return view
    }





}