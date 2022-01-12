package com.reactnativeilablibrarydemo

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.ilab.omnikey.OmniCard
import dev.utils.app.AppUtils
import dev.utils.app.PathUtils
import dev.utils.app.ResourceUtils
import dev.utils.app.toast.ToastUtils
import dev.utils.common.FileUtils
import top.wuhaojie.installerlibrary.AutoInstaller
import java.io.File
import java.math.BigDecimal
import java.util.concurrent.atomic.AtomicBoolean
import java.util.regex.Pattern
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter


class ILabLibraryDemoModule(
    reactContext: ReactApplicationContext
) : ReactContextBaseJavaModule(reactContext) {

    // 读卡器读到的信息
    private val omniMsg = MutableLiveData<Pair<OmniCard.Status, String>>()

    // 允许处理读卡器的消息
    private var isCanRead = AtomicBoolean(false)

    override fun getName() = "ILabLibraryOmniKey"

    init {
        this.currentActivity?.run {
            val owner = this as LifecycleOwner
            omniMsg.observe(owner, Observer {
                if (it.second == OmniCard.READER_NOT_FOUND) {
                    showNoButtonDialog(
                        owner = owner,
                        title = "未找到刷卡硬件",
                        messageVisible = View.GONE,
                        photo = R.drawable.card_red
                    )
                    OmniCard.unbind()
                    return@Observer
                }
                if (it.first == OmniCard.Status.MESSAGE && isCanRead.get()) {
                    showNoButtonDialog(
                        owner = owner,
                        title = "温馨提示",
                        message = "请将IC卡放置到您的读写设备上",
                        photo = R.drawable.iccard_hand,
                        time = 30 * 1000,
                        onTimeout = {
                            OmniCard.unbind()
                        }
                    )
                    return@Observer
                }
                if (isNumeric(it.second) && isCanRead.get()) {
                    hideNoButtonDialog()
                    isCanRead.set(false)
                    OmniCard.unbind()
                    reactApplicationContext
                        .getJSModule(RCTDeviceEventEmitter::class.java)
                        .emit("cardId", it.second)
                    Log.e("aaa", it.second)
                }
            })
        }
    }

    @ReactMethod
    fun readCard() {
        val activity = this.currentActivity

        if (ContextCompat.checkSelfPermission(
                reactApplicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (AppUtils.isInstalledApp(OmniCard.PACKAGE_NAME)) {
                requestCardPermission()
            } else {
                installOmniCardApk()
                activity?.showNoButtonDialog(
                    owner = activity as LifecycleOwner,
                    photo = R.drawable.contacted_icon,
                    title = "正在安装读卡驱动软件...",
                    messageVisible = View.INVISIBLE,
                    onTimeout = {
                        requestCardPermission(true)
                    }
                )
            }
        } else {
            activity?.showNoButtonDialog(
                owner = activity as LifecycleOwner,
                title = "您未赋予权限，无法使用此功能",
                messageVisible = View.INVISIBLE
            )
        }
    }

    private fun installOmniCardApk() {
        val dest = PathUtils.getSDCard().sdCardPath + File.separator + OmniCard.APK_NAME
        try {
            FileUtils.copyFile(ResourceUtils.getAssets().open(OmniCard.APK_NAME), dest, true)
            AutoInstaller.getDefault(reactApplicationContext.applicationContext).install(dest)
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtils.showLong("未找到要安装的 APK")
        }
    }

    private fun requestCardPermission(isInit: Boolean = false) {
        if (ContextCompat.checkSelfPermission(
                reactApplicationContext,
                OmniCard.CARD_PERMISSION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            isCanRead.set(true)
            //权限允许
            OmniCard.bind(
                reactApplicationContext.applicationContext as Application,
                omniMsg,
                isInit
            )
        } else {
            val activity = this.currentActivity
            activity?.showNoButtonDialog(
                owner = activity as LifecycleOwner,
                title = "您未赋予权限，无法使用此功能",
                messageVisible = View.INVISIBLE
            )
        }
    }

    // 匹配是否为数字
    private fun isNumeric(str: String): Boolean {
        // 该正则表达式可以匹配所有的数字 包括负数
        val pattern = Pattern.compile("-?[0-9]+(\\.[0-9]+)?")
        val bigStr = try {
            BigDecimal(str).toString()
        } catch (e: java.lang.Exception) {
            return false //异常 说明包含非数字。
        }
        val isNum = pattern.matcher(bigStr) // matcher是全匹配
        return isNum.matches()
    }

    @ReactMethod
    fun release() {
        OmniCard.release()
    }
}
