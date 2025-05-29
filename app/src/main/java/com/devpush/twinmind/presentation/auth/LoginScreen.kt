package com.devpush.twinmind.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.devpush.twinmind.R

@Composable
fun LoginScreen(
    navController: NavHostController,
    onGoogleSignIn: () -> Unit = {},
    onAppleSignIn: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF5A7CA7), // top blue
                        Color(0xFFDDB893)  // bottom warm beige
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_twinmind_logo),
                contentDescription = "TwinMind Logo",
                modifier = Modifier
                    .height(120.dp)
            )

            Spacer(modifier = Modifier.height(60.dp))

            // Google Sign-In Button
            SignInButton(
                icon = painterResource(id = R.drawable.rounded_login_24),
                text = "Continue with Google",
                onClick = onGoogleSignIn
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Apple Sign-In Button
            SignInButton(
                icon = painterResource(id = R.drawable.ic_apple),
                text = "Continue with Apple",
                onClick = onAppleSignIn
            )
        }

        // Bottom links
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = "Privacy Policy",
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.clickable { /* TODO */ }
            )
            Spacer(modifier = Modifier.width(24.dp))
            Text(
                text = "Terms of Service",
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.clickable { /* TODO */ }
            )
        }
    }
}

@Composable
fun SignInButton(
    icon: Painter,
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 12.dp)
            )
            Text(
                text = text,
                color = Color.Black,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}