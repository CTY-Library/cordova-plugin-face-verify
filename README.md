AI语音转写和语音合成插件,基于阿里云的SDK (V2.7.1-039-20251125)

## 功能特性


## 参考链接
https://help.aliyun.com/zh/viapi/developer-reference/the-android-client-access-tutorial?spm=a2c4g.11186623.help-menu-142958.d_4_3_2_7_1_1_4.21e47777FY6OGi&scm=20140722.H_201380._.OR_help-T_cn~zh-V_1

## 安装命令

### 完整功能安装（包含语音转写和语音合成）
```
ionic cordova plugin add https://github.com/CTY-Library/cordova-plugin-face-verify --variable ACCESSKEYID=xxxxxx --variable ACCESSKEYSECRET=66778dfggh --save
```

### 本地安装
```
ionic cordova plugin add F:\backend\cordova_plugin\cordova-plugin-face-verify --variable ACCESSKEYID=1ww23 --variable ACCESSKEYSECRET=66778dfggh --save
```

## Android平台配置
```permission
//如果需要适配折叠屏手机，需要在App的AndroidManifest.xml中注册以下权限，以便SDK监听折叠屏设备的折叠/展开状态改变
 <uses-permission android:name="android.permission.CHANGE_CONFIGURATION"/>
```
```gradle
// 新增：解决META-INF文件重复冲突
packagingOptions {
    // 处理netty版本文件冲突：只保留第一个找到的文件
    pickFirst 'META-INF/io.netty.versions.properties'
    // 忽略重复的 INDEX.LIST 文件
    exclude 'META-INF/INDEX.LIST'
    // 可选：同时忽略其他常见的重复META文件（避免后续报错）
    exclude 'META-INF/DEPENDENCIES'
    exclude 'META-INF/LICENSE'
    exclude 'META-INF/LICENSE.txt'
    exclude 'META-INF/NOTICE'
    exclude 'META-INF/NOTICE.txt'
    exclude 'META-INF/AL2.0'
    exclude 'META-INF/LGPL2.1'
    exclude 'META-INF/LICENSE.md'
    exclude 'META-INF/NOTICE.md'
}

dependencies {
    // 其他已有的依赖（implementation、api等）
    
    implementation 'com.alibaba:fastjson:1.2.83_noneautotype'
    
    implementation(name: 'baseverify-2.3.32.1.250219103020', ext: 'aar')
    implementation(name: 'facade-2.3.32.1.250219103020', ext: 'aar')
    implementation(name: 'face-2.3.32.1.250219103020', ext: 'aar')
    implementation(name: 'ocr-2.3.32.1.250219103020', ext: 'aar')
    implementation(name: 'faceaudio-2.3.32.1.250219103020', ext: 'aar')
    implementation(name: 'facelanguage-2.3.32.1.250219103020', ext: 'aar')
    implementation(name: 'facephotinus-2.3.32.1.250219103020', ext: 'aar')
    implementation(name: 'APSecuritySDK-deepSec-7.0.1.20240528.jiagu', ext: 'aar')
}
```

## 使用案例
FaceVerify.hasCameraPermission(function(hasPermission) {
  console.log('camera permission:', hasPermission);
}, function(err) {
  console.error('hasPermission failed:', err);
});

FaceVerify.requestCameraPermission(function() {
  console.log('camera permission granted');
}, function(err) {
  console.error('requestPermission failed:', err.code, err.message);
});

openAppSettings([successCallback, errorCallback])
Open this app's system settings page. Useful after receiving PERMISSION_DENIED_NEED_SETTINGS.

FaceVerify.openAppSettings(function() {
  console.log('opened app settings');
}, function(err) {
  console.error('openAppSettings failed:', err);
});

FaceVerify.getMetaInfo(
    {},
    // 成功回调
    function(result) {
        console.log("成功", result);
    },
    // 失败/取消回调
    function(error) {
        console.error("失败或取消", error);
    }
);


FaceVerify.startVerify(
    {
        certifyId: "e6834222978a3e02e6ba236595b9c65cd"
    },
    // 成功回调
    function(result) {
        console.log("人脸核验成功", result);
        // result 通常是一个对象，例如: { status: "success", message: "..." }
        alert("验证通过");
    },
    // 失败/取消回调
    function(error) {
        console.error("人脸核验失败或取消", error);
        // error 通常是一个字符串或对象，例如: "User Cancelled" 或 { status: "error", message: "..." }
        alert("验证失败: " + (typeof error === 'object' ? error.message : error));
    }
);


## 返回结果说明

- `1000`: 表示刷脸成功，该结果仅供参考，可通过移动端核身认证移动端查询获取最终认证结果
- `1001`: 表示系统错误
- `1003`: 表示验证中断
- `2002`: 表示网络错误
- `2006`: 表示刷脸失败，如需获取更详细的失败原因，可通过移动端核身认证移动端查询获取最终认证结果


## 注意事项

1. **权限管理**: 确保应用已获得相机权限
2. **网络连接**: 确保设备可访问阿里云服务
4. **资源释放**: 使用完毕后及时释放SDK资源



    


```

    

