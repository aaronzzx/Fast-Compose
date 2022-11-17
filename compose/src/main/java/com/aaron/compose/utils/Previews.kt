package com.aaron.compose.utils

import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/9/22
 */
@Preview(
    name = "DevicePreview",
    showBackground = true,
    device = Devices.PIXEL_2
)
annotation class DevicePreview

@Preview(
    name = "LargeFontPreview",
    fontScale = 1.5f
)
annotation class LargeFontPreview