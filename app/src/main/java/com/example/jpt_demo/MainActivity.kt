package com.example.jpt_demo

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import androidx.work.*
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_webview.*
import kotlinx.android.synthetic.main.productimage.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), LifecycleOwner {

    private lateinit var viewmodel : ProductsViewModel
    private val sharedPrefFile = "LoginPrefFile"
    private val fragmentManager = supportFragmentManager
    private val fragmentTransaction = fragmentManager.beginTransaction()
    private val frag1 = LoginFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNotificationChannel()

        viewmodel = ViewModelProvider(this).get(ProductsViewModel::class.java)

        val sharedPreferences : SharedPreferences = this.getSharedPreferences(sharedPrefFile,
            Context.MODE_PRIVATE)
        val loginState = sharedPreferences.getBoolean("login_state_key",false)

        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        val getprice = PeriodicWorkRequestBuilder<TrackPrice>(10,TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork("Price Tracker",ExistingPeriodicWorkPolicy.REPLACE,getprice)
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(getprice.id)
            .observe(this, Observer {workInfo ->
                if (workInfo != null && workInfo.state == WorkInfo.State.RUNNING){
                    Toast.makeText(this,"Price Tracking Running",Toast.LENGTH_LONG).show()
                    Log.i("Working","Price Tracking Running")
                }
            })

        if (!loginState){
            val appbar = findViewById<View>(R.id.appbarlayout) as View
            appbar.setVisibility(View.GONE)
            fab_track.hide()
            fragmentTransaction.add(R.id.mainlayout, frag1)
            fragmentTransaction.commit()
        }else {

            toolbar.setTitle("Jumia Price Tracker")
            setSupportActionBar(toolbar)

            val fragmentAdapter = PagerAdapter(supportFragmentManager)
            viewPager.adapter = fragmentAdapter
            tabLayout.setupWithViewPager(viewPager)

            viewPager?.addOnPageChangeListener(object : OnPageChangeListener {

                override fun onPageScrollStateChanged(state: Int) {
                }

                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                }

                override fun onPageSelected(position: Int) {
                    when (position) {
                        0 -> fab_track.show()
                        else -> fab_track.hide()
                    }
                }
            })

            val fab_track = findViewById<View>(R.id.fab_track)

            fab_track.setOnClickListener {
                showProductDialog(
                    "TRACK THIS ITEM?",
                     this
                )
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.overflow, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId){
            R.id.logout -> {
                showLogoutDialog(
                    "Are you sure you want to logout?",
                    this
                )
            }
            R.id.settings -> {

            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showProductDialog(title: String, context: Context) {
        val productdialog = AlertDialog.Builder(context)
        val infl = layoutInflater
        val vyu = infl.inflate(R.layout.productimage,null)
        val prodimage = vyu.dialogprodimage
        val prodprice = vyu.dialogprodcurrentprice
        val prodseller = vyu.dialogprodseller
        val prodname = vyu.dialogprodname
        val prodImageUrl = vyu.dialogimageurl

        val url = webView.url

        suspend fun getDetails(url : String) {
            var resarr = arrayOf("")
            withContext(Dispatchers.IO) {
                val doc = Jsoup.connect(url).get()
                try {
                    val prodName = doc.select("h1.-fs20").text()
                    val sellerName = doc.select("p.-m").first().text()
                    val price = doc.select("span[data-price]").select(".-fs24").text()
                    val imgurl = doc.select("img[data-lazy-slide]").attr("data-src")

                    resarr = arrayOf(prodName,sellerName,price,imgurl)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                withContext(Dispatchers.Main){
                    prodname.text =resarr[0]
                    prodseller.text = resarr[1]
                    prodprice.text = resarr[2]
                    prodImageUrl.text = resarr[3]
                    Glide.with(this@MainActivity).load(resarr[3]).into(prodimage)
                }
            }
        }

        CoroutineScope(Dispatchers.Main).launch { getDetails(url) }
        productdialog.setTitle(title)
        productdialog.setView(vyu)

        viewmodel.result.observe(this, Observer {
            if (it == null){
                Toast.makeText(this,"Product Added Successfully",Toast.LENGTH_SHORT).show()
            }else {
                Toast.makeText(this,it.message,Toast.LENGTH_SHORT).show()
            }
        })

        productdialog.setNegativeButton("Cancel") { _, _ ->

        }
        productdialog.setPositiveButton("Confirm") { _, _->
            val regex = """[^0-9]"""
            val prodPrice = prodprice.text.trim().replace(regex.toRegex(),"").toInt()
            val product = Product()
            product.productname = prodname.text.toString()
            product.seller = prodseller.text.toString()
            product.previousprice = prodPrice
            product.imageurl = prodImageUrl.text.toString()
            product.producturl = url

            viewmodel.addProduct(product)

        }
        productdialog.create().show()
    }

    private fun showLogoutDialog(message: String, context: Context) {
        val sharedPrefFile = "LoginPrefFile"
        val sharedPreferences : SharedPreferences = this.getSharedPreferences(sharedPrefFile,
            Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = sharedPreferences.edit()
        val logoutdialog = AlertDialog.Builder(context)
        logoutdialog.setMessage(message)
        logoutdialog.setNegativeButton("Cancel") { _, _ ->

        }
        logoutdialog.setPositiveButton("Logout") { _, _ ->
            FirebaseAuth.getInstance().signOut()
            editor.clear()
            editor.apply()
            editor.commit()
            appbarlayout.visibility = View.GONE
            fragmentTransaction.replace(R.id.mainlayout,frag1)
            fragmentTransaction.commit()
        }
        logoutdialog.create().show()
    }

    private fun createNotificationChannel () {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val channel = NotificationChannel(CHANNEL_ID,name,importance).apply {
                description = descriptionText
            }

            val notificationManager : NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
        }
    }
}