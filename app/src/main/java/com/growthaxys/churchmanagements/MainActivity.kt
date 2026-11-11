package com.growthaxys.churchmanagements

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.growthaxys.churchmanagements.feature.app.ChurchManagementRoot
import com.growthaxys.churchmanagements.core.designsystem.ChurchTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { ChurchTheme { ChurchManagementRoot() } }
    }
}
