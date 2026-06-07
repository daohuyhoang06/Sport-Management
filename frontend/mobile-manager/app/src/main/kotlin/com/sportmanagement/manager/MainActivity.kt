package com.sportmanagement.manager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sportmanagement.manager.data.AppContainer
import com.sportmanagement.manager.ui.ManagerApp
import com.sportmanagement.manager.ui.theme.SportManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Khởi tạo toàn bộ data layer (Retrofit, SessionManager, Repositories)
        AppContainer.initialize(applicationContext)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            SportManagerTheme {
                ManagerApp()
            }
        }
    }
}
