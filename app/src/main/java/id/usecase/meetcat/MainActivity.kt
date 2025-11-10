package id.usecase.meetcat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import id.usecase.meetcat.presentation.navigation.AppRouter
import id.usecase.meetcat.ui.theme.MeetCatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MeetCatTheme {
                AppRouter()
            }
        }
    }
}