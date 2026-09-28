package ai.hackerai.companion.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StatusScreen(isDcaConnected: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "HackerAI Companion",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = if (isDcaConnected) "Connected to DroidCommand AI" else "Waiting for DroidCommand AI...",
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
