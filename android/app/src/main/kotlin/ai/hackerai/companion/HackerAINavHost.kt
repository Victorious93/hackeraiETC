package ai.hackerai.companion

import ai.hackerai.companion.ui.StatusScreen
import androidx.compose.runtime.Composable

@Composable
fun HackerAINavHost() {
    // Stub nav scaffold — Phase 5 wires real navigation + ViewModels.
    StatusScreen(isDcaConnected = true)
}

@Composable
fun DcaRequiredScreen() {
    StatusScreen(isDcaConnected = false)
}
