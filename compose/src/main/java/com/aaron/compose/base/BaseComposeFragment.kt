package com.aaron.compose.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/7/30
 */
abstract class BaseComposeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(savedInstanceState)
        (view as ComposeView).setContent {
            MainContent()
        }
    }

    protected open fun init(savedInstanceState: Bundle?) {
    }

    @Composable
    protected open fun MainContent() {
        BaseComposeDefaults.entrance {
            Content()
        }
    }

    @Composable
    protected abstract fun Content()
}