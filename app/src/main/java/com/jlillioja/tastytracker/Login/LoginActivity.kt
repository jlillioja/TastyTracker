package com.jlillioja.tastytracker.Login

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.login_button
import com.facebook.login.LoginResult
import android.content.Intent
import android.util.Log
import com.facebook.*
import com.jlillioja.tastytracker.Facebook.FacebookNetworkWrapper
import com.jlillioja.tastytracker.R
import com.jlillioja.tastytracker.Watchlist.WatchlistActivity


private val LOG_TAG = "Login Activity"

class LoginActivity : AppCompatActivity() {

    val callbackManager = CallbackManager.Factory.create()
    val retrofit = FacebookNetworkWrapper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        FacebookSdk.sdkInitialize(getApplicationContext())


        val accessToken = AccessToken.getCurrentAccessToken()
        if (accessToken.isExpired) { AccessToken.refreshCurrentAccessTokenAsync(validTokenCallback()) } else {
            navigateWithValidToken(accessToken.token)
        }

        login_button.setReadPermissions("public_profile")
        login_button.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                navigateWithValidToken(loginResult.accessToken.token)
            }

            override fun onCancel() {
                Log.d(LOG_TAG, "failure!")
            }

            override fun onError(exception: FacebookException) {
                Log.d(LOG_TAG, "error!")
            }
        })
    }

    private fun validTokenCallback(): AccessToken.AccessTokenRefreshCallback {
        return object: AccessToken.AccessTokenRefreshCallback {
            override fun OnTokenRefreshed(accessToken: AccessToken) {
                navigateWithValidToken(accessToken.token)
            }
            override fun OnTokenRefreshFailed(exception: FacebookException) {
                Log.d(LOG_TAG, "refresh failed")
            }
        }
    }

    private fun navigateWithValidToken(token: String) {
        val intent = Intent(this, WatchlistActivity::class.java)
        intent.putExtra("token", token)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
}
