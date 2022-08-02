package com.aaron.fastcompose

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.TextUtils
import android.widget.TextView

/**
 * @author : Create by DS-Q
 * @date : 2022/7/5
 * description:权限打开提示dialog
 **/
class PermissionOpenTipDialog(context: Context) : Dialog(context, R.style.style_base_dialog) {

    private var tvContent: TextView? = null
    private var content: String = ""
    private val handler by lazy { Handler() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_permission_open_tip)
        tvContent = findViewById(R.id.tv_msg)


        tvContent?.let {
            it.text = content
        }

        findViewById<TextView>(R.id.tv_left).setOnClickListener { dismiss() }
        findViewById<TextView>(R.id.tv_right).setOnClickListener {
            context.startActivity(Intent().also {
                it.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                it.data = Uri.parse("package:${context.packageName}")
            })
            dismiss()
        }
    }

    fun showDialog(permission: String) {

        content = when (permission) {

            Manifest.permission.READ_CONTACTS -> {
                "在设置-应用-GimiGimi-权限中开启通讯录权限，以正常使用匹配和推荐通讯录中的朋友添加等功能"
            }
            Manifest.permission.ACCESS_FINE_LOCATION -> {
                "在设置-应用-GimiGimi-权限中开启位置权限，以正常使用与位置相关的推荐/定位/安全保障等功能"
            }
            Manifest.permission.CAMERA -> {
                "在设置-应用-GimiGimi-权限中开启相机权限，以正常使用拍照/视频通话/视频拍摄/扫一扫/直播/嗨聊室等功能"
            }
            Manifest.permission.RECORD_AUDIO -> {
                "在设置-应用-GimiGimi-权限中开启麦克风权限，以正常使用语音通话/视频通话/直播/嗨聊室等功能"
            }
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                "在设置-应用-GimiGimi-权限中开启相册/文件夹权限，以正常使用文件上传下载/视频上传/图片上传下载/开启直播/开启嗨聊室等功能"
            }
            else -> {
                ""
            }
        }

        tvContent?.let {
            it.text = content
        }

        if (!isShowing && !TextUtils.isEmpty(content)) {
            handler.postDelayed({ show() }, 100)
        }
    }


}