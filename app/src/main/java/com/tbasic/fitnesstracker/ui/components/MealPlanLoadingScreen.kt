package com.tbasic.fitnesstracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.tbasic.fitnesstracker.R
import com.tbasic.fitnesstracker.localization.LocalLocalizedContext

@Composable
fun MealPlanLoadingScreen() {
    val robotComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lt))
    val robot2Composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading_food))
    val robotProgress by animateLottieCompositionAsState(
        robotComposition,
        iterations = LottieConstants.IterateForever
    )
    val robot2Progress by animateLottieCompositionAsState(
        robot2Composition,
        iterations = LottieConstants.IterateForever
    )
    val localizedContext = LocalLocalizedContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (robot2Composition == null) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 4.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                LottieAnimation(
                    composition = robot2Composition,
                    progress = { robot2Progress },
                    modifier = Modifier.height(180.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = localizedContext.getString(R.string.generating_meal_plan_short),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = localizedContext.getString(R.string.generating_meal_plan_detailed),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (robotComposition == null) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 4.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(8.dp)
                ) {
                    LottieAnimation(
                        composition = robotComposition,
                        progress = { robotProgress },
                        modifier = Modifier.height(180.dp)
                    )
                }
            }
        }
    }
}
