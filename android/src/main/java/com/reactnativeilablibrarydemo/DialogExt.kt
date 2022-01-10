package com.reactnativeilablibrarydemo

import android.app.Dialog
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.reactnativeilablibrarydemo.Const.DIALOG_TIMER
import com.reactnativeilablibrarydemo.Const.TIMER_UNIT
import java.util.concurrent.atomic.AtomicInteger

// 0: 无弹窗, 1: 有弹窗
val isHasDialog = AtomicInteger(0)
var noButtonDialog: Dialog? = null

const val TITLE = "标题"
const val MESSAGE = "消息"
const val HINT = "提示"
const val BACK = "返回首页"
const val CONFIRM = "我知道了"
const val DIALOG_WIDTH = 628
const val DIALOG_HEIGHT = 464

fun AppCompatActivity.showTwoButtonDialog(
    owner: LifecycleOwner,
    time: Long = DIALOG_TIMER * TIMER_UNIT,
    photo: Int = R.drawable.custom_error,
    photoVisible: Int = View.VISIBLE,
    title: String = TITLE,
    titleVisible: Int = View.VISIBLE,
    message: String = MESSAGE,
    messageVisible: Int = View.VISIBLE,
    hint: String = HINT,
    hintVisible: Int = View.VISIBLE,
    tvBack: String = BACK,
    tvConfirm: String = CONFIRM,
    onBackListener: (() -> Unit) = {},
    onConfirm: (() -> Unit) = onBackListener,
    onTimeout: (() -> Unit) = onBackListener,
    dialogWidth: Int = DIALOG_WIDTH,
    dialogHeight: Int = DIALOG_HEIGHT
) {
    if (!isFinishing) {
        if (isHasDialog.get() == 0) {
            isHasDialog.set(1)
            TwoButtonDialog(
                this, time, photo, photoVisible, title,
                titleVisible, message, messageVisible, hint, hintVisible, tvBack, tvConfirm,
                onBackListener = {
                    isHasDialog.set(0)
                    onBackListener.invoke()
                },
                onConfirmListener = {
                    isHasDialog.set(0)
                    onConfirm.invoke()
                },
                onTimeout = {
                    isHasDialog.set(0)
                    onTimeout.invoke()
                },
                dialogWidth = dialogWidth,
                dialogHeight = dialogHeight
            ).lifecycleOwner(owner).show()
        }
    }
}

/**
 * 打开错误对话框
 */
fun AppCompatActivity.showErrorDialog(owner: LifecycleOwner, msg: String) {
    if (!isFinishing) {
        if (isHasDialog.get() == 0) {
            isHasDialog.set(1)
            OneButtonDialog(
                context = this,
                photo = R.drawable.custom_error,
                titleVisible = View.GONE,
                message = if (msg.length > 20) msg.substring(0, 19) else msg,
                onConfirmListener = {
                    isHasDialog.set(0)
                },
                onTimeout = {
                    isHasDialog.set(0)
                }
            ).lifecycleOwner(owner).show()
        }
    }
}

fun AppCompatActivity.showNoButtonDialog(
    owner: LifecycleOwner,
    title: String = TITLE,
    message: String = MESSAGE,
    messageVisible: Int = View.VISIBLE,
    photo: Int = R.drawable.custom_error,
    time: Long = DIALOG_TIMER * TIMER_UNIT,
    onConfirmListener: () -> Unit = {},
    onTimeout: () -> Unit = onConfirmListener
) {
    if (!isFinishing) {
        if (isHasDialog.get() == 0) {
            isHasDialog.set(1)
            noButtonDialog = OneButtonDialog(
                context = this,
                title = title,
                message = message,
                messageVisible = messageVisible,
                photo = photo,
                time = time,
                onConfirmListener = {
                    isHasDialog.set(0)
                    onConfirmListener.invoke()
                },
                onTimeout = {
                    isHasDialog.set(0)
                    onTimeout.invoke()
                },
                hideButton = true
            ).lifecycleOwner(owner)
            noButtonDialog?.show()
        }
    }
}

fun hideNoButtonDialog() {
    if (isHasDialog.get() == 1) {
        noButtonDialog?.hide()
        noButtonDialog = null
        isHasDialog.set(0)
    }
}


fun <T : Dialog> T.lifecycleOwner(owner: LifecycleOwner? = null): T {
    val observer = DialogLifecycleObserver(::dismiss)
    val lifecycleOwner = owner ?: (context as? LifecycleOwner
        ?: throw IllegalStateException(
            "$context is not a LifecycleOwner."
        ))
    lifecycleOwner.lifecycle.addObserver(observer)
    return this
}

internal class DialogLifecycleObserver(private val dismiss: () -> Unit) : LifecycleObserver {
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() = run {
        dismiss()
        isHasDialog.set(0)
    }
}
