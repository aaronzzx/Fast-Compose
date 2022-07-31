package com.aaron.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

/**
 * Dp 转像素
 */
@Composable
fun Dp.toPx(): Float = with(LocalDensity.current) { toPx() }

/**
 * Dp 四舍五入转像素
 */
@Composable
fun Dp.roundToPx(): Int = with(LocalDensity.current) { roundToPx() }

/**
 * Dp 转 Sp
 */
@Composable
fun Dp.toSp(): TextUnit = with(LocalDensity.current) { toSp() }

/**
 * Sp 转像素
 */
@Composable
fun TextUnit.toPx(): Float = with(LocalDensity.current) { toPx() }

/**
 * Sp 四舍五入转像素
 */
@Composable
fun TextUnit.roundToPx(): Int = with(LocalDensity.current) { roundToPx() }

/**
 * Sp 转 Dp
 */
@Composable
fun TextUnit.toDp(): Dp = with(LocalDensity.current) { toDp() }

/**
 * 像素转 Dp
 */
@Composable
fun Number.toDp(): Dp = with(LocalDensity.current) { toFloat().toDp() }

/**
 * 像素转 Sp
 */
@Composable
fun Number.toSp(): TextUnit = with(LocalDensity.current) { toFloat().toSp() }