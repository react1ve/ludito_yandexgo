package com.reactive.premier.utils.extensions

inline fun <reified T> Any?.tryCast(block: T.() -> Unit) {
    if (this is T) block()
}

inline fun <reified T> tryCastClazz(clazz: Any?, block: T.() -> Unit) {
    if (clazz is T) block(clazz)
}
