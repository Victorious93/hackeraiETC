package ai.hackerai.companion

import ai.hackerai.companion.llm.HttpLocalLlmProvider
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var dependencyGuard: DependencyGuard
    @Inject lateinit var llmProvider: HttpLocalLlmProvider

    private var dcaInstalledAtCreate: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dcaInstalledAtCreate = dependencyGuard.isDcaInstalled()
        renderContent(dcaInstalledAtCreate)
    }

    override fun onResume() {
        super.onResume()
        // Only recreate when DCA install state actually changed to avoid an
        // infinite recreation loop (recreate() → onResume() → recreate() …).
        if (dependencyGuard.isDcaInstalled() != dcaInstalledAtCreate) {
            recreate()
        }
    }

    private fun renderContent(isDcaInstalled: Boolean) {
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (isDcaInstalled) {
                        HackerAINavHost(llmProvider = llmProvider)
                    } else {
                        DcaRequiredScreen()
                    }
                }
            }
        }
    }
}
