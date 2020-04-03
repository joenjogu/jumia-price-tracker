package com.example.jpt_demo


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_tracklist.*

/**
 * A simple [Fragment] subclass.
 */
class FragmentTracklist : Fragment(), RecyclerViewClickListener{

    private val dbProducts = FirebaseDatabase.getInstance().getReference(NODE_PRODUCTS)
    private lateinit var viewModel : ProductsViewModel
    private val adapter = ProductAdapter()
    private val user = User()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewModel = ViewModelProvider(this).get(ProductsViewModel::class.java)

        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_tracklist, container, false)

        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        productrecyclerview.adapter = adapter
        adapter.listener = this

        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        user.userid = currentUserId

        val product = Product()
        product.id = dbProducts.child(user.userid!!).push().key

        viewModel.fetchProducts(product,user)
        viewModel.getRealtimeUpdates(user)

        viewModel.products.observe(viewLifecycleOwner, Observer {
            adapter.setProducts(it)
        })

        viewModel.product.observe(viewLifecycleOwner, Observer {
            adapter.addProduct(it)
        })

        productrecyclerview.layoutManager = LinearLayoutManager(context)
    }

    override fun itemClicked(view: View, product: Product, user: User) {
        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        user.userid = currentUserId

        when (view.id){
            R.id.btn_target_price ->{
                FragmentSetTargetPrice(product,user).show(childFragmentManager,"")
            }

            R.id.btn_product_delete ->{
                AlertDialog.Builder(requireContext()).also {
                    it.setTitle(getString(R.string.delete_confirmation))
                    it.setPositiveButton(getString(R.string.yes)) { _, _ ->
                        viewModel.deleteProduct(product,user)
                    }
                }.create().show()
            }
        }
    }
}
