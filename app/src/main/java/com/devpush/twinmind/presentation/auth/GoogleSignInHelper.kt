package com.devpush.twinmind.presentation.auth

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.navigation.NavHostController
import com.devpush.twinmind.presentation.navigation.Screen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import timber.log.Timber

object FirebaseAuthHelper {
    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(com.devpush.twinmind.R.string.default_web_client_id))
            .requestEmail()
            .requestScopes(Scope("https://www.googleapis.com/auth/calendar"))
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    fun signInWithFirebase(
        idToken: String,
        context: Context,
        navController: NavHostController,
        onLoginSuccess: () -> Unit = {},
        onLoginFailure: () -> Unit = {}
    ) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(context as Activity) { task ->
                if (task.isSuccessful) {
                    onLoginSuccess()
                    navController.navigate(Screen.Settings.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                } else {
                    Toast.makeText(context, "Login Failed", Toast.LENGTH_SHORT).show()
                    Timber.tag("LoginScreen").e(task.exception, "Firebase sign-in failed")
                    onLoginFailure()
                }
            }
    }
}