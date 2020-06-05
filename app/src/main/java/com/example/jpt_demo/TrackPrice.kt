package com.example.jpt_demo

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

class TrackPrice (appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {

    private val dbProducts = FirebaseDatabase.getInstance().getReference(NODE_PRODUCTS)
    var products = mutableListOf<Product>()
    private val user = User()
    val latch = CountDownLatch(1)

    override fun doWork(): Result {
        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        user.userid = currentUserId
        Log.i("tracker", currentUserId)

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

                val currPrice = currentPrice.await()

                Log.i("Working","got current price $currPrice")

                if (currPrice < products[position].previousprice!!){
                    val notificationPrevPrice = products[position].previousprice!!.toString()
                    val notificationCurrPrice =  currPrice.toString()
                    val notificationProduct = products[position].productname!!

                    showPriceDropNotification(notificationProduct,notificationCurrPrice,notificationPrevPrice)

                    fun setCurrentPrice (){
                        val product = Product(products[position].id,
                            products[position].productname,
                            products[position].seller,currPrice,products[position].previousprice,
                            products[position].targetprice,products[position].producturl,
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
                        Log.i("Working","set current price $currPrice")
                    }
                    launch {
                        setCurrentPrice()
                        latch.countDown()
                    }

                }
                if (products[position].targetprice != null)

                    if (currPrice == products[position].targetprice){
                        val notificationTargetPrice = products[position].targetprice.toString()
                        val notificationProduct = products[position].productname!!

                        showTargetPriceHitNotification(notificationProduct,notificationTargetPrice)
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
                currentprice = doc.select("span[data-price]").select(".-fs24").text()
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
        latch.countDown()
        val regex = """[^0-9]"""
        return currentprice.trim().replace(regex.toRegex(),"").toInt()
    }

    private fun showPriceDropNotification
                (notificationProduct: String,notificationCurrPrice : String,notificationPrevPrice : String){

        val notificationText =
            "The price of $notificationProduct has dropped from $notificationPrevPrice to $notificationCurrPrice"

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_trending_down_black_24dp)
            .setContentTitle("Price Drop Detected")
            .setContentText("A lower Price has been detected for your item")
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notifManager : NotificationManagerCompat = NotificationManagerCompat
            .from(applicationContext)

        notifManager.notify(NotificationID.id,builder.build())
    }

    private fun showTargetPriceHitNotification (notificationProduct: String, notificationTargetPrice: String){
        val notificationText =
            "Target Price $notificationTargetPrice Hit for $notificationProduct"

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_trending_down_black_24dp)
            .setContentTitle("Target Price Hit")
            .setContentText("Your Target Price has been hit")
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notifManager : NotificationManagerCompat = NotificationManagerCompat
            .from(applicationContext)

        notifManager.notify(NotificationID.id,builder.build())
    }

    object NotificationID {
        private val c = AtomicInteger(0)
        val id:Int
            get() {
                return c.incrementAndGet()
            }
    }
}