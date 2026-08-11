package com.tbasic.fitnesstracker.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tbasic.fitnesstracker.R
import com.tbasic.fitnesstracker.localization.LocalLocalizedContext

@Composable
fun SortOrderToggle(
    descending: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val nextDescending = !descending
    val localizedContext = LocalLocalizedContext.current

    Button(onClick = { onToggle(nextDescending) }) {
        Icon(
            imageVector = if (nextDescending) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
            contentDescription = if (nextDescending) {
                localizedContext.getString(R.string.sort_newest_first_desc)
            } else {
                localizedContext.getString(R.string.sort_oldest_first_desc)
            }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (nextDescending) {
                localizedContext.getString(R.string.sort_newest_first)
            } else {
                localizedContext.getString(R.string.sort_oldest_first)
            }
        )
    }
}
