package vcmsa.projects.donationsnewlife

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PaymentFragment : Fragment() {

    private lateinit var tvBankDetails: TextView
    private lateinit var tvPaymentInfo: TextView
    private lateinit var btnDonate: Button
    private lateinit var tvGoalTitle: TextView
    private lateinit var progressBarGoal: ProgressBar
    private lateinit var tvProgressHint: TextView
    private lateinit var btnEditGoal: Button

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_payment, container, false)

        // Bind views
        tvBankDetails = view.findViewById(R.id.tvBankDetails)
        tvPaymentInfo = view.findViewById(R.id.tvPaymentInfo)
        btnDonate = view.findViewById(R.id.btnDonate)
        tvGoalTitle = view.findViewById(R.id.tvGoalTitle)
        progressBarGoal = view.findViewById(R.id.progressBarGoal)
        tvProgressHint = view.findViewById(R.id.tvProgressHint)
        btnEditGoal = view.findViewById(R.id.btnEditGoal)

        // Donate button (kept your PayPal link)
        btnDonate.setOnClickListener {
            val paypalUrl = "https://www.paypal.com/donate?hosted_button_id=EXAMPLE"
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(paypalUrl))
                startActivity(browserIntent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Cannot open link", Toast.LENGTH_SHORT).show()
            }
        }

        // Fetch donation goal from Firestore
        loadGoalProgress()

        // Check if user is admin -> show edit button
        checkIfAdmin { isAdmin ->
            if (isAdmin) {
                btnEditGoal.visibility = View.VISIBLE
                btnEditGoal.setOnClickListener { showEditGoalDialog() }
            }
        }

        return view
    }

    private fun loadGoalProgress() {
        db.collection("donationGoal").document("goalData")
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val goalAmount = doc.getLong("goalAmount")?.toInt() ?: 0
                    val currentProgress = doc.getLong("currentProgress")?.toInt() ?: 0

                    if (goalAmount > 0) {
                        val percentage = (currentProgress * 100) / goalAmount
                        progressBarGoal.progress = percentage
                        tvGoalTitle.text = "Goal for this Year: $currentProgress / $goalAmount"
                    } else {
                        tvGoalTitle.text = "No Goal set"
                        progressBarGoal.progress = 0
                    }
                } else {
                    tvGoalTitle.text = "No Goal set"
                    progressBarGoal.progress = 0
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load goal", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showEditGoalDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_goal, null, false)

        val etGoalAmount = dialogView.findViewById<EditText>(R.id.etGoalAmount)
        val etCurrentProgress = dialogView.findViewById<EditText>(R.id.etCurrentProgress)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveGoal)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancelGoal)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnSave.setOnClickListener {
            val goalAmount = etGoalAmount.text.toString().toIntOrNull()
            val currentProgress = etCurrentProgress.text.toString().toIntOrNull()

            if (goalAmount != null && currentProgress != null) {
                val data = mapOf(
                    "goalAmount" to goalAmount,
                    "currentProgress" to currentProgress
                )

                db.collection("donationGoal").document("goalData")
                    .set(data)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Goal updated", Toast.LENGTH_SHORT).show()
                        loadGoalProgress()
                        dialog.dismiss()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to update goal", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(requireContext(), "Enter valid numbers", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun checkIfAdmin(callback: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: return callback(false)
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val role = doc.getString("role") ?: "user"
                callback(role == "admin")
            }
            .addOnFailureListener { callback(false) }
    }
}