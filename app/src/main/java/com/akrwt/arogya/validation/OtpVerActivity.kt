package com.akrwt.arogya.validation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.akrwt.arogya.R
import com.google.android.gms.tasks.TaskExecutors
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_otp_ver.*
import java.util.concurrent.TimeUnit

class OtpVerActivity : AppCompatActivity() {

    private var mVerificationId: String? = null
    private var mAuth: FirebaseAuth? = null

    private val mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {

            //Getting the code sent by SMS
            val code: String? = phoneAuthCredential.smsCode

            if (code != null) {
                editTextCode.setText(code)
                verifyVerificationCode(code)
            } else {
                Toast.makeText(applicationContext, "Failed", Toast.LENGTH_LONG).show()
            }
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Toast.makeText(this@OtpVerActivity, e.message, Toast.LENGTH_LONG).show()
        }

        override fun onCodeSent(
            s: String,
            forceResendingToken: PhoneAuthProvider.ForceResendingToken
        ) {
            mVerificationId = s
            progressbar.visibility = View.GONE
            textView.text = "Code Sent .."
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_ver)

        mAuth = FirebaseAuth.getInstance()

        if(intent.hasExtra("mobile")) {

            val mobile = intent.getStringExtra("mobile")
            sendVerificationCode(mobile)
        }

        buttonSignIn.setOnClickListener {
            val code = editTextCode.text.toString()
            if (code.isEmpty() || code.length < 6) {
                Toast.makeText(applicationContext, "Code is not valid", Toast.LENGTH_LONG).show()
            } else {

                verifyVerificationCode(code)
            }
        }
    }

    private fun sendVerificationCode(mobile: String?) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            "+91" + mobile!!,
            60,
            TimeUnit.SECONDS,
            TaskExecutors.MAIN_THREAD,
            mCallbacks
        )
    }


    private fun verifyVerificationCode(code: String?) {
        //creating the credentials
        val credential = PhoneAuthProvider.getCredential(mVerificationId!!, code!!)

        //signing in the user
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(this@OtpVerActivity) { task ->
                if (task.isSuccessful) {
                    if(intent.hasExtra("mobile")) {
                        val mobile = intent.getStringExtra("mobile")
                        val intent = Intent(this@OtpVerActivity, UserInfoActivity::class.java)
                        intent.putExtra("phone",mobile)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }

                } else {

                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(
                            applicationContext,
                            "Invalid Code Entered ...",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }
            }
    }
}
