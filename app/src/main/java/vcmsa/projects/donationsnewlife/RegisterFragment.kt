package vcmsa.projects.donationsnewlife

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import vcmsa.projects.donationsnewlife.data.repository.AuthRepository

class RegisterFragment : Fragment() {

    private lateinit var etFullName: EditText
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvAlreadyHaveAccount: TextView

    private val repo = AuthRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        // Bind views
        etFullName = view.findViewById(R.id.fullName)
        etUsername = view.findViewById(R.id.username)
        etEmail = view.findViewById(R.id.email)
        etPhone = view.findViewById(R.id.phone)
        etPassword = view.findViewById(R.id.password)
        etConfirmPassword = view.findViewById(R.id.ConfirmPassword)
        btnRegister = view.findViewById(R.id.register)
        progressBar = view.findViewById(R.id.progressBar)
        tvAlreadyHaveAccount = view.findViewById(R.id.tvAlreadyHaveAccount)

        btnRegister.setOnClickListener { registerUser() }

        tvAlreadyHaveAccount.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        return view
    }

    private fun registerUser() {
        val fullName = etFullName.text.toString().trim()
        val username = etUsername.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        // Basic validation
        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() ||
            password.isEmpty() || confirmPassword.isEmpty()
        ) {
            toast("Please fill in all required fields")
            return
        }
        if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            etPassword.requestFocus()
            return
        }
        if (password != confirmPassword) {
            etConfirmPassword.error = "Passwords do not match"
            etConfirmPassword.requestFocus()
            return
        }

        setLoading(true)

        // Use your repository: it creates the Firebase Auth user and writes Firestore user doc
        repo.register(
            email = email,
            password = password,
            fullName = fullName,
            phone = phone,
            username = username
        )
            .addOnSuccessListener {
                setLoading(false)
                toast("Registration successful")

                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            }
            .addOnFailureListener { e ->
                setLoading(false)
                toast(e.localizedMessage ?: "Registration failed")
            }
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        btnRegister.isEnabled = !loading
        etFullName.isEnabled = !loading
        etUsername.isEnabled = !loading
        etEmail.isEnabled = !loading
        etPhone.isEnabled = !loading
        etPassword.isEnabled = !loading
        etConfirmPassword.isEnabled = !loading
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}