#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface GoogleAuthBridge : NSObject

+ (void)signInWithGoogleWithCompletion:(void (^)(NSDictionary * _Nullable result, NSString * _Nullable error))completion;
+ (void)signInAnonymouslyWithCompletion:(void (^)(NSDictionary * _Nullable result, NSString * _Nullable error))completion;
+ (void)signInWithEmail:(NSString *)email
        password:(NSString *)password
        completion:(void (^)(NSDictionary * _Nullable result, NSString * _Nullable error))completion;
+ (void)registerWithEmail:(NSString *)email
        password:(NSString *)password
        completion:(void (^)(NSDictionary * _Nullable result, NSString * _Nullable error))completion;
+ (void)sendPasswordReset:(NSString *)email
        completion:(void (^)(NSString * _Nullable error))completion;
+ (NSDictionary * _Nullable)currentUser;
+ (NSString * _Nullable)signOutUser;
+ (void)callBackend:(NSString *)name
        payload:(NSDictionary *)payload
        completion:(void (^)(NSDictionary * _Nullable result, NSString * _Nullable error))completion;

@end

        NS_ASSUME_NONNULL_END