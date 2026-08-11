package com.tbasic.fitnesstracker.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tbasic.fitnesstracker.R
import com.tbasic.fitnesstracker.localization.LocalLocalizedContext
import com.tbasic.fitnesstracker.vm.AuthViewModel
import com.tbasic.fitnesstracker.vm.UserViewModel

@Composable
fun UserScreen(
    userViewModel: UserViewModel,
    authViewModel: AuthViewModel,
    onLogout: () -> Unit,
    onChangePassword: () -> Unit,
    onEditPhysicalData: () -> Unit,
    onSeeCaloriesClick: () -> Unit,
    onSeePreviousGoalsClick: () -> Unit
) {
    val localizedContext = LocalLocalizedContext.current
    val selectedLanguage = userViewModel.getCurrentLanguage()
    var expanded by remember { mutableStateOf(false) }

    val user by userViewModel.currentUser
    val firstName = user?.firstName ?: "User"
    val lastName = user?.lastName ?: ""
    val email = user?.email ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // --- User info: avatar + name ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .padding(12.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "$firstName $lastName",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }

        // --- Language Selector ---
        Column {
            Text(
                text = localizedContext.getString(R.string.language),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = MaterialTheme.shapes.medium
                    )
                    .clickable { expanded = true }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text("${selectedLanguage.flag} ${selectedLanguage.name}")
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    userViewModel.languages.forEach { language ->
                        DropdownMenuItem(
                            text = { Text("${language.flag} ${language.name}") },
                            onClick = {
                                userViewModel.setLanguage(language.code)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        // --- Action Buttons as Card Items ---
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ActionCard(
                title = localizedContext.getString(R.string.edit_physical_data),
                onClick = onEditPhysicalData
            )
            ActionCard(
                title = localizedContext.getString(R.string.view_past_goals),
                onClick = onSeePreviousGoalsClick
            )
            ActionCard(
                title = localizedContext.getString(R.string.tracked_calories),
                onClick = onSeeCaloriesClick
            )
            ActionCard(
                title = localizedContext.getString(R.string.change_password),
                onClick = onChangePassword
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- Logout ---
        OutlinedButton(
            onClick = {
                authViewModel.logout()
                onLogout()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = MaterialTheme.shapes.extraLarge,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text(
                text = localizedContext.getString(R.string.logout),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    onClick: () -> Unit,
    icon: ImageVector = Icons.Default.ArrowDropDown
) {
    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}
