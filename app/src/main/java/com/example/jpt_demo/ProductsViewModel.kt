package com.example.jpt_demo

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

    fun addProduct (product: Product){

        product.id = dbProducts.push().key

        dbProducts.child(product.id!!).setValue(product)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    _result.value = null
                }else {
                    _result.value = it.exception
                }
            }
    }

    fun deleteProduct (product: Product){
        product.id = dbProducts.push().key

        dbProducts.child(product.id!!).setValue(null)
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

    fun getRealtimeUpdates(){
        dbProducts.addChildEventListener(childEventListener)
    }

    fun fetchProducts (){
        dbProducts.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val products = mutableListOf<Product>()
                    for (productSnapshot in snapshot.children){
                        val product = productSnapshot.getValue(Product::class.java)
                        product?.id = productSnapshot.key
                        product?.let { products.add(it) }
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