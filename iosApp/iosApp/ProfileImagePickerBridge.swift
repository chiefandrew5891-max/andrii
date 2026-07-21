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

                    let cropped = Self.cropCenterSquare(image)
                    let resized = Self.resizeImage(cropped, targetSize: CGSize(width: 512, height: 512))

                    guard let data = resized.jpegData(compressionQuality: 0.82) else {
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

    private static func cropCenterSquare(_ image: UIImage) -> UIImage {
        guard let cgImage = image.cgImage else { return image }

        let width = CGFloat(cgImage.width)
        let height = CGFloat(cgImage.height)
        let side = min(width, height)
        let x = (width - side) / 2.0
        let y = (height - side) / 2.0

        guard let croppedCg = cgImage.cropping(to: CGRect(x: x, y: y, width: side, height: side)) else {
            return image
        }

        return UIImage(cgImage: croppedCg, scale: image.scale, orientation: image.imageOrientation)
    }

    private static func resizeImage(_ image: UIImage, targetSize: CGSize) -> UIImage {
        let renderer = UIGraphicsImageRenderer(size: targetSize)
        return renderer.image { _ in
            image.draw(in: CGRect(origin: .zero, size: targetSize))
        }
    }

    private static func cleanup() {
        completion = nil
        delegateHolder = nil
    }
}