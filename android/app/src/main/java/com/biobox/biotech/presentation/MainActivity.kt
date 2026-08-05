package com.biobox.biotech.presentation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.biobox.biotech.core.security.BiometricAuth
import com.biobox.biotech.core.security.SessionMonitor
import com.biobox.biotech.presentation.auth.AuthViewModel
import com.biobox.biotech.presentation.navigation.BioTechNav
import com.biobox.biotech.presentation.theme.BioTechTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    @Inject lateinit var sessionMonitor: SessionMonitor
    @Inject lateinit var biometricAuth: BiometricAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BioTechTheme {
                val authViewModel: AuthViewModel = hiltViewModel()

                LaunchedEffect(Unit) {
                    sessionMonitor.startMonitoring {
                        authViewModel.logout()
                    }
                }

                BioTechNav(
                    biometricAuth = biometricAuth
                )
            }
        }
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        sessionMonitor.recordActivity()
    }

    override fun onPause() {
        super.onPause()
        if (sessionMonitor.isSessionExpired()) {
            sessionMonitor.recordActivity()
        }
    }
}
