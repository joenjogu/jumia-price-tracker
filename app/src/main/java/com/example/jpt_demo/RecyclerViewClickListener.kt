package com.example.jpt_demo

import android.view.View

interface RecyclerViewClickListener {

    fun itemClicked(view : View, product: Product)
}