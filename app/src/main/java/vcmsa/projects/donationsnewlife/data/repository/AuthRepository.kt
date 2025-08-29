package vcmsa.projects.donationsnewlife.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

data class AppUser(
    val uid: String = "",
    val email: String = "",
    val fullName: String = "",
    val username: String = "",
    val phone: String = "",
    val role: String = "user",
    val createdAt: Timestamp = Timestamp.now()
)

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun currentUser() = auth.currentUser

    // username is optional to avoid breaking existing calls
    fun register(
        email: String,
        password: String,
        fullName: String,
        phone: String,
        username: String = "",
        role: String = "user"
    ): Task<AuthResult> {
        val normalizedEmail = email.trim()
        return auth.createUserWithEmailAndPassword(normalizedEmail, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                val userDoc = AppUser(
                    uid = uid,
                    email = normalizedEmail,
                    fullName = fullName.trim(),
                    username = username.trim(),
                    phone = phone.trim(),
                    role = role
                )
                db.collection("users").document(uid).set(userDoc)
            }
    }

    fun login(email: String, password: String): Task<AuthResult> {
        return auth.signInWithEmailAndPassword(email.trim(), password)
    }

    fun logout() = auth.signOut()
}