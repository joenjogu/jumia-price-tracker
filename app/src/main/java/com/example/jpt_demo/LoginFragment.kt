package com.example.jpt_demo


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

/**
 * A simple [Fragment] subclass.
 */
class LoginFragment : Fragment() {

    private lateinit var mAuth : FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()

        val mEmail = view!!.findViewById<View>(R.id.et_email) as EditText
        val mPassword = view!!.findViewById<View>(R.id.et_password) as EditText
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
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                mEmail.setError("Invalid Email Type")
            }
            if (password.length < 8 && password.isNotEmpty()){
                mPassword.setError("Password should be at least 8 characters!")
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
                Toast.makeText(context,"Login Successful",Toast.LENGTH_SHORT).show()
                startActivity(Intent(context,MainActivity::class.java))
            } else {
                Toast.makeText(context,it.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}