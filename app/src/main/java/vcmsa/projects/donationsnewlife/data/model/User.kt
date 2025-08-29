package vcmsa.projects.donationsnewlife.data.model

data class User(
    val fullName: String = "",
    val username: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val userId: String = "" // This will come from Firebase Auth UID
)

