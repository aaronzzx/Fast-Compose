package com.aaron.fastcompose

import android.content.Context
import android.content.Intent
import android.graphics.Outline
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aaron.fastcompose.databinding.ActivitySecondBinding
import com.aaron.fastcompose.databinding.RecyclerItemCardBinding
import com.aaron.fastcompose.ui.theme.FastComposeTheme
import com.blankj.utilcode.util.ConvertUtils
import com.chad.library.adapter.base.BaseQuickAdapter

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/8/30
 */
class SecondActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, SecondActivity::class.java))
        }
    }

    private val binding by lazy { ActivitySecondBinding.inflate(layoutInflater) }

    private val adapter = MyAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.refreshLayout.setOnRefreshListener {

        }

        val rv = binding.rv
        rv.layoutManager = LinearLayoutManager(this)
        rv.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                super.getItemOffsets(outRect, view, parent, state)
                val spacing = ConvertUtils.dp2px(16f)
                val position = parent.getChildAdapterPosition(view)
                if (position != 0) {
                    outRect.top = spacing
                }
            }
        })
        adapter.setOnItemClickListener { _, _, _ ->
            binding.refreshLayout.finishRefresh(true)
        }
        rv.adapter = adapter

        adapter.addAll(fillData())
    }

    private fun fillData(): List<String> = listOf(
        "James", "Michael", "Second", "Compose",
        "Activity", "Community", "Information", "What",
        "Cool", "Tea", "Rice", "Basketball", "Footbal"
    )
}

private class MyAdapter : BaseQuickAdapter<String, ComposeViewHolder>() {

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): ComposeViewHolder {
        return ComposeViewHolder(ComposeView(context))
    }

    override fun onBindViewHolder(holder: ComposeViewHolder, position: Int, item: String?) {
        holder.setContent {
            Card(position = position, string = item)
        }
    }
}

private class MyAdapter2 : BaseQuickAdapter<String, RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val binding = RecyclerItemCardBinding.inflate(LayoutInflater.from(parent.context))
        return object : RecyclerView.ViewHolder(binding.root) {}
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, item: String?) {
        holder.itemView.findViewById<TextView>(R.id.tv).text = item
        holder.itemView.findViewById<ImageView>(R.id.avatar).also {
            it.clipToOutline = true
            it.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setOval(0, 0, view.width, view.height)
                }
            }
        }
    }
}

@Preview
@Composable
private fun Card(position: Int = 0, string: String? = "James") {
    FastComposeTheme {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(4.dp),
            color = Color.White,
            elevation = 4.dp
        ) {
            Column {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        painter = painterResource(id = R.drawable.ide_bg),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.padding(8.dp))
                    Text(
                        text = string ?: "Unknown",
                        fontSize = 20.sp
                    )
                }
                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    painter = painterResource(id = R.drawable.ide_bg),
                    contentDescription = null
                )
            }
        }
    }
}

open class ComposeViewHolder(private val composeView: ComposeView) :
    RecyclerView.ViewHolder(composeView) {

    open fun setContent(content: @Composable () -> Unit) {
        composeView.setContent(content)
    }
}