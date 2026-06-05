package com.canlitv.app

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient

class WebViewScraper(context: Context, private val onLinkFound: (String) -> Unit) {
    
    @SuppressLint("SetJavaScriptEnabled")
    val webView = WebView(context).apply {
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.mediaPlaybackRequiresUserGesture = false // Otomatik oynatmayı tetikler
        settings.userAgentString = "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
        
        webViewClient = object : WebViewClient() {
            override fun onLoadResource(view: WebView?, url: String?) {
                super.onLoadResource(view, url)
                val fullUrl = url.toString()
                if (fullUrl.contains(".m3u8")) {
                    onLinkFound(fullUrl)
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // ATV sitesi yüklendiği an videoyu otomatik başlat
                view?.evaluateJavascript("document.querySelector('video')?.play();", null)
            }
        }
    }

    fun scrape(url: String) {
        val headers = mapOf("Referer" to "https://www.atv.com.tr/")
        webView.loadUrl(url, headers)
    }
}

