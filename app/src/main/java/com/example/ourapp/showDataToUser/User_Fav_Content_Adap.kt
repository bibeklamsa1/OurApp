package com.example.ourapp.showDataToUser

import android.app.DownloadManager
import android.content.Context


import android.net.Uri


import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.ourapp.R
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import es.dmoral.toasty.Toasty
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class User_Fav_Content_Adap(
    var context: Context?,
    var content_show_list: ArrayList<UserUploadContent>,
    var containerList_fav: ArrayList<UserUploadContent>,
    var users_data_favourate_hashMap: HashMap<String, Boolean>,
    var users_data_show_hashMap: HashMap<String, Boolean>,
    var containerAdapter_show: RecyclerView.Adapter<User_content_Adapter.ViewHolder>?
):
        RecyclerView.Adapter<User_Fav_Content_Adap.ViewHolder>() {
    val WRITE_STORAGE_PERSSION_CODE=100
    var myDeletePostRef = FirebaseDatabase.getInstance().getReference("DeletedPostByUsers")
    var myFavRef = FirebaseDatabase.getInstance().getReference("UserFavourateContent")
    var myAuth = FirebaseAuth.getInstance()
    val user = myAuth.currentUser
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{
        var view = LayoutInflater.from(context).inflate(R.layout.show_fav_content_to_user, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var user_content =containerList_fav.get(position)
        holder.description_view.text = user_content.pdf_des
        holder.user_name_view.text = user_content.user_email
        holder.user_uploaded_date_view.text = user_content.date
        var st = RetrievePDFStream()
        var stream = st.execute(user_content.pdf_url).get()

        //pdf name is
        var  pdf_name_array = user_content.pdf_url.split("/")
        var pdf_name_array2 = pdf_name_array[pdf_name_array.size-1].split("?alt")[0].split("%2F")
        var  pdf_name = pdf_name_array2[pdf_name_array2.size-1]


        holder.uploaded_pdf_show_view.fromStream(stream).defaultPage(0)
                .enableSwipe(true) // allows to block changing pages using swipe
                .swipeHorizontal(true)
                .enableDoubletap(true).load()
        //do deletiion
        holder.unfavourate_button.setOnClickListener(View.OnClickListener {
            myFavRef.child(user!!.uid).child(user_content.user_id_unique).removeValue()
           containerList_fav.remove(user_content)
            users_data_show_hashMap.remove(user_content.user_id_unique)
            content_show_list.add(user_content)
            Toasty.error(context!!,"Removed from Favourate List !!!",Toasty.LENGTH_SHORT).show()
            notifyDataSetChanged()
            containerAdapter_show?.notifyDataSetChanged()

        })


        holder.delete_button.setOnClickListener(View.OnClickListener {
            myFavRef.child(user!!.uid).child(user_content.user_id_unique).removeValue()
            containerList_fav.remove(user_content)

            context?.let { it1 -> Toasty.warning(it1,"Deleted SuccessFully ",Toasty.LENGTH_SHORT).show() }
            notifyDataSetChanged()
            if (user != null) {
                myDeletePostRef.child(user.uid).child(user_content.user_id_unique).setValue(user_content.user_id_unique)
            }

        })

        holder.download_button.setOnClickListener(View.OnClickListener {
            goforDownnload(pdf_name,user_content.pdf_url)
        })
    }

    private fun goforDownnload(pdf_name: String, pdf_url: String) {
        val request = DownloadManager.Request(Uri.parse(pdf_url))
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
        request.setTitle(pdf_name)
        request.setDescription("The file is downloading ...")
        request.allowScanningByMediaScanner()
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"${System.currentTimeMillis()}")

        val manager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
    }


    override fun getItemCount(): Int {
        return containerList_fav.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //four variables
        var user_name_view = itemView.findViewById<TextView>(R.id.fav_user_name)
        var user_uploaded_date_view = itemView.findViewById<TextView>(R.id.fav_date)
        var description_view = itemView.findViewById<TextView>(R.id.fav_content_description)
        var uploaded_pdf_show_view = itemView.findViewById<PDFView>(R.id.fav_uploaede_pdf)
        var delete_button = itemView.findViewById<ImageButton>(R.id.delete_fav)
        var unfavourate_button = itemView.findViewById<ImageButton>(R.id.fav_unclicked_image)
        var download_button = itemView.findViewById<ImageButton>(R.id.download_button)
    }


    internal class RetrievePDFStream : AsyncTask<String?, Void?, InputStream?>() {
        override fun doInBackground(vararg params: String?): InputStream? {
            var inputStream: InputStream? = null
            try {
                val urlx = URL(params[0])
                val urlConnection: HttpURLConnection = urlx.openConnection() as HttpURLConnection
                if (urlConnection.getResponseCode() === 200) {
                    inputStream =  urlConnection.inputStream

                }
            } catch (e: IOException) {
                return null
            }
            return inputStream
        }

    }




}
