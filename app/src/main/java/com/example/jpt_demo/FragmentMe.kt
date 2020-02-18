package com.example.jpt_demo


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_me.*

/**
 * A simple [Fragment] subclass.
 */
class FragmentMe : Fragment() {

    private lateinit var mAuth : FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_me, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()

        val user = mAuth.currentUser
        mefragemail.text = user?.email

        if (!(user!!.isEmailVerified)){
            mefragverificationstatus.text = getString(R.string.notverified)

            btn_verify.setOnClickListener{
                user.sendEmailVerification().addOnCompleteListener {
                    if (it.isSuccessful){
                        Toast.makeText(context,"Verification Email Sent!",Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context,it.exception?.message,Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            mefragverificationstatus.text = getString(R.string.verified)
            btn_verify.setOnClickListener {
                Toast.makeText(context,"Email Already Verified!",Toast.LENGTH_SHORT).show()
            }
        }
    }
}
