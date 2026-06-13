//
//  DTFRpcException.h
//  APMobileRPC
//
//  Created by richard on 11/02/2018.
//  Copyright © 2018 DTF. All rights reserved.
//

#import "DTFRpcErrorCode.h"

/** The name of the RPC exception. */
extern NSString * const kDTFRpcException;

/**
 * NSException is used to implement exception handling and contains information about an RPC exception.
 */
@interface DTFRpcException : NSException

@property(nonatomic, assign) DTFRpcErrorCode code;

@property(nonatomic, assign) NSInteger originErrorCode;

+ (void)raise:(DTFRpcErrorCode)code message:(NSString *)message;

+ (void)raise:(DTFRpcErrorCode)code message:(NSString *)message userInfo:(NSDictionary*)userInfo;

+ (void)raise:(DTFRpcErrorCode)code originErrorCode:(NSInteger)originErrorCode message:(NSString *)message userInfo:(NSDictionary*)userInfo ;

@end
