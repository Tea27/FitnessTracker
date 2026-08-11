package com.tbasic.fitnesstracker.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.tbasic.fitnesstracker.R
import com.tbasic.fitnesstracker.data.UserRoutine
import com.tbasic.fitnesstracker.localization.LocalLocalizedContext
import com.tbasic.fitnesstracker.navigation.User
import com.tbasic.fitnesstracker.ui.components.TopAppBarWithBack
import com.tbasic.fitnesstracker.vm.AuthViewModel

@Composable
fun ChangePasswordScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember {
        mutableStateOf(false)
    }

    var error by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalLocalizedContext.current
    val localizedTitle = context.getString(R.string.change_password)

    Scaffold(
        topBar = {
            TopAppBarWithBack(
                title = "",
                onBack = onBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text(context.getString(R.string.current_password)) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text(context.getString(R.string.new_password)) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            error = if (confirmPassword != newPassword) context.getString(R.string.passwords_do_not_match) else null
                        },
                        label = { Text(context.getString(R.string.confirm_new_password)) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )

                    if (error != null) {
                        Text(
                            text = error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    if (successMessage != null) {
                        AlertDialog(
                            onDismissRequest = {
                                successMessage = null
                                onBack()
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        successMessage = null
                                        onBack()
                                    }
                                ) {
                                    Text(context.getString(R.string.finish))
                                }
                            },
                            title = {
                                Text(text = context.getString(R.string.password_changed_title))
                            },
                            text = {
                                Text(text = successMessage ?: "")
                            }
                        )
                    }

                    Button(
                        onClick = {
                            error = null
                            successMessage = null

                            if (newPassword != confirmPassword) {
                                error = context.getString(R.string.passwords_do_not_match)
                                return@Button
                            }
                            if (newPassword.length < 6) {
                                error = context.getString(R.string.password_too_short)
                                return@Button
                            }

                            isLoading = true
                            authViewModel.changePassword(
                                currentPassword,
                                newPassword
                            ) { success, message ->
                                isLoading = false
                                if (success) {
                                    successMessage = context.getString(R.string.password_change_success)
                                    currentPassword = ""
                                    newPassword = ""
                                    confirmPassword = ""
                                } else {
                                    error = message
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(localizedTitle)
                        }
                    }
                }
            }
        }
    }
}
