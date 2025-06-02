package com.devpush.twinmind.presentation.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.devpush.twinmind.R
import com.devpush.twinmind.domain.repository.UserPreferencesRepository
import com.devpush.twinmind.presentation.navigation.Screen
import com.devpush.twinmind.ui.theme.BeigeBottom
import com.devpush.twinmind.ui.theme.BlueTop
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import timber.log.Timber

@Composable
fun LoginScreen(
    navController: NavHostController,
    onAppleSignIn: () -> Unit = {}
) {
    val context = navController.context
    val coroutineScope = rememberCoroutineScope()
    val userPreferencesRepository: UserPreferencesRepository = koinInject()

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    FirebaseAuthHelper.signInWithFirebase(
                        idToken = idToken,
                        context = context,
                        navController = navController,
                        onLoginSuccess = {
                            coroutineScope.launch {
                                userPreferencesRepository.saveLoginStatus(true)

                                navController.navigate(Screen.Main.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }
            } catch (e: ApiException) {
                Timber.tag("LoginScreen").e(" - Google sign-in failed ${e.message}")
            }
        }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        BlueTop,
                        BeigeBottom
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
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "TwinMind Logo",
                modifier = Modifier
                    .height(120.dp)
            )

            Spacer(modifier = Modifier.height(60.dp))

            // Google Sign-In Button
            SignInButton(
                icon = painterResource(id = R.drawable.google),
                text = "Continue with Google",
                onClick = {
                    val signInClient = FirebaseAuthHelper.getGoogleSignInClient(context)
                    launcher.launch(signInClient.signInIntent)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Apple Sign-In Button
            SignInButton(
                icon = painterResource(id = R.drawable.apple),
                text = "Continue with Apple",
                onClick = {
                    coroutineScope.launch {
                        userPreferencesRepository.saveLoginStatus(true)
                        navController.navigate(Screen.Settings.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                    onAppleSignIn() // Call original lambda if needed
                }
            )
        }

        // Bottom links
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = stringResource(R.string.privacy),
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.clickable { /* TODO */ }
            )
            Spacer(modifier = Modifier.width(24.dp))
            Text(
                text = stringResource(R.string.term),
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
                    .size(48.dp)
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