package com.biobox.biotech.core.security

import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

@Composable
fun SecureScreen(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    DisposableEffect(Unit) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    content()
}

@Composable
fun SessionLockScreen(
    onUnlock: () -> Unit,
    biometricAuth: BiometricAuth,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    DisposableEffect(Unit) {
        if (activity != null && biometricAuth.isAvailable(context)) {
            biometricAuth.authenticate(
                activity = activity,
                title = "BioTech",
                subtitle = "Verifica tu identidad para continuar",
                onSuccess = { onUnlock.invoke() },
                onError = { onUnlock.invoke() }
            )
        } else {
            onUnlock.invoke()
        }
        onDispose { }
    }

    content()
}
