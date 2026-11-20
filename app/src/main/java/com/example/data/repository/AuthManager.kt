package com.example.data.repository

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.MutableContextWrapper
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.example.data.model.InitialCatalog
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object GoogleAuthManager {
    private const val TAG = "Auth"

    fun authStateFlow(auth: FirebaseAuth = Firebase.auth): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    fun isVerifiedOwnerUser(user: FirebaseUser?): Boolean {
        if (user == null) return false
        return user.email.equals(InitialCatalog.OWNER_EMAIL, ignoreCase = true) && user.isEmailVerified
    }

    /**
     * One-tap frictionless Google Sign-In / Sign-Up flow:
     * 1. Checks previously authorized accounts for silent auto-login.
     * 2. If none are authorized yet (new user or signed out), automatically opens the
     *    Google One-Tap bottom sheet displaying ALL Gmail accounts on the device so
     *    the user can pick one and sign in / sign up in a single tap.
     */
    fun attemptAutoSignIn(
        context: Context,
        credentialManager: CredentialManager,
        onAuthSuccess: () -> Unit,
        onUnauthenticated: () -> Unit,
        onSignInStarted: () -> Unit = {},
        scope: CoroutineScope
    ) {
        if (Firebase.auth.currentUser != null) {
            onAuthSuccess()
            return
        }
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (_: Exception) {
            onUnauthenticated()
            return
        }
        val uiContext = context.toCredentialUiContext()

        val authorizedOnlyOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()
        val authorizedRequest = GetCredentialRequest.Builder()
            .addCredentialOption(authorizedOnlyOption)
            .build()

        scope.launch {
            try {
                onSignInStarted()
                val cred = credentialManager.getCredential(uiContext, authorizedRequest).credential
                signInWithGoogleCredential(cred, onAuthSuccess) {
                    onUnauthenticated()
                }
            } catch (_: NoCredentialException) {
                // No previously authorized account yet -> show ALL Gmail accounts on device automatically
                showAllGoogleAccountsOneTap(
                    uiContext = uiContext,
                    clientId = clientId,
                    credentialManager = credentialManager,
                    onAuthSuccess = onAuthSuccess,
                    onDismissedOrUnavailable = onUnauthenticated
                )
            } catch (e: GetCredentialCancellationException) {
                Log.w(TAG, "Auto Google Sign-In cancelled or dismissed: ${e.message}", e)
                onUnauthenticated()
            } catch (e: Exception) {
                Log.w(TAG, "Auto Google Sign-In unavailable: ${e.message}")
                onUnauthenticated()
            }
        }
    }

    private suspend fun showAllGoogleAccountsOneTap(
        uiContext: Context,
        clientId: String,
        credentialManager: CredentialManager,
        onAuthSuccess: () -> Unit,
        onDismissedOrUnavailable: () -> Unit
    ) {
        val allAccountsOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(false)
            .build()
        val allAccountsRequest = GetCredentialRequest.Builder()
            .addCredentialOption(allAccountsOption)
            .build()

        try {
            val cred = credentialManager.getCredential(uiContext, allAccountsRequest).credential
            signInWithGoogleCredential(cred, onAuthSuccess) {
                onDismissedOrUnavailable()
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w(TAG, "Google account picker dismissed by user: ${e.message}", e)
            onDismissedOrUnavailable()
        } catch (_: NoCredentialException) {
            onDismissedOrUnavailable()
        } catch (e: Exception) {
            Log.w(TAG, "One-Tap all-accounts sheet unavailable: ${e.message}")
            onDismissedOrUnavailable()
        }
    }

    /**
     * Interactive Sign-In button using GetSignInWithGoogleOption:
     * Always opens the Google account chooser listing all Gmail accounts on the device
     * (plus "Add another account"), even if the user previously dismissed One-Tap.
     */
    fun onGoogleSignInClicked(
        context: Context,
        credentialManager: CredentialManager,
        onAuthSuccess: () -> Unit,
        onAuthError: (String) -> Unit,
        scope: CoroutineScope,
        onAuthCancelled: () -> Unit = {}
    ) {
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (_: Exception) {
            onAuthError("Google Sign-In configuration missing: default_web_client_id not found")
            return
        }
        val uiContext = context.toCredentialUiContext()
        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        scope.launch {
            try {
                val cred = credentialManager.getCredential(uiContext, request).credential
                signInWithGoogleCredential(cred, onAuthSuccess, onAuthError)
            } catch (e: GetCredentialCancellationException) {
                Log.w(TAG, "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
                onAuthCancelled()
            } catch (e: Exception) {
                Log.e(TAG, "Google Sign-In failed", e)
                onAuthError(e.localizedMessage ?: "Google Sign-In failed. Please try again.")
            }
        }
    }

    private suspend fun signInWithGoogleCredential(
        cred: androidx.credentials.Credential,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (cred is CustomCredential && cred.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val token = GoogleIdTokenCredential.createFrom(cred.data).idToken
            Firebase.auth.signInWithCredential(GoogleAuthProvider.getCredential(token, null)).await()
            onSuccess()
        } else {
            onError("Unexpected credential type returned by Google Sign-In.")
        }
    }

    fun signOut(
        credentialManager: CredentialManager,
        scope: CoroutineScope,
        onSignOutComplete: () -> Unit = {}
    ) {
        scope.launch {
            withContext(NonCancellable) {
                try {
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to clear credential state: ${e.message}")
                } finally {
                    Firebase.auth.signOut()
                    onSignOutComplete()
                }
            }
        }
    }

    private fun Context.toCredentialUiContext(): Context {
        val activity = findActivity()
        return if (activity != null) MutableContextWrapper(activity) else this
    }

    private tailrec fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
