package com.reactive.premier.utils.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.markodevcic.peko.PermissionRequester
import com.markodevcic.peko.allGranted
import com.reactive.premier.R
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onCompletion
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

var exit = false
fun FragmentActivity.exitVariant() {
    if (exit) {
        finishAffinity()
    } else {
        Toast.makeText(this, this.getString(R.string.back_again), Toast.LENGTH_SHORT).show()
        exit = true
        Handler().postDelayed({ exit = false }, 2000)
    }
}

fun Activity.changeStatusBarColor(color: Int) {
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.statusBarColor = ContextCompat.getColor(this, color)

    WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
}

fun hideKeyboard(view: View?) {
    if (view != null) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

fun AppCompatActivity.makeFullScreen() {
    window.setFlags(
        WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN
    )
}


@SuppressLint("CheckResult")
fun Fragment.checkPermissions(vararg permissions: String, action: () -> Unit) {
    checkAppPermissions(this, this.requireContext(), permissions, action)
}

@SuppressLint("CheckResult")
fun AppCompatActivity.checkPermissions(vararg permissions: String, action: () -> Unit = {}) {
    checkAppPermissions(this, this, permissions, action)
}

private fun checkAppPermissions(
    lifecycleOwner: LifecycleOwner,
    context: Context,
    permissions: Array<out String>,
    action: () -> Unit
) {
    lifecycleOwner.lifecycleScope.launchWhenStarted {
        PermissionRequester.instance().request(*permissions)
            .onCompletion {
                val allGranted: Boolean =
                    PermissionRequester.instance().request(*permissions).allGranted()
                if (allGranted) {
                    action()
                } else {
                    toastLong(context, context.getString(R.string.givePermission))
                }
            }
            .distinctUntilChanged()
            .collect()

    }
}

fun Activity.getFileUri(file: File) = FileProvider.getUriForFile(
    this, "$packageName.provider", file
)!!

fun Activity.openFile(url: File) {
    try {
        val uri = getFileUri(url)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (url.toString().contains(".doc") || url.toString().contains(".docx")) {
            // Word document
            intent.setDataAndType(uri, "application/msword")
        } else if (url.toString().contains(".pdf")) {
            // PDF file
            intent.setDataAndType(uri, "application/pdf")
        } else if (url.toString().contains(".ppt") || url.toString().contains(".pptx")) {
            // Powerpoint file
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint")
        } else if (url.toString().contains(".xls") || url.toString().contains(".xlsx")) {
            // Excel file
            intent.setDataAndType(uri, "application/vnd.ms-excel")
        } else if (url.toString().contains(".zip")) {
            // ZIP file
            intent.setDataAndType(uri, "application/zip")
        } else if (url.toString().contains(".rar")) {
            // RAR file
            intent.setDataAndType(uri, "application/x-rar-compressed")
        } else if (url.toString().contains(".rtf")) {
            // RTF file
            intent.setDataAndType(uri, "application/rtf")
        } else if (url.toString().contains(".wav") || url.toString().contains(".mp3")) {
            // WAV audio file
            intent.setDataAndType(uri, "audio/x-wav")
        } else if (url.toString().contains(".gif")) {
            // GIF file
            intent.setDataAndType(uri, "image/gif")
        } else if (url.toString().contains(".jpg") || url.toString()
                .contains(".jpeg") || url.toString().contains(".png")
        ) {
            // JPG file
            intent.setDataAndType(uri, "image/jpeg")
        } else if (url.toString().contains(".txt")) {
            // Text file
            intent.setDataAndType(uri, "text/plain")
        } else if (url.toString().contains(".3gp") || url.toString().contains(".mpg") ||
            url.toString().contains(".mpeg") || url.toString()
                .contains(".mpe") || url.toString().contains(".mp4") || url.toString()
                .contains(".avi")
        ) {
            // Video files
            intent.setDataAndType(uri, "video/*")
        } else {
            intent.setDataAndType(uri, "*/*")
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        toast(this, "No application found which can open the file")
    }
}


fun Activity.getFilePath(it: Uri): String {
    //Later we will use this bitmap to create the File.
    val selectedBitmap = getBitmap(this, it)!!

    /*We can access getExternalFileDir() without asking any storage permission.*/
    val selectedImgFile = File(
        getExternalFilesDir(Environment.DIRECTORY_PICTURES),
        System.currentTimeMillis()
            .toString() + "_selectedImg.jpg"
    )

    convertBitmapToFile(selectedImgFile, selectedBitmap)

    return selectedImgFile.path
}

private fun getBitmap(context: Context, imageUri: Uri): Bitmap? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        ImageDecoder.decodeBitmap(
            ImageDecoder.createSource(
                context.contentResolver,
                imageUri
            )
        )

    } else {
        context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    }
}

private fun convertBitmapToFile(destinationFile: File, bitmap: Bitmap) {
    //create a file to write bitmap data
    destinationFile.createNewFile()
    //Convert bitmap to byte array
    val bos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bos)
    val bitmapData = bos.toByteArray()
    //write the bytes in file
    val fos = FileOutputStream(destinationFile)
    fos.write(bitmapData)
    fos.flush()
    fos.close()
}


fun createFileMultipart(name: String, file: File): MultipartBody.Part {
    return MultipartBody.Part.createFormData(
        name,
        file.name,
        file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
    )
}
