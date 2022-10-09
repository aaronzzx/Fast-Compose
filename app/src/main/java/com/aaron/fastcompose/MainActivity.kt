package com.aaron.fastcompose

import android.content.Intent
import android.os.Bundle
import androidx.compose.runtime.Composable
import com.aaron.compose.base.BaseComposeActivity
import com.aaron.fastcompose.paging3.PagingActivity

class MainActivity : BaseComposeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        SecondActivity.start(this)
        startActivity(Intent(this, PagingActivity::class.java))
    }

    @Composable
    override fun Content() {
    }
}