package com.example.smarthaircare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.smarthaircare.ui.theme.SmartHairCareTheme
import com.example.smarthaircare.navigation.NavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartHairCareTheme {
                NavGraph()
            }
        }
    }
}
