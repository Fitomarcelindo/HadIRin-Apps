package by.marcel.apps_lab.mainhome

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import by.marcel.apps_lab.R
import by.marcel.apps_lab.camera.CameraActivity
import by.marcel.apps_lab.databinding.ActivityMainBinding
import by.marcel.apps_lab.loginPage.LoginActivity
import by.marcel.apps_lab.pagResult.ResultActivity
import by.marcel.apps_lab.registerPage.RegisterActivity
import by.marcel.apps_lab.registerPage.User
import by.marcel.apps_lab.room.AppDatabase
import by.marcel.apps_lab.room.helper.rotateFile
import by.marcel.apps_lab.setting.ProfileActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var db: AppDatabase
    private var getFile: File? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var location: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "HadIRin-db")
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()

        setupBottomNavigation()
        showProgressBar()
        getUserName()

        if (!checkAllPermissionsGranted()) {
            requestPermissions(
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        addTextWatchers()

        binding.submit.setOnClickListener {
            showProgressBar()
            uploadAbsen()
            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            builder.setTitle("Success")
            builder.setMessage("Data uploaded successfully!")
            builder.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss() // Dismiss the dialog
                // After the dialog is dismissed, navigate to RegisterActivity
                val intent = Intent(this, ResultActivity::class.java)
                startActivity(intent)
                finish() // Close the current activity if needed
            }

            // Show the dialog
            val dialog = builder.create()
            hideProgressBar() // Hide progress bar once the upload is done
            dialog.show()
        }

        binding.takeBtn.setOnClickListener { startCamera() }

        binding.btnLoc.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                getCurrentLocation()
            } else {
                location = null
            }
        }

        binding.ivLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }


    private fun addTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val name = binding.edtMajor.text.toString().trim()
                val phone = binding.edtClass.text.toString().trim()

                binding.submit.isEnabled = name.isNotEmpty() && phone.isNotEmpty()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        binding.edtMajor.addTextChangedListener(textWatcher)
        binding.edtClass.addTextChangedListener(textWatcher)

    }

    private fun uploadAbsen() {
        showProgressBar()
        val user = auth.currentUser ?: return
        val uid = user.uid
        val major = binding.edtMajor.text.toString().trim()
        val kelas = binding.edtClass.text.toString().trim()

        val userData = mapOf(
            "Major" to major,
            "Class" to kelas,

            )

        firestore.collection("users").document(uid)
            .set(userData)
            .addOnSuccessListener {
                hideProgressBar()
                showSnackbar("Profile updated successfully")

            }
            .addOnFailureListener {
                hideProgressBar()
                showSnackbar("Failed to update profile")
            }
    }

    private fun checkAllPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun getCurrentLocation() {
        if (checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager. PERMISSION_GRANTED
        ) {
            fusedLocationClient. lastLocation. addOnSuccessListener { loc ->
                if (loc != null) {
                    location = loc
                } else {
                    binding.btnLoc.isChecked = false
                    Toast. makeText(
                        this,
                        resources. getString(R. string.cant_find_location),
                        Toast. LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getCurrentLocation()
            }
        }

    private fun getUserName() {
        val user = auth.currentUser ?: return
        val uid = user.uid

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val localUser = db.userDao().getUserById(uid)
                withContext(Dispatchers.Main) {
                    if (localUser != null) {
                        binding.popUpEmail.text = localUser.name
                        hideProgressBar()
                    } else {
                        firestore.collection("users").document(uid).get()
                            .addOnSuccessListener { document ->
                                hideProgressBar()
                                if (document != null && document.exists()) {
                                    val name = document.getString("name") ?: "Unknown User"
                                    binding.popUpEmail.text = name
                                    saveUserToLocalDb(uid, name)
                                } else {
                                    binding.popUpEmail.text = "Unknown User"
                                }
                            }
                            .addOnFailureListener {
                                hideProgressBar()
                                binding.popUpEmail.text = "Error fetching name"
                            }
                    }
                }
            }
        }
    }

    private fun saveUserToLocalDb(uid: String, name: String) {
        val user = User(uid, name)
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                db.userDao().insert(user)
            }
        }
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { dialog, id ->
                showProgressBar()
                navigateToLogin()
                signOut()
            }
            .setNegativeButton("No") { dialog, id ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }


    private fun signOut() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val credentialManager = CredentialManager.create(this@MainActivity)
                auth.signOut()
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            }
        }
    }
    private fun setupBottomNavigation() {
        binding.bottomNavigationView.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_home -> {
                    return@OnNavigationItemSelectedListener true
                }
                R.id.bottom_home -> {
                    val intent = Intent(this@MainActivity, MainActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.btm_attendance -> {
                    val intent = Intent(this@MainActivity, ResultActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.bottom_settings -> {
                    val intent = Intent(this@MainActivity, ProfileActivity::class.java)
                    startActivityForResult(intent, SETTINGS_REQUEST_CODE)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        })
    }

    private fun navigateToLogin() {
        showProgressBar()
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun startCamera() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERA_X_ResultAlert) {
            val filePath = it.data?.getStringExtra("picture")
            val isBackCamera = it.data?.getBooleanExtra("isBackCamera", true) ?: true

            filePath?.let { path ->
                // Ensure the path is valid and points to a file
                val file = File(path)
                if (file.exists()) {
                    getFile = file

                    // Show the image in the preview
                    binding.previewImageView.setImageBitmap(BitmapFactory.decodeFile(getFile?.path))

                    // You can now upload the file to Firebase or perform any other action
                    uploadImageToFirebaseStorage(getFile!!)
                } else {
                    Toast.makeText(this, "File not found at the given path", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                Toast.makeText(this, "No file path returned", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadImageToFirebaseStorage(file: File) {
        val user = auth.currentUser ?: return
        val uid = user.uid
        val storageRef = firebaseStorage.reference.child("images/$uid/${UUID.randomUUID()}.jpg")

        showProgressBar()

        val fileUri = Uri.fromFile(file)
        val uploadTask = storageRef.putFile(fileUri)

        uploadTask.addOnSuccessListener {
            // Get the URL of the uploaded image
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()

                // Save the image URL to Firestore
                saveImageUrlToFirestore(imageUrl)
            }
        }.addOnFailureListener {
            hideProgressBar()
            Toast.makeText(this, "Failed to upload image", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveImageUrlToFirestore(imageUrl: String) {
        val user = auth.currentUser ?: return
        val uid = user.uid

        val userData = mapOf(
            "imageUrl" to imageUrl
        )

        firestore.collection("users").document(uid)
            .update(userData)
            .addOnSuccessListener {
                hideProgressBar()
                Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                hideProgressBar()
                Toast.makeText(this, "Failed to save image URL", Toast.LENGTH_LONG).show()
            }
    }


    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SETTINGS_REQUEST_CODE && resultCode == RESULT_OK) {
            val updatedName = data?.getStringExtra("updatedName")
            if (!updatedName.isNullOrEmpty()) {
                binding.popUpEmail.text = updatedName
            }
        }
    }
    companion object {
        private const val SETTINGS_REQUEST_CODE = 1001
        const val CAMERA_X_ResultAlert = 200
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}