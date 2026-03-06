package com.lunyx.qrscanner.activities

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Vibrator
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.lunyx.qrscanner.R
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView

class ScanActivity : AppCompatActivity() {

    private lateinit var barcodeView: DecoratedBarcodeView
    private lateinit var tvResult: TextView
    private lateinit var btnFlash: ImageButton
    private var flashOn = false
    private var lastResult = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.scan_qr)

        barcodeView = findViewById(R.id.barcodeView)
        tvResult = findViewById(R.id.tvResult)
        btnFlash = findViewById(R.id.btnFlash)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
        } else {
            startScanning()
        }

        btnFlash.setOnClickListener {
            flashOn = !flashOn
            if (flashOn) {
                barcodeView.setTorchOn()
                btnFlash.setImageResource(R.drawable.ic_flash_on)
            } else {
                barcodeView.setTorchOff()
                btnFlash.setImageResource(R.drawable.ic_flash_off)
            }
        }
    }

    private fun startScanning() {
        barcodeView.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult) {
                if (result.text == lastResult) return
                lastResult = result.text
                tvResult.text = result.text

                // Vibrate
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                @Suppress("DEPRECATION")
                vibrator.vibrate(100)

                showResultDialog(result.text)
            }

            override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
        })
    }

    private fun showResultDialog(text: String) {
        val isUrl = text.startsWith("http://") || text.startsWith("https://")

        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.scan_result))
        builder.setMessage(text)

        builder.setNeutralButton(getString(R.string.copy)) { _, _ ->
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("QR", text))
            Toast.makeText(this, getString(R.string.copied), Toast.LENGTH_SHORT).show()
        }

        if (isUrl) {
            builder.setPositiveButton(getString(R.string.open_link)) { _, _ ->
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(text)))
            }
        }

        builder.setNegativeButton(getString(R.string.close)) { dialog, _ ->
            dialog.dismiss()
            lastResult = ""
        }

        builder.show()
    }

    override fun onResume() {
        super.onResume()
        barcodeView.resume()
    }

    override fun onPause() {
        super.onPause()
        barcodeView.pause()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startScanning()
        } else {
            Toast.makeText(this, getString(R.string.camera_permission_needed), Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
