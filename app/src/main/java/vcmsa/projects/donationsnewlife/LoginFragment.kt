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

class LoginFragment : Fragment() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvSignUp: TextView

    private val repo = AuthRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        etEmail = view.findViewById(R.id.email)
        etPassword = view.findViewById(R.id.password)
        btnLogin = view.findViewById(R.id.login)
        progressBar = view.findViewById(R.id.loading)
        tvSignUp = view.findViewById(R.id.tvSignUp)

        btnLogin.setOnClickListener { doLogin() }

        tvSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        return view
    }

    private fun doLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        if (email.isEmpty()) {
            etEmail.error = "Email required"
            etEmail.requestFocus()
            return
        }
        if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            etPassword.requestFocus()
            return
        }

        setLoading(true)

        repo.login(email, password)
            .addOnSuccessListener {
                setLoading(false)
                Toast.makeText(requireContext(), "Logged in", Toast.LENGTH_SHORT).show()

                // Navigate to Dashboard (no AdminDashboardFragment)
                findNavController().navigate(R.id.action_loginFragment_to_dashboardFragment)
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(
                    requireContext(),
                    e.localizedMessage ?: "Login failed",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !loading
        etEmail.isEnabled = !loading
        etPassword.isEnabled = !loading
    }
}