package com.example.jpt_demo


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_tracklist.*

/**
 * A simple [Fragment] subclass.
 */
class FragmentTracklist : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_tracklist, container, false)

        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val product = listOf(
            Product("Jimbi Maize Meal Fortified With Vitamins And Minerals - 2Kg","Carrefour",99,120,90),
            Product("Jimbi Maize Meal Fortified With Vitamins And Minerals - 2Kg","Carrefour",99,120,90),
            Product("Jimbi Maize Meal Fortified With Vitamins And Minerals - 2Kg","Carrefour",99,120,90),
            Product("Jimbi Maize Meal Fortified With Vitamins And Minerals - 2Kg","Carrefour",99,120,90),
            Product("Jimbi Maize Meal Fortified With Vitamins And Minerals - 2Kg","Carrefour",99,120,90)

        )
        productrecyclerview.layoutManager = LinearLayoutManager(context)
        productrecyclerview.adapter = ProductAdapter(product)

    }


}
