package com.sportmanagement.user

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.sportmanagement.user.ui.UserApp
import com.sportmanagement.user.ui.theme.SportUserTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SportUserTheme {
                UserApp()
            }
        }
    }
}
