@file:Suppress("NOTHING_TO_INLINE")

package com.aaron.compose.ktx

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

/**
 * Dp 转像素
 */
@Composable
inline fun Dp.toPx(): Float = with(LocalDensity.current) { toPx() }

/**
 * Dp 四舍五入转像素
 */
@Composable
inline fun Dp.roundToPx(): Int = with(LocalDensity.current) { roundToPx() }

/**
 * Dp 转 Sp
 */
@Composable
inline fun Dp.toSp(): TextUnit = with(LocalDensity.current) { toSp() }

/**
 * Sp 转像素
 */
@Composable
inline fun TextUnit.toPx(): Float = with(LocalDensity.current) { toPx() }

/**
 * Sp 四舍五入转像素
 */
@Composable
inline fun TextUnit.roundToPx(): Int = with(LocalDensity.current) { roundToPx() }

/**
 * Sp 转 Dp
 */
@Composable
inline fun TextUnit.toDp(): Dp = with(LocalDensity.current) { toDp() }

/**
 * 像素转 Dp
 */
@Composable
inline fun Number.toDp(): Dp = with(LocalDensity.current) { toFloat().toDp() }

/**
 * 像素转 Sp
 */
@Composable
inline fun Number.toSp(): TextUnit = with(LocalDensity.current) { toFloat().toSp() }