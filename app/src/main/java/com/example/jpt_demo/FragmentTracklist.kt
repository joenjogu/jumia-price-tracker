package com.example.jpt_demo


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_tracklist.*

/**
 * A simple [Fragment] subclass.
 */
class FragmentTracklist : Fragment() {

    private lateinit var viewModel : ProductsViewModel
    private val adapter = ProductAdapter()

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

        viewModel.fetchProducts()
        viewModel.getRealtimeUpdates()

        viewModel.products.observe(viewLifecycleOwner, Observer {
            adapter.setProducts(it)
        })

        viewModel.product.observe(viewLifecycleOwner, Observer {
            adapter.addProduct(it)
        })

        productrecyclerview.layoutManager = LinearLayoutManager(context)
    }


}
