package com.plugin.aliyun.faceverify;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.alipay.face.api.ZIMFacade;
import com.alipay.face.api.ZIMFacadeBuilder;


import java.util.HashMap;

public class FaceVerifyActivity extends Activity {


    private final String TAG = "MultiActionDemo";
    private String certifyId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String certifyId = getIntent().getStringExtra("certifyId");
        
        if (certifyId == null || certifyId.isEmpty()) {
            finishWithResult(false, 2006, "certifyId is null or empty");
            return;
        }
        // 开始验证
        startFaceVerification(certifyId);

    }


    /**
     * 进入认证
     *
     * @param certifyID 认证唯一标识
     */
    private void startFaceVerification(String certifyID) {
        Log.i(TAG, "certifyID=" + certifyID);
        ZIMFacade zimFacade = ZIMFacadeBuilder.create(this);
        HashMap<String, String> extParams = new HashMap<>();
        //如需指定活体检测UI界面方向(横屏+竖屏)，请指定这一项。
        //extParams.put(ZIMFacade.ZIM_EXT_PARAMS_KEY_SCREEN_ORIENTATION, ZIMFacade.ZIM_EXT_PARAMS_VAL_SCREEN_LAND);
        //如需支持活体视频返回，请指定这一项，并在response.videoFilePath中获取视频本地路径。
        extParams.put(ZIMFacade.ZIM_EXT_PARAMS_KEY_USE_VIDEO, ZIMFacade.ZIM_EXT_PARAMS_VAL_USE_VIDEO_TRUE);

        //如需设置OCR的“下一步”按钮颜色（默认可不设置），请设置此项，如红色 #FF0000。
        extParams.put(ZIMFacade.ZIM_EXT_PARAMS_KEY_OCR_BOTTOM_BUTTON_COLOR, "#FF0000");
        //如需自定义活体检测页面的进度条颜色（默认可不设置），请设置此项，如红色 #FF0000。
        extParams.put(ZIMFacade.ZIM_EXT_PARAMS_KEY_FACE_PROGRESS_COLOR, "#FF0000");
        //如需设置自定义语言，请设置此项为以下值：zh-CN：中文简体 zh-HK：中文繁体 zh-TW：中文繁体，en：英文 in：印尼文 ja：日文 ko：韩文
        //设置为其他值，则显示系统当前语言
        extParams.put(zimFacade.ZIM_EXT_PARAMS_KEY_LANGUAGE, "zh-CN");
        //如需打开刷脸页业务提示开关（默认可不设置），请设置此项,需要结合自定义UI的配置文件使用
        extParams.put(ZIMFacade.ZIM_EXT_PARAMS_KEY_NEED_FACE_NOTICE, ZIMFacade.ZIM_EXT_PARAMS_VAL_NEED_FACE_NOTICE_TRUE);
        zimFacade.verify(certifyID, true, extParams, response -> {
            if (1000 == response.code) {
                finishWithResult(true, response.code,response.reason);
                Log.i(TAG, "verify callback 认证通过  videoFilePath=" + response.videoFilePath);
            } else {
                finishWithResult(false, response.code,response.reason);
                Log.i(TAG, "verify callback 认证失败  " + response.code + ":" + response.reason);
            }

            return true;
        });

    }

    

    private void finishWithResult(boolean isSuccess, int code, String message) {
        Intent data = new Intent();
        data.putExtra("is_success", isSuccess);
        data.putExtra("code", code);
        data.putExtra("message", message);

        setResult(isSuccess ? RESULT_OK : RESULT_CANCELED, data);
        finish();
    }

    
}
