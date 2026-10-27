package dev.onn.launcher.ui

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import dev.onn.launcher.system.HomeSignals

/** Pantalla de inicio unica (HOME + LEANBACK_LAUNCHER, singleTask). */
class HomeActivity : ComponentActivity() {
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // En la pantalla de inicio, Atras no hace nada (spec UX).
        onBackPressedDispatcher.addCallback(this) { }
        setContent { HomeScreen(viewModel) }
    }

    override fun onResume() {
        super.onResume()
        HomeSignals.lastResumeMs = SystemClock.uptimeMillis()
        viewModel.refreshRecents()
    }
}