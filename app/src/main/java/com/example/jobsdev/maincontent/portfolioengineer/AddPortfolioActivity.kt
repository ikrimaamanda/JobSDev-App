package com.example.jobsdev.maincontent.portfolioengineer

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.loader.content.CursorLoader
import com.example.jobsdev.R
import com.example.jobsdev.databinding.ActivityAddPortfolioBinding
import com.example.jobsdev.maincontent.MainContentActivity
import com.example.jobsdev.maincontent.hireengineer.HireApiService
import com.example.jobsdev.maincontent.hireengineer.HireResponse
import com.example.jobsdev.maincontent.projectcompany.AddNewProjectActivity
import com.example.jobsdev.remote.ApiClient
import com.example.jobsdev.retfrofit.GeneralResponse
import com.example.jobsdev.retfrofit.JobSDevApiService
import com.example.jobsdev.sharedpreference.*
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class AddPortfolioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPortfolioBinding
    private lateinit var sharedPref : PreferencesHelper
    private lateinit var coroutineScope: CoroutineScope
    private lateinit var service : JobSDevApiService
    val typeApp = arrayOf("mobile app", "web app")

    companion object {
        private const val IMAGE_PICK_CODE = 1000
        private const val PERMISSION_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_portfolio)
        sharedPref = PreferencesHelper(this)

        coroutineScope = CoroutineScope(Job() + Dispatchers.Main)
        service = ApiClient.getApiClient(context = this)!!.create(JobSDevApiService::class.java)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        configSpinnerTypeApp()

        binding.ivUploadPortfolioImage.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                    requestPermissions(permissions, PERMISSION_CODE)
                } else {
                    pickImageFromGalery()
                }
            } else {
                pickImageFromGalery()
            }
        }

        binding.btnCancel.setOnClickListener {
            onBackPressed()
        }

    }

    private fun pickImageFromGalery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            binding.ivUploadPortfolioImage.setImageURI(data?.data)

            val filePath = data?.data?.let { getPath(this, it) }
            val file = File(filePath)

            var img : MultipartBody.Part? = null
            val mediaTypeImg = "image/jpeg".toMediaType()
            val inputStream = data?.data?.let { contentResolver.openInputStream(it) }
            val reqFile : RequestBody? = inputStream?.readBytes()?.toRequestBody(mediaTypeImg)

            img = reqFile?.let { it1 ->
                MultipartBody.Part.createFormData("image", file.name, it1)
            }

            binding.btnAddPortfolio.setOnClickListener {
                if(binding.etAppName.text.isNullOrEmpty() || binding.etDescriptionPortfolio.text.isNullOrEmpty() || binding.etLinkPubPortfolio.text.isNullOrEmpty() || binding.etLinkRepoPortfolio.text.isNullOrEmpty() || binding.etWorkplacePortfolio.text.isNullOrEmpty()) {
                    showMessage("Please filled all field")
                    binding.etAppName.requestFocus()
                } else {
                    if (img != null) {
                        callAddPortfolioApi(img)
                    }
                }
            }

        }
    }

    private fun getPath(context : Context, contentUri : Uri) : String? {
        var result : String? = null
        val proj = arrayOf(MediaStore.Images.Media.DATA)

        val cursorLoader = CursorLoader(context, contentUri, proj, null, null, null)
        val cursor = cursorLoader.loadInBackground()

        if (cursor != null) {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            result = cursor.getString(columnIndex)
            cursor.close()
        }
        return result
    }

    private fun configSpinnerTypeApp() {
        binding.spinnerTypeApp.adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, typeApp)
        binding.spinnerTypeApp.onItemSelectedListener = object :AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Toast.makeText(this@AddPortfolioActivity, "None", Toast.LENGTH_SHORT).show()
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                Toast.makeText(this@AddPortfolioActivity, "${typeApp[position]} clicked", Toast.LENGTH_SHORT).show();
                sharedPref.putValue(ConstantPortfolio.typeApp, typeApp[position])
            }

        }
    }

    private fun callAddPortfolioApi(image : MultipartBody.Part) {
        coroutineScope.launch {
            val results = withContext(Dispatchers.IO){
                try {
                    val enId = sharedPref.getValueString(ConstantAccountEngineer.engineerId)!!.toRequestBody("text/plain".toMediaTypeOrNull())
                    val prAppName = binding.etAppName.text.toString()
                        .toRequestBody("text/plain".toMediaTypeOrNull())
                    val prDesc = binding.etDescriptionPortfolio.text.toString()
                        .toRequestBody("text/plain".toMediaTypeOrNull())
                    val prLinkPub = binding.etLinkPubPortfolio.text.toString()
                        .toRequestBody("text/plain".toMediaTypeOrNull())
                    val prLinkRepo = binding.etLinkRepoPortfolio.text.toString()
                        .toRequestBody("text/plain".toMediaTypeOrNull())
                    val prWorkplace = binding.etWorkplacePortfolio.text.toString()
                        .toRequestBody("text/plain".toMediaTypeOrNull())
                    val prType = sharedPref.getValueString(ConstantPortfolio.typeApp)!!
                        .toRequestBody("text/plain".toMediaTypeOrNull())

                    service.addPortfolio(prAppName, prDesc, prLinkPub, prLinkRepo, prWorkplace, prType, image, enId!!)
                } catch (e:Throwable) {
                    Log.e("errorM", e.message.toString())
                    e.printStackTrace()
                }
            }
            Log.d("addPortfolioReq", results.toString())

            if(results is GeneralResponse) {
                Log.d("addPortfolioReq", results.toString())

                if(results.success) {
                    showMessage(results.message)
                    moveActivity()
                } else {
                    showMessage(results.message)
                }
            }
            showMessage("Something wrong...")
        }
    }

    private fun showMessage(message : String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun moveActivity() {
        startActivity(Intent(this, MainContentActivity::class.java))
        finish()
    }
}