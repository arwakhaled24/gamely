package com.example.gamely

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.gamely.presentation.navigation.NavGraph
import com.example.gamely.ui.theme.Gamely

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Gamely {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
