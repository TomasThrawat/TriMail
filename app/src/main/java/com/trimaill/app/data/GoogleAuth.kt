package com.trimaill.app.data

import android.content.Context
import android.util.Base64
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.trimaill.app.BuildConfig
import com.trimaill.app.model.Provider
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import java.security.SecureRandom

data class GoogleProfile(
    val email: String,
    val displayName: String?
)

object GoogleAuthPolicy {
    fun inferProvider(email: String, current: Provider): Provider {
        return if (email.trim().endsWith("@gmail.com", ignoreCase = true)) {
            Provider.GOOGLE
        } else {
            current
        }
    }

    fun matchesEmail(expected: String, actual: String): Boolean {
        return expected.trim().equals(actual.trim(), ignoreCase = true)
    }
}

class GoogleAuthClient(context: Context) {
    private val appContext = context.applicationContext
    private val credentialManager = CredentialManager.create(appContext)

    suspend fun signIn(expectedEmail: String): Result<GoogleProfile> {
        val webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID.trim()
        if (webClientId.isEmpty()) {
            return Result.failure(
                IllegalStateException(
                    "Google Sign-In is not configured. Set GOOGLE_WEB_CLIENT_ID for the build."
                )
            )
        }

        val option = GetSignInWithGoogleOption.Builder(webClientId)
            .setNonce(generateSecureRandomNonce())
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        return try {
            val result = credentialManager.getCredential(
                request = request,
                context = appContext
            )
            val credential = result.credential
            if (
                credential !is CustomCredential ||
                credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                Result.failure(IllegalStateException("Google did not return a supported credential."))
            } else {
                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val email = googleCredential.email?.trim().orEmpty()
                if (email.isEmpty()) {
                    Result.failure(IllegalStateException("Google did not return an email address."))
                } else if (!GoogleAuthPolicy.matchesEmail(expectedEmail, email)) {
                    Result.failure(
                        IllegalStateException(
                            "The selected Google account does not match the email you typed."
                        )
                    )
                } else {
                    Result.success(
                        GoogleProfile(
                            email = email,
                            displayName = googleCredential.displayName
                        )
                    )
                }
            }
        } catch (error: GoogleIdTokenParsingException) {
            Result.failure(IllegalStateException("Google returned an invalid sign-in response.", error))
        } catch (error: GetCredentialException) {
            Result.failure(
                IllegalStateException(
                    error.message ?: "Google sign-in was cancelled or unavailable.",
                    error
                )
            )
        }
    }

    private fun generateSecureRandomNonce(byteLength: Int = 32): String {
        val randomBytes = ByteArray(byteLength)
        SecureRandom().nextBytes(randomBytes)
        return Base64.encodeToString(
            randomBytes,
            Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING
        )
    }
}
