package id.usecase.meetcat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import id.usecase.meetcat.presentation.screen.main.MainScreen
import id.usecase.meetcat.ui.theme.MeetCatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MeetCatTheme {
                MainScreen()
            }
        }
    }
}