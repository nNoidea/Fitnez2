package com.nnoidea.fitnez2.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

// Placeholder content for testing animations
// Easily deletable as per user request
@Composable
fun PlaceholderTestContent() {
    Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Placeholder Graph
        Box(
                modifier =
                        Modifier.fillMaxWidth(0.9f)
                                .height(200.dp)
                                .background(
                                        MaterialTheme.colorScheme.secondaryContainer,
                                        shape = MaterialTheme.shapes.medium
                                )
                                .padding(16.dp)
        ) {
            Text(
                    "Activity Graph",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.align(Alignment.TopStart)
            )

            Canvas(modifier = Modifier.fillMaxSize().padding(top = 24.dp)) {
                val path = Path()
                val w = size.width
                val h = size.height

                path.moveTo(0f, h * 0.8f)
                path.cubicTo(w * 0.3f, h * 0.5f, w * 0.6f, h * 0.9f, w, h * 0.2f)

                drawPath(
                        path = path,
                        color = Color(0xFF4CAF50), // Simple Green
                        style = Stroke(width = 4.dp.toPx())
                )
            }
        }

        // Placeholder Buttons
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = {}) { Text("Start w/o") }
            Button(onClick = {}) { Text("Log Data") }
        }

        Button(onClick = {}) { Text("View Detailed Analysis") }

        Text(
                "This content is just for testing animations.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
