#import <Cordova/CDV.h>
#import <Foundation/Foundation.h>
#define DEBUG_MODE

#import <ZolozFaceAuthFacade/ZolozFaceAuthFacade.h>

#import "FaceVerify.h"


static NSString * const CPCameraErrorPermissionDeniedFirstTime = @"PERMISSION_DENIED_FIRST_TIME";
static NSString * const CPCameraErrorPermissionDeniedNeedSettings = @"PERMISSION_DENIED_NEED_SETTINGS";
static NSString * const CPCameraErrorPermissionRestricted = @"PERMISSION_RESTRICTED";

@implementation FaceVerify

- (void)pluginInitialize {
    [super pluginInitialize];
    // dispatch_once 保证 block 内的代码在整个应用生命周期中只执行一次
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        NSLog(@"[AliyunFace] Initializing Zoloz SDK...");
        
        // 1. 初始化 SDK
        [ZolozFaceAuthFacade init]; 
        
        NSLog(@"[AliyunFace] Zoloz SDK Initialized successfully.");
    });
}


#pragma mark - Cordova Plugin Methods

/**
 * Action: getMetaInfo
 * 获取设备环境信息
 */
- (void)getMetaInfo:(CDVInvokedUrlCommand *)command {
    self.currentCommand = command;
    
    // 获取 MetaInfo
    NSDictionary *dicMetaInfo = [ZolozFaceAuthFacade getMetaInfo];
    NSString *metaInfo = nil;

    if (dicMetaInfo) {
        NSError *error = nil;
        // 1. 将 Dictionary 转换为 NSData (JSON 格式)
        NSData *jsonData = [NSJSONSerialization dataWithJSONObject:dicMetaInfo 
                                                       options:0 
                                                         error:&error];
    
        if (error) {
            NSLog(@"JSON 序列化失败: %@", error.localizedDescription);
        } else {
            // 2. 将 NSData 转换为 NSString
            metaInfo = [[NSString alloc] initWithData:jsonData 
                                           encoding:NSUTF8StringEncoding];
        }
    }
    
    if (metaInfo && metaInfo.length > 0) {
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                  messageAsDictionary:@{
                                                      @"status": @"success",
                                                      @"metaInfo": metaInfo
                                                  }];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    } else {
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                  messageAsString:@"Failed to get MetaInfo"];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }
    
    self.currentCommand = nil;
}

/**
 * Action: startVerify
 * 启动人脸验证界面
 */
- (void)startVerify:(CDVInvokedUrlCommand *)command {
    self.currentCommand = command;
    
    NSString *certifyId = [command.arguments objectAtIndex:0];
    
    if (!certifyId || certifyId.length == 0) {
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                  messageAsString:@"CertifyId is empty"];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        self.currentCommand = nil;
        return;
    }
    
    NSLog(@"certifyId ------>> %@",certifyId);
    NSMutableDictionary  *extParams = [NSMutableDictionary new];
    [extParams setValue:self forKey:@"currentCtr"];
    [ZolozFaceAuthFacade verifyWith:certifyId extParams:extParams onCompletion:^(ZIMResponse *response) {
            dispatch_async(dispatch_get_main_queue(), ^{
            // 防御性检查：防止命令已被释放或重复回调
            if (!self.currentCommand) {
                return;
            }
               // 6. 根据返回码判断结果
            if (response.code == 1000) {
                // --- 验证成功 ---
                NSDictionary *resultData = @{
                    @"status": @"success",
                    @"message": @"Verification successful",
                    @"rawResponse": response.retMessageSub ?: @""
                };
                [self sendSuccessResult:resultData];
                
            } else if (response.code == 1003) {
                // --- 用户主动取消 ---
                [self sendErrorResult:@{
                    @"status": @"cancel",
                    @"code": @(response.code),
                    @"message": @"User cancelled verification"
                }];
                
            } else {
                // --- 验证失败 (网络错误、人脸不匹配、超时等) ---
                NSString *errorMsg = [self getErrorMessageByCode:response.code];
                
                // 拼接更详细的错误信息
                NSString *detailMsg = [NSString stringWithFormat:@"%@ (Sub: %@)", 
                                       errorMsg, 
                                       response.retMessageSub ?: @"Unknown"];
                
                [self sendErrorResult:@{
                    @"status": @"error",
                    @"code": @(response.code),
                    @"subCode": response.retCodeSub ?: @"",
                    @"message": detailMsg
                }];
            }
            
            // 7. 清空当前命令，防止内存泄漏
            self.currentCommand = nil;
            
        //认证结果描述信息
        NSLog(@"主码：%d,子码：%@,子码描述信息：%@",(int)response.code,response.retCodeSub,response.retMessageSub);
        });
    }];
}

