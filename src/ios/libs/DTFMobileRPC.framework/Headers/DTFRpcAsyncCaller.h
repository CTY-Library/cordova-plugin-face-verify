//
//  DTFRpcAsyncCaller.h
//  APMobileRPC
//
//  Created by richard on 12/02/2018.
//  Copyright © 2018 DTF. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface DTFRpcAsyncCaller : NSObject

+ (DTFRpcAsyncCaller *)callAsyncBlock:(void (^)(void))block completion:(void (^)(void))completion;

- (void)startAsyncBlock:(void (^)(void))block completion:(void (^)(void))completion;
- (void)cancel;

- (BOOL)isFinished;
- (BOOL)isCancelled;

- (NSThread *)asyncThread;

- (void)handleException:(NSException *)exception;

@end
