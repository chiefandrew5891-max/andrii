import Foundation
import CryptoKit

@objc class BackupCryptoBridge: NSObject {

    @objc static func encrypt(
        plaintext: String,
        password: String,
        saltBase64: String,
        iterations: Int32,
        error: NSErrorPointer
    ) -> NSDictionary? {
        do {
            guard let plaintextData = plaintext.data(using: .utf8) else {
                throw NSError(domain: "BackupCryptoBridge", code: 1001, userInfo: [
                    NSLocalizedDescriptionKey: "Plaintext encoding failed."
                ])
            }

            guard let saltData = Data(base64Encoded: saltBase64) else {
                throw NSError(domain: "BackupCryptoBridge", code: 1002, userInfo: [
                    NSLocalizedDescriptionKey: "Invalid salt."
                ])
            }

            let keyData = try deriveKeyPBKDF2(
                password: password,
                salt: saltData,
                iterations: Int(iterations),
                keyByteCount: 32
            )

            let key = SymmetricKey(data: keyData)
            let nonce = AES.GCM.Nonce()

            let sealed = try AES.GCM.seal(plaintextData, using: key, nonce: nonce)

            let ivData = Data(nonce)
            let ciphertextData = sealed.ciphertext + sealed.tag

            return [
                "ivBase64": ivData.base64EncodedString(),
                "ciphertextBase64": ciphertextData.base64EncodedString()
            ]
        } catch let e as NSError {
            error?.pointee = e
            return nil
        } catch {
            let nsError = NSError(
                domain: "BackupCryptoBridge",
                code: 1099,
                userInfo: [NSLocalizedDescriptionKey: error.localizedDescription]
            )
            error?.pointee = nsError
            return nil
        }
    }

    @objc static func decrypt(
        ciphertextBase64: String,
        password: String,
        saltBase64: String,
        ivBase64: String,
        iterations: Int32,
        error: NSErrorPointer
    ) -> NSString? {
        do {
            guard let saltData = Data(base64Encoded: saltBase64) else {
                throw NSError(domain: "BackupCryptoBridge", code: 2001, userInfo: [
                    NSLocalizedDescriptionKey: "Invalid salt."
                ])
            }

            guard let ivData = Data(base64Encoded: ivBase64) else {
                throw NSError(domain: "BackupCryptoBridge", code: 2002, userInfo: [
                    NSLocalizedDescriptionKey: "Invalid IV."
                ])
            }

            guard let combinedCipherData = Data(base64Encoded: ciphertextBase64) else {
                throw NSError(domain: "BackupCryptoBridge", code: 2003, userInfo: [
                    NSLocalizedDescriptionKey: "Invalid ciphertext."
                ])
            }

            guard combinedCipherData.count >= 16 else {
                throw NSError(domain: "BackupCryptoBridge", code: 2004, userInfo: [
                    NSLocalizedDescriptionKey: "Ciphertext too short."
                ])
            }

            let keyData = try deriveKeyPBKDF2(
                password: password,
                salt: saltData,
                iterations: Int(iterations),
                keyByteCount: 32
            )

            let key = SymmetricKey(data: keyData)

            let cipherCount = combinedCipherData.count - 16
            let ciphertext = combinedCipherData.prefix(cipherCount)
            let tag = combinedCipherData.suffix(16)

            let nonce = try AES.GCM.Nonce(data: ivData)
            let sealed = try AES.GCM.SealedBox(
                nonce: nonce,
                ciphertext: ciphertext,
                tag: tag
            )

            let opened = try AES.GCM.open(sealed, using: key)

            guard let plaintext = String(data: opened, encoding: .utf8) else {
                throw NSError(domain: "BackupCryptoBridge", code: 2005, userInfo: [
                    NSLocalizedDescriptionKey: "Plaintext decoding failed."
                ])
            }

            return plaintext as NSString
        } catch let e as NSError {
            error?.pointee = e
            return nil
        } catch {
            let nsError = NSError(
                domain: "BackupCryptoBridge",
                code: 2099,
                userInfo: [NSLocalizedDescriptionKey: error.localizedDescription]
            )
            error?.pointee = nsError
            return nil
        }
    }

    private static func deriveKeyPBKDF2(
        password: String,
        salt: Data,
        iterations: Int,
        keyByteCount: Int
    ) throws -> Data {
        var derived = Data(count: keyByteCount)

        let result = derived.withUnsafeMutableBytes { derivedBytes in
            salt.withUnsafeBytes { saltBytes in
                CCKeyDerivationPBKDF(
                    CCPBKDFAlgorithm(kCCPBKDF2),
                    password,
                    password.lengthOfBytes(using: .utf8),
                    saltBytes.bindMemory(to: UInt8.self).baseAddress,
                    salt.count,
                    CCPseudoRandomAlgorithm(kCCPRFHmacAlgSHA256),
                    UInt32(iterations),
                    derivedBytes.bindMemory(to: UInt8.self).baseAddress,
                    keyByteCount
                )
            }
        }

        guard result == kCCSuccess else {
            throw NSError(domain: "BackupCryptoBridge", code: 3001, userInfo: [
                NSLocalizedDescriptionKey: "PBKDF2 derivation failed."
            ])
        }

        return derived
    }
}