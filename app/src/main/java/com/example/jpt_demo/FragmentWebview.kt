package com.example.jpt_demo


import android.os.Bundle
import android.view.*
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment

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
        // Inflate the layout for this fragment
        return v
    }

}

