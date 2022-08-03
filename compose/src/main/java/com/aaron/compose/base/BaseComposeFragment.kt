package com.aaron.compose.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.aaron.compose.ui.FragmentComposeView

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/7/30
 */
abstract class BaseComposeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentComposeView(requireContext())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(savedInstanceState)
        (view as ComposeView).setContent {
            PreContent {
                Content()
            }
        }
    }

    protected open fun init(savedInstanceState: Bundle?) {
    }

    @Composable
    protected open fun PreContent(content: @Composable () -> Unit) {
        content()
    }

    @Composable
    protected abstract fun Content()
}