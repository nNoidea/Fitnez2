package com.nnoidea.fitnez2.ui.screens.developer

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.ui.components.dialog.PredictiveModal

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoadingShowcaseDialog(
    show: Boolean,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loadingShowcase")
    val animatedProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "determinateProgress"
    )

    PredictiveModal(show = show, onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text("Material 3 Loading Indicators", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text("--- M3 Expressive (Wavy) ---", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))

            // 1. LinearWavyProgressIndicator (indeterminate)
            Text("1. LinearWavyProgressIndicator (indeterminate)", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            LinearWavyProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(Modifier.height(20.dp))

            // 2. LinearWavyProgressIndicator (determinate)
            Text("2. LinearWavyProgressIndicator (determinate)", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            LinearWavyProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(Modifier.height(20.dp))

            // 3. CircularWavyProgressIndicator (indeterminate)
            Text("3. CircularWavyProgressIndicator (indeterminate)", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            CircularWavyProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(Modifier.height(20.dp))

            // 4. CircularWavyProgressIndicator (determinate)
            Text("4. CircularWavyProgressIndicator (determinate)", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            CircularWavyProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(Modifier.height(20.dp))

            // 5. LoadingIndicator (morphing shapes, indeterminate)
            Text("5. LoadingIndicator (morphing shapes)", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            LoadingIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(20.dp))

            // 6. ContainedLoadingIndicator
            Text("6. ContainedLoadingIndicator", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            ContainedLoadingIndicator(
                modifier = Modifier.size(56.dp),
                indicatorColor = MaterialTheme.colorScheme.primary,
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
            Spacer(Modifier.height(24.dp))

            Text("--- Classic M3 ---", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.height(16.dp))

            // 7. Linear indefinite
            Text("7. LinearProgressIndicator (indeterminate)", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(Modifier.height(20.dp))

            // 8. Circular indeterminate
            Text("8. CircularProgressIndicator (indeterminate)", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}
