package com.example.kidsdrawingapp

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.databinding.DataBindingUtil
import com.example.kidsdrawingapp.databinding.ActivityMainBinding
import com.example.kidsdrawingapp.databinding.DialogBrushSizeBinding
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private val scope = CoroutineScope(Dispatchers.IO + CoroutineName("My Coroutine"))

    private lateinit var binding: ActivityMainBinding

    private var mImageButtonCurrentPaint: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)

        binding.drawingView.setSizeForBrush(20.toFloat())

        mImageButtonCurrentPaint = binding.llPaintColors[1] as ImageButton
        mImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.pallet_selected))

        binding.ibBrush.setOnClickListener {
            showBrushDialog()
        }

            binding.ibGallery.setOnClickListener{
                if(isReadStorageAllowed()){

                //Run our code to get the image from our gallery
                val pickPhotoIntent = Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

                    startActivityForResult(pickPhotoIntent,GALLERY)

                }else{
                    requestStoragePermission()
                }
            }


        binding.ibUndo.setOnClickListener{
           binding.drawingView.onClickUndo()
        }

        binding.ibSave.setOnClickListener{
            scope.launch {
                if(isReadStorageAllowed()){
                    bitmapCoroutines(getBitmapFromView(binding.flDvContainer))
                }else{
                    requestStoragePermission()
                }
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == GALLERY){
                try{
                    if(data!!.data != null){
                        binding.ivImage.visibility = View.VISIBLE
                        binding.ivImage.setImageURI(data.data)
                    }else{
                        Toast.makeText(this,
                                "Error in Parsing the image or its Corrupted.",
                                Toast.LENGTH_SHORT).show()
                    }
                }catch (e: Exception){
                    e.printStackTrace()
                }
            }
        }
    }

    private fun showBrushDialog()
    {
        val binding: DialogBrushSizeBinding = DialogBrushSizeBinding.inflate(layoutInflater)
        val brushDialog = Dialog(this)
        brushDialog.setContentView(binding.root)
        brushDialog.setTitle("Brush size: ")

        binding.ibSmallBrush.setOnClickListener {
            this.binding.drawingView.setSizeForBrush(10f)
            brushDialog.dismiss()
        }

        binding.ibMediumBrush.setOnClickListener {
            this.binding.drawingView.setSizeForBrush(20f)
            brushDialog.dismiss()
        }

        binding.ibLargeBrush.setOnClickListener {
            this.binding.drawingView.setSizeForBrush(30f)
            brushDialog.dismiss()
        }
        brushDialog.show()
    }

    fun paintClicked(view: View)
    {
        if(view != mImageButtonCurrentPaint){
            val imageButton = view as ImageButton

            val colorTag = imageButton.tag.toString()
            binding.drawingView.setColor(colorTag)
            imageButton.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.pallet_selected))

            mImageButtonCurrentPaint!!.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.pallet_normal))

            mImageButtonCurrentPaint = view
        }
    }

    private fun requestStoragePermission()
    {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE).toString())){
            Toast.makeText(this,"Need Permission to add a background",
            Toast.LENGTH_SHORT).show()
        }
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == STORAGE_PERMISSION_CODE){
            if(grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,
                        "Permission granted now you can read the storage files.",
                        Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this,
                        "Ohh..please give us the permission for application to work correctly.",
                        Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isReadStorageAllowed(): Boolean{
        val result = ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)

        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun getBitmapFromView(view: View): Bitmap{
        val returnedBitmap = Bitmap.createBitmap(view.width,
                view.height,Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background
        if(bgDrawable != null){
            bgDrawable.draw(canvas)
        }else{
            canvas.drawColor(Color.WHITE)
        }

        view.draw(canvas)
        return returnedBitmap
    }

    private fun bitmapCoroutines(mBitmap: Bitmap): String {
        var result = ""
        scope.launch {

            try {
                val bytes = ByteArrayOutputStream()
                mBitmap.compress(Bitmap.CompressFormat.PNG,90,bytes)

                val f = File(externalCacheDir!!.absoluteFile.toString() +
                        File.separator + "KidsDrawingApp_" +
                        System.currentTimeMillis() / 1000 + ".png")

                val fos = FileOutputStream(f)
                fos.write(bytes.toByteArray())
                fos.close()
                result = f.absolutePath

                if(result.isNotEmpty()){
                    Toast.makeText(this@MainActivity,
                            "File saved successfully",
                            Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this@MainActivity,
                            "There was an error saving the file",
                            Toast.LENGTH_SHORT).show()
                }

            }catch (e: Exception){
                e.printStackTrace()
            }
        }
        MediaScannerConnection.scanFile(this@MainActivity, arrayOf(result), null){
                _, uri -> val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            shareIntent.type = "image/png"
            startActivity(
                Intent.createChooser(
                    shareIntent,"Share"
                )
            )
        }
        return result
    }
    companion object{
        private const val STORAGE_PERMISSION_CODE = 1
        private const val GALLERY =2
    }
}