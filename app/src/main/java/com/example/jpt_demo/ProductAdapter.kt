package com.example.jpt_demo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.layout_product.view.*

class ProductAdapter : RecyclerView.Adapter<ProductAdapter.ProductViewModel> (){

    private var products = mutableListOf<Product>()
    var listener : RecyclerViewClickListener? = null
    private val user = User()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ProductViewModel(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_product,parent,false)
        )

    override fun getItemCount() = products.size

    override fun onBindViewHolder(holder: ProductViewModel, position: Int) {
        holder.view.tv_productname.text = products[position].productname
        holder.view.tv_seller.text = products[position].seller
        holder.view.tv_previousprice.text =  "Previous Price Ksh. " + products[position].previousprice.toString()
        holder.view.tv_currentprice.text = "Current Price Ksh. " +products[position].currentprice.toString()
        holder.view.tv_targetprice.text = "Target Price Ksh. " +products[position].targetprice.toString()

        Glide.with(holder.itemView.context).load(products[position].imageurl)
            .placeholder(R.drawable.loading_image_placeholder)
            .dontAnimate().into(holder.view.productimage)

        holder.view.btn_product_delete.setOnClickListener{
            listener?.itemClicked(it,products[position],user)
        }
        holder.view.btn_target_price.setOnClickListener{
            listener?.itemClicked(it,products[position],user)
        }
    }
    fun setProducts(products: List<Product>){
        this.products = products as MutableList<Product>
        notifyDataSetChanged()
    }

    fun addProduct(product: Product){
        if (!products.contains(product)) {
            products.add(product)
        } else {
            val index = products.indexOf(product)
            if (product.isDeleted){
                products.removeAt(index)
            }
        }
        notifyDataSetChanged()
    }

    class ProductViewModel (val view: View) : RecyclerView.ViewHolder(view)
}