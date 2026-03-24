@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.andrey.beautyplanner

import platform.Foundation.NSObject
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSString
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIModalPresentationFullScreen
import platform.UIKit.UIViewController
import platform.UniformTypeIdentifiers.UTType
import platform.UniformTypeIdentifiers.UTTypeJSON

actual object BackupFilePicker {

    private fun topViewController(): UIViewController? {
        val windows = UIApplication.sharedApplication.connectedScenes
            .flatMap { scene ->
                val w = scene.valueForKey("windows") as? List<*>
                w?.filterIsInstance<UIWindow>() ?: emptyList()
            }
        val window = windows.firstOrNull { it.isKeyWindow } ?: return null
        var vc = window.rootViewController
        while (vc?.presentedViewController != null) vc = vc.presentedViewController
        return vc
    }

    actual fun exportJson(suggestedFileName: String, json: String) {
        val vc = topViewController() ?: return

        val name = suggestedFileName.trim().ifBlank { "beautyplanner-backup" }
        val fileName = if (name.lowercase().endsWith(".json")) name else "$name.json"

        val data = (json as NSString).dataUsingEncoding(NSUTF8StringEncoding) ?: return
        val path = NSTemporaryDirectory() + fileName
        val url = NSURL.fileURLWithPath(path)

        data.writeToURL(url, atomically = true)

        val picker = UIDocumentPickerViewController(forExportingURLs = listOf(url))
        picker.modalPresentationStyle = UIModalPresentationFullScreen
        vc.presentViewController(picker, animated = true, completion = null)
    }

    actual fun importJson(onPicked: (String) -> Unit, onError: (String) -> Unit) {
        val vc = topViewController()
        if (vc == null) {
            onError(Locales.t("backup_import_error_no_vc"))
            return
        }

        val delegate = DocumentPickerDelegate(onPicked, onError)

        val picker = UIDocumentPickerViewController(
            forOpeningContentTypes = listOfNotNull(UTTypeJSON),
            asCopy = true
        )

        picker.delegate = delegate
        picker.modalPresentationStyle = UIModalPresentationFullScreen

        DocumentPickerDelegateHolder.current = delegate
        vc.presentViewController(picker, animated = true, completion = null)
    }
}

private class DocumentPickerDelegate(
    val onPicked: (String) -> Unit,
    val onError: (String) -> Unit
) : NSObject(), UIDocumentPickerDelegateProtocol {

    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
            ?: run {
                onError(Locales.t("backup_import_error_read"))
                DocumentPickerDelegateHolder.current = null
                return
            }

        val text = runCatching {
            NSString.stringWithContentsOfFile(url.path ?: "", encoding = NSUTF8StringEncoding, error = null) as String?
        }.getOrNull()

        if (text.isNullOrBlank()) onError(Locales.t("backup_import_error_empty"))
        else onPicked(text)

        DocumentPickerDelegateHolder.current = null
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        DocumentPickerDelegateHolder.current = null
    }
}

private object DocumentPickerDelegateHolder {
    var current: Any? = null
}