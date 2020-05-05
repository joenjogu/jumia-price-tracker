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
    private val user = User()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNotificationChannel()

        viewmodel = ViewModelProvider(this).get(ProductsViewModel::class.java)

        val sharedPreferences : SharedPreferences = this.getSharedPreferences(sharedPrefFile,
            Context.MODE_PRIVATE)
        val loginState = sharedPreferences.getBoolean("login_state_key",false)

        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        val getPrice = PeriodicWorkRequestBuilder<TrackPrice>(15,TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork("Price Tracker",ExistingPeriodicWorkPolicy.KEEP,getPrice)
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(getPrice.id)
            .observe(this, Observer {workInfo ->
                if (workInfo != null && workInfo.state == WorkInfo.State.RUNNING){
                    Toast.makeText(this,"Price Tracking Running",Toast.LENGTH_LONG).show()
                    Log.i("Working","Price Tracking Running")
                }
            })

        if (!loginState){
            val appbar = findViewById<View>(R.id.appbarlayout)
            appbar.visibility = View.GONE
            fab_track.hide()
            fragmentTransaction.add(R.id.mainlayout, frag1)
            fragmentTransaction.commit()
        }else {

            toolbar.title = "Jumia Price Tracker"
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

            val fabTrack = findViewById<View>(R.id.fab_track)
            fabTrack.setOnClickListener {
                if ((webView.url).endsWith("html")) {
                    showProductDialog(
                        "TRACK THIS ITEM?",
                        this
                    )
                }else {
                    Toast.makeText(this,getString(R.string.no_item_alert),Toast.LENGTH_LONG).show()
                }
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
//                appbarlayout.visibility = View.GONE
                fab_track.hide()
                fragmentTransaction.add(R.id.mainlayout,FragmentSettings())
                fragmentTransaction.addToBackStack("frag")
                fragmentTransaction.commit()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showProductDialog(title: String, context: Context) {
        val productDialog = AlertDialog.Builder(context)
        val inflater = layoutInflater
        val vyu = inflater.inflate(R.layout.productimage,null)
        val productImage = vyu.dialogprodimage
        val productPrice = vyu.dialogprodcurrentprice
        val productSeller = vyu.dialogprodseller
        val productName = vyu.dialogprodname
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
                    val imageUrl = doc.select("img[data-lazy-slide]").attr("data-src")

                    resarr = arrayOf(prodName,sellerName,price,imageUrl)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                withContext(Dispatchers.Main){
                    productName.text =resarr[0]
                    productSeller.text = resarr[1]
                    productPrice.text = resarr[2]
                    prodImageUrl.text = resarr[3]
                    Glide.with(this@MainActivity).load(resarr[3]).into(productImage)
                }
            }
        }

        CoroutineScope(Dispatchers.Main).launch { getDetails(url) }
        productDialog.setTitle(title)
        productDialog.setView(vyu)

        viewmodel.result.observe(this, Observer {
            if (it == null){
                Toast.makeText(this,"Added Successfully",Toast.LENGTH_SHORT).show()
            }else {
                Toast.makeText(this,it.message,Toast.LENGTH_SHORT).show()
            }
        })

        productDialog.setNegativeButton("Cancel") { _, _ ->

        }
        productDialog.setPositiveButton("Confirm") { _, _->
            val regex = """[^0-9]"""
            val prodPrice = productPrice.text.trim().replace(regex.toRegex(),"").toInt()
            val product = Product()
            product.productname = productName.text.toString()
            product.seller = productSeller.text.toString()
            product.previousprice = prodPrice
            product.imageurl = prodImageUrl.text.toString()
            product.producturl = url

            val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
            user.userid = currentUserId

            viewmodel.addProduct(product,user)

        }
        productDialog.create().show()
    }

    private fun showLogoutDialog(message: String, context: Context) {
        val sharedPrefFile = "LoginPrefFile"
        val sharedPreferences : SharedPreferences = this.getSharedPreferences(sharedPrefFile,
            Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = sharedPreferences.edit()
        val logoutDialog = AlertDialog.Builder(context)
        logoutDialog.setMessage(message)
        logoutDialog.setNegativeButton("Cancel") { _, _ ->

        }
        logoutDialog.setPositiveButton("Logout") { _, _ ->
            FirebaseAuth.getInstance().signOut()
            editor.clear()
            editor.apply()
            editor.commit()
            appbarlayout.visibility = View.GONE
            fragmentTransaction.replace(R.id.mainlayout,frag1)
            fragmentTransaction.commit()
        }
        logoutDialog.create().show()
    }

    private fun createNotificationChannel () {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val importance = NotificationManager.IMPORTANCE_HIGH
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