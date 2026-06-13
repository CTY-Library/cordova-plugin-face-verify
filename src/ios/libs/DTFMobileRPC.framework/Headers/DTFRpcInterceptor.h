//
//  DTFRpcInterceptor.h
//  APMobileRPC
//
//  Created by richard on 12/02/2018.
//  Copyright © 2018 DTF. All rights reserved.
//

#import <Foundation/Foundation.h>



@class DTFRpcOperation;

/**
 * 对 RPC 请求的拦截器。所有的 RPC 请求，在发送到服务端之前或从服务端接收到回应之后，
 * 都会经过拦截器，拦截器可以做相应的处理。
 */
@protocol DTFRpcInterceptor <NSObject>

@optional

/**
 * RPC 请求的前置拦截器。在 RPC 请求开始之前，会调用该方法。
 *
 * @param operation 当前的 PRC 请求。
 */
- (DTFRpcOperation *)beforeRpcOperation:(DTFRpcOperation *)operation;

/**
 * RPC 请求后置拦截器。在 RPC 请求结束之后，会调用该方法。
 *
 * @param operation 当前的 RPC 请求。
 */
- (DTFRpcOperation *)afterRpcOperation:(DTFRpcOperation *)operation;

/**
 *  RPC 异常处理器。在RPC处理过程中出现DTRpcException异常
 *
 *  @param exception Rpc异常
 */
- (void)handleException:(NSException *)exception;

@end
