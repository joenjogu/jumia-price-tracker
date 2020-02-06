package com.example.jpt_demo


import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.fragment_one.*

/**
 * A simple [Fragment] subclass.
 */
class Fragment_one : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_one, container, false)

        val mWebView = v.findViewById<View>(R.id.webView) as WebView
        mWebView.loadUrl("https://jumia.co.ke")

        // Enable Javascript
        val webSettings = mWebView.settings
        webSettings.javaScriptEnabled = true
        webSettings.setSupportZoom(false)

        // Force links and redirects to open in the WebView instead of in a browser
        mWebView.webViewClient = WebViewClient()

        mWebView.canGoBack()
        mWebView.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK
                && event.action == MotionEvent.ACTION_UP
                && mWebView.canGoBack()
            ) {
                mWebView.goBack()
                return@OnKeyListener true
            }
            false
        })

        val fab_track = v.findViewById<View>(R.id.fab_track)

//        fab_track.setOnClickListener(View.OnClickListener {
////            showProductDialog(
////                "Track this item?",
////                "Product Info and image Here", this
////            )
////        })

        // Inflate the layout for this fragment
        return v
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
