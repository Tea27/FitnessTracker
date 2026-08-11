package com.tbasic.fitnesstracker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.tbasic.fitnesstracker.localization.LocalLocalizedContext
import com.tbasic.fitnesstracker.localization.LocalizedContextProvider
import com.tbasic.fitnesstracker.ui.navigation.AppNavigation
import com.tbasic.fitnesstracker.ui.theme.FitnessTrackerTheme
import com.tbasic.fitnesstracker.utils.scheduleDailyTrainingNotificationWorker
import com.tbasic.fitnesstracker.utils.storeUserId
import com.tbasic.fitnesstracker.vm.AuthViewModel
import com.tbasic.fitnesstracker.vm.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var showToastMessageId by mutableStateOf<Int?>(null)
    private var hasRequestedNotificationPermission = false

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Kad se dozvoli, pokrece worker s točnim jezikom iz prefs
                val languageCode = getSelectedLanguageCode()
                scheduleDailyTrainingNotificationWorker(this, languageCode)
            } else {
                showToastMessageId = R.string.notif_permission_denied
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FitnessTrackerTheme {
                val userViewModel: UserViewModel = hiltViewModel()
                val authViewModel: AuthViewModel = hiltViewModel()
                val selectedLang by userViewModel.selectedLanguage
                val isLoggedIn by authViewModel.isUserLoggedIn.collectAsState()
                val context = LocalContext.current

                LaunchedEffect(isLoggedIn) {
                    val userId = authViewModel.currentUserId() ?: "unknown"
                    storeUserId(context, userId)
                    userViewModel.loadLanguage(isLoggedIn)
                }

                // Kad se dobije selectedLang, spremi u prefs i pitaj za permission / startaj worker
                LaunchedEffect(isLoggedIn, selectedLang) {
                    if (isLoggedIn && selectedLang != null) {
                        saveSelectedLanguageCode(context, selectedLang!!)
                        askNotificationPermissionIfNeeded(selectedLang!!)
                    }
                }

                if (selectedLang != null) {
                    LocalizedContextProvider(languageCode = selectedLang!!) {
                        val localizedContext = LocalLocalizedContext.current

                        LaunchedEffect(showToastMessageId) {
                            showToastMessageId?.let { msgId ->
                                Toast.makeText(localizedContext, localizedContext.getString(msgId), Toast.LENGTH_LONG).show()
                                showToastMessageId = null
                            }
                        }

                        AppNavigation(
                            userViewModel = userViewModel,
                            authViewModel = authViewModel
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

    private fun askNotificationPermissionIfNeeded(languageCode: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                scheduleDailyTrainingNotificationWorker(this, languageCode)
            } else {
                if (!hasRequestedNotificationPermission) {
                    hasRequestedNotificationPermission = true
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            scheduleDailyTrainingNotificationWorker(this, languageCode)
        }
    }

    private fun saveSelectedLanguageCode(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("languageCode", languageCode).apply()
    }

    private fun getSelectedLanguageCode(): String {
        val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        return prefs.getString("languageCode", "en") ?: "en"
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FitnessTrackerTheme {
        Greeting("Android")
    }
}
