package com.example.jpt_demo


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

/**
 * A simple [Fragment] subclass.
 */
class RegisterFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val mFirstName = view!!.findViewById<View>(R.id.et_firstname) as EditText
        val mLastName = view!!.findViewById<View>(R.id.et_lastname) as EditText
        val mEmail = view!!.findViewById<View>(R.id.et_email) as EditText
        val mPassword = view!!.findViewById<View>(R.id.et_password) as EditText
        val mRePassword = view!!.findViewById<View>(R.id.et_repassword) as EditText
        val mProgressbar = view!!.findViewById<View>(R.id.loginprogressbar) as ProgressBar
        val mLoginbutton = view!!.findViewById<View>(R.id.btn_login) as Button
        val mLogin = view!!.findViewById<View>(R.id.alreadyregistered) as TextView

        mLoginbutton.setOnClickListener {
            val firstname = mFirstName.text.toString().trim()
            val lastname = mLastName.text.toString().trim()
            val email = mEmail.text.toString().trim()
            val password = mPassword.text.toString().trim()
            val repassword = mRePassword.text.toString().trim()

            if (firstname.isEmpty()){
                mFirstName.setError("Please enter First Name!")
            }
            if (lastname.isEmpty()){
                mLastName.setError("Please enter Last Name!")
            }
            if (email.isEmpty()){
                mEmail.setError("Email is required!")
            }
            if (password.isEmpty()){
                mPassword.setError("Please enter password!")
            }
            if (repassword.isEmpty()) {
                mRePassword.setError("Please confirm password!")
            }
            if (password.length < 8 && password.isNotEmpty()){
                mPassword.setError("Password should be at least 8 characters!")
            }
            if (password!=repassword){
                mRePassword.setError("Passwords do not match!")
            }

            mProgressbar.setVisibility (View.VISIBLE)

            if (mFirstName.error.isNullOrEmpty()
                && mLastName.error.isNullOrEmpty()
                && mEmail.error.isNullOrEmpty()
                && mPassword.error.isNullOrEmpty()
                && mRePassword.error.isNullOrEmpty()){
                mFirstName.text.clear()
                mLastName.text.clear()
                mEmail.text.clear()
                mPassword.text.clear()
                mRePassword.text.clear()
                Toast.makeText(context,"Registration Successful", Toast.LENGTH_SHORT).show()
                mProgressbar.setVisibility (View.GONE)
                startActivity(Intent(context,MainActivity::class.java))
            } else {
                Toast.makeText(context, " Registration Error! Try Again" , Toast.LENGTH_SHORT).show()
            }
        }

        mLogin.setOnClickListener{
            startActivity(Intent(context,LoginFragment::class.java))
        }
    }
}
