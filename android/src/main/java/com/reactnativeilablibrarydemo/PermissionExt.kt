package com.reactnativeilablibrarydemo

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.ftd.livepermissions.LivePermissions
import com.ftd.livepermissions.PermissionResult

fun AppCompatActivity.requestPermission(
    vararg permissions: String,
    onGrant: () -> Unit,
    onRationale: () -> Unit,
    onDeny: (() -> Unit)? = null
) {
    LivePermissions(this)
        .request(*permissions)
        .observe(this, Observer<PermissionResult> {
            when (it) {
                is PermissionResult.Grant -> {
                    //权限允许
                    onGrant.invoke()
                }
                is PermissionResult.Rationale -> {
                    //权限拒绝
                    showTwoButtonDialog(
                        this,
                        message = "该功能需开启全部权限",
                        tvBack = "拒绝",
                        tvConfirm = "重选",
                        titleVisible = View.GONE,
                        photo = R.drawable.custom_error,
                        onBackListener = {
                            showErrorDialog(this, "请联系管理员重新授权")
                        },
                        onConfirm = {
                            onRationale.invoke()
                        }
                    )
                }
                is PermissionResult.Deny -> {
                    //权限拒绝，且勾选了不再询问
                    showErrorDialog(this, "请联系管理员重新授权")
                    onDeny?.invoke()
                }
            }
        })
}