- (void) hasCameraPermission:(CDVInvokedUrlCommand*)command {
    AVAuthorizationStatus status = [AVCaptureDevice authorizationStatusForMediaType:AVMediaTypeVideo];
    BOOL hasPermission = (status == AVAuthorizationStatusAuthorized);
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:hasPermission];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) requestCameraPermission:(CDVInvokedUrlCommand*)command {
    AVAuthorizationStatus status = [AVCaptureDevice authorizationStatusForMediaType:AVMediaTypeVideo];

    if (status == AVAuthorizationStatusAuthorized) {
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }

    if (status == AVAuthorizationStatusDenied) {
        NSDictionary *payload = @{
        @"code": CPCameraErrorPermissionDeniedNeedSettings,
        @"message": @"Camera permission denied. Please open Settings > Camera to enable access."
        };
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:payload];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }

    if (status == AVAuthorizationStatusRestricted) {
        NSDictionary *payload = @{
        @"code": CPCameraErrorPermissionRestricted,
        @"message": @"Camera permission restricted. Please check iOS restrictions in Settings > Screen Time > Content & Privacy Restrictions."
        };
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:payload];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }

    [AVCaptureDevice requestAccessForMediaType:AVMediaTypeVideo completionHandler:^(BOOL granted) {
        dispatch_async(dispatch_get_main_queue(), ^{
        if (granted) {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        } else {
            NSDictionary *payload = @{
            @"code": CPCameraErrorPermissionDeniedFirstTime,
            @"message": @"Camera permission denied. Please enable camera access in app settings."
            };
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:payload];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }
        });
    }];
}

- (void) openAppSettings:(CDVInvokedUrlCommand*)command {
    NSURL *settingsURL = [NSURL URLWithString:UIApplicationOpenSettingsURLString];
    if (settingsURL == nil) {
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Unable to open app settings"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }

    UIApplication *application = [UIApplication sharedApplication];
    if (![application canOpenURL:settingsURL]) {
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Unable to open app settings"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }

    if (@available(iOS 10.0, *)) {
        [application openURL:settingsURL options:@{} completionHandler:^(BOOL success) {
        CDVPluginResult *pluginResult = success
            ? [CDVPluginResult resultWithStatus:CDVCommandStatus_OK]
            : [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Unable to open app settings"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }];
    } else {
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wdeprecated-declarations"
        BOOL success = [application openURL:settingsURL];
#pragma clang diagnostic pop
        CDVPluginResult *pluginResult = success
        ? [CDVPluginResult resultWithStatus:CDVCommandStatus_OK]
        : [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Unable to open app settings"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}

#pragma mark - Helper Methods (辅助方法)

- (void)sendSuccessResult:(NSDictionary *)data {
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                              messageAsDictionary:data];
    [self.commandDelegate sendPluginResult:result callbackId:self.currentCommand.callbackId];
}

- (void)sendErrorResult:(id)message {
    CDVPluginResult *result = nil;
    
    if ([message isKindOfClass:[NSDictionary class]]) {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                 messageAsDictionary:message];
    } else {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                 messageAsString:[NSString stringWithFormat:@"%@", message]];
    }
    
    [self.commandDelegate sendPluginResult:result callbackId:self.currentCommand.callbackId];
}

- (NSString *)getErrorMessageByCode:(NSInteger)code {
    switch (code) {
        case 1000: return @"Success";
        case 1001: return @"System Error";
        case 1002: return @"Parameter Error";
        case 1003: return @"User Cancelled";
        case 2001: return @"Camera Permission Denied";
        case 2002: return @"Network Error";
        case 2003: return @"Device Time Incorrect";
        case 2004: return @"Face Detection Failed";
        case 2005: return @"Face Match Failed";
        case 2006: return @"Verification Failed";
        default: return [NSString stringWithFormat:@"Unknown Error (%ld)", (long)code];
    }
}

@end
