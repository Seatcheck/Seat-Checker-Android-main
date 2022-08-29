package app.rubbickcube.seatcheck.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.ImageView
import android.widget.Toast
import app.rubbickcube.seatcheck.Helpers.HawkUtils
import app.rubbickcube.seatcheck.Helpers.Utils
import app.rubbickcube.seatcheck.R
import app.rubbickcube.seatcheck.model.Users
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.files.BackendlessFile
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.features.ReturnMode
import com.kaopiz.kprogresshud.KProgressHUD
import com.orhanobut.hawk.Hawk
import com.pixplicity.easyprefs.library.Prefs
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.app_bar_layout.*
import kotlinx.android.synthetic.main.profile_edit_dialog.*
import java.io.File
import java.util.*

class EditProfileActivity : AppCompatActivity() {
    var pd: KProgressHUD? = null
    var profileURL: String? = ""
    var backendlessUser: BackendlessUser = BackendlessUser()


    val options = RequestOptions()
            .centerCrop()
            .placeholder(R.drawable.avatar)
            .error(R.drawable.avatar)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .priority(Priority.HIGH)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        supportActionBar?.hide()
        Hawk.init(this).build()
        if (Utils.getBackendLessUser(this) != null) {
            backendlessUser = Utils.getBackendLessUser(this)
        } else {
            return
        }
        setupComponents()
        btn_profile_update?.setOnClickListener {
            updateUserProfile(et_profile_name?.text.toString(), et_profile_displayname?.text.toString(), et_profile_bio?.text.toString(), et_profile_status?.text.toString(), et_profile_phone?.text.toString(), profileURL.toString())
        }

        findViewById<ImageView>(R.id.appbar_back).setOnClickListener {
            finish()
        }
    }


    private fun setupComponents() {
        showEditDialog()
    }

    private fun showEditDialog() {


        et_profile_email?.setText(backendlessUser.email)

        et_profile_displayname?.setText(backendlessUser.properties["name"].toString())

        if (backendlessUser.properties["firstName"] != null) {
            et_profile_name?.setText(backendlessUser.properties["firstName"].toString())
        }
        if (backendlessUser.properties["status"] != null) {
            et_profile_status?.setText(backendlessUser.properties["status"].toString())
        }
        if (backendlessUser.properties["about"] != null) {
            et_profile_bio?.setText(backendlessUser.properties["about"].toString())
        }
        if (backendlessUser.properties["phone"] != null) {
            et_profile_phone?.setText(backendlessUser.properties["phone"].toString())
        }

        Glide.with(this@EditProfileActivity).load(backendlessUser.properties["profileImage"].toString()).apply(options).into(img_profile_avatar!!)

        findViewById<ImageView>(R.id.btn_profile_change)?.setOnClickListener {


            ImagePicker.create(this@EditProfileActivity)
                    .returnMode(ReturnMode.ALL) // set whether pick and / or camera action should return immediate result or not.
                    .folderMode(true) // folder mode (false by default)
                    .toolbarFolderTitle("Select Image") // folder selection title
                    .toolbarImageTitle("Tap to select") // image selection title
                    .toolbarArrowColor(Color.WHITE) // Toolbar 'up' arrow color
                    .single() // single mode
                    .theme(R.style.ImagePickerTheme)
                    .limit(1) // max images can be selected (99 by default)
                    .showCamera(true) // show camera or not (true by default)
                    .imageDirectory("Camera") // directory name for captured image  ("Camera" folder by default)
                    .start(); // start image p


        }


    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            // Get a list of picked images
            val images = ImagePicker.getImages(data) as List<Image>
            // or get a single image only
            val image = ImagePicker.getFirstImageOrNull(data)


            Glide.with(this@EditProfileActivity).load(image.path).apply(options).into(img_profile_avatar!!)
//
//

            val compressedImage = Compressor(this)
                    .setMaxWidth(640)
                    .setMaxHeight(480)
                    .setQuality(50)
                    .setCompressFormat(Bitmap.CompressFormat.WEBP)
                    .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES).getAbsolutePath())
                    .compressToBitmap(File(image.path))


            //  val mBitmap = Compressor(this@ProfileActivity).compressToBitmap(File(image.path))
