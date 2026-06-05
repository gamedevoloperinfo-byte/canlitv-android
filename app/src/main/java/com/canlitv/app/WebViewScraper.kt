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
        // Sitenin bizi mobil cihaz sanması için ajan kimliği
        settings.userAgentString = "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
        
        webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                val url = request?.url.toString()
                
                // KESKİN NİŞANCI FİLTRESİ: Sadece .m3u8 olan ve ATV sunucusundan gelen linki yakala
                if (url.contains(".m3u8") && url.contains("ercdn")) {
                    onLinkFound(url) // Linki bulduğumuz an ExoPlayer'a göndermek için yakaladık!
                }
                return super.shouldInterceptRequest(view, request)
            }
        }
    }

    fun scrape(url: String) {
        // ATV'ye "Kendi sitesinden geliyorum" yalanını söylemek için Header ekliyoruz
        val headers = mapOf("Referer" to "https://www.atv.com.tr/")
        webView.loadUrl(url, headers)
    }
}
