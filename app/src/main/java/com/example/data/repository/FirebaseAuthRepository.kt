package com.example.data.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val credentialManager: CredentialManager = CredentialManager.create(context)

    // Mandatory custom database ID resolution
    private val db: FirebaseFirestore by lazy {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
    }

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _isCloudSyncing = MutableStateFlow(false)
    val isCloudSyncing: StateFlow<Boolean> = _isCloudSyncing.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
            firebaseAuth.currentUser?.let { user ->
                syncUserProfile(user)
            }
        }
    }

    fun requireUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("User must be signed in with Google")
    }

    fun signInWithGoogle(
        activity: Activity,
        scope: CoroutineScope,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            onError("Web Client ID not found: ${e.message}")
            return
        }

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

        scope.launch(Dispatchers.Main) {
            try {
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    auth.signInWithCredential(authCredential).await()
                    _currentUser.value = auth.currentUser
                    onSuccess()
                } else {
                    onError("Unexpected credential format received.")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w("FirebaseAuthRepository", "Google Sign-In cancelled by user: ${e.message}")
            } catch (e: Exception) {
                Log.e("FirebaseAuthRepository", "Google Sign-In failure", e)
                onError(e.localizedMessage ?: "Google Sign-In failed.")
            }
        }
    }

    fun signOut(scope: CoroutineScope, onComplete: () -> Unit) {
        auth.signOut()
        _currentUser.value = null
        scope.launch(Dispatchers.IO) {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e("FirebaseAuthRepository", "Error clearing credentials", e)
            } finally {
                launch(Dispatchers.Main) {
                    onComplete()
                }
            }
        }
    }

    private fun syncUserProfile(user: FirebaseUser) {
        val uid = user.uid
        val profileData = mapOf(
            "userId" to uid,
            "displayName" to (user.displayName ?: "Panchayat Officer"),
            "email" to (user.email ?: ""),
            "role" to "SUPERVISOR",
            "updatedAt" to FieldValue.serverTimestamp()
        )
        db.collection("users").document(uid).collection("profile").document("info")
            .set(profileData)
            .addOnFailureListener { e ->
                Log.w("FirebaseAuthRepository", "Profile sync notice: ${e.message}")
            }
    }

    fun saveCollectionToFirestore(
        pointId: String,
        pointName: String,
        villageName: String,
        status: String,
        wetKg: Double,
        dryKg: Double,
        hazardousKg: Double,
        notes: String?
    ) {
        val uid = auth.currentUser?.uid ?: return
        _isCloudSyncing.value = true
        val payload = mapOf(
            "id" to pointId,
            "userId" to uid,
            "pointId" to pointId,
            "pointName" to pointName,
            "villageName" to villageName,
            "status" to status,
            "wetKg" to wetKg,
            "dryKg" to dryKg,
            "hazardousKg" to hazardousKg,
            "notes" to (notes ?: ""),
            "timestamp" to FieldValue.serverTimestamp()
        )
        db.collection("users").document(uid).collection("collections").document(pointId)
            .set(payload)
            .addOnCompleteListener {
                _isCloudSyncing.value = false
            }
    }

    fun saveTelemetryToFirestore(vanId: String, lat: Double, lng: Double, speed: Float, heading: Float) {
        val uid = auth.currentUser?.uid ?: return
        val payload = mapOf(
            "id" to vanId,
            "userId" to uid,
            "vanId" to vanId,
            "latitude" to lat,
            "longitude" to lng,
            "speedKmH" to speed,
            "heading" to heading,
            "timestamp" to FieldValue.serverTimestamp()
        )
        db.collection("users").document(uid).collection("telemetry").document(vanId)
            .set(payload)
    }
}
