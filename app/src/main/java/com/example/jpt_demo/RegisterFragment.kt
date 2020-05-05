package com.example.jpt_demo


import android.content.Intent
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
class RegisterFragment : Fragment() {

    private lateinit var mAuth : FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()

        val mFirstName = view!!.findViewById<View>(R.id.et_firstname) as EditText
        val mLastName = view!!.findViewById<View>(R.id.et_lastname) as EditText
        val mEmail = view!!.findViewById<View>(R.id.et_email) as EditText
        val mPassword = view!!.findViewById<View>(R.id.et_password) as EditText
        val mRePassword = view!!.findViewById<View>(R.id.et_repassword) as EditText
        val mRegisterButton = view!!.findViewById<View>(R.id.btn_register) as Button
        val mLogin = view!!.findViewById<View>(R.id.alreadyregistered) as TextView

        mRegisterButton.setOnClickListener {
            val firstName = mFirstName.text.toString().trim()
            val lastName = mLastName.text.toString().trim()
            val email = mEmail.text.toString().trim()
            val password = mPassword.text.toString().trim()
            val retypedPassword = mRePassword.text.toString().trim()

            if (firstName.isEmpty()){
                mFirstName.error = "Please enter First Name!"
            }
            if (lastName.isEmpty()){
                mLastName.error = "Please enter Last Name!"
            }
            if (email.isEmpty()){
                mEmail.error = "Email is required!"
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                mEmail.error = "Invalid Email Type"
            }
            if (password.isEmpty()){
                mPassword.error = "Please enter password!"
            }
            if (retypedPassword.isEmpty()) {
                mRePassword.error = "Please confirm password!"
            }
            if (password.length < 8 && password.isNotEmpty()){
                mPassword.error = "Password should be at least 8 characters!"
            }
            if (password!=retypedPassword){
                mRePassword.error = "Passwords do not match!"
            }

            if (mFirstName.error.isNullOrEmpty()
                && mLastName.error.isNullOrEmpty()
                && mEmail.error.isNullOrEmpty()
                && mPassword.error.isNullOrEmpty()
                && mRePassword.error.isNullOrEmpty()){

                registerUser(email,password)

                mFirstName.text.clear()
                mLastName.text.clear()
                mEmail.text.clear()
                mPassword.text.clear()
                mRePassword.text.clear()
            } else {
                Toast.makeText(context, " Registration Error! Try Again" , Toast.LENGTH_SHORT).show()
            }
        }

        mLogin.setOnClickListener{
            val frag1 = LoginFragment()
            val fragmentManager = getActivity()!!.supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.mainlayout, frag1)
            fragmentTransaction.commit()
        }
    }

    private fun registerUser(email: String, password: String) {
        val mProgressbar = view!!.findViewById<View>(R.id.registerprogressbar) as ProgressBar
        mProgressbar.visibility = View.VISIBLE
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener {
            mProgressbar.visibility = View.GONE
            if (it.isSuccessful){
                Toast.makeText(context,"Registration Successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(context,MainActivity::class.java))
            } else {
                Toast.makeText(context,it.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}