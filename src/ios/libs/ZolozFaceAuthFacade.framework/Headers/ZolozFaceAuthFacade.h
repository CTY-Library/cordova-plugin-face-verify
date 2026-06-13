//
//  ZolozFaceAuthFacade.h
//  ZolozFaceAuthFacade
//
//  Created by mengbingchuan on 2022/11/17.
//  Copyright © 2022 DTF. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <DTFUtility/DTFConstant.h>
#import <DTFUtility/ZIMResponse.h>
NS_ASSUME_NONNULL_BEGIN

@interface ZolozFaceAuthFacade : NSObject

+ (void)init;

+ (void)initSDK;

+ (void)initIPv6;

+ (void)preload:(NSDictionary *)param completion:(void (^)(BOOL success))completion;

+ (NSDictionary *)getMetaInfo;

+ (void)verifyWith:(NSString *)zimId
         extParams:(NSDictionary *)params
      onCompletion:(void (^)(ZIMResponse *response))callback;
+ (NSDictionary *)getNFCMetaInfo;

+ (void)nfcVerifyWith:(NSString *)zimId extParams:(NSDictionary *)params onCompletion:(void (^)(ZIMResponse *response))callback;

+ (void)setCustomUI:(nonnull NSString *)configuration type:(nonnull NSString *)type completion:(void(^)(BOOL success, NSError *error))completion;

+ (void)setCustomLanguage:(nonnull NSString *)configuration type:(nonnull NSString *)type completion:(void(^)(BOOL success, NSError *error))completion;

@end

NS_ASSUME_NONNULL_END
