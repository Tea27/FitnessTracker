package com.tbasic.fitnesstracker.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun LabeledIconTextRow(
    icon: ImageVector,
    iconTint: Color,
    label: String? = null,
    value: String
) {
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (!label.isNullOrEmpty()) "$label: $value" else value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
