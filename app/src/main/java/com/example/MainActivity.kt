package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.ai.FirebaseVariAiRepository
import com.example.data.ai.configureAppCheck
import com.example.data.local.VariDatabase
import com.example.data.local.VariLocalRepository
import com.example.ui.VariApp
import com.example.ui.VariViewModel
import com.example.ui.VariViewModelFactory
import com.example.ui.theme.VariTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure Firebase App Check with launch intent before any UI, ViewModel, or Firebase calls
        configureAppCheck(this, intent)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )

        val database = VariDatabase.getInstance(applicationContext)
        val localRepository = VariLocalRepository(database.variDao())
        val aiRepository = FirebaseVariAiRepository()
        val viewModel = ViewModelProvider(
            this,
            VariViewModelFactory(localRepository, aiRepository)
        )[VariViewModel::class.java]

        setContent {
            VariTheme {
                VariApp(viewModel = viewModel)
            }
        }
    }
}
