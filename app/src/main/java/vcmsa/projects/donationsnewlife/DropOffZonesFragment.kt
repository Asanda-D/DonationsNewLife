package vcmsa.projects.donationsnewlife

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

// Data class for wishlist items
data class ProductItem(
    val productName: String = "",
    val priority: String = "" // high / medium / low
)

// RecyclerView Adapter
class WishlistAdapter(private var items: List<ProductItem>) :
    RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder>() {

    class WishlistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtProduct: TextView = itemView.findViewById(R.id.txtProduct)
        val txtPriority: TextView = itemView.findViewById(R.id.txtPriority)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishlistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_wishlist_item, parent, false)
        return WishlistViewHolder(view)
    }

    override fun onBindViewHolder(holder: WishlistViewHolder, position: Int) {
        val item = items[position]
        holder.txtProduct.text = item.productName

        // Map priority to an icon
        holder.txtPriority.text = when (item.priority.lowercase()) {
            "high" -> "🔥"
            "medium" -> "🍼"
            "low" -> "🧸"
            else -> item.priority
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<ProductItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}

class DropOffZonesFragment : Fragment(), OnMapReadyCallback {

    private lateinit var recyclerWishlist: RecyclerView
    private lateinit var btnEdit: Button
    private lateinit var adapter: WishlistAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private var listener: ListenerRegistration? = null

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_drop_off_zones, container, false)

        recyclerWishlist = view.findViewById(R.id.recyclerWishlist)
        btnEdit = view.findViewById(R.id.btnEditWishlist)

        // Setup RecyclerView
        adapter = WishlistAdapter(emptyList())
        recyclerWishlist.layoutManager = LinearLayoutManager(requireContext())
        recyclerWishlist.adapter = adapter

        // Check if user is admin -> show edit button
        checkIfAdmin { isAdmin ->
            if (isAdmin) {
                btnEdit.visibility = View.VISIBLE
                btnEdit.setOnClickListener {
                    findNavController().navigate(R.id.action_dropOffZonesFragment_to_manageWishlistFragment)
                }
            }
        }

        loadWishlist()

        // ---- CLEAN MAP SETUP ----
        var mapFragment = childFragmentManager.findFragmentById(R.id.mapContainer) as? SupportMapFragment
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance()
            childFragmentManager.beginTransaction()
                .replace(R.id.mapContainer, mapFragment)
                .commit()
        }

        // Only get map async after transaction completes
        childFragmentManager.executePendingTransactions()
        mapFragment.getMapAsync(this)
        // --------------------------

        return view
    }

    private fun loadWishlist() {
        listener = firestore.collection("wishlist")
            .document("products")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null || !snapshot.exists()) {
                    return@addSnapshotListener
                }

                val itemsList = mutableListOf<ProductItem>()
                val items = snapshot.get("items") as? List<Map<String, Any>>
                items?.forEach { map ->
                    val product = map["productName"] as? String ?: ""
                    val priority = map["priority"] as? String ?: ""
                    itemsList.add(ProductItem(product, priority))
                }
                adapter.updateData(itemsList)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listener?.remove()
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

    // Map setup
    override fun onMapReady(googleMap: GoogleMap) {
        val babyHomeLocation = LatLng(-29.7823, 30.7677) // 📍 Hillcrest, KZN (example coords)
        googleMap.addMarker(
            MarkerOptions()
                .position(babyHomeLocation)
                .title("Baby Home Drop-Off Zone")
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(babyHomeLocation, 15f))
    }
}