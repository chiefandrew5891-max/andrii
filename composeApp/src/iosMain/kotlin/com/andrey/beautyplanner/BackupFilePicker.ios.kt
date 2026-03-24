@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.andrey.beautyplanner

import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.NSObject
import platform.UIKit.*
import platform.UniformTypeIdentifiers.UTType
import platform.UniformTypeIdentifiers.UTTypeJSON

actual object BackupFilePicker {

    private fun topViewController(): UIViewController? {
        val window = UIApplication.sharedApplication.windows.firstOrNull()
        var vc = window?.rootViewController
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
            forOpeningContentTypes = listOf(UTTypeJSON),
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

    // Добавляем все REQUIRED методы ObjC NSObject (заглушки, чтобы компилировалось на CI)
    override fun isEqual(`object`: Any?): Boolean = super.equals(`object`)
    override fun hash(): ULong = super.hashCode().toULong()
    override fun superclass(): ObjCClass? = null
    override fun description(): String? = null
    override fun isProxy(): Boolean = false
    override fun class_(): ObjCClass? = null
    override fun isKindOfClass(aClass: ObjCClass?): Boolean = false
    override fun isMemberOfClass(aClass: ObjCClass?): Boolean = false
    override fun conformsToProtocol(aProtocol: Protocol?): Boolean = false
    override fun respondsToSelector(aSelector: CPointer<out CPointed>?): Boolean = false
    override fun performSelector(aSelector: CPointer<out CPointed>?): Any? = null
    override fun performSelector(aSelector: CPointer<out CPointed>?, withObject: Any?): Any? = null
    override fun performSelector(aSelector: CPointer<out CPointed>?, withObject: Any?, _withObject: Any?): Any? = null
}

private object DocumentPickerDelegateHolder {
    var current: Any? = null
}