package vcmsa.projects.donationsnewlife

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController

class DonateFragment : Fragment() {

    private lateinit var btnDonatePayment: Button
    private lateinit var btnDropOffZones: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_donate, container, false)

        btnDonatePayment = view.findViewById(R.id.btnDonatePayment)
        btnDropOffZones = view.findViewById(R.id.btnDropOffZones)

        btnDonatePayment.setOnClickListener {
            findNavController().navigate(R.id.action_donateFragment_to_paymentFragment)
        }

        btnDropOffZones.setOnClickListener {
            findNavController().navigate(R.id.action_donateFragment_to_dropOffZonesFragment)
        }

        return view
    }
}