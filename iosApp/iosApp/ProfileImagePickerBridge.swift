import UIKit
import PhotosUI

final class ProfileImagePickerBridge: NSObject, PHPickerViewControllerDelegate {

    private static var completion: ((String?) -> Void)?
    private static var delegateHolder: ProfileImagePickerBridge?

    static func pickImage(completion: @escaping (String?) -> Void) {
        guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let window = windowScene.windows.first(where: { $0.isKeyWindow }),
              let rootViewController = window.rootViewController else {
            completion(nil)
            return
        }

        self.completion = completion

        var config = PHPickerConfiguration()
        config.filter = .images
        config.selectionLimit = 1

        let picker = PHPickerViewController(configuration: config)
        let delegate = ProfileImagePickerBridge()
        picker.delegate = delegate
        delegateHolder = delegate

        rootViewController.present(picker, animated: true)
    }

    func picker(_ picker: PHPickerViewController, didFinishPicking results: [PHPickerResult]) {
        picker.dismiss(animated: true)

        guard let result = results.first else {
            Self.completion?(nil)
            Self.cleanup()
            return
        }

        let provider = result.itemProvider

        if provider.canLoadObject(ofClass: UIImage.self) {
            provider.loadObject(ofClass: UIImage.self) { object, error in
                DispatchQueue.main.async {
                    guard error == nil, let image = object as? UIImage else {
                        Self.completion?(nil)
                        Self.cleanup()
                        return
                    }

                    // UIImage respects EXIF orientation automatically when rendered.
                    // Resize to at most 1024 on the longer side; no center-crop here —
                    // the Compose crop editor handles final positioning and cropping.
                    let resized = Self.resizeImageMax(image, maxDim: 1024)

                    guard let data = resized.jpegData(compressionQuality: 0.85) else {
                        Self.completion?(nil)
                        Self.cleanup()
                        return
                    }

                    let base64 = data.base64EncodedString()
                    Self.completion?(base64)
                    Self.cleanup()
                }
            }
        } else {
            Self.completion?(nil)
            Self.cleanup()
        }
    }

    /// Resize image so that the longer side is at most maxDim points, preserving aspect ratio.
    private static func resizeImageMax(_ image: UIImage, maxDim: CGFloat) -> UIImage {
        let size = image.size
        let longerSide = max(size.width, size.height)
        if longerSide <= maxDim { return image }
        let scale = maxDim / longerSide
        let newSize = CGSize(width: (size.width * scale).rounded(), height: (size.height * scale).rounded())
        let renderer = UIGraphicsImageRenderer(size: newSize)
        return renderer.image { _ in
            image.draw(in: CGRect(origin: .zero, size: newSize))
        }
    }

    private static func cleanup() {
        completion = nil
        delegateHolder = nil
    }
}
