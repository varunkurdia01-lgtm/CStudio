package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.example.ui.theme.MyApplicationTheme
import com.example.data.SettingsRepository

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val context = LocalContext.current
      val themePreference by SettingsRepository.getThemeFlow(context).collectAsState()
      
      MyApplicationTheme(themePreference = themePreference) {
        Surface(modifier = Modifier.fillMaxSize()) {
            CStudioApp()
        }
      }
    }
  }
}
