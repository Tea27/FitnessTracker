package com.tbasic.fitnesstracker.data

import androidx.compose.ui.graphics.vector.ImageVector

enum class SelectionType {
    SINGLE, MULTIPLE
}

data class MealSelectionOption(
    val id: String,
    val label: String,
    val isSelected: Boolean = false
)

data class MealSelectionSection(
    val id: String,
    val title: String,
    val selectionType: SelectionType,
    val options: List<MealSelectionOption>,
    val description: String,
    val icon: ImageVector? = null
)
