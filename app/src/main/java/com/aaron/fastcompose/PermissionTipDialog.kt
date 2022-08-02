package com.aaron.fastcompose

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView

/**
 * @author : Create by DS-Q
 * @date : 2022/7/5
 * description:
 **/
class PermissionTipDialog(context: Context) : Dialog(context, R.style.style_base_dialog) {

    private var tvContent: TextView? = null
    private var content: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_permission_tip)
        tvContent = findViewById(R.id.tv_content)
        val window = window
        val lp = window!!.attributes
        lp.gravity = Gravity.TOP
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        window.attributes = lp

        tvContent?.let {
            it.text = content
        }
    }

    fun showDialog(permission: String) {

        content = when (permission) {

            Manifest.permission.READ_CONTACTS -> {
                "需要您授予GimiGimi  APP通讯录权限，以开启匹配和推荐通讯录中的朋友添加等服务"
            }
            Manifest.permission.ACCESS_FINE_LOCATION -> {
                "需要您授予GimiGimi  APP位置信息权限，以开启与位置相关的推荐/定位/安全保障等服务"
            }
            Manifest.permission.CAMERA -> {
                "需要您授予GimiGimi  APP访问您的摄像头权限，以开启拍照/视频通话/视频拍摄/扫一扫/直播/嗨聊室等服务"
            }
            Manifest.permission.RECORD_AUDIO -> {
                "需要您授予GimiGimi  APP录制音频权限，以开启语音通话/视频通话/直播/嗨聊室等服务"
            }
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                "需要您授予GimiGimi  APP访问您的相册/文件夹权限，以开启文件上传下载/视频上传/图片上传下载/开启直播/开启嗨聊室等服务"
            }
            else -> {
                ""
            }
        }

        tvContent?.let {
            it.text = content
        }

        if (!isShowing && !TextUtils.isEmpty(content)) {
            show()
        }
    }


}