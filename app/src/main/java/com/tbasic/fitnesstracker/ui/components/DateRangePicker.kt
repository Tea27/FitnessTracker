package com.tbasic.fitnesstracker.ui.components

import android.app.DatePickerDialog
import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tbasic.fitnesstracker.R
import com.tbasic.fitnesstracker.localization.LocalLocalizedContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DateRangePicker(
    startDate: Long?,
    endDate: Long?,
    onStartDateSelected: (Long?) -> Unit,
    onEndDateSelected: (Long?) -> Unit,
    allowPast: Boolean = true,
    allowFuture: Boolean = false // default ponašanje: zabrani budućnost
) {
    val localizedContext = LocalLocalizedContext.current
    val dateFormatter = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }

    val startPicker = rememberDatePickerLauncher(
        onDateSelected = onStartDateSelected,
        allowPast = allowPast,
        allowFuture = allowFuture
    )
    val endPicker = rememberDatePickerLauncher(
        onDateSelected = onEndDateSelected,
        allowPast = allowPast,
        allowFuture = allowFuture
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = localizedContext.getString(R.string.filter_by_date),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        DateField(
            label = localizedContext.getString(R.string.from),
            date = startDate,
            onClick = { startPicker.launch(startDate) },
            onClear = { onStartDateSelected(null) },
            dateFormatter = dateFormatter
        )

        Spacer(modifier = Modifier.height(12.dp))

        DateField(
            label = localizedContext.getString(R.string.to),
            date = endDate,
            onClick = { endPicker.launch(endDate) },
            onClear = { onEndDateSelected(null) },
            dateFormatter = dateFormatter
        )
    }
}

@Composable
fun DateField(
    label: String,
    date: Long?,
    onClick: () -> Unit,
    onClear: (() -> Unit)? = null,
    dateFormatter: SimpleDateFormat
) {
    val localizedContext = LocalLocalizedContext.current

    val text = date?.let { dateFormatter.format(Date(it)) } ?: ""
    val placeholder = localizedContext.getString(R.string.date_format)
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = shape
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(2.dp))

                if (text.isNotEmpty()) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            if (date != null && onClear != null) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = localizedContext.getString(R.string.clear_date_description, label)
                    )
                }
            }
        }
    }
}

@Composable
fun rememberDatePickerLauncher(
    onDateSelected: (Long) -> Unit,
    allowPast: Boolean? = true,
    allowFuture: Boolean? = false
): DatePickerLauncher {
    val context = LocalContext.current

    return remember {
        DatePickerLauncher(context, onDateSelected, allowPast, allowFuture)
    }
}

class DatePickerLauncher(
    private val context: Context,
    private val onDateSelected: (Long) -> Unit,
    private val allowPast: Boolean? = true,
    private val allowFuture: Boolean? = false
) {
    fun launch(initial: Long?) {
        val calendar = Calendar.getInstance()
        initial?.let { calendar.timeInMillis = it }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val uiMode = context.resources.configuration.uiMode
        val isDarkTheme = (uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val dialogTheme = if (isDarkTheme) {
            android.R.style.Theme_Material_Dialog
        } else {
            android.R.style.Theme_Material_Light_Dialog
        }

        val pickerDialog = DatePickerDialog(context, dialogTheme, { _, y, m, d ->
            val pickedCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, y)
                set(Calendar.MONTH, m)
                set(Calendar.DAY_OF_MONTH, d)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            onDateSelected(pickedCal.timeInMillis)
        }, year, month, day)

        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        // Primijeni ograničenja
        if (!allowPast!!) pickerDialog.datePicker.minDate = todayStart
        if (!allowFuture!!) pickerDialog.datePicker.maxDate = todayStart

        pickerDialog.show()
    }
}

