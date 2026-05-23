package com.sportmanagement.user

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sportmanagement.user.ui.UserApp
import com.sportmanagement.user.ui.theme.SportUserTheme
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Khởi tạo MapLibre tại đây để tránh lỗi MapLibreConfigurationException và SIGSEGV
        MapLibre.getInstance(this, null, WellKnownTileServer.MapLibre)

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
        Configuration.getInstance().userAgentValue = packageName

        setContent {
            SportUserTheme {
                UserApp()
            }
        }
    }
}
