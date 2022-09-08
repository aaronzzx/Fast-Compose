package com.aaron.compose.feature

/**
 * 用于 CompositionLocal
 */
fun noLocalProvidedFor(name: String): Nothing {
    error("CompositionLocal $name not present")
}