#import <Foundation/Foundation.h>
#import <Cordova/CDV.h>
#import <AVFoundation/AVFoundation.h>
#define DEBUG_MODE


NS_ASSUME_NONNULL_BEGIN

/**
 * 扫脸错误码枚举
 */
typedef NS_ENUM(NSInteger, FaceVerifyErrorCode) {
    FaceVerifyErrorCodeNone = 1000,                // 无错误
    FaceVerifyErrorCodeNetworkError = 1003,     // 用户退出
    FaceVerifyErrorCodeAuthFailed = 2002,       // 网络错误
    FaceVerifyErrorCodeNoPermission = 2006,     // 刷脸失败
    FaceVerifyErrorCodeEngineError = 2003,      // 设备时间不准确
};


@interface FaceVerify : CDVPlugin

/**
 * 当前正在执行的命令上下文
 * 用于在异步回调中获取 callbackId 并返回结果
 */
@property (nonatomic, strong) CDVInvokedUrlCommand *currentCommand;

#pragma mark - Cordova Actions

/**
 * 获取设备 MetaInfo (设备指纹)
 * @param command Cordova 命令对象
 */
- (void)getMetaInfo:(CDVInvokedUrlCommand *)command;

/**
 * 启动人脸验证界面
 * @param command Cordova 命令对象，arguments 应为 certifyId
 */
- (void)startVerify:(CDVInvokedUrlCommand *)command;

/**
 * 是否有权限
 * @param command Cordova 命令对象
 */
- (void) hasCameraPermission:(CDVInvokedUrlCommand*)command;
/**
 * 申请权限
 * @param command Cordova 命令对象
 */
- (void) requestCameraPermission:(CDVInvokedUrlCommand*)command;
/**
 * 打开app设置页
 * @param command Cordova 命令对象
 */
- (void) openAppSettings:(CDVInvokedUrlCommand*)command;

#pragma mark - Helper Methods

/**
 * 发送成功结果给 JS
 * @param data 包含结果数据的字典
 */
- (void)sendSuccessResult:(NSDictionary *)data;

/**
 * 发送错误结果给 JS
 * @param message 错误信息字符串或包含错误详情的字典
 */
- (void)sendErrorResult:(id)message;

@end

NS_ASSUME_NONNULL_END
