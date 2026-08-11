package com.tbasic.fitnesstracker.localization

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

val LocalLocalizedContext = staticCompositionLocalOf<Context> {
    error("No localized context found! Did you forget to wrap in LocalizedContextProvider?")
}

@Composable
fun LocalizedContextProvider(
    languageCode: String,
    content: @Composable () -> Unit
) {
    val baseContext = LocalContext.current
    val localizedContext = remember(languageCode) {
        LocaleManager.setLocale(baseContext, languageCode)
    }

    CompositionLocalProvider(
        LocalLocalizedContext provides localizedContext,
        content = content
    )
}
