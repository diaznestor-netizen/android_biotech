package com.biobox.biotech.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biobox.biotech.BuildConfig
import com.biobox.biotech.R
import com.biobox.biotech.core.common.UiState
import com.biobox.biotech.presentation.theme.AzulOscuro
import com.biobox.biotech.presentation.theme.Blanco
import com.biobox.biotech.presentation.theme.VerdePrincipal
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    sessionState: UiState<Boolean>,
    onValidateSession: () -> Unit,
    onNavigate: (Boolean) -> Unit
) {
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.7f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(1200))
        scale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        onValidateSession()
    }

    LaunchedEffect(sessionState) {
        if (sessionState is UiState.Success) {
            delay(900)
            onNavigate(sessionState.data)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(AzulOscuro, Color(0xFF071B2A))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.logo_biotech_clean),
                contentDescription = "Logo BioTech",
                modifier = Modifier
                    .size(180.dp)
                    .scale(scale.value)
                    .alpha(alpha.value)
            )
            Spacer(modifier = Modifier.height(24.dp))
            /*
            Text(
                text = "BIOTECH",
                color = Blanco,
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
                modifier = Modifier.alpha(alpha.value)
            )
            Text(
                text = "Control inteligente de materiales y maquinaria industrial.",
                color = VerdePrincipal,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.alpha(alpha.value)
            )
            */

            Spacer(modifier = Modifier.height(60.dp))

            CircularProgressIndicator(
                modifier = Modifier
                    .size(32.dp)
                    .alpha(alpha.value),
                color = Blanco,
                strokeWidth = 3.dp
            )
        }

        Text(
            text = "v${BuildConfig.VERSION_NAME}",
            color = Blanco.copy(alpha = 0.3f),
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}
