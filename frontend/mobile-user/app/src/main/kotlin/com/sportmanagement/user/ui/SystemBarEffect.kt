package com.sportmanagement.user.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun AppStatusBarEffect(
    statusBarColor: Color,
    useDarkIcons: Boolean
) {
    val view = LocalView.current
    if (view.isInEditMode) return

    DisposableEffect(view, statusBarColor, useDarkIcons) {
        val activity = view.context.findActivity()
        if (activity == null) {
            onDispose { }
        } else {
            val window = activity.window
            val insetsController = WindowCompat.getInsetsController(window, view)
            val previousStatusBarColor = window.statusBarColor
            val previousLightStatusBar = insetsController.isAppearanceLightStatusBars

            window.statusBarColor = statusBarColor.toArgb()
            insetsController.isAppearanceLightStatusBars = useDarkIcons

            onDispose {
                window.statusBarColor = previousStatusBarColor
                insetsController.isAppearanceLightStatusBars = previousLightStatusBar
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
