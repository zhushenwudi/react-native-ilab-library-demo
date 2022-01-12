package com.example.reactnativeilablibrarydemo

import android.Manifest
import com.facebook.react.ReactActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.reactnativeilablibrarydemo.R
import com.reactnativeilablibrarydemo.showNoButtonDialog

class MainActivity : ReactActivity() {
    override fun getMainComponentName() = "IlabLibraryDemoExample"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAppPermission()
    }

    private fun checkAppPermission() {
        requestPermission(
            permissions = arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                "com.hidglobal.ia.omnikey.service.permission.SMARTCARDIO"
            ),
            onGrant = {
                myToast("授权成功")
            },
            onRationale = {
                myToast("您拒绝了权限，开启后才可使用读卡器")
            }
        )
    }

    private fun myToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
