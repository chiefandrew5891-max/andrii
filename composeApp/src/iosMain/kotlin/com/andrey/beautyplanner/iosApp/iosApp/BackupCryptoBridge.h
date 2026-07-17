#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface BackupCryptoBridge : NSObject

+ (NSDictionary * _Nullable)encryptWithPlaintext:(NSString *)plaintext
        password:(NSString *)password
        saltBase64:(NSString *)saltBase64
        iterations:(int32_t)iterations
        error:(NSError * _Nullable * _Nullable)error;

+ (NSString * _Nullable)decryptWithCiphertextBase64:(NSString *)ciphertextBase64
        password:(NSString *)password
        saltBase64:(NSString *)saltBase64
        ivBase64:(NSString *)ivBase64
        iterations:(int32_t)iterations
        error:(NSError * _Nullable * _Nullable)error;

@end

        NS_ASSUME_NONNULL_END