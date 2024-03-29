package com.example.jpt_demo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import java.util.concurrent.CountDownLatch

class TrackPrice (appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {

    private val dbProducts = FirebaseDatabase.getInstance().getReference("products")
    var products = mutableListOf<Product>()
    private val user = User()
    val latch = CountDownLatch(1)
    val notificationHandler = NotificationHandler(applicationContext)

    override fun doWork(): Result {
        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        user.userid = currentUserId
        Log.i("tracker", currentUserId)
        notificationHandler.showTrackerRunningNotification()

        dbProducts.child(currentUserId).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                latch.countDown()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                Log.i("Working","on DataChange")
                if (snapshot.exists()){
                    for (productSnapshot in snapshot.children){
                        val prod = productSnapshot.getValue(Product::class.java)
                        prod?.id = productSnapshot.key
                        prod?.let { products.add(it) }
                        Log.i("Working","Products list added successfully $products")
                    }
                }
                latch.countDown()
                Log.i("Working","onDataChange latch completed")
            }
        })
        latch.await()

        for ((position,value)in products.withIndex()){
            Log.i("Working","For loop started")

            runBlocking {
                val currentPrice = async {
                    getCurrentPrice(products[position].producturl)
                }

                val scrapedPrice = currentPrice.await() - 100

                Log.i("Working","got current price $scrapedPrice")

                if (scrapedPrice < products[position].currentprice!! && scrapedPrice != 0){
                    products[position].previousprice = products[position].currentprice
                    val notificationPrevPrice = products[position].previousprice!!.toString()
                    val notificationCurrPrice =  scrapedPrice.toString()
                    val notificationProduct = products[position].productname!!

                    Glide.with(applicationContext).asBitmap().load(products[position].imageurl)
                        .into(object : CustomTarget<Bitmap>(){
                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: Transition<in Bitmap>?
                            ) {
                                notificationHandler.showPriceDropNotification(
                                    notificationProduct,notificationCurrPrice,notificationPrevPrice, resource)
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {

                            }

                        })

                    fun setCurrentPrice (){
                        val product = Product(
                            products[position].id,
                            products[position].productname,
                            products[position].seller,
                            scrapedPrice,
                            products[position].previousprice,
                            products[position].targetprice,
                            products[position].producturl,
                            products[position].imageurl)

                        dbProducts.child(user.userid!!).child(products[position].id!!)
                            .setValue(product)
                            .addOnCompleteListener {
                                if (it.isSuccessful){
                                     Log.i("Working","Current Price added successfully")
                                } else{
                                    Log.d("Error",it.exception.toString())
                                }
                            }
                        Log.i("Working","set current price $scrapedPrice")
                    }
                    launch {
                        setCurrentPrice()
                        latch.countDown()
                    }

                } else if (scrapedPrice > products[position].currentprice!! ){
                    fun setCurrentPrice (){
                        val product = Product(
                            products[position].id,
                            products[position].productname,
                            products[position].seller,
                            scrapedPrice,
                            products[position].previousprice,
                            products[position].targetprice,
                            products[position].producturl,
                            products[position].imageurl)

                        dbProducts.child(user.userid!!).child(products[position].id!!)
                            .setValue(product)
                            .addOnCompleteListener {
                                if (it.isSuccessful){
                                    Log.i("Working","Current Price added successfully")
                                } else{
                                    Log.d("Error",it.exception.toString())
                                }
                            }
                        Log.i("Working","set current price $scrapedPrice")
                    }
                    launch {
                        setCurrentPrice()
                        latch.countDown()
                    }
                }
                if (products[position].targetprice != null)
                    if (scrapedPrice == products[position].targetprice){
                        val notificationTargetPrice = products[position].targetprice.toString()
                        val notificationProduct = products[position].productname!!

                        notificationHandler.showTargetPriceHitNotification(
                            notificationProduct,notificationTargetPrice)
                    }
            }
            latch.countDown()
            latch.await()
            Log.i("Working","For loop latch completed successfully")
        }
        latch.await()
        return Result.success()
    }

    private suspend fun getCurrentPrice (url : String?) : Int{
        var currentprice = ""
        withContext(Dispatchers.IO){
            val doc = Jsoup.connect(url).get()
            try {
                currentprice = doc.select(".-fs24.-tal.-ltr.-b").text()
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
        latch.countDown()
        val regex = """[^0-9]"""
        return if (!TextUtils.isEmpty(currentprice))
            currentprice.trim().replace(regex.toRegex(),"").toInt()
        else 0
    }
}