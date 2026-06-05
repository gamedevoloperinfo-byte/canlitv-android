package com.canlitv.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            var m3u8Link by remember { mutableStateOf("Ajan ATV'ye sızıyor...") }
            val scraper = remember { WebViewScraper(this) { m3u8Link = it } }

            // Arayüz
            Column(modifier = Modifier.fillMaxSize()) {
                Text(text = "Link: $m3u8Link", modifier = Modifier.padding(16.dp))
                
                // WebView'ı görünür kıl ki ATV'nin "Play" butonunu tetikleyebilelim
                AndroidView(
                    factory = { scraper.webView },
                    modifier = Modifier.fillMaxSize()
                )
            }

            LaunchedEffect(Unit) {
                scraper.scrape("https://www.atv.com.tr/webtv/canli-yayin")
            }
        }
    }
}
