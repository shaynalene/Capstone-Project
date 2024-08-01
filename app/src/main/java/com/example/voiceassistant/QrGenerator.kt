package com.example.voiceassistant

\import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.voiceassistant.LoginActivity.Companion.randomCode
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class QrGenerator : AppCompatActivity() {

    private lateinit var qrBitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.qr_generator)

        val qrCodeImage: ImageView = findViewById(R.id.qr_code_image)
        val backButton: Button = findViewById(R.id.goBackButton)
        val downloadButton: Button = findViewById(R.id.downloadButton)

        backButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        downloadButton.setOnClickListener {
            checkStoragePermission()
        }

        val order_id = randomCode
        if (order_id.isNotEmpty()) {
            try {
                qrBitmap = generateQRCode(order_id)
                qrCodeImage.setImageBitmap(qrBitmap)
            } catch (e: WriterException) {
                e.printStackTrace()
            }
        }
    }

    private val STORAGE_PERMISSION_CODE = 100

    private fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
        } else {
            saveQRCode(qrBitmap)
        }
    }

    private fun saveQRCode(bitmap: Bitmap) {
        val filename = "QRCode_${System.currentTimeMillis()}.png"
        val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename)

        try {
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
            fos.close()
            Toast.makeText(this, "QR Code saved to ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving QR Code", Toast.LENGTH_LONG).show()
        }

        MediaScannerConnection.scanFile(this, arrayOf(file.toString()), null, null)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            STORAGE_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveQRCode(qrBitmap)
                } else {
                    Toast.makeText(this, "Storage permission is required to save QR codes", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun generateQRCode(text: String): Bitmap {
        val size = 512 // pixels
        val qrCodeWriter = QRCodeWriter()
        val hints = HashMap<EncodeHintType, Any>()
        hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.L
        hints[EncodeHintType.MARGIN] = 1 // default margin
        val bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, size, size, hints)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        return bmp
    }
}
