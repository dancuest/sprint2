package com.example.animedev20.ui.theme.ux

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

enum class AnimeDevMessageType {
    SUCCESS,
    ERROR,
    INFO
}

@Composable
fun AnimeDevInlineMessage(
    text: String,
    type: AnimeDevMessageType,
    modifier: Modifier = Modifier
) {
    val containerColor = when (type) {
        AnimeDevMessageType.SUCCESS -> MaterialTheme.colorScheme.tertiaryContainer
        AnimeDevMessageType.ERROR -> MaterialTheme.colorScheme.errorContainer
        AnimeDevMessageType.INFO -> MaterialTheme.colorScheme.secondaryContainer
    }

    val contentColor = when (type) {
        AnimeDevMessageType.SUCCESS -> MaterialTheme.colorScheme.onTertiaryContainer
        AnimeDevMessageType.ERROR -> MaterialTheme.colorScheme.onErrorContainer
        AnimeDevMessageType.INFO -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor
        )
    }
}

@Composable
fun AnimeDevFullScreenLoading(
    message: String = "Cargando contenido...",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CircularProgressIndicator()

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AnimeDevEmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    primaryActionLabel: String? = null,
    onPrimaryAction: (() -> Unit)? = null,
    secondaryActionLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null
) {
    AnimeDevStateCard(
        title = title,
        message = message,
        modifier = modifier,
        tone = AnimeDevMessageType.INFO,
        primaryActionLabel = primaryActionLabel,
        onPrimaryAction = onPrimaryAction,
        secondaryActionLabel = secondaryActionLabel,
        onSecondaryAction = onSecondaryAction
    )
}

@Composable
fun AnimeDevErrorState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    primaryActionLabel: String? = AnimeDevCopy.Actions.tryAgain,
    onPrimaryAction: (() -> Unit)? = null,
    secondaryActionLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null
) {
    AnimeDevStateCard(
        title = title,
        message = message,
        modifier = modifier,
        tone = AnimeDevMessageType.ERROR,
        primaryActionLabel = primaryActionLabel,
        onPrimaryAction = onPrimaryAction,
        secondaryActionLabel = secondaryActionLabel,
        onSecondaryAction = onSecondaryAction
    )
}

@Composable
fun AnimeDevInfoCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AnimeDevStateCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    tone: AnimeDevMessageType,
    primaryActionLabel: String? = null,
    onPrimaryAction: (() -> Unit)? = null,
    secondaryActionLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null
) {
    val containerColor = when (tone) {
        AnimeDevMessageType.SUCCESS -> MaterialTheme.colorScheme.tertiaryContainer
        AnimeDevMessageType.ERROR -> MaterialTheme.colorScheme.errorContainer
        AnimeDevMessageType.INFO -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = when (tone) {
        AnimeDevMessageType.SUCCESS -> MaterialTheme.colorScheme.onTertiaryContainer
        AnimeDevMessageType.ERROR -> MaterialTheme.colorScheme.onErrorContainer
        AnimeDevMessageType.INFO -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = MaterialTheme.shapes.extraLarge,
        border = if (tone == AnimeDevMessageType.INFO) {
            BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        } else {
            null
        }
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                textAlign = TextAlign.Center
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                textAlign = TextAlign.Center
            )

            if (primaryActionLabel != null && onPrimaryAction != null) {
                Button(
                    onClick = onPrimaryAction,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(primaryActionLabel)
                }
            }

            if (secondaryActionLabel != null && onSecondaryAction != null) {
                if (primaryActionLabel != null) {
                    OutlinedButton(
                        onClick = onSecondaryAction,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(secondaryActionLabel)
                    }
                } else {
                    TextButton(onClick = onSecondaryAction) {
                        Text(secondaryActionLabel)
                    }
                }
            }
        }
    }
}