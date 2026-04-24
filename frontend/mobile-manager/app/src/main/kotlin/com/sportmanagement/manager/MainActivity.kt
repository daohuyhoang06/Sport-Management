package com.sportmanagement.manager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.sportmanagement.manager.ui.ManagerApp
import com.sportmanagement.manager.ui.theme.SportManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SportManagerTheme {
                ManagerApp()
            }
        }
    }
}
