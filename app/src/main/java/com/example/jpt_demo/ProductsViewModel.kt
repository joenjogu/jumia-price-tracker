package com.example.jpt_demo

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.*

class ProductsViewModel : ViewModel() {

    private val dbProducts = FirebaseDatabase.getInstance().getReference(NODE_PRODUCTS)

    private val _products = MutableLiveData<List<Product>>()
    val products : LiveData<List<Product>>
    get() = _products

    private val _product = MutableLiveData<Product>()
    val product : LiveData<Product>
        get() = _product

    private val _result = MutableLiveData<Exception?>()
    val result : LiveData<Exception?>
        get() = _result

    fun addUser (user: User){
        dbProducts.child(user.userid!!).setValue(null)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    _result.value = null
                }else {
                    _result.value = it.exception
                }
            }
    }

    fun addProduct (product: Product, user: User){

        product.id = dbProducts.child(user.userid!!).push().key

        dbProducts.child(user.userid!!).child(product.id!!).setValue(product)
                .addOnCompleteListener {
                    if (it.isSuccessful){
                        _result.value = null
                    }else {
                        _result.value = it.exception
                    }
                }
    }

    fun deleteProduct (product: Product, user: User){
        dbProducts.child(user.userid!!).child(product.id!!).setValue(null)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    _result.value = null
                }else {
                    _result.value = it.exception
                }
            }
    }

    private val childEventListener = object : ChildEventListener {
        override fun onCancelled(error: DatabaseError) {}

        override fun onChildMoved(snapshot: DataSnapshot, p1: String?) {}

        override fun onChildChanged(snapshot: DataSnapshot, p1: String?) {
            val product = snapshot.getValue(Product::class.java)
            product?.id = snapshot.key
            _product.value = product
        }

        override fun onChildAdded(snapshot: DataSnapshot, p1: String?) {
            val product = snapshot.getValue(Product::class.java)
            product?.id = snapshot.key
            _product.value = product
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            val product = snapshot.getValue(Product::class.java)
            product?.id = snapshot.key
            product?.isDeleted = true
            _product.value = product
        }
    }

    fun getRealtimeUpdates(user: User){
        dbProducts.child(user.userid!!).addChildEventListener(childEventListener)
    }

    fun fetchProducts (prod: Product, user: User){
        dbProducts.child(user.userid!!)
            .addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("prod","onDataChange")
                if (snapshot.exists()){
                    Log.d("prod","snapshot exists")
                    val products = mutableListOf<Product>()
                    for (productSnapshot in snapshot.children){
                        val product = productSnapshot.getValue(Product::class.java)
                        product?.id = productSnapshot.key
                        product?.let { products.add(it) }
                        Log.d("prod","found products: $products ")
                    }
                    _products.value = products
                }
            }
        })
    }

    override fun onCleared() {
        super.onCleared()
        dbProducts.removeEventListener(childEventListener)
    }
}