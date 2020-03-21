package com.example.jpt_demo


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_set_target_price.*
import java.util.concurrent.CountDownLatch

/**
 * A simple [Fragment] subclass.
 */
class FragmentSetTargetPrice (private val product: Product): DialogFragment() {

    private lateinit var viewmodel : ProductsViewModel
    private val dbProducts = FirebaseDatabase.getInstance().getReference(NODE_PRODUCTS)
    var products = mutableListOf<Product>()
    val latch = CountDownLatch(1)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewmodel = ViewModelProvider(this).get(ProductsViewModel::class.java)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_set_target_price, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_Light_Dialog_MinWidth)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        dbProducts.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                latch.countDown()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val prod = snapshot.child(product.id!!).getValue(Product::class.java)
                    prod?.id = snapshot.child(product.id!!).key
                    prod?.let { products.add(it) }
                    Log.i("Workingg","Products list added successfully $products")
                }
                latch.countDown()
            }
        })

        btn_set_target_price.setOnClickListener{
            val targetPriceString = et_target_price.text.toString().trim()
            if (targetPriceString.isEmpty()){
                et_target_price.error = "Please Set Target Price"
                return@setOnClickListener
            }
            val targetPrice = targetPriceString.toInt()
            if (targetPrice < 1){
                et_target_price.error = "Target Price cannot be 0"
                return@setOnClickListener
            }

            val product = Product(products[0].id,products[0].productname,
                products[0].seller,products[0].currentprice,products[0].previousprice,
                targetPrice,products[0].producturl,products[0].imageurl)

            dbProducts.child(products[0].id!!)
                .setValue(product)
                .addOnCompleteListener {
                    if (it.isSuccessful){
//                        Toast.makeText(this,"Target Price added successfully",Toast.LENGTH_LONG).show()
                        Log.i("Workingg","Target Price added successfully")
                    } else{
//                        Toast.makeText(this,it.exception.toString(),Toast.LENGTH_LONG).show()
                        Log.d("Error",it.exception.toString())
                    }
                }

            dismiss()
        }
    }

}