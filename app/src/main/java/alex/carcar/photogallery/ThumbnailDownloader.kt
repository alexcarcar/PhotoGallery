package alex.carcar.photogallery

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import android.util.LruCache
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "ThumbnailDownloader"
private const val MESSAGE_DOWNLOAD = 0
private const val MESSAGE_PRELOAD = 1
private const val CACHE_SIZE = 25 * 1024 * 1024

class ThumbnailDownloader<in T>(
    private val responseHandler: Handler,
    private val onThumbnailDownloaded: (T, Bitmap) -> Unit
) : HandlerThread(TAG) {
    var lruCache: LruCache<String, Bitmap> = LruCache(CACHE_SIZE)

    val fragmentLifecycleObserver: LifecycleObserver =
        object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
            fun setup() {
                Log.i(TAG, "Starting background thread")
                start()
                looper
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun tearDown() {
                Log.i(TAG, "Destroying background thread")
                quit()
            }
        }

    val viewLifecycleObserver: LifecycleObserver =
        object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun clearQueue() {
                Log.i(TAG, "Clearing all requests from queue")
                responseHandler.removeMessages(MESSAGE_DOWNLOAD)
                requestMap.clear()
            }
        }
    private var hasQuit = false
    private lateinit var requestHandler: Handler
    private val requestMap = ConcurrentHashMap<T, String>()
    private val flickrFetchr = FlickrFetchr()

    @Suppress("UNCHECKED_CAST")
    @SuppressLint("HandlerLeak")
    override fun onLooperPrepared() {
        requestHandler = object : Handler() {
            override fun handleMessage(msg: Message) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    val target = msg.obj as T
                    Log.i(TAG, "Got a request for a URL: ${requestMap[target]}")
                    handleRequest(target)
                } else if (msg.what == MESSAGE_PRELOAD) {
                    preload(msg.obj as String)
                }
            }
        }
    }

    private fun handleRequest(target: T) {
        val url = requestMap[target] ?: return
        var bitmap: Bitmap
        if (lruCache.get(url) == null) {
            bitmap = flickrFetchr.fetchPhoto(url) ?: return
            lruCache.put(url, bitmap)
        } else {
            bitmap = lruCache.get(url)
        }
        responseHandler.post(Runnable {
            if (requestMap[target] != url || hasQuit) {
                return@Runnable
            }
            requestMap.remove(target)
            onThumbnailDownloaded(target, bitmap)
        })
    }

    fun preload(url: String) {
        var bitmap: Bitmap
        if (lruCache.get(url) == null) {
            bitmap = flickrFetchr.fetchPhoto(url) ?: return
            lruCache.put(url, bitmap)
        }
    }

    override fun quit(): Boolean {
        hasQuit = true
        return super.quit()
    }

    fun queueThumbnail(target: T?, url: String) {
        Log.i(TAG, "Got a URL: $url")
        if (target != null) {
            requestMap[target] = url
            requestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget()
        } else {
            requestHandler.obtainMessage(MESSAGE_PRELOAD, url).sendToTarget()
        }
    }
}