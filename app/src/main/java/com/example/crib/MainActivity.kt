package com.example.crib

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.Telephony
import android.text.Editable
import android.text.TextUtils
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {

    lateinit var searchBtn: MaterialButton
    lateinit var mobileNumberET: AppCompatEditText
    lateinit var endDaysET: AppCompatEditText
    lateinit var resultTV: AppCompatTextView
    lateinit var context: Context
    lateinit var pBar: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = this
        init()
        handleAction()

    }

    fun handleAction() {
        searchBtn.setOnClickListener {
            if (isValid(mobileNumberET, endDaysET)) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_SMS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.READ_SMS
                        )
                    ) {
                        showExplanation(
                            "Permission Needed",
                            "Please give sms reading permission from phone setting",
                            Manifest.permission.READ_SMS,
                            101
                        )

                    } else {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.READ_SMS), 1
                        )
                    }

                } else {
                    coroutines()

                }


            }

        }

    }

    private fun showExplanation(
        title: String,
        message: String,
        permission: String,
        permissionRequestCode: Int
    ) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton(
                android.R.string.ok
            ) { dialog, id ->
                ActivityCompat.requestPermissions(
                    (context as Activity), arrayOf(
                        Manifest.permission.READ_SMS
                    ), permissionRequestCode
                )
            }
        builder.create().show()
    }

    private fun coroutines() {
        pBar.visibility = View.VISIBLE
        GlobalScope.launch(Dispatchers.IO) {
            searchMessage(mobileNumberET.text, endDaysET.text)
        }

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    if ((ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.READ_SMS
                        ) ===
                                PackageManager.PERMISSION_GRANTED)
                    ) {
                        coroutines()
                    }
                } else {
                    showExplanation(
                        "Permission Needed",
                        "Please give sms reading permission from phone setting",
                        Manifest.permission.READ_SMS,
                        101
                    )
                }
                return
            }

        }
    }


    suspend fun searchMessage(mobileNumber: Editable?, endDate: Editable?) {
        val nDaysBack = 24 * endDate.toString().toLong() * 60 * 60 * 1000
        var totalMes = 0
        val currentTimeMilis = System.currentTimeMillis()
        val nDaysBackTime: Long = currentTimeMilis - nDaysBack


        val phoneNumber = arrayOf(mobileNumber.toString())
        val cursor1: Cursor? = contentResolver.query(
            Uri.parse("content://sms/inbox"),
            arrayOf("_id", "thread_id", "address", "person", "date", "body", "type"),
            "address=?",
            phoneNumber,
            null
        )
        if (cursor1?.moveToFirst()!!) {
            do {

                if (cursor1.getString(cursor1.getColumnIndex(Telephony.Sms.DATE))
                        .toLong() >= nDaysBackTime
                ) {
                    totalMes += 1
                }


            } while (cursor1.moveToNext())
            handleResult(totalMes)


        } else {
            handleResult(totalMes)

        }

    }

    suspend fun handleResult(totalMes: Int) {
        withContext(context = Dispatchers.Main) {
            pBar.visibility = View.GONE
            resultTV.text =
                if (totalMes == 0) "Sorry, no messages found" else totalMes.toString() + " number of messages found"
        }
    }

    fun init() {
        searchBtn = findViewById(R.id.searchBtn)
        mobileNumberET = findViewById(R.id.numberET)
        endDaysET = findViewById(R.id.dateET)
        resultTV = findViewById(R.id.resultTV)
        pBar = findViewById(R.id.pBar)

    }


    private fun isValid(
        mobileNumberET: AppCompatEditText,
        endDaysET: AppCompatEditText
    ): Boolean {
        if (TextUtils.isEmpty(mobileNumberET.text.toString())) {
            Toast.makeText(this, "Please enter mobile number", Toast.LENGTH_LONG)
                .show()
            return false
        }
        if (TextUtils.isEmpty(endDaysET.text.toString())) {
            Toast.makeText(this, "Please enter days", Toast.LENGTH_LONG).show()
            return false

        }
        return true

    }
}






