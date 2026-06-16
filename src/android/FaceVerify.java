package com.plugin.aliyun.faceverify;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;

import com.alipay.face.api.ZIMFacade;

/**
 * 阿里云扫脸核验 Cordova 插件核心类
 * 功能：设备信息，扫脸认证，Camera权限判断和申请
 * 适配 Cordova 插件规范，无 UI 依赖
 */
public class FaceVerify extends CordovaPlugin {

    private static final String ACTION_HAS_CAMERA_PERMISSION = "hasCameraPermission";
    private static final String ACTION_REQUEST_CAMERA_PERMISSION = "requestCameraPermission";
    private static final String ACTION_OPEN_APP_SETTINGS = "openAppSettings";
    private static final String ACTION_GET_METAINFO = "getMetaInfo";
    private static final String ACTION_STARTVERIFY = "startVerify";

    // 日志标签
    private static final String TAG = "FaceVerify";
    private static volatile boolean isSdkInitialized = false; // volatile 保证可见性
    private static final Object initLock = new Object();
    
    private CallbackContext currentCallbackContext;
    private CallbackContext permissionCallbackContext;

    
    private static final String ERROR_PERMISSION_DENIED_FIRST_TIME = "PERMISSION_DENIED_FIRST_TIME";
    private static final String ERROR_PERMISSION_DENIED_NEED_SETTINGS = "PERMISSION_DENIED_NEED_SETTINGS";
    private static final int REQUEST_CODE_VERIFY = 1001;
    private static final int PERMISSION_REQ_CODE = 0;

    /**
     * 线程安全的单例初始化
     */
    private void initSdk() {
        if (isSdkInitialized) return;

        synchronized (initLock) {
            if (isSdkInitialized) return; // 二次检查

            try {
                // 必须使用 ApplicationContext，防止 Activity 销毁导致 Context 泄漏
                ZIMFacade.install(cordova.getActivity().getApplicationContext());
                isSdkInitialized = true;
                Log.d(TAG, "Aliyun SDK Initialized successfully.");
            } catch (Throwable t) {
                Log.e(TAG, "Failed to initialize Aliyun SDK", t);
            }
        }
    }

    /**
     * Cordova 插件核心入口：处理 JS 调用的方法
     */
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.currentCallbackContext = callbackContext;
        initSdk();

