package com.example.jpt_demo

import android.app.AlertDialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_one.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar.setTitle("Jumia Price Tracker")
        setSupportActionBar(toolbar)

        val fragmentAdapter = PagerAdapter(supportFragmentManager)
        viewPager.adapter = fragmentAdapter
        tabLayout.setupWithViewPager(viewPager)

        val fab_track = findViewById<View>(R.id.fab_track)

        this.fab_track.setOnClickListener( View.OnClickListener { showProductDialog("Track this item?",
            "Product Info and image Here", this@MainActivity ) })

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
