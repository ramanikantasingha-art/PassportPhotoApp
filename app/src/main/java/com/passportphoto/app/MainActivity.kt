package com.passportphoto.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.passportphoto.app.navigation.PassportPhotoNavGraph
import com.passportphoto.app.ui.theme.PassportPhotoAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PassportPhotoAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PassportPhotoNavGraph()
                }
            }
        }
    }
}
