package com.example.jpt_demo


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_login.*

/**
 * A simple [Fragment] subclass.
 */
class LoginFragment : Fragment() {

    private lateinit var mAuth : FirebaseAuth
    private lateinit var viewmodel : ProductsViewModel
    private val user = User()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewmodel = ViewModelProvider(this).get(ProductsViewModel::class.java)
        mAuth = FirebaseAuth.getInstance()

        val mEmail = view!!.findViewById<View>(R.id.et_email) as EditText
        val mPassword = view!!.findViewById<View>(R.id.et_password) as EditText
        val mLoginButton = view!!.findViewById<View>(R.id.btn_login) as Button
        val mRegister = view!!.findViewById<View>(R.id.registerhere) as TextView

        mLoginButton.setOnClickListener {
            val email = mEmail.text.toString().trim()
            val password = mPassword.text.toString().trim()

            if (email.isEmpty()){
                mEmail.error = "Email is required!"
            }
            if (password.isEmpty()){
                mPassword.error = "Please enter password!"
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                mEmail.error = "Invalid Email Type"
            }
            if (password.length < 8 && password.isNotEmpty()){
                mPassword.error = "Password should be at least 8 characters!"
            }

            if (mEmail.error.isNullOrEmpty() && mPassword.error.isNullOrEmpty()){
                loginUser(email,password)
                mEmail.text.clear()
                mPassword.text.clear()
            } else {
                Toast.makeText(context, " Login Error! Try Again" , Toast.LENGTH_SHORT).show()
            }
        }

        mRegister.setOnClickListener{
            val frag2 = RegisterFragment()
            val fragmentManager = getActivity()!!.supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.mainlayout, frag2)
            fragmentTransaction.commit()
        }

        tv_reset_password.setOnClickListener{
            if (TextUtils.isEmpty(mEmail.text)){
                Toast.makeText(context, "Please enter your email first", Toast.LENGTH_LONG).show()
            } else{
                resetPassword(mEmail.text.toString().trim())
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        val sharedPrefFile = "LoginPrefFile"
        val sharedPreferences : SharedPreferences = this.activity!!.getSharedPreferences(sharedPrefFile,
            Context.MODE_PRIVATE)
        val mProgressbar = view!!.findViewById<View>(R.id.loginprogressbar) as ProgressBar
        mProgressbar.visibility = View.VISIBLE
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener {
            mProgressbar.visibility = View.GONE
            if (it.isSuccessful){
                val editor : SharedPreferences.Editor = sharedPreferences.edit()
                editor.putString("email_key",email)
                editor.putBoolean("login_state_key", true)
                editor.apply()
                editor.commit()
                user.isLoggedIn = true
                Toast.makeText(context,"Login Successful",Toast.LENGTH_SHORT).show()
                startActivity(Intent(context,MainActivity::class.java))
                fragmentManager?.popBackStack()
            } else {
                Toast.makeText(context,it.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun resetPassword(email : String){
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener {
            if (it.isSuccessful){
                Toast.makeText(context,"Reset Email Sent",Toast.LENGTH_LONG).show()
            } else{
                Toast.makeText(context,it.exception?.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}