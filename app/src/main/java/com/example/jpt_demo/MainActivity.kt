package com.example.jpt_demo

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar.setTitle("Jumia Price Tracker")
        setSupportActionBar(toolbar)

        val fragmentAdapter = PagerAdapter(supportFragmentManager)
        viewPager.adapter = fragmentAdapter
        tabLayout.setupWithViewPager(viewPager)

        viewPager?.addOnPageChangeListener(object:OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }
            override fun onPageSelected(position: Int) {
                when (position){
                    0 -> fab_track.show()
                    else -> fab_track.hide()
                }
            }

        })

        val fab_track = findViewById<View>(R.id.fab_track)

        fab_track.setOnClickListener(View.OnClickListener {
            showProductDialog(
                "Track this item?",
                "Product Info and image Here", this
            )
        }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.overflow, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item){
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showProductDialog(title: String, message: String, context: Context) {
        val productdialog = AlertDialog.Builder(context)
        productdialog.setTitle(title)
        productdialog.setMessage(message)
        productdialog.setNegativeButton("Cancel") { _, _ ->

        }
        productdialog.setPositiveButton("Confirm") { _, _ ->

        }
        productdialog.create().show()
    }

}
