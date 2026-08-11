package com.tbasic.fitnesstracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun LocationSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onLocationSelected: (String) -> Unit,
    fetchSuggestions: suspend (String) -> List<String>
) {
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    // Flag koji prati je li korisnik aktivno mijenjao tekst u polju
    var userTyped by remember { mutableStateOf(false) }

    // Flag koji označava da li je query promijenjen izborom iz dropdowna
    var ignoreNextFetch by remember { mutableStateOf(false) }

    LaunchedEffect(query, userTyped) {
        if (ignoreNextFetch) {
            ignoreNextFetch = false
            return@LaunchedEffect
        }

        // Fetchaj samo ako je korisnik počeo tipkati i query je dovoljne duljine
        if (!userTyped || query.length < 3) {
            suggestions = emptyList()
            dropdownExpanded = false
            return@LaunchedEffect
        }

        isLoading = true
        delay(500) // debounce
        suggestions = fetchSuggestions(query)
        isLoading = false
        dropdownExpanded = suggestions.isNotEmpty()
    }

    Column {
        OutlinedTextField(
            value = query,
            onValueChange = {
                onQueryChange(it)
                userTyped = true
            },
            label = { Text("Location") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = {
                        onQueryChange("")
                        suggestions = emptyList()
                        dropdownExpanded = false
                        userTyped = false
                        onLocationSelected("")
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "Clear text"
                        )
                    }
                }
            }
        )

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }

        DropdownMenu(
            expanded = dropdownExpanded,
            onDismissRequest = { dropdownExpanded = false }
        ) {
            suggestions.forEach { suggestion ->
                DropdownMenuItem(
                    text = { Text(suggestion) },
                    onClick = {
                        ignoreNextFetch = true
                        onQueryChange(suggestion)
                        suggestions = emptyList()
                        dropdownExpanded = false
                        userTyped = false
                        onLocationSelected(suggestion)
                    }
                )
            }
        }
    }
}
