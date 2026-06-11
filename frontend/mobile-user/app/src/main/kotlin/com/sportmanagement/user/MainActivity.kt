package com.sportmanagement.user

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.sportmanagement.user.push.PushNotificationRegistrar
import com.sportmanagement.user.ui.UserApp
import com.sportmanagement.user.ui.share.FieldShareLink
import com.sportmanagement.user.ui.theme.SportUserTheme
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
    private var pendingDeepLinkFieldId by mutableStateOf<Int?>(null)
    private var pendingMomoPaymentReturn by mutableStateOf<FieldShareLink.MomoPaymentReturn?>(null)
    private var showReviewDemoUi by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingDeepLinkFieldId = parseFieldIdFromIntent(intent)
        pendingMomoPaymentReturn = parseMomoPaymentReturnFromIntent(intent)
        showReviewDemoUi = intent?.getBooleanExtra(EXTRA_SHOW_REVIEW_DEMO_UI, false) == true
        requestNotificationPermissionIfNeeded()
        PushNotificationRegistrar.registerCurrentToken(applicationContext)

        try {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.light(
                    android.graphics.Color.WHITE,
                    android.graphics.Color.BLACK
                )
            )
        } catch (_: Exception) {
            // Fallback: some emulators crash with Index 0 out of bounds
            enableEdgeToEdge()
        }

        if (!MapLibre.hasInstance()) {
            MapLibre.getInstance(applicationContext, null, WellKnownTileServer.MapLibre)
        }

        Configuration.getInstance().userAgentValue = packageName

        setContent {
            SportUserTheme {
                UserApp(
                    incomingDeepLinkFieldId = pendingDeepLinkFieldId,
                    onDeepLinkConsumed = { pendingDeepLinkFieldId = null },
                    incomingMomoPaymentReturn = pendingMomoPaymentReturn,
                    onMomoPaymentReturnConsumed = { pendingMomoPaymentReturn = null },
                    showReviewDemoUi = showReviewDemoUi
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingDeepLinkFieldId = parseFieldIdFromIntent(intent)
        pendingMomoPaymentReturn = parseMomoPaymentReturnFromIntent(intent)
        showReviewDemoUi = intent.getBooleanExtra(EXTRA_SHOW_REVIEW_DEMO_UI, false)
    }

    private fun parseFieldIdFromIntent(intent: Intent?): Int? {
        return FieldShareLink.parseFieldId(intent?.data)
    }

    private fun parseMomoPaymentReturnFromIntent(
        intent: Intent?
    ): FieldShareLink.MomoPaymentReturn? {
        return FieldShareLink.parseMomoPaymentReturn(intent?.data)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        requestPermissions(
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            REQUEST_POST_NOTIFICATIONS
        )
    }

    companion object {
        const val EXTRA_SHOW_REVIEW_DEMO_UI = "show_review_demo_ui"
        private const val REQUEST_POST_NOTIFICATIONS = 1001
    }
}
