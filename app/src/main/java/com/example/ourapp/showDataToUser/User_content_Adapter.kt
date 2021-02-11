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
import androidx.core.content.ContextCompat.getSystemService
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
import kotlin.collections.ArrayList

class User_content_Adapter(
    var context: Context?,

    var contentList_show: ArrayList<UserUploadContent>,
    var containerList_fav: ArrayList<UserUploadContent>,
    var users_data_favourate_hashMap: HashMap<String, Boolean>,
    var users_data_show_hashMap: HashMap<String, Boolean>
):
        RecyclerView.Adapter<User_content_Adapter.ViewHolder>() {
    var myFavRef = FirebaseDatabase.getInstance().getReference("UserFavourateContent")
    var myDeletePostRef = FirebaseDatabase.getInstance().getReference("DeletedPostByUsers")
    var myAuth = FirebaseAuth.getInstance()
    val user = myAuth.currentUser
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.show_users_the_content, parent, false)
        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var user_content = contentList_show.get(position)
        holder.description_view.text = user_content.pdf_des
        holder.user_name_view.text = user_content.user_email
        holder.user_uploaded_date_view.text = user_content.date
        var st = RetrievePDFStream()
        var stream = st.execute(user_content.pdf_url).get()

        holder.uploaded_pdf_show_view.fromStream(stream).defaultPage(0)
                .enableSwipe(true) // allows to block changing pages using swipe
                .swipeHorizontal(true)
                .enableDoubletap(true).load()

        //pdf name is
        var  pdf_name_array = user_content.pdf_url.split("/")
        var pdf_name_array2 = pdf_name_array[pdf_name_array.size-1].split("?alt")[0].split("%2F")
        var  pdf_name = pdf_name_array2[pdf_name_array2.size-1]


        holder.favourate_add_button.setOnClickListener(View.OnClickListener {
            //var myshowRef = FirebaseDatabase.getInstance().getReference("UsersImportantMaterials")
            //myshowRef.child(user_content.user_id_unique).removeValue()
            contentList_show.remove(user_content)

            val unique_token = user_content.user_id_unique
            notifyDataSetChanged()
                myFavRef.child(user!!.uid).child(user_content.user_id_unique).setValue(user_content)
                containerList_fav.add(user_content)
                notifyDataSetChanged()
                users_data_favourate_hashMap.put(unique_token,true)
                context?.let { it1 -> Toasty.success(it1,"Successfully added to your FavourateList",Toasty.LENGTH_SHORT).show() }


        })

        holder.delete_user_content.setOnClickListener(View.OnClickListener {

            users_data_show_hashMap.remove(user_content.user_id_unique)
            contentList_show.remove(user_content)
            notifyDataSetChanged()
            if (user != null) {
                myDeletePostRef.child(user.uid).child(user_content.user_id_unique).setValue(user_content.user_id_unique)
                context?.let { it1 -> Toasty.warning(it1,"Deleted SuccessFully ",Toasty.LENGTH_SHORT).show() }
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


    @RequiresApi(Build.VERSION_CODES.O)
    private fun findTokenForUser(): String {
        val current = LocalDateTime.now()

        val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        return current.format(formatter)
    }



    override fun getItemCount(): Int {
        return contentList_show.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //four variables
        var user_name_view = itemView.findViewById<TextView>(R.id.uploaded_user_name)
        var user_uploaded_date_view = itemView.findViewById<TextView>(R.id.uploaded_date)
        var description_view = itemView.findViewById<TextView>(R.id.upload_content_description)
        var uploaded_pdf_show_view = itemView.findViewById<PDFView>(R.id.user_uploaede_pdf)
        var favourate_add_button = itemView.findViewById<ImageButton>(R.id.add_toFavourate)
        var delete_user_content = itemView.findViewById<ImageButton>(R.id.delete_show_content)
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
