package com.lunyx.qrscanner.activities

import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.lunyx.qrscanner.R
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class GenerateActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var spinnerType: Spinner
    private lateinit var ivQrCode: ImageView
    private lateinit var btnGenerate: MaterialButton
    private lateinit var btnSaveShare: MaterialButton
    private lateinit var layoutText: View
    private lateinit var layoutUrl: View
    private lateinit var layoutWifi: View
    private lateinit var layoutContact: View
    private lateinit var layoutEmail: View
    private lateinit var layoutPhone: View
    private lateinit var layoutSms: View

    private var currentBitmap: Bitmap? = null

    private val qrTypes = arrayOf("Текст", "Посилання", "Wi-Fi", "Контакт", "Email", "Телефон", "SMS")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("qr_prefs", MODE_PRIVATE)
        setContentView(R.layout.activity_generate)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.create_qr)

        initViews()
        setupSpinner()
        setupButtons()
    }

    private fun initViews() {
        spinnerType = findViewById(R.id.spinnerType)
        ivQrCode = findViewById(R.id.ivQrCode)
        btnGenerate = findViewById(R.id.btnGenerate)
        btnSaveShare = findViewById(R.id.btnSaveShare)
        layoutText = findViewById(R.id.layoutText)
        layoutUrl = findViewById(R.id.layoutUrl)
        layoutWifi = findViewById(R.id.layoutWifi)
        layoutContact = findViewById(R.id.layoutContact)
        layoutEmail = findViewById(R.id.layoutEmail)
        layoutPhone = findViewById(R.id.layoutPhone)
        layoutSms = findViewById(R.id.layoutSms)

        btnSaveShare.visibility = View.GONE
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, qrTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = adapter

        spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                showLayout(pos)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun showLayout(pos: Int) {
        layoutText.visibility = View.GONE
        layoutUrl.visibility = View.GONE
        layoutWifi.visibility = View.GONE
        layoutContact.visibility = View.GONE
        layoutEmail.visibility = View.GONE
        layoutPhone.visibility = View.GONE
        layoutSms.visibility = View.GONE

        when (pos) {
            0 -> layoutText.visibility = View.VISIBLE
            1 -> layoutUrl.visibility = View.VISIBLE
            2 -> layoutWifi.visibility = View.VISIBLE
            3 -> layoutContact.visibility = View.VISIBLE
            4 -> layoutEmail.visibility = View.VISIBLE
            5 -> layoutPhone.visibility = View.VISIBLE
            6 -> layoutSms.visibility = View.VISIBLE
        }
    }

    private fun setupButtons() {
        btnGenerate.setOnClickListener {
            val content = buildContent()
            if (content.isNullOrBlank()) {
                Toast.makeText(this, getString(R.string.fill_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            generateQr(content)
        }

        btnSaveShare.setOnClickListener {
            showSaveShareDialog()
        }
    }

    private fun buildContent(): String? {
        return when (spinnerType.selectedItemPosition) {
            0 -> getField(R.id.etText)
            1 -> getField(R.id.etUrl)
            2 -> {
                val ssid = getField(R.id.etWifiSsid) ?: return null
                val pass = getField(R.id.etWifiPass) ?: ""
                val type = if ((findViewById<Spinner>(R.id.spinnerWifiType)).selectedItemPosition == 0) "WPA" else "nopass"
                "WIFI:T:$type;S:$ssid;P:$pass;;"
            }
            3 -> {
                val name = getField(R.id.etContactName) ?: return null
                val phone = getField(R.id.etContactPhone) ?: ""
                val email = getField(R.id.etContactEmail) ?: ""
                "BEGIN:VCARD\nVERSION:3.0\nFN:$name\nTEL:$phone\nEMAIL:$email\nEND:VCARD"
            }
            4 -> {
                val to = getField(R.id.etEmailTo) ?: return null
                val subject = getField(R.id.etEmailSubject) ?: ""
                val body = getField(R.id.etEmailBody) ?: ""
                "mailto:$to?subject=$subject&body=$body"
            }
            5 -> "tel:${getField(R.id.etPhone)}"
            6 -> {
                val number = getField(R.id.etSmsNumber) ?: return null
                val msg = getField(R.id.etSmsText) ?: ""
                "smsto:$number:$msg"
            }
            else -> null
        }
    }

    private fun getField(id: Int): String? {
        val text = findViewById<TextInputEditText>(id).text?.toString()?.trim()
        return if (text.isNullOrBlank()) null else text
    }

    private fun generateQr(content: String) {
        try {
            val hints = mapOf(EncodeHintType.MARGIN to 2)
            val writer = QRCodeWriter()
            val matrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512, hints)
            val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565)
            for (x in 0 until 512) {
                for (y in 0 until 512) {
                    bitmap.setPixel(x, y, if (matrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            currentBitmap = bitmap
            ivQrCode.setImageBitmap(bitmap)
            btnSaveShare.visibility = View.VISIBLE
        } catch (e: Exception) {
            Toast.makeText(this, "Помилка генерації: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSaveShareDialog() {
        val options = arrayOf(getString(R.string.download), getString(R.string.share))
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.choose_action))
            .setItems(options) { _, which ->
                when (which) {
                    0 -> saveBitmap()
                    1 -> shareBitmap()
                }
            }
            .show()
    }

    private fun getFormat(): Bitmap.CompressFormat {
        return when (prefs.getString("format", "PNG")) {
            "JPG" -> Bitmap.CompressFormat.JPEG
            "WEBP" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                Bitmap.CompressFormat.WEBP_LOSSLESS else Bitmap.CompressFormat.WEBP
            else -> Bitmap.CompressFormat.PNG
        }
    }

    private fun getExtension(): String {
        return when (prefs.getString("format", "PNG")) {
            "JPG" -> "jpg"
            "WEBP" -> "webp"
            else -> "png"
        }
    }

    private fun getMimeType(): String {
        return when (prefs.getString("format", "PNG")) {
            "JPG" -> "image/jpeg"
            "WEBP" -> "image/webp"
            else -> "image/png"
        }
    }

    private fun saveBitmap() {
        val bitmap = currentBitmap ?: return
        val filename = "QR_${System.currentTimeMillis()}.${getExtension()}"

        try {
            val outputStream: OutputStream
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, getMimeType())
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/QR Scanner")
                }
                val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!
                outputStream = contentResolver.openOutputStream(uri)!!
            } else {
                val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "QR Scanner")
                dir.mkdirs()
                val file = File(dir, filename)
                outputStream = FileOutputStream(file)
            }

            bitmap.compress(getFormat(), 100, outputStream)
            outputStream.close()
            Toast.makeText(this, getString(R.string.saved_to_gallery), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Помилка збереження: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareBitmap() {
        val bitmap = currentBitmap ?: return
        val filename = "QR_${System.currentTimeMillis()}.${getExtension()}"

        try {
            val file = File(cacheDir, filename)
            val outputStream = FileOutputStream(file)
            bitmap.compress(getFormat(), 100, outputStream)
            outputStream.close()

            val uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = getMimeType()
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, getString(R.string.share)))
        } catch (e: Exception) {
            Toast.makeText(this, "Помилка: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}
