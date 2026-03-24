@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.andrey.beautyplanner

import platform.Foundation.*
import platform.UIKit.*
import platform.UniformTypeIdentifiers.*
import kotlinx.cinterop.*
import platform.darwin.NSObject

actual object BackupFilePicker {

    private fun topViewController(): UIViewController? {
        val window = UIApplication.sharedApplication.windows.firstOrNull() as? UIWindow
        var vc = window?.rootViewController
        while (vc?.presentedViewController != null) vc = vc.presentedViewController
        return vc
    }

    actual fun exportJson(suggestedFileName: String, json: String) {
        val vc = topViewController() ?: return

        val name = suggestedFileName.trim().ifBlank { "beautyplanner-backup" }
        val fileName = if (name.lowercase().endsWith(".json")) name else "$name.json"

        // Исправленный способ работы со строкой для iOS
        val nsString = json as NSString
        val data = nsString.dataUsingEncoding(NSUTF8StringEncoding) ?: return

        val path = NSTemporaryDirectory() + fileName
        val url = NSURL.fileURLWithPath(path)

        data.writeToURL(url, true)

        val picker = UIDocumentPickerViewController(forExportingURLs = listOf(url))
        picker.modalPresentationStyle = UIModalPresentationFullScreen
        vc.presentViewController(picker, animated = true, completion = null)
    }

    actual fun importJson(onPicked: (String) -> Unit, onError: (String) -> Unit) {
        val vc = topViewController() ?: return

        val delegate = DocumentPickerDelegate(onPicked, onError)
        val picker = UIDocumentPickerViewController(
            forOpeningContentTypes = listOf(UTTypeJSON),
            asCopy = true
        )
        picker.delegate = delegate
        picker.modalPresentationStyle = UIModalPresentationFullScreen

        // Удерживаем делегат в памяти
        DocumentPickerDelegateHolder.current = delegate
        vc.presentViewController(picker, animated = true, completion = null)
    }
}

// ВАЖНО: Добавляем интерфейс протокола UIDocumentPickerDelegateProtocol
private class DocumentPickerDelegate(
    val onPicked: (String) -> Unit,
    val onError: (String) -> Unit
) : NSObject(), UIDocumentPickerDelegateProtocol {

    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL ?: return

        val content = NSString.stringWithContentsOfURL(url, NSUTF8StringEncoding, null)
        if (content != null) {
            onPicked(content.toString())
        } else {
            onError("Failed to read file")
        }
        DocumentPickerDelegateHolder.current = null
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        DocumentPickerDelegateHolder.current = null
    }
}

private object DocumentPickerDelegateHolder {
    var current: Any? = null
}