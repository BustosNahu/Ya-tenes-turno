package com.yatenesturno.activities.splash_activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.yatenesturno.R
import com.yatenesturno.activities.first_shop.FirstShop
import com.yatenesturno.activities.main_screen.MainActivity
import com.yatenesturno.activities.onboarding.OnboardingFragment
import com.yatenesturno.activities.sign_in.ActivitySignIn
import com.yatenesturno.databinding.ActivitySplashBinding
import com.yatenesturno.functionality.ManagerPlace
import com.yatenesturno.functionality.ManagerPlace.OnPlaceListFetchListener
import com.yatenesturno.functionality.PlacePremiumManager
import com.yatenesturno.object_interfaces.Place
import com.yatenesturno.user_auth.UserManagement
import com.yatenesturno.user_auth.UserManagement.UserManagementAuthenticateListener
import kotlinx.coroutines.*

class SplashActivity : AppCompatActivity() {
    private val editor: SharedPreferences.Editor? = null
    private lateinit var binding: ActivitySplashBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = getColor(R.color.black)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var isAnimating = true
        UserManagement.getInstance().authenticate(this@SplashActivity, userCallback)
        binding.lottieSplashAnimation.visibility = View.VISIBLE
        binding.lottieSplashAnimation.playAnimation()
        binding.lottieSplashAnimation.loop(false)


    }



    private fun startSignInActivity() {
        val i = Intent(this@SplashActivity, ActivitySignIn::class.java)
        startActivity(i)
        overridePendingTransition(R.anim.short_slide_up_in, R.anim.short_slide_up_out)

        finish()
    }

    private var userCallback = (object : UserManagementAuthenticateListener{
        override fun userNotAuthenticated() {

            CoroutineScope(Dispatchers.Main).launch {
                if (isFirstTime) {
                    delay(3400)
                    val fragmentTransaction = supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.slide_up_in, R.anim.slide_down_out)
                    fragmentTransaction.replace(R.id.splash_screen, OnboardingFragment()).commit()
                    binding.lottieSplashAnimation.visibility = View.GONE
                } else {
                    delay(3300)
                    startSignInActivity()
                }

            }

        }

        override fun userAuthenticated() {
            userIsAuthenticated()
        }
    })

    private fun userIsAuthenticated() {
        checkCanCreateNewPlace()
    }

    private fun checkCanCreateNewPlace() {
        ManagerPlace.getInstance().getOwnedPlaces(object : OnPlaceListFetchListener {
            override fun onFetch(placeList: List<Place>) {
                Log.d("checkCanCreateNewPlace", "OnFetch , $placeList" )

                Handler().postDelayed({ binding.lottieSplashAnimation.animate() }, 2000)
                var hasAtLeastOnePremium = false
                for (place in placeList) {
                    if (PlacePremiumManager.getInstance()
                            .getIsPremium(place.id, UserManagement.getInstance().user.id)
                    ) {
                        hasAtLeastOnePremium = true
                        break
                    }
                }
                if (hasAtLeastOnePremium) {
                    val intent = Intent(this@SplashActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_up_in, R.anim.slide_down_out)
                    finish()
                } else {
                    if (placeList.isNotEmpty()) {
                        val intent = Intent(this@SplashActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                        overridePendingTransition(R.anim.slide_up_in, R.anim.slide_down_out)
                        finish()
                    } else {
                        if (isFirstShop){
                            val intent = Intent(this@SplashActivity, FirstShop::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            startActivity(intent)
                            overridePendingTransition(R.anim.slide_up_in, R.anim.slide_down_out)
                            finish()
                        }else{
                            val intent2 = Intent(this@SplashActivity, MainActivity::class.java)
                            intent2.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            startActivity(intent2)
                            overridePendingTransition(R.anim.slide_up_in, R.anim.slide_down_out)
                            finish()
                        }

                    }
                }
            }

            override fun onFailure() {
                Log.d("checkCanCreateNewPlace", "onFailure" ) /* !!! Hit visitElement for element type: class org.jetbrains.kotlin.nj2k.tree.JKErrorExpression !!! */

                Toast.makeText(applicationContext, "Error de Conexion", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private val isFirstShop: Boolean
        private get() {
            val sharedPreferences = getSharedPreferences("firstShop", MODE_PRIVATE)
            return sharedPreferences!!.getBoolean("firstShop", true)
        }
    private val isFirstTime: Boolean
        private get() {
            val sharedPreferences = getSharedPreferences("isFirstTime", MODE_PRIVATE)
            return sharedPreferences!!.getBoolean("isFirstTime", true)
        }
}