package id.usecase.meetcat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import id.usecase.meetcat.presentation.screen.explore.ExploreScreen
import id.usecase.meetcat.presentation.screen.explore.ExploreViewModel
import id.usecase.meetcat.ui.theme.MeetCatTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MeetCatTheme {
                val viewModel: ExploreViewModel = koinViewModel()
                ExploreScreen(viewModel = viewModel)
            }
        }
    }
}