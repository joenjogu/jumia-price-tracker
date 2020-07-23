package com.example.jpt_demo

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.concurrent.atomic.AtomicInteger

class NotificationHandler (appContext : Context) {
    
    private val context : Context = appContext
    
    fun showPriceDropNotification(
        notificationProduct: String,
        notificationCurrPrice : String,
        notificationPrevPrice : String,
        productImage : Bitmap){
        val productName = notificationProduct.trim().slice(0..20)
        val notificationText =
            "$productName dropped from Ksh$notificationPrevPrice to $notificationCurrPrice"
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        val builder = NotificationCompat.Builder(
            context, "Price Tracker Notification Channel ID")
            .setSmallIcon(R.drawable.ic_trending_down_black_24dp)
            .setContentTitle("Price Drop Detected")
            .setContentText(notificationText)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setLargeIcon(productImage)
            .setStyle(NotificationCompat.BigPictureStyle().bigPicture(productImage).bigLargeIcon(null))
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager : NotificationManagerCompat = NotificationManagerCompat
            .from(context)

        notificationManager.notify(NotificationID.id,builder.build())
    }

    fun showTargetPriceHitNotification (notificationProduct: String, notificationTargetPrice: String){
        val notificationText =
            "Target Price $notificationTargetPrice Hit for $notificationProduct"
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        val builder = NotificationCompat.Builder(context, "Price Tracker Notification Channel ID")
            .setSmallIcon(R.drawable.ic_trending_down_black_24dp)
            .setContentTitle("Target Price Hit")
            .setContentText("Your Target Price has been hit")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager : NotificationManagerCompat = NotificationManagerCompat
            .from(context)

        notificationManager.notify(NotificationID.id,builder.build())
    }

    fun showTrackerRunningNotification(){
        val notificationText = "Price Tracker Running"

        val builder = NotificationCompat.Builder(context, "Price Tracker Notification Channel ID")
            .setSmallIcon(R.drawable.ic_trending_down_black_24dp)
            .setContentTitle("Price Tracker")
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setTimeoutAfter(5000)

        val notificationManager : NotificationManagerCompat = NotificationManagerCompat
            .from(context)

        notificationManager.notify(1,builder.build())
    }

    object NotificationID {
        private val c = AtomicInteger(0)
        val id:Int
            get() {
                return c.incrementAndGet()
            }
    }
}