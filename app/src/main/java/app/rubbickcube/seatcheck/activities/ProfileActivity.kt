package app.rubbickcube.seatcheck.activities

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import app.rubbickcube.seatcheck.R
import app.rubbickcube.seatcheck.adapters.SimpleFragmentPagerAdapter
import com.bumptech.glide.Glide
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.app_bar_layout.*
import kotlinx.android.synthetic.main.nav_panel_layout.*
import kotlinx.android.synthetic.main.profile_edit_dialog.view.*
import app.rubbickcube.seatcheck.R.id.imageView
import androidx.annotation.NonNull
import com.facebook.share.internal.ShareInternalUtility.handleActivityResult
import android.content.Intent
import android.graphics.Bitmap
import app.rubbickcube.seatcheck.Helpers.Utils
import app.rubbickcube.seatcheck.MainActivity
import app.rubbickcube.seatcheck.model.Users
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.files.BackendlessFile
import java.util.*
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.Image
import android.net.Uri
import android.os.Environment
import android.widget.ImageView
import app.rubbickcube.seatcheck.AppClass
import app.rubbickcube.seatcheck.Helpers.HawkUtils
import app.rubbickcube.seatcheck.ImageUtils
import com.backendless.BackendlessUser
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.features.ReturnMode
import com.kaopiz.kprogresshud.KProgressHUD
import com.orhanobut.hawk.Hawk
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.activity_other_user_profile.*
import kotlinx.android.synthetic.main.activity_profile.bg_layer_image
import kotlinx.android.synthetic.main.activity_profile.dp_image_profile
import kotlinx.android.synthetic.main.activity_profile.dp_name_profile
import kotlinx.android.synthetic.main.activity_profile.sliding_tabs
import kotlinx.android.synthetic.main.activity_profile.viewpager
import kotlinx.android.synthetic.main.fragment_about.view.*
import kotlinx.android.synthetic.main.nd_profile_activity.*
import kotlinx.android.synthetic.main.profile_edit_dialog.*
import java.io.*
import javax.inject.Inject


class ProfileActivity : AppCompatActivity() {



    val options =  RequestOptions()
            .centerCrop()
            .placeholder(R.drawable.avatar)
            .error(R.drawable.avatar)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .priority(Priority.HIGH)



    var  profileURL : String? = ""
    var pd: KProgressHUD? = null
    var backendlessUser : BackendlessUser = BackendlessUser()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
      //  AppClass.appComponent?.inject(this)
        Hawk.init(this).build()
        if(Utils.getBackendLessUser(this) != null) {
            backendlessUser = Utils.getBackendLessUser(this)
        }else {
            return
        }
        findViewById<ImageView>(R.id.nd_backbtn).setOnClickListener {
            finish()
        }

