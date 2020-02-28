package com.example.jpt_demo

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.productimage.view.*

class MainActivity : AppCompatActivity() {

    private val sharedPrefFile = "LoginPrefFile"
    private lateinit var mAuth : FirebaseAuth
    val fragmentManager = supportFragmentManager
    val fragmentTransaction = fragmentManager.beginTransaction()
    val frag1 = LoginFragment()
    val frag2 = RegisterFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPreferences : SharedPreferences = this.getSharedPreferences(sharedPrefFile,
            Context.MODE_PRIVATE)
        val loginState = sharedPreferences.getBoolean("login_state_key",false)

        if (!loginState){
            val app_bar = findViewById<View>(R.id.appbarlayout) as View
            app_bar.setVisibility(View.GONE)
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
                    "Track this item?",
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
        val prodimage = vyu.imagefromurl
        val url = "https://ke.jumia.is/unsafe/fit-in/680x680/filters:fill(white)/product/22/58921/1.jpg?1720"
        if (prodimage!=null) {
            Glide.with(this).load(url).placeholder(R.drawable.loading_image_placeholder).into(prodimage)
            Toast.makeText(this,"Image Downloaded",Toast.LENGTH_SHORT).show()
        }
        productdialog.setTitle(title)
        productdialog.setView(vyu)
        productdialog.setNegativeButton("Cancel") { _, _ ->

        }
        productdialog.setPositiveButton("Confirm") { _, _ ->

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
}