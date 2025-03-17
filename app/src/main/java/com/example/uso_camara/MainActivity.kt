package com.example.uso_camara

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var imgFoto: ImageView
    private var photoURI: Uri? = null
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_PERMISSION = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imgFoto = findViewById(R.id.imgFoto)
        val btnTomarFoto: Button = findViewById(R.id.btnTomarFoto)
        val btnEnviarWhatsApp: Button = findViewById(R.id.btnEnviarWhatsApp)
        val btnEnviarCorreo: Button = findViewById(R.id.btnEnviarCorreo)


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_PERMISSION)
        }

        btnTomarFoto.setOnClickListener {
            tomarFoto()
        }

        btnEnviarWhatsApp.setOnClickListener {
            enviarWhatsApp()
        }

        btnEnviarCorreo.setOnClickListener {
            enviarCorreo()
        }
    }


    private fun tomarFoto() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {

            val photoFile: File? = try {
                crearArchivo()
            } catch (ex: IOException) {
                Toast.makeText(this, "Error al crear archivo", Toast.LENGTH_SHORT).show()
                null
            }

            photoFile?.also {
                photoURI = FileProvider.getUriForFile(
                    this,
                    "com.example.uso_camara.fileprovider",
                    it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }


    @Throws(IOException::class)
    private fun crearArchivo(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            imgFoto.setImageURI(photoURI)
        }
    }

    private fun enviarWhatsApp() {
        if (photoURI != null) {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_STREAM, photoURI)
            intent.setPackage("com.whatsapp")
                startActivity(intent)
        } else {
            Toast.makeText(this, "No hay imagen para enviar", Toast.LENGTH_SHORT).show()
        }
    }

    // Método para enviar la imagen a través de correo electrónico
    private fun enviarCorreo() {
        if (photoURI != null) {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "message/rfc822"
            intent.putExtra(Intent.EXTRA_SUBJECT, "Foto tomada")
            intent.putExtra(Intent.EXTRA_TEXT, "Adjunto la foto tomada desde la aplicación.")
            intent.putExtra(Intent.EXTRA_STREAM, photoURI)
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(Intent.createChooser(intent, "Enviar correo..."))
            } else {
                Toast.makeText(this, "No se encontró aplicación de correo", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No hay imagen para enviar", Toast.LENGTH_SHORT).show()
        }
    }
}
