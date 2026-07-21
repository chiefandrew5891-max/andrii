package com.andrey.beautyplanner

object ProfileImagePicker {
    var pickImageImpl: (((String?) -> Unit) -> Unit)? = null

    fun pickImage(onImagePicked: (String?) -> Unit) {
        pickImageImpl?.invoke(onImagePicked) ?: onImagePicked(null)
    }
}