        if (ACTION_HAS_CAMERA_PERMISSION.equals(action)) {
            return hasCameraPermission(callbackContext);
        } else if (ACTION_REQUEST_CAMERA_PERMISSION.equals(action)) {
            return requestCameraPermissions(callbackContext);
        } else if (ACTION_OPEN_APP_SETTINGS.equals(action)) {
            return openAppSettings(callbackContext);
        } else if (ACTION_GET_METAINFO.equals(action)) {
            this.getMetaInfo(callbackContext);
            return true;
        } else if (ACTION_STARTVERIFY.equals(action)) {
            String certifyId = args.getString(0);
            this.startVerify(certifyId, callbackContext);
            return true;
        }
        return false;
    }


    private void getMetaInfo(CallbackContext callbackContext) {
        cordova.getThreadPool().execute(() -> {
            try {
                // 调用阿里云SDK获取MetaInfo
                String metaInfo = ZIMFacade.getMetaInfos(cordova.getActivity().getApplicationContext());
                if (metaInfo != null && !metaInfo.isEmpty()) {
                    JSONObject result = new JSONObject();
                    result.put("status", "success");
                    result.put("metaInfo", metaInfo);
                    callbackContext.success(result);
                } else {
                    callbackContext.error("Failed to get MetaInfo");
                } 
                    
            } catch (Exception e) {
                Log.e(TAG, "Get MetaInfo Error", e);
                callbackContext.error("Failed to get MetaInfo: " + e.getMessage());
            }
        });
    }

    private void startVerify(String certifyId, CallbackContext callbackContext) {
        Activity activity = cordova.getActivity();
        
        // 构建Intent启动阿里云认证Activity
         Intent intent = new Intent(activity, FaceVerifyActivity.class);
         intent.putExtra("certifyId", certifyId);
        
        // 由于Cordova插件中启动Activity需要特殊处理以保留WebView状态
        this.cordova.startActivityForResult(this, intent, REQUEST_CODE_VERIFY);
    }

    private boolean hasCameraPermission(CallbackContext callbackContext) {
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, cordova.hasPermission(Manifest.permission.CAMERA)));
        return true;
    }

    private boolean requestCameraPermissions(CallbackContext callbackContext) {
        if (cordova.hasPermission(Manifest.permission.CAMERA)) {
            callbackContext.success();
            return true;
        }

        if (permissionCallbackContext != null) {
            callbackContext.error("Camera permission request already in progress");
            return true;
        }
        permissionCallbackContext = callbackContext;
        cordova.requestPermissions(this, PERMISSION_REQ_CODE, new String[]{Manifest.permission.CAMERA});
        return true;
    }

    private boolean openAppSettings(CallbackContext callbackContext) {
        Activity activity = cordova.getActivity();
        if (activity == null) {
            callbackContext.error("Unable to open app settings");
            return true;
        }

        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
            intent.setData(uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
            callbackContext.success();
        } catch (Exception e) {
            Log.e(TAG, "openAppSettings failed", e);
            callbackContext.error("Unable to open app settings");
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_VERIFY) {
            if (currentCallbackContext != null) {
                // 解析阿里云SDK返回的结果
                boolean is_success = data.getBooleanExtra("is_success", false);
                int code = data.getIntExtra("code",-1);
                String message = data.getStringExtra("message");

                JSONObject result = new JSONObject();
                try {
                    result.put("is_success", is_success);
                    result.put("code", code);
                    result.put("message", message);
                    if (resultCode == Activity.RESULT_OK) {
                        currentCallbackContext.success(result);
                    }else{
                        currentCallbackContext.error(result);
                    }
                } catch (JSONException e) {
                    currentCallbackContext.error("Result parse error");
                }
                currentCallbackContext = null;
            }
        }
    }

     @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        boolean hasGrantResult = grantResults != null && grantResults.length > 0;
        if (!hasGrantResult) {
            if (requestCode == PERMISSION_REQ_CODE) {
                if (permissionCallbackContext != null) {
                    sendPermissionError(permissionCallbackContext,
                    ERROR_PERMISSION_DENIED_FIRST_TIME,
                    "Camera permission request dismissed.");
                    permissionCallbackContext = null;
                }
            }
            return;
        }

        for(int r:grantResults){
            if(r == PackageManager.PERMISSION_DENIED){
                if (requestCode == PERMISSION_REQ_CODE) {
                    if (permissionCallbackContext != null) {
                        String code = shouldPromptToOpenSettings(permissions)
                            ? ERROR_PERMISSION_DENIED_NEED_SETTINGS
                            : ERROR_PERMISSION_DENIED_FIRST_TIME;
                        String message = ERROR_PERMISSION_DENIED_NEED_SETTINGS.equals(code)
                            ? "Camera permission denied. Please open app settings and enable the required permissions."
                            : "Camera permission denied.";
                        sendPermissionError(permissionCallbackContext,
                            code,
                            message);
                        permissionCallbackContext = null;
                    }
                    return;
                }
            }
        }
        if (requestCode == PERMISSION_REQ_CODE) {
            if (permissionCallbackContext != null) {
                permissionCallbackContext.success();
                permissionCallbackContext = null;
            }
        }
    }

    private boolean shouldPromptToOpenSettings(String[] requestedPermissions) {
        Activity activity = cordova.getActivity();
        if (activity == null || requestedPermissions == null || requestedPermissions.length == 0) {
            return false;
        }

        for (String permission : requestedPermissions) {
            if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED
                && !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return true;
            }
        }

        return false;
    }
    
    private void sendPermissionError(CallbackContext callbackContext, String code, String message) {
        JSONObject payload = new JSONObject();
        try {
            payload.put("code", code);
            payload.put("message", message);
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, payload));
        } catch (JSONException e) {
            callbackContext.error(code);
        }
    }
    

}
