package com.example.whatsappclone.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import com.example.whatsappclone.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var countryCode:String
    private lateinit var phoneNumber:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //Hint Request Assignment not complete
        /*
        val request = GetPhoneNumberHintIntentRequest.builder().build()

        val phoneNumberHintIntentResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
                try {
                    val phone = Identity.getSignInClient(this).getPhoneNumberFromIntent(result.data)
                }catch (e: Exception){
                    Log.e("TAG","Phone Number Hint Failed")
                }
        }
            Identity.getSignInClient(this)
            .getPhoneNumberHintIntent(request)
            .addOnSuccessListener {
                try {
                    phoneNumberHintIntentResultLauncher.launch(request.intent)
                }catch (e: Exception){
                    Log.e("TAG","Launching the Pending Intent Failed")
                }
            }
                .addOnFailureListener(
                    Log.e("TAG","Phone Number Hint Failed")
                )
         */

        phoneNumberEt.addTextChangedListener {
            nextBtn.isEnabled = !(it.isNullOrEmpty() || it.length < 10)
        }
        nextBtn.setOnClickListener {
            checkNumber()
        }

    }

    private fun checkNumber() {
        countryCode = ccp.selectedCountryCodeWithPlus
        phoneNumber = countryCode + phoneNumberEt.text.toString()

        notifyUser()
    }

    private fun notifyUser() {
        MaterialAlertDialogBuilder(this).apply {
            setMessage("We will be verifying the phone number: $phoneNumber\n"+
            "Is this OK, or would you like to edit the number?")

            setPositiveButton("OK"){_,_ ->
                showOtpActivity()
            }

            setNegativeButton("Edit"){ dialog, _ ->
                dialog.dismiss()
            }
            setCancelable(false)
            create()
            show()
        }
    }

    private fun showOtpActivity() {
        startActivity(Intent(this@LoginActivity, OtpActivity::class.java).putExtra(PHONE_NUMBER,phoneNumber))
        finish()
    }
}