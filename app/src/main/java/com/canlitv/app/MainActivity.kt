package com.canlitv.app

import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.RequestMetadata
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class MainActivity : ComponentActivity() {
    private var exoPlayer: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        exoPlayer = ExoPlayer.Builder(this).build()

        setContent {
            var m3u8Link by remember { mutableStateOf("Ajan ATV'ye sızıyor...") }
            
            val scraper = remember { 
                WebViewScraper(this) { link ->
                    m3u8Link = link
                    
                    // ATV için Referer içeren MediaItem oluştur
                    val mediaItem = MediaItem.Builder()
                        .setUri(link)
                        .setRequestMetadata(
                            RequestMetadata.Builder()
                                .build()
                        )
                        .build()
                    
                    // Media3 için Referer ekleme (Doğru yöntem)
                    exoPlayer?.setMediaItems(listOf(mediaItem))
                    exoPlayer?.prepare()
                    exoPlayer?.play()
                } 
            }

            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "Link: $m3u8Link",
                    modifier = Modifier.padding(16.dp)
                )

                AndroidView(
                    factory = { context ->
                        PlayerView(context).apply {
                            player = exoPlayer
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    modifier = Modifier.weight(1f)
                )

                // Görünmez Ajan
                AndroidView(
                    factory = { scraper.webView },
                    modifier = Modifier.size(1.dp)
                )
            }

            LaunchedEffect(Unit) {
                scraper.scrape("https://www.atv.com.tr/webtv/canli-yayin")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
        exoPlayer = null
    }
}
