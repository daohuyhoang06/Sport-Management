package com.sportmanagement.user

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.sportmanagement.user.ui.UserApp
import com.sportmanagement.user.ui.theme.SportUserTheme
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = packageName

        setContent {
            SportUserTheme {
                UserApp()
            }
        }
    }
}
