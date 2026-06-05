package com.canlitv.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            var m3u8Link by remember { mutableStateOf("Ajan ATV'ye sızıyor...") }

            // Ajanımızı başlatıyoruz
            val scraper = remember { 
                WebViewScraper(this) { foundLink -> 
                    m3u8Link = foundLink // Linki yakaladık!
                } 
            }

            // ATV'ye operasyon başlat
            LaunchedEffect(Unit) {
                scraper.scrape("https://www.atv.com.tr/webtv/canli-yayin")
            }

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text(text = m3u8Link)
            }
        }
    }
}
