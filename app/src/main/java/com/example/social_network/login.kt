package com.example.social_network

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds


const val ADMOB_AD_UNIT_ID = "ca-app-pub-1839648225801792/8563792553"

class login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        MobileAds.initialize(this) {}

        val adLoader = AdLoader.Builder(this, ADMOB_AD_UNIT_ID)
            .forNativeAd { nativeAd ->
                val background = ColorDrawable(-0x999a)
                val styles: NativeTemplateStyle =
                    com.google.android.ads.nativetemplates.NativeTemplateStyle.Builder()
                        .withMainBackgroundColor(background).build()
                val template: TemplateView = findViewById<TemplateView>(R.id.my_template)
                template.setStyles(styles)
                template.setNativeAd(nativeAd)
            }
            .build()

        adLoader.loadAd(AdRequest.Builder().build())

    }
}