package com.canlitv.app

import android.annotation.SuppressLint
import android.view.View
import android.graphics.Bitmap
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.json.JSONArray

private const val DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36"
private const val SECURE_TOKEN_HOST = "securevideotoken.tmgrup.com.tr/webtv/secure"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScraper(
    pageUrl: String,
    hidden: Boolean,
    onStreamFound: (String) -> Unit,
    onStatus: (String) -> Unit,
) {
    val context = LocalContext.current

    AndroidView(
        modifier = Modifier,
        factory = {
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.userAgentString = DEFAULT_USER_AGENT
                settings.javaScriptCanOpenWindowsAutomatically = false
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        injectFetchInterceptorJs(view)
                    }

                    override fun shouldInterceptRequest(
                        view: WebView,
                        request: WebResourceRequest,
                    ): android.webkit.WebResourceResponse? {
                        val url = request.url.toString()
                        if (url.contains(SECURE_TOKEN_HOST, ignoreCase = true)) {
                            view.post {
                                onStatus("Secure token isteği yakalandı: $url")
                            }
                        }
                        if (url.contains(".m3u8", ignoreCase = true)) {
                            view.post {
                                onStatus(".m3u8 isteği yakalandı: $url")
                                onStreamFound(url)
                            }
                        }
                        return super.shouldInterceptRequest(view, request)
                    }

                    override fun onPageFinished(view: WebView, url: String) {
                        super.onPageFinished(view, url)
                        onStatus("Sayfa yüklendi: $url")
                        injectVideoScraperJs(view)
                    }
                }
                addJavascriptInterface(
                    object {
                        @JavascriptInterface
                        fun onVideoUrls(jsonUrls: String) {
                            view.post {
                                val streamUrl = extractM3u8FromJson(jsonUrls)
                                if (streamUrl != null) {
                                    onStatus("Video etiketi bulundu: $streamUrl")
                                    onStreamFound(streamUrl)
                                }
                            }
                        }

                        @JavascriptInterface
                        fun onTokenResponse(jsonData: String) {
                            view.post {
                                val streamUrl = extractUrlFromJson(jsonData)
                                if (streamUrl != null) {
                                    onStatus("Secure token sonucu bulundu: $streamUrl")
                                    onStreamFound(streamUrl)
                                }
                            }
                        }
                    },
                    "AndroidScraper",
                )
                loadUrl(pageUrl)
            }
        },
        update = { webView ->
            webView.visibility = if (hidden) View.GONE else View.VISIBLE
        },
    )
}

private fun extractM3u8FromJson(jsonUrls: String): String? {
    return try {
        val array = JSONArray(jsonUrls)
        (0 until array.length())
            .mapNotNull { array.optString(it, null) }
            .firstOrNull { it.contains(".m3u8", ignoreCase = true) }
    } catch (ex: Exception) {
        null
    }
}

private fun extractUrlFromJson(jsonData: String): String? {
    return try {
        val json = org.json.JSONObject(jsonData)
        val url = json.optString("Url", null)
        val alternateUrl = json.optString("AlternateUrl", null)
        listOf(url, alternateUrl)
            .firstOrNull { it != null && it.contains(".m3u8", ignoreCase = true) }
    } catch (ex: Exception) {
        null
    }
}

private fun injectVideoScraperJs(webView: WebView) {
    val js = """
        (function() {
          try {
            var urls = [];
            document.querySelectorAll('video').forEach(function(video) {
              if (video.currentSrc) urls.push(video.currentSrc);
              if (video.src) urls.push(video.src);
              video.querySelectorAll('source').forEach(function(source) {
                if (source.src) urls.push(source.src);
              });
            });
            document.querySelectorAll('source').forEach(function(source) {
              if (source.src) urls.push(source.src);
            });
            urls = urls.filter(function(url) { return url && url.toLowerCase().indexOf('.m3u8') !== -1; });
            AndroidScraper.onVideoUrls(JSON.stringify(urls));
          } catch (error) {
            console.log('WebViewScraper JS error:', error);
          }
        })();
    """.trimIndent()
    webView.evaluateJavascript(js, null)
}

private fun injectFetchInterceptorJs(webView: WebView) {
        val js = """
                (function() {
                    try {
                        var originalFetch = window.fetch;
                        if (!originalFetch) return;
                        window.fetch = function(input, init) {
                            var url = typeof input === 'string' ? input : input.url;
                            return originalFetch(input, init).then(function(response) {
                                try {
                                    if (url && url.indexOf('$SECURE_TOKEN_HOST') !== -1) {
                                        response.clone().json().then(function(data) {
                                            AndroidScraper.onTokenResponse(JSON.stringify(data));
                                        }).catch(function(err) {
                                            console.log('Fetch interceptor parse error', err);
                                        });
                                    }
                                } catch (err) {
                                    console.log('Fetch interceptor error', err);
                                }
                                return response;
                            });
                        };
                    } catch (error) {
                        console.log('Fetch interceptor install error:', error);
                    }
                })();
        """.trimIndent()
        webView.evaluateJavascript(js, null)
}