//                 val original =   BitmapFactory.decodeFile(image.path)
//                 val out =  ByteArrayOutputStream()
//                original.compress(Bitmap.CompressFormat.PNG, 40, out);
//                val decoded = BitmapFactory.decodeStream( ByteArrayInputStream(out.toByteArray()));

//
//               // val convertedImagePath = ImageUtils.decodeFile(File(imageUri.f).path,500,500)
//
//          //       val bitmap = BitmapFactory.decodeFile(convertedImagePath)
            uploadImage(compressedImage)

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    fun uploadImage(bitmap: Bitmap) {


        pd = Utils.SCProgressDialog(this@EditProfileActivity, null, "Uploading profile image..")
        pd?.show()
        Backendless.Files.Android.upload(bitmap, Bitmap.CompressFormat.PNG,
                40,
                UUID.randomUUID().toString() + ".png",
                "mypics", object : AsyncCallback<BackendlessFile> {
            override fun handleFault(fault: BackendlessFault?) {
                Toast.makeText(this@EditProfileActivity, fault?.message, Toast.LENGTH_SHORT).show()
                dismissProgressDialog()
            }

            override fun handleResponse(response: BackendlessFile?) {

                dismissProgressDialog()
                profileURL = response?.fileURL
            }

        })

    }


    fun updateUserProfile(name: String, displayName: String, bio: String, status: String, phone: String, profileImage: String) {


        val user = Users()
        user.objectId = backendlessUser.objectId
        user.fcmToken = Prefs.getString("fcmToken", "")
        user.email = backendlessUser.email
//        if(AppClass.lat != null && AppClass.lng != null) {
//            user.lat = AppClass.lat.toString()
//            user.lng = AppClass.lng.toString()
//
//        }

        if (displayName.isNullOrEmpty()) {
            Toast.makeText(this@EditProfileActivity, "Display name is required...", Toast.LENGTH_SHORT).show()
            return
        } else {
            user.name = displayName
        }
        if (!name.isNullOrEmpty()) {
            user.firstName = name
        }
        if (!status.isNullOrEmpty()) {
            user.status = status
        }
        if (!phone.isNullOrEmpty()) {
            user.phone = phone
        }
        if (!profileImage.isNullOrEmpty()) {
            user.profileImage = profileImage
        } else {
            user.profileImage = backendlessUser.properties["profileImage"].toString()
        }
        if (!bio.isNullOrEmpty()) {
            user.about = bio
        }


        pd = Utils.SCProgressDialog(this@EditProfileActivity, null, "Updating Profile...")
        pd?.show()

        Backendless.Data.of(Users::class.java).save(user, object : AsyncCallback<Users> {
            override fun handleFault(fault: BackendlessFault?) {
                dismissProgressDialog()
                Toast.makeText(this@EditProfileActivity, fault?.message, Toast.LENGTH_SHORT).show()

            }

            override fun handleResponse(response: Users?) {

                dismissProgressDialog()
                grantCurrentUser()
                /*Prefs.putString("profileName",response?.name)
                Prefs.putString("about",response?.about)
                Prefs.putString("status",response?.status)
                Prefs.putString("phone",response?.phone)
                Prefs.putString("displayName",response?.firstName)
                Prefs.putString("profileImage",response?.profileImage)
               */
                Prefs.putBoolean("updateprofile",true)
                Toast.makeText(this@EditProfileActivity, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show()
//                val intent = Intent(this@EditProfileActivity, NeedASeatActivity::class.java)
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
//                startActivity(intent)
//                finish() // call this to finish the current activity

            }


        })
    }

    private fun grantCurrentUser() {

        Backendless.UserService.findById(Prefs.getString("userId", ""), object : AsyncCallback<BackendlessUser> {
            override fun handleFault(fault: BackendlessFault?) {
                //Toast.makeText(this@SplashActivity,"Something went wrong.."+fault?.message,Toast.LENGTH_LONG).show()
            }

            override fun handleResponse(response: BackendlessUser?) {
                Backendless.UserService.setCurrentUser(response)
                HawkUtils.putHawk("BackendlessUser", response)
                //Utils.startActivityWithAnimation(this@SplashActivity,MainActivity::class.java,findViewById(R.id.vview),true,R.color.app_orange)
            }

        })

    }

    private fun dismissProgressDialog() {
        if (pd != null && pd?.isShowing()!!) {
            pd?.dismiss()
        }
    }
}