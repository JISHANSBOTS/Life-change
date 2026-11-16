package com.example.htmlbackgroundrunner

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.ValueCallback
import android.webkit.WebChromeClient.FileChooserParams
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import java.io.OutputStreamWriter

class MainActivity : Activity() {
    private lateinit var web: WebView
    private var pendingExport: String? = null
    private var pendingMime: String = "text/csv"
    private var pendingName: String = "trading-export.csv"
    private var filePathCallback: ValueCallback<Array<Uri>>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 903)
        }
        web = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            settings.javaScriptCanOpenWindowsAutomatically = false
            settings.setSupportMultipleWindows(false)
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val host = request?.url?.host ?: return false
                    return host != "appassets.androidplatform.net"
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onShowFileChooser(view: WebView?, callback: ValueCallback<Array<Uri>>?, params: FileChooserParams?): Boolean {
                    filePathCallback?.onReceiveValue(null)
                    filePathCallback = callback
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "text/*"
                        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("text/csv", "application/vnd.ms-excel", "text/plain"))
                    }
                    startActivityForResult(intent, 901)
                    return true
                }
            }
            addJavascriptInterface(NativeBridge(), "TradingNative")
            setBackgroundColor(android.graphics.Color.rgb(8, 12, 19))
            loadUrl("file:///android_asset/web/index.html")
        }
        setContentView(web)
    }

    inner class NativeBridge {
        @JavascriptInterface fun startEngine() {
            runOnUiThread {
                try { RunnerService.start(this@MainActivity); toast("Research session started") }
                catch (e: Exception) { toast("Unable to start service: ${e.message}") }
            }
        }
        @JavascriptInterface fun stopEngine() {
            runOnUiThread { RunnerService.stop(this@MainActivity); toast("Research session stopped") }
        }
        @JavascriptInterface fun engineStatus(): String = if (RunnerService.isRunning(this@MainActivity)) "running" else "stopped"
        @JavascriptInterface fun exportCsv(fileName: String, csv: String) {
            runOnUiThread {
                pendingName = fileName.filter { it.isLetterOrDigit() || it in "._-" }.ifBlank { "trading-export.csv" }
                pendingExport = csv
                pendingMime = "text/csv"
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = pendingMime
                    putExtra(Intent.EXTRA_TITLE, pendingName)
                }
                startActivityForResult(intent, 902)
            }
        }
        @JavascriptInterface fun appVersion(): String = "2.0.0"
    }

    @Deprecated("Activity result API retained for broad compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 901) {
            val result = if (resultCode == RESULT_OK && data?.data != null) arrayOf(data.data!!) else null
            filePathCallback?.onReceiveValue(result)
            filePathCallback = null
        } else if (requestCode == 902 && resultCode == RESULT_OK) data?.data?.let { writePendingExport(it) }
    }

    private fun writePendingExport(uri: Uri) {
        val content = pendingExport ?: return
        try {
            contentResolver.openOutputStream(uri)?.use { out -> OutputStreamWriter(out, Charsets.UTF_8).use { it.write(content) } }
            toast("Export saved")
        } catch (e: Exception) { toast("Export failed: ${e.message}") }
        finally { pendingExport = null }
    }

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    override fun onBackPressed() {
        if (::web.isInitialized && web.canGoBack()) web.goBack() else super.onBackPressed()
    }
}
