package com.tbasic.fitnesstracker.ui.components

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.tbasic.fitnesstracker.BuildConfig
import com.tbasic.fitnesstracker.R

@Composable
fun ImageWithFallback(
    exerciseId: String,
    modifier: Modifier = Modifier.size(100.dp),
    contentScale: ContentScale = ContentScale.Fit,
    enableGif: Boolean = false
) {
    val context = LocalContext.current
    val isOnline = isConnected(context)
    val useGif = enableGif && isOnline

    val url = "${BuildConfig.DATABASE_URL}/storage/v1/object/public/fitness.tracker.gifs/$exerciseId.gif"

    val imageLoader = ImageLoader.Builder(context).apply {
        if (useGif) {
            components { add(GifDecoder.Factory()) }
            diskCachePolicy(CachePolicy.DISABLED)
            memoryCachePolicy(CachePolicy.DISABLED)
        } else {
            diskCachePolicy(CachePolicy.ENABLED)
            memoryCachePolicy(CachePolicy.ENABLED)
        }
    }.build()

    // Pokušaj: iz keša samo
    val cacheOnlyRequest = remember(url) {
        ImageRequest.Builder(context)
            .data(url)
            .diskCachePolicy(CachePolicy.READ_ONLY)
            .networkCachePolicy(CachePolicy.DISABLED)
            .error(R.drawable.exercise_placeholder)
            .build()
    }

    // Ako nema u kešu, probaj s mreže
    val networkRequest = remember(url) {
        ImageRequest.Builder(context)
            .data(url)
            .crossfade(true)
            .apply {
                if (useGif) {
                    diskCachePolicy(CachePolicy.DISABLED)
                    networkCachePolicy(CachePolicy.ENABLED)
                } else {
                    diskCachePolicy(CachePolicy.ENABLED)
                    networkCachePolicy(if (isOnline) CachePolicy.ENABLED else CachePolicy.DISABLED)
                }
            }
            .error(R.drawable.exercise_placeholder)
            .build()
    }

    var useNetwork by remember { mutableStateOf(false) }

    val painter = rememberAsyncImagePainter(
        model = if (useNetwork) networkRequest else cacheOnlyRequest,
        imageLoader = imageLoader,
        onState = { state ->
            if (state is AsyncImagePainter.State.Error && !useNetwork) {
                useNetwork = true // ako nije u kešu, idi na mrežu
            }
        }
    )

    val isLoading = painter.state is AsyncImagePainter.State.Loading

    Box(modifier = modifier) {
        if (isLoading) {
            ShimmerBox(modifier = Modifier.matchParentSize())
        }
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = contentScale
        )
    }
}

// Provjera mrežne konekcije
fun isConnected(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = cm.activeNetwork ?: return false
    val capabilities = cm.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
