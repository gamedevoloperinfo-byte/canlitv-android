package com.canlitv.app

import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView

class MainActivity : ComponentActivity() {
    private var exoPlayer: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ExoPlayer'ı hazırla
        exoPlayer = ExoPlayer.Builder(this).build()

        setContent {
            // Ajanımızı başlat
            val scraper = remember { 
                WebViewScraper(this) { m3u8Link ->
                    // Linki yakaladığımız an oynatıcıya gönder
                    val mediaItem = MediaItem.fromUri(m3u8Link)
                    exoPlayer?.setMediaItem(mediaItem)
                    exoPlayer?.prepare()
                    exoPlayer?.play()
                } 
            }

            // Oynatıcıyı ekrana göm
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
                modifier = Modifier.fillMaxSize()
            )

            // Görünmez Ajan (1x1 piksel boyutunda, ekranda yer kaplamaz)
            AndroidView(
                factory = { scraper.webView },
                modifier = Modifier.size(1.dp)
            )

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
