package com.aaron.fastcompose

import android.Manifest
import android.os.Handler
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.PermissionUtils
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun CheckPermissionsComponent(
    permissions: Array<String>,
    enabled: Boolean = true,
    onSuccess: () -> Unit,
    onFailure: () -> Unit,
    content: @Composable () -> Unit
) {
    Log.d("zzx", "CheckPermissionsComponent")
    if (enabled) {
        val act = LocalContext.current as FragmentActivity
        LaunchedEffect(Unit) {
            act.checkPermissions(permissions, onSuccess, onFailure)
        }
    }
    content()
}

/**
 * 批量检查并申请权限
 */
inline fun FragmentActivity.checkPermissions(
        permissions: Array<String>,
        crossinline success: () -> Unit,
        noinline failure: (() -> Unit)
) {
    requestPermissions(this, permissions, success, failure)
}

inline fun requestPermissions(
        activity: FragmentActivity,
        permissions: Array<String>,
        crossinline success: () -> Unit,
        noinline failure: (() -> Unit)?
) {
    val dialog = PermissionTipDialog(activity)
    activity.lifecycleScope.launch {
        var isOk = true
        var deniedPermission = ""
        for (permission in permissions) {
            deniedPermission = permission
            if (PermissionUtils.isGranted(permission)) {
                continue
            }
            isOk = permissionSingle(activity, dialog, permission)
            if (!isOk) break
        }
        dialog.dismiss()
        if (isOk) {
            success()
        } else {
            PermissionOpenTipDialog(activity).showDialog(deniedPermission)
            failure?.invoke()
        }
    }
}

/**
 * 申请单个权限
 */
suspend fun permissionSingle(activity: FragmentActivity, dialog: PermissionTipDialog, permission: String): Boolean {
    var canShowDialog = true
    return suspendCoroutine {
        PermissionUtils.permission(permission).callback(object : PermissionUtils.SimpleCallback {
            override fun onGranted() {
                dialog.dismiss()
                it.resume(true)
            }

            override fun onDenied() {
                dialog.dismiss()
                canShowDialog = false
                it.resume(false)
            }
        }).request()

        //如果可以申请权限的直接弹出
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && activity.shouldShowRequestPermissionRationale(permission)) {
            dialog.showDialog(permission)
        } else {
            Handler().postDelayed({
                if (canShowDialog) {
                    dialog.showDialog(permission)
                }
            }, 300)
        }
    }
}




/**
 * 检查并申请定位权限
 */
inline fun FragmentActivity.checkLocationPermissions(
        crossinline success: () -> Unit,
        noinline failure: (() -> Unit)? = null
) {
    requestPermissions(this, arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION
    ), { success() }, {
        failure?.invoke()
    })
}

/**
 * 检查并申请相机权限
 */
inline fun FragmentActivity.checkCameraPermissions(
        crossinline success: () -> Unit,
        noinline failure: (() -> Unit)? = null
) {

    requestPermissions(this, arrayOf(Manifest.permission.CAMERA), { success() }, {
        failure?.invoke()
    })

}

/**
 * 检查并申请存储权限
 */
inline fun FragmentActivity.checkStoragePermissions(
        crossinline success: () -> Unit,
        noinline failure: (() -> Unit)? = null
) {
    requestPermissions(this, arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    ), { success() }, {
        failure?.invoke()
    })
}


