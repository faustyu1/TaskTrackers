package ru.faustyu.tasktrackers

import android.animation.ObjectAnimator
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.faustyu.tasktrackers.navigation.AppNavigation
import ru.faustyu.tasktrackers.ui.theme.TaskTrackersTheme
import ru.faustyu.tasktrackers.viewmodel.SettingsViewModel
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen BEFORE super.onCreate
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Smooth fade-out exit animation for splash
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            val fadeOut = ObjectAnimator.ofFloat(
                splashScreenView.view,
                View.ALPHA,
                1f, 0f
            ).apply {
                duration = 500L
                interpolator = DecelerateInterpolator()
            }
            fadeOut.addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    splashScreenView.remove()
                }
            })
            fadeOut.start()
        }

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val currentLocale by settingsViewModel.currentLocale.collectAsStateWithLifecycle()

            LocaleWrapper(localeCode = currentLocale) {
                TaskTrackersTheme {
                    AppNavigation(settingsViewModel = settingsViewModel)
                }
            }
        }
    }
}

@Composable
fun LocaleWrapper(
    localeCode: String,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val localizedContext = if (localeCode != "system") {
        val locale = Locale(localeCode)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.createConfigurationContext(config)
    } else {
        context
    }

    CompositionLocalProvider(LocalContext provides localizedContext) {
        content()
    }
}