package ai.hackerai.companion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val guard = (application as HackerAIApp).dependencyGuard
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (guard.isDcaInstalled()) {
                        HackerAINavHost()
                    } else {
                        DcaRequiredScreen()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-check on every resume so the UI reacts when DCA is installed/removed.
        recreate()
    }
}
