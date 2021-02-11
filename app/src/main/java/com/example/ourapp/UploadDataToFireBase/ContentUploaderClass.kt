package com.example.ourapp.UploadDataToFireBase

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.ourapp.HomePageUser.UserHome
import com.example.ourapp.R
import com.example.ourapp.showDataToUser.UserUploadContent
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import es.dmoral.toasty.Toasty
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class ContentUploaderClass : AppCompatActivity() {
    private var myRef = FirebaseDatabase.getInstance().getReference("UsersImportantMaterials")
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val user = mAuth.currentUser
    var FILE_SELECT_CODE = 0;
    lateinit var dialog_yes_button: Button;
    lateinit var dialog_no_button: Button;
    lateinit var file_path: PDFView;
    lateinit var description_pdf: EditText;
    lateinit var circular_view_file_not_found: ImageButton;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content_uploader_class)

    dialog_yes_button = findViewById(R.id.yesUpload)
    dialog_no_button = findViewById(R.id.noUpload)
    file_path = findViewById(R.id.path_file)
    circular_view_file_not_found = findViewById(R.id.unable_to_find_pdf_file)
    description_pdf = findViewById(R.id.upload_content_description)
    showFileChooser()
    circular_view_file_not_found.setOnClickListener {
        circular_view_file_not_found.visibility = View.INVISIBLE

        showFileChooser()

    }
    file_path.setOnClickListener {
        showFileChooser()
    }

}


private fun showFileChooser() {
    val intent = Intent(Intent.ACTION_GET_CONTENT)
    intent.type = "*/*"
    intent.addCategory(Intent.CATEGORY_OPENABLE)
    try {
        startActivityForResult(
            Intent.createChooser(intent, "Select a File to Upload"),
            FILE_SELECT_CODE
        )
    } catch (ex: ActivityNotFoundException) {
        // Potentially direct the user to the Market with a Dialog
        Toast.makeText(
            this, "Please install a File Manager.",
            Toast.LENGTH_SHORT
        ).show()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    when (requestCode) {
        FILE_SELECT_CODE -> if (resultCode === AppCompatActivity.RESULT_OK) {
            // Get the Uri of the selected file
            val uri: Uri? = data?.data
            if (uri!=null) {
                file_path.fromUri(uri).defaultPage(0)
                    .enableSwipe(true) // allows to block changing pages using swipe
                    .swipeHorizontal(false)
                    .enableDoubletap(true)
                    .onError {
                        circular_view_file_not_found.visibility = View.VISIBLE
                    }
                    .defaultPage(0).load()


                dialog_yes_button.setOnClickListener(View.OnClickListener {
                    val storage =
                        FirebaseStorage.getInstance()
                            .getReferenceFromUrl("gs://my-application-bc31e.appspot.com/")
                    Toasty.info(this, "Uploading ...", Toasty.LENGTH_SHORT).show()
                    val df = SimpleDateFormat("ddMMyyHHmmss")
                    val dataobj = Date()
                    val path = df.format(dataobj) + uri.lastPathSegment
                    val storageRef = storage.child("Doc/$path")
                    val upload = storageRef.putFile(uri)
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
                            val current = LocalDateTime.now()

                            val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
                            val formatted = current.format(formatter)

                            val getdiff_token = findTokenForUser()

                            var pdf_download_url = downloadUri.toString()
                            var username_find = getUserName(user?.email as String)
                            var model = UserUploadContent(
                                    user.uid+findTokenForUser(),
                                username_find,
                                pdf_download_url,
                                description_pdf.text.toString(),
                                formatted
                            )
                            myRef.child(user.uid+findTokenForUser()).setValue(model)


                        } else {
                            Toast.makeText(
                                this, "Not able to upload Pdf error occured !",
                                Toast.LENGTH_LONG
                            ).show()

                        }
                    }

                    var intent = Intent(this, UserHome::class.java)
                    startActivity(intent)
                    finish()
                })

                dialog_no_button.setOnClickListener(View.OnClickListener {
                    Toasty.warning(this, "going to home ...", Toasty.LENGTH_SHORT).show()
                    var intent = Intent(this, UserHome::class.java)
                    startActivity(intent)
                    finish()
                })

            }
        }
        else {

            Toasty.warning(this, "File not selected...", Toasty.LENGTH_SHORT).show()
                Toasty.success(this, "going back to home ...", Toasty.LENGTH_SHORT).show()
                var intent = Intent(this, UserHome::class.java)
                startActivity(intent)
                finish()

        }
    }
    super.onActivityResult(requestCode, resultCode, data)
}

    @RequiresApi(Build.VERSION_CODES.O)
    private fun findTokenForUser(): String {
        val current = LocalDateTime.now()

        val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        return current.format(formatter)
    }

    private fun getUserName(s: String): String {
    return s.split("@")[0]
}
}