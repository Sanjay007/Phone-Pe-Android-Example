package com.mahbio.myapplication

import android.os.Bundle
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.snackbar.Snackbar
import com.mahbio.myapplication.databinding.ActivityMainBinding
import com.phonepe.intent.sdk.api.B2BPGRequestBuilder
import com.phonepe.intent.sdk.api.PhonePe
import com.phonepe.intent.sdk.api.PhonePeInitException
import com.phonepe.intent.sdk.api.models.PhonePeEnvironment
import org.json.JSONObject
import java.math.BigInteger
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)


        PhonePe.init(applicationContext, PhonePeEnvironment.SANDBOX, "PGTESTPAYUAT", "")
        val data = JSONObject()
        data.put("merchantTransactionId","23344334223")
        data.put("merchantId", "PGTESTPAYUAT")
        data.put("merchantUserId", System.currentTimeMillis().toString())
        data.put("amount", 1200)
        data.put("mobileNumber", "999999999")
        data.put("callbackUrl", "https://webhook.site/0ceef0c2-6b5f-48e7-aaa5-f06d7b8ce50a")
        val mPaymentInstrument = JSONObject()
        mPaymentInstrument.put("type", "PAY_PAGE")
            //mPaymentInstrument.put("targetApp", "com.phonepe.simulator")
        data.put("paymentInstrument", mPaymentInstrument)
        val devicecon= JSONObject();
        devicecon.put("deviceOS","ANDROID");
        data.put("deviceContext",devicecon);

        val base64Body: String = Base64.encodeToString(data.toString().toByteArray(
            Charset.defaultCharset()), Base64.NO_WRAP)
        val checkSumGet: String = base64Body + "/pg/v1/pay" + "099eb0cd-02cf-4e2a-8aca-3e6c6aff0399";
        val hexcodedString = toHexString(getSHA(checkSumGet)) + "###" + 1
        val b2BPGRequest = B2BPGRequestBuilder().setData(base64Body).setChecksum(hexcodedString).setUrl("/pg/v1/pay").build()


        binding.fab.setOnClickListener { view ->

            try {
                startActivityForResult(PhonePe.getImplicitIntent(this, b2BPGRequest, "")!!,777);

            } catch(e: PhonePeInitException){
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null)
                    .setAnchorView(R.id.fab).show()
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }


    fun toHexString(hash: ByteArray?): String {
        // Convert byte array into signum representation
        val number = BigInteger(1, hash)

        // Convert message digest into hex value
        val hexString = StringBuilder(number.toString(16))

        // Pad with leading zeros
        while (hexString.length < 64) {
            hexString.insert(0, '0')
        }
        return hexString.toString()
    }

    @Throws(NoSuchAlgorithmException::class)
    fun getSHA(input: String): ByteArray {
        // Static getInstance method is called with hashing SHA
        val md = MessageDigest.getInstance("SHA-256")

        // digest() method called
        // to calculate message digest of an input
        // and return array of byte
        return md.digest(input.toByteArray(StandardCharsets.UTF_8))
    }
}