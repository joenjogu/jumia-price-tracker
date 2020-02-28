package com.example.jpt_demo

import android.os.Bundle
import android.os.Handler
import android.view.*
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

/**
 * A simple [Fragment] subclass.
 */
class FragmentWebview : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_webview, container, false)

        val mWebView = v.findViewById<View>(R.id.webView) as WebView
        val progbar = v.findViewById<View>(R.id.webviewprogressBar) as ProgressBar
        val swiperef = v.findViewById<View>(R.id.webviewswiperefresh) as SwipeRefreshLayout
        mWebView.loadUrl("https://jumia.co.ke")
//        progbar.max = 100
//        progbar.progress = 1

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

        swiperef.setOnRefreshListener{
            mWebView.reload()
            Handler().postDelayed({swiperef.isRefreshing = false},4000)
        }

        // Inflate the layout for this fragment
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

}

