package com.andrey.beautyplanner

object ProfileAvatarUrlProcessor {
    var processImpl: ((url: String, onResult: (String?) -> Unit) -> Unit)? = null

    fun processAvatar(
        url: String,
        onResult: (String?) -> Unit
    ) {
        processImpl?.invoke(url, onResult) ?: onResult(null)
    }
}
