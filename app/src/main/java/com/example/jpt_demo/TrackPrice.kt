package com.example.jpt_demo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
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

                val scrapedPrice = currentPrice.await()

                Log.i("Working","got current price $scrapedPrice")

                if (scrapedPrice < products[position].currentprice!!){
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
                currentprice = doc.select("span[data-price]").select(".-fs24").text()
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
        latch.countDown()
        val regex = """[^0-9]"""
        return currentprice.trim().replace(regex.toRegex(),"").toInt()
    }

//    private fun showPriceDropNotification
//                (notificationProduct: String, notificationCurrPrice : String, notificationPrevPrice : String, productImage : Bitmap){
//        val productName = notificationProduct.trim().slice(0..20)
//        val notificationText =
//            "$productName dropped from Ksh$notificationPrevPrice to $notificationCurrPrice"
//        val intent = Intent(applicationContext, MainActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        }
//        val pendingIntent: PendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)
//
//        val builder = NotificationCompat.Builder(applicationContext, "Price Tracker Notification Channel ID")
//            .setSmallIcon(R.drawable.ic_trending_down_black_24dp)
//            .setContentTitle("Price Drop Detected")
//            .setContentText(notificationText)
//            .setContentIntent(pendingIntent)
//            .setAutoCancel(true)
//            .setLargeIcon(productImage)
//            .setStyle(NotificationCompat.BigPictureStyle().bigPicture(productImage).bigLargeIcon(null))
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//
//        val notificationManager : NotificationManagerCompat = NotificationManagerCompat
//            .from(applicationContext)
//
//        notificationManager.notify(NotificationID.id,builder.build())
//    }
//
//    private fun showTargetPriceHitNotification (notificationProduct: String, notificationTargetPrice: String){
//        val notificationText =
//            "Target Price $notificationTargetPrice Hit for $notificationProduct"
//        val intent = Intent(applicationContext, MainActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        }
//        val pendingIntent: PendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)
//
//        val builder = NotificationCompat.Builder(applicationContext, "Price Tracker Notification Channel ID")
//            .setSmallIcon(R.drawable.ic_trending_down_black_24dp)
//            .setContentTitle("Target Price Hit")
//            .setContentText("Your Target Price has been hit")
//            .setContentIntent(pendingIntent)
//            .setAutoCancel(true)
//            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//
//        val notificationManager : NotificationManagerCompat = NotificationManagerCompat
//            .from(applicationContext)
//
//        notificationManager.notify(NotificationID.id,builder.build())
//    }
//
//    private fun showTrackerRunningNotification(){
//        val notificationText = "Price Tracker Running"
//
//        val builder = NotificationCompat.Builder(applicationContext, "Price Tracker Notification Channel ID")
//            .setSmallIcon(R.drawable.ic_trending_down_black_24dp)
//            .setContentTitle("Price Tracker")
//            .setContentText(notificationText)
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//
//        val notificationManager : NotificationManagerCompat = NotificationManagerCompat
//            .from(applicationContext)
//
//        notificationManager.notify(1,builder.build())
//    }
//
//    object NotificationID {
//        private val c = AtomicInteger(0)
//        val id:Int
//            get() {
//                return c.incrementAndGet()
//            }
//    }
}