package vcmsa.projects.donationsnewlife

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DashboardFragment : Fragment() {

    private lateinit var btnProfile: Button
    private lateinit var btnVolunteer: Button
    private lateinit var btnDonate: Button
    private lateinit var btnAdminEdit: Button // Admin-only button

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        btnProfile = view.findViewById(R.id.btnProfile)
        btnVolunteer = view.findViewById(R.id.btnVolunteer)
        btnDonate = view.findViewById(R.id.btnDonate)
        btnAdminEdit = view.findViewById(R.id.btnAdminEdit)

        btnProfile.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_userProfileFragment)
        }

        btnVolunteer.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_volunteerFragment)
        }

        btnDonate.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_donateFragment)
        }

        // Admin button click for testing
        btnAdminEdit.setOnClickListener {
            Toast.makeText(requireContext(), "Admin Edit Clicked!", Toast.LENGTH_SHORT).show()
        }

        checkUserRole()

        return view
    }

    private fun checkUserRole() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val role = doc.getString("role") ?: "user"

                // Toggle admin-only button visibility
                btnAdminEdit.visibility = if (role == "admin") View.VISIBLE else View.GONE
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to fetch role", Toast.LENGTH_SHORT).show()
            }
    }
}