        supportActionBar?.hide()
        setupComponents()
        setupListener()
    }







    private fun setupComponents() {

        setHeader()
        appbar_title.text = ""
        nd_chat_icon.visibility = View.INVISIBLE
        nd_edit_profile.setOnClickListener {
          //  showEditDialog()
            val intent = Intent(this@ProfileActivity, EditProfileActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }



        viewpager.adapter = SimpleFragmentPagerAdapter(this,supportFragmentManager)
        sliding_tabs.setupWithViewPager(viewpager)



    }

    private fun setupListener() {

        appbar_back.setOnClickListener { finish() }
    }

    private lateinit var imagePicker: ImagePicker

    private var dialog_layout: View? = null

    private fun showEditDialog() {

        val builder = AlertDialog.Builder(this@ProfileActivity)
        val inflater = LayoutInflater.from(applicationContext)

         dialog_layout = inflater.inflate(R.layout.profile_edit_dialog, null)
         builder.setView(dialog_layout)
        val dialog = builder.create()




        dialog_layout?.et_profile_email?.setText(backendlessUser.email)

        dialog_layout?.et_profile_displayname?.setText(backendlessUser.properties["name"].toString())

        if(backendlessUser.properties["firstName"] != null) {
            dialog_layout?.et_profile_name?.setText(backendlessUser.properties["firstName"].toString())
        }
        if(backendlessUser.properties["status"] != null) {
            dialog_layout?.et_profile_status?.setText(backendlessUser.properties["status"].toString())
        }
        if(backendlessUser.properties["about"] != null) {
            dialog_layout?.et_profile_bio?.setText(backendlessUser.properties["about"].toString())
        }
         if(backendlessUser.properties["phone"] != null) {
             dialog_layout?.et_profile_phone?.setText(backendlessUser.properties["phone"].toString())
        }

            Glide.with(this@ProfileActivity).load(backendlessUser.properties["profileImage"].toString()).apply(options).into(dialog_layout?.img_profile_avatar!!)

        dialog_layout?.btn_close_btn_orange?.setOnClickListener {
            dialog.dismiss()
        }

        dialog_layout?.btn_profile_change?.setOnClickListener {




      ImagePicker.create(this@ProfileActivity)
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

//             imagePicker = ImagePicker(this@ProfileActivity /*activity non null*/,
//                    null /*fragment nullable*/) { /*on image picked*/
//
//                 imageUri -> dialog_layout.img_profile_avatar.setImageURI(imageUri)
//
//
//
//                 val original =   BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
//                 val out =  ByteArrayOutputStream()
//                original.compress(Bitmap.CompressFormat.PNG, 40, out);
//                val decoded = BitmapFactory.decodeStream( ByteArrayInputStream(out.toByteArray()));
//
//
//               // val convertedImagePath = ImageUtils.decodeFile(File(imageUri.f).path,500,500)
//
//          //       val bitmap = BitmapFactory.decodeFile(convertedImagePath)
//                 uploadImage(decoded)
//            }
//
//            imagePicker.choosePicture(false)

        }

        dialog_layout?.btn_profile_update?.setOnClickListener {

            updateUserProfile(dialog_layout?.et_profile_name?.text.toString(),dialog_layout?.et_profile_displayname?.text.toString(),dialog_layout?.et_profile_bio?.text.toString(),dialog_layout?.et_profile_status?.text.toString(),dialog_layout?.et_profile_phone?.text.toString(),profileURL.toString())
        }
        dialog.getWindow()!!.setBackgroundDrawableResource(R.drawable.rounded_corner)

        dialog.show()


    }

    private fun setHeader() {


            Glide.with(this@ProfileActivity).load(backendlessUser.properties["profileImage"]).apply(options).into(dp_image_profile)
            Glide.with(this@ProfileActivity).load(backendlessUser.properties["profileImage"]).apply(options).into(bg_layer_image)

        seatcheck_other_user_name.setText(backendlessUser.properties["name"].toString())
        Glide.with(this@ProfileActivity).load(backendlessUser.properties["profileImage"].toString()).apply(options).into(seatcheck_live_user_dp!!)

        tv_status_profile.setText(backendlessUser.properties["status"].toString())
        tv_about_profile.setText(backendlessUser.properties["about"].toString())
        tv_email_profile.setText(backendlessUser.properties["email"].toString())
        tv_phone_profile.setText(backendlessUser.properties["phone"].toString())

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            // Get a list of picked images
            val images = ImagePicker.getImages(data) as List<Image>
            // or get a single image only
            val image = ImagePicker.getFirstImageOrNull(data)

            Glide.with(this@ProfileActivity).load(image.path).apply(options).into(dialog_layout?.img_profile_avatar!!)

            val compressedImage =  Compressor(this)
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

//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        imagePicker.handlePermission(requestCode, grantResults)
//    }


    fun uploadImage(bitmap: Bitmap) {



         pd = Utils.SCProgressDialog(this@ProfileActivity,null,"Uploading profile image..")
         pd?.show()
        Backendless.Files.Android.upload(bitmap, Bitmap.CompressFormat.PNG,
                40,
                UUID.randomUUID().toString()+".png",
                "mypics",object : AsyncCallback<BackendlessFile> {
            override fun handleFault(fault: BackendlessFault?) {
                Toast.makeText(this@ProfileActivity,fault?.message,Toast.LENGTH_SHORT).show()
              dismissProgressDialog()
            }

            override fun handleResponse(response: BackendlessFile?) {

                dismissProgressDialog()
               profileURL = response?.fileURL
            }

        })

    }

    fun updateUserProfile(name : String, displayName : String, bio : String, status : String, phone : String, profileImage : String) {


        val user = Users()
        user.objectId = backendlessUser.objectId
        user.fcmToken = Prefs.getString("fcmToken","")
        user.email = backendlessUser.email
//        if(AppClass.lat != null && AppClass.lng != null) {
//            user.lat = AppClass.lat.toString()
//            user.lng = AppClass.lng.toString()
//
//        }

        if(displayName.isNullOrEmpty()) {
            Toast.makeText(this@ProfileActivity,"Display name is required...",Toast.LENGTH_SHORT).show()
            return
        }else {
            user.name = displayName
        }
        if(!name.isNullOrEmpty()) {
            user.firstName = name
        }
        if(!status.isNullOrEmpty()) {
            user.status = status
        }
        if(!phone.isNullOrEmpty()) {
            user.phone = phone
        }
        if(!profileImage.isNullOrEmpty()) {
            user.profileImage = profileImage
        }else {
            user.profileImage = backendlessUser.properties["profileImage"].toString()
        }
        if(!bio.isNullOrEmpty()) {
            user.about = bio
        }


             pd = Utils.SCProgressDialog(this@ProfileActivity,null,"Updating Profile...")
             pd?.show()

            Backendless.Data.of(Users::class.java).save(user,object : AsyncCallback<Users> {
                override fun handleFault(fault: BackendlessFault?) {
                    dismissProgressDialog()
                    Toast.makeText(this@ProfileActivity,fault?.message,Toast.LENGTH_SHORT).show()

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
                    Toast.makeText(this@ProfileActivity,"Profile Updated Successfully!",Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@ProfileActivity, NeedASeatActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish() // call this to finish the current activity

                }


            })
        }


    private fun grantCurrentUser() {

        Backendless.UserService.findById(Prefs.getString("userId",""), object : AsyncCallback<BackendlessUser> {
            override fun handleFault(fault: BackendlessFault?) {
                //Toast.makeText(this@SplashActivity,"Something went wrong.."+fault?.message,Toast.LENGTH_LONG).show()
            }

            override fun handleResponse(response: BackendlessUser?) {

                Backendless.UserService.setCurrentUser( response )


                HawkUtils.putHawk("BackendlessUser",response)

                //Utils.startActivityWithAnimation(this@SplashActivity,MainActivity::class.java,findViewById(R.id.vview),true,R.color.app_orange)
            }

        })

    }


    override fun onResume() {
        super.onResume()

        if(Prefs.getBoolean("updateprofile",false)){
            Prefs.putBoolean("updateprofile",false)
            finish();
            startActivity(getIntent());
        }
        if(Prefs.getInt("rating",0) > 0) {
            ratingBar_rating_stars_profile.visibility = View.VISIBLE
            ratingBar_rating_stars_profile.rating = Prefs.getInt("rating",0).toFloat()
        }else {
            ratingBar_rating_stars_profile.visibility = View.GONE
        }
    }

    private fun dismissProgressDialog() {
        if (pd != null && pd?.isShowing()!!) {
            pd?.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AppClass.reviewForOwner = true
    }


}



