package com.tbasic.fitnesstracker.ui.screens.routine

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.tbasic.fitnesstracker.R
import com.tbasic.fitnesstracker.localization.LocalLocalizedContext

@Composable
fun CongratulationsScreen(
    onDone: () -> Unit
) {
    val confettiComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.confetti))
    val completedComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.completed))
    val confettiProgress by animateLottieCompositionAsState(
        confettiComposition,
        iterations = LottieConstants.IterateForever
    )
    val completedProgress by animateLottieCompositionAsState(
        completedComposition,
        iterations = 1
    )
    val localizedContext = LocalLocalizedContext.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (confettiComposition == null) {
                CircularProgressIndicator()
            } else {
                LottieAnimation(
                    composition = confettiComposition,
                    progress = { confettiProgress },
                    modifier = Modifier.height(180.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (completedComposition == null) {
                CircularProgressIndicator()
            } else {
                LottieAnimation(
                    composition = completedComposition,
                    progress = { completedProgress },
                    modifier = Modifier.height(180.dp)
                )
            }
            Text(
                localizedContext.getString(R.string.congratulations),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                localizedContext.getString(R.string.workout_completed),
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onDone) {
                Text(localizedContext.getString(R.string.finish))
            }
        }
    }
}
