//
//  AFEWebGuideView.h
//  BioAuthEngine
//
//  Created by yukun.tyk on 3/21/16.
//  Copyright © 2016 DTF. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <BioAuthEngine/AFEStatusBar.h>
#import <WebKit/WebKit.h>
#import <DTFUtility/APBToygerRemoteConfig.h>
#import "DTFUICustom.h"

typedef NS_ENUM(NSUInteger, AFEWebGuideCancelType) {
    AFEWebGuideCancelTypeActiveWithdrawal = 0,//主动退出
    AFEWebGuideCancelTypeNotCertifiedYet,//暂不认证
};

@protocol AFEWebGuideViewDelegate <NSObject>

- (void)onButtonBegin:(BOOL)suitableType query:(NSDictionary *)dict;
- (void)onButtonCancel:(AFEWebGuideCancelType)cancelType;//退出

@optional
- (void)onLoadFinished:(BOOL)success;
- (void)onH5Logger:(NSString *)h5Logger;
- (void)onButtonAgreement;

@end

@interface AFEWebTopBar: UIView

@end

@interface AFEWebGuideView : UIView <IStatusBarDelegate,WKScriptMessageHandler, WKUIDelegate, WKNavigationDelegate>

@property(strong,nonatomic) WKWebView *wkwebView;

@property(nonatomic, assign)BOOL loaded;
@property(nonatomic, strong)APBToygerAlertConfig *toygerAlertConfig;
@property(nonatomic, weak)UIViewController *currentViewController;
@property(nonatomic, strong)DTFUICustom *customProtocol;
@property(nonatomic, strong)NSDictionary *alertParam;

- (void)setWebGuideViewDelegate:(id<AFEWebGuideViewDelegate>)webGuideViewDeleage;
- (void)setURL:(NSURL *)url;
//清除网页的localstorage,localStorage存储了自定义UI信息
- (void)clearUICustomSettingWebLocalStorage;
@end
