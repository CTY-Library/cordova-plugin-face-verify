var exec = require('cordova/exec');

var FaceVerify = {
    /**
     * 获取设备环境信息 (MetaInfo)
     * 此数据需发送给后端，用于调用 InitFaceVerify 接口
     * @param {Function} successCallback 
     * @param {Function} errorCallback 
     */
    getMetaInfo: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, "FaceVerify", "getMetaInfo", []);
    },

    /**
     * 发起人脸核验
     * @param {Object} options - 配置项
     * @param {String} options.certifyId - 从后端获取的唯一认证ID
     * @param {Function} successCallback - 认证成功回调，返回结果对象
     * @param {Function} errorCallback - 认证失败或取消回调
     */
    startVerify: function(options, successCallback, errorCallback) {
        if (!options || !options.certifyId) {
            errorCallback("CertifyId is required");
            return;
        }
        exec(successCallback, errorCallback, "FaceVerify", "startVerify", [options.certifyId]);
    },

    hasCameraPermission: function(success, error) {
        exec(success, error, 'FaceVerify', 'hasCameraPermission', []);
    },
    requestCameraPermission: function(success, error) {
        exec(success, error, 'FaceVerify', 'requestCameraPermission', []);
    },
    openAppSettings: function(success, error) {
        exec(success, error, 'FaceVerify', 'openAppSettings', []);
    } 
};

module.exports = FaceVerify;