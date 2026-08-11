package com.tbasic.fitnesstracker.ui.components

import android.content.Context
import android.util.Log
import android.widget.Space
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tbasic.fitnesstracker.R
import com.tbasic.fitnesstracker.localization.LocalLocalizedContext
import com.tbasic.fitnesstracker.localization.supportedLanguages
import com.tbasic.fitnesstracker.vm.UserViewModel
import java.util.Locale

@Composable
fun BmiCardSection(viewModel: UserViewModel, navToProfile: () -> Unit) {
    val user by viewModel.currentUser

    val weight = user?.weight
    val height = user?.height

    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(16.dp)

    var showDialog by remember { mutableStateOf(false) }

    val isDarkTheme = isSystemInDarkTheme()

    val localizedContext = LocalLocalizedContext.current
    val currentLanguageCode = user?.language ?: "en" // fallback na engleski ako null

    val localeMap = mapOf(
        "en" to Locale.ENGLISH,
        "hr" to Locale("hr"),
        "de" to Locale.GERMAN,
        "fr" to Locale.FRENCH,
        "it" to Locale.ITALIAN
    )

    val userLocale = localeMap[currentLanguageCode] ?: Locale.ENGLISH

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (weight != null && height != null && weight > 30f && height > 100f) {
                val heightInMeters = height / 100f
                val bmi = weight / (heightInMeters * heightInMeters)
                val bmiCategory = viewModel.getBmiCategory(bmi, localizedContext)

                val (icon, iconColor) = when (bmiCategory) {
                    localizedContext.getString(R.string.underweight) -> Icons.Default.Warning to colors.tertiary
                    localizedContext.getString(R.string.normal_weight) -> Icons.Default.CheckCircle to colors.primary
                    localizedContext.getString(R.string.overweight) -> Icons.Default.Warning to colors.secondary
                    else -> Icons.Default.Warning to colors.error
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = localizedContext.getString(R.string.your_bmi),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))


                UniformImage(
                    resId = R.drawable.bmi,
                    contentDescription = "BMI Icon",
                    size = 120.dp,
                    shape = shape,
                    modifier = Modifier
                        .padding(8.dp) .shadow(2.dp, shape)
                        .then(
                            if (!isDarkTheme) Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow, shape) else Modifier.background(MaterialTheme.colorScheme.surfaceContainer, shape)
                        )
                )


                Spacer(modifier = Modifier.height(12.dp))
                Log.d("ovo je locale", user?.language.toString())
                Text(
                    text = String.format(userLocale, "%.1f", bmi),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = iconColor
                )
                Text(
                    text = bmiCategory,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = colors.onSurface
                )


                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = localizedContext.getString(R.string.bmi_categories),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = localizedContext.getString(R.string.bmi_info),
                                tint = colors.primary,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { showDialog = true }
                            )

                        }

                        BmiCategoryRow( localizedContext.getString(R.string.bmi_below_18_5),  localizedContext.getString(R.string.underweight))
                        BmiCategoryRow("18.5 – 24.9",  localizedContext.getString(R.string.normal_weight))
                        BmiCategoryRow("25 – 29.9",  localizedContext.getString(R.string.overweight))
                        BmiCategoryRow( localizedContext.getString(R.string.bmi_30_and_above),  localizedContext.getString(R.string.obesity))
                    }
                }


            } else {
                // Fallback kada nema podataka
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text =  localizedContext.getString(R.string.bmi_not_available),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription =  localizedContext.getString(R.string.bmi_info),
                                tint = colors.primary,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { showDialog = true }
                            )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text =  localizedContext.getString(R.string.please_complete_profile),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onErrorContainer.copy(alpha = 0.8f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = navToProfile,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Complete profile")
                }
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    confirmButton = {
                        Text(
                            text = "OK",
                            modifier = Modifier
                                .clickable { showDialog = false }
                                .padding(8.dp),
                            color = colors.primary
                        )
                    },
                    title = {
                        Text( localizedContext.getString(R.string.bmi_info))
                    },
                    text = {
                        Text(
                            text = localizedContext.getString(R.string.bmi_explanation),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }


        }
    }
}

@Composable
fun BmiCategoryRow(range: String, label: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = range,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}



