package com.canlitv.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.hls.HlsMediaSource
import androidx.media3.ui.StyledPlayerView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CanliTvApp()
        }
    }
}

private const val DEFAULT_HLS_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanliTvApp() {
    val context = LocalContext.current
    var pageUrl by remember { mutableStateOf("https://www.atv.com.tr/canli-yayin") }
    var streamUrl by remember { mutableStateOf<String?>(null) }
    var statusText by remember { mutableStateOf("Hazır") }
    var isScraping by remember { mutableStateOf(false) }
    var scrapeRequested by remember { mutableStateOf(false) }

    val player = remember {
        ExoPlayer.Builder(context).build()
    }

    DisposableEffect(player) {
        onDispose {
            player.release()
        }
    }

    LaunchedEffect(streamUrl) {
        streamUrl?.let { url ->
            statusText = "Stream bulundu, oynatılıyor"
            val dataSourceFactory = DefaultHttpDataSource.Factory()
                .setUserAgent(DEFAULT_HLS_USER_AGENT)
                    .setDefaultRequestProperties(
                        mapOf(
                            "Referer" to pageUrl,
                            "Origin" to "https://www.atv.com.tr",
                            "Accept" to "application/vnd.apple.mpegurl,application/x-mpegURL,video/*,*/*;q=0.9",
                        ),
                )
                .setAllowCrossProtocolRedirects(true)

            val mediaSource = HlsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(url))

            player.setMediaSource(mediaSource)
            player.prepare()
            player.playWhenReady = true
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Canlı TV Scraper + Media3 Player", style = MaterialTheme.typography.headlineSmall)

            OutlinedTextField(
                value = pageUrl,
                onValueChange = { pageUrl = it },
                label = { Text("Live page URL") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = {
                    statusText = "Tarama başlatıldı"
                    streamUrl = null
                    isScraping = true
                    scrapeRequested = true
                }) {
                    Text("Scrape")
                }
                Button(onClick = {
                    statusText = "Temizlendi"
                    streamUrl = null
                    isScraping = false
                    scrapeRequested = false
                }) {
                    Text("Clear")
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Durum: $statusText")
                    Text(text = "Bulunan stream: ${streamUrl ?: "Henüz yok"}")
                }
            }

            if (streamUrl != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Oynatıcı")
                        StreamingPlayer(player = player)
                    }
                }
            }

            if (scrapeRequested && isScraping) {
                WebViewScraper(
                    pageUrl = pageUrl,
                    hidden = true,
                    onStreamFound = { url ->
                        if (streamUrl == null) {
                            streamUrl = url
                            isScraping = false
                            scrapeRequested = false
                        }
                    },
                    onStatus = { event -> statusText = event },
                )
            }
        }
    }
}

@Composable
fun StreamingPlayer(player: ExoPlayer) {
    AndroidView(
        factory = { context ->
            StyledPlayerView(context).apply {
                this.player = player
                useController = true
                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
    )
}
