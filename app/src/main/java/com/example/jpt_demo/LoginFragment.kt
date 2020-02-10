package com.example.jpt_demo


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

/**
 * A simple [Fragment] subclass.
 */
class LoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val sharedPrefFile = "LoginPrefFile"
        val sharedPreferences : SharedPreferences = this.activity!!.getSharedPreferences(sharedPrefFile,
            Context.MODE_PRIVATE)

        val mEmail = view!!.findViewById<View>(R.id.et_email) as EditText
        val mPassword = view!!.findViewById<View>(R.id.et_password) as EditText
        val mProgressbar = view!!.findViewById<View>(R.id.loginprogressbar) as ProgressBar
        val mLoginbutton = view!!.findViewById<View>(R.id.btn_login) as Button
        val mRegister = view!!.findViewById<View>(R.id.registerhere) as TextView

        mLoginbutton.setOnClickListener {
            val email = mEmail.text.toString().trim()
            val password = mPassword.text.toString().trim()

            if (email.isEmpty()){
                mEmail.setError("Email is required!")
            }
            if (password.isEmpty()){
                mPassword.setError("Please enter password!")
            }
            if (password.length < 8 && password.isNotEmpty()){
                mPassword.setError("Password should be at least 8 characters!")
            }

            mProgressbar.setVisibility (View.VISIBLE)

            if (mEmail.error.isNullOrEmpty() && mPassword.error.isNullOrEmpty()){
                val editor : SharedPreferences.Editor = sharedPreferences.edit()
                editor.putString("email_key",email)
                editor.putBoolean("login_state_key", true)
                editor.apply()
                editor.commit()
                mEmail.text.clear()
                mPassword.text.clear()
                Toast.makeText(context,"Login Successful",Toast.LENGTH_SHORT).show()
                mProgressbar.setVisibility (View.GONE)
                startActivity(Intent(context,MainActivity::class.java))
                fragmentManager!!.popBackStackImmediate()
            } else {
                Toast.makeText(context, " Login Error! Try Again" , Toast.LENGTH_SHORT).show()
                mProgressbar.setVisibility (View.GONE)
            }
        }

        mRegister.setOnClickListener{
            val frag2 = RegisterFragment()
            val fragmentManager = getFragmentManager()
            val fragmentTransaction = fragmentManager!!.beginTransaction()
            fragmentTransaction.replace(R.id.mainlayout, frag2)
        }
    }
}