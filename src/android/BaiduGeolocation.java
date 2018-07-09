package com.easytraval.geolocation;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PermissionHelper;
import com.baidu.location.BDLocation;
import com.baidu.location.BDAbstractLocationListener;

import com.easytraval.geolocation.w3.PositionOptions;
import com.easytraval.geolocation.service.LocationService;

import android.Manifest;
import android.util.Log;
import android.content.pm.PackageManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class BaiduGeolocation extends CordovaPlugin {

    private static final String TAG = "GeolocationPlugin";

    private static final int GET_CURRENT_POSITION = 0;
    private static final int WATCH_POSITION = 1;
    private static final int CLEAR_WATCH = 2;

    private LocationService locationService = null;
    private String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};

    private JSONArray requestArgs;
    private CallbackContext context;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Log.i(TAG, "插件调用");
        JSONObject options = new JSONObject();
        if (locationService == null)
            locationService = new LocationService(cordova.getActivity());

        requestArgs = args;
        context = callbackContext;

        if (action.equals("getCurrentPosition")) {
            getPermission(GET_CURRENT_POSITION);
            try {
                options = args.getJSONObject(0);
            } catch (JSONException e) {
                Log.v(TAG, "options 未传入");
            }
            return getCurrentPosition(options, callbackContext);

        } else if (action.equals("watchPosition")) {
            getPermission(WATCH_POSITION);
            try {
                options = args.getJSONObject(0);
            } catch (JSONException e) {
                Log.v(TAG, "options 未传入");
            }
            int watchId = args.getInt(1);
            return watchPosition(options, callbackContext);

        } else if (action.equals("clearWatch")) {
            getPermission(CLEAR_WATCH);
            return clearWatch(callbackContext);
        }

        return false;
    }


    private boolean clearWatch(CallbackContext callback) {
        Log.i(TAG, "停止监听");

        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                locationService.clearWatch();
                if (callback != null)
                    callback.success();
            }});

        return true;
    }

    private boolean watchPosition(JSONObject options, final CallbackContext callback) {
        Log.i(TAG, "监听位置变化");

        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                locationService.watchPosition(options, new BDAbstractLocationListener() {
                    @Override
                    public void onReceiveLocation(BDLocation location) {
                        JSONArray message = new MessageBuilder(location).build();
                        PluginResult result = new PluginResult(PluginResult.Status.OK, message);
                        result.setKeepCallback(true);
                        if (callback != null)
                            callback.sendPluginResult(result);
                    }
                });
            }});

        return true;
    }

    private boolean getCurrentPosition(JSONObject options, final CallbackContext callback) {
        Log.i(TAG, "请求当前地理位置");

        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                locationService.getCurrentPosition(options, new BDAbstractLocationListener() {
                    @Override
                    public void onReceiveLocation(BDLocation location) {
                        JSONArray message = new MessageBuilder(location).build();
                        if (callback != null)
                            callback.success(message);
                    }
                });
            }});
        return true;
    }

    /**
     * 获取对应权限
     * int requestCode Action代码
     */
    public void getPermission(int requestCode) {
        if (!hasPermisssion()) {
            PermissionHelper.requestPermissions(this, requestCode, permissions);
        }
    }

    /**
     * 权限请求结果处理函数
     * int requestCode Action代码
     * String[] permissions 权限集合
     * int[] grantResults 授权结果集合
     */
    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException
    {
        PluginResult result;
        //This is important if we're using Cordova without using Cordova, but we have the geolocation plugin installed
        if (context != null) {
            for (int r : grantResults) {
                if (r == PackageManager.PERMISSION_DENIED) {
                    Log.d(TAG, "Permission Denied!");
                    result = new PluginResult(PluginResult.Status.ILLEGAL_ACCESS_EXCEPTION);
                    context.sendPluginResult(result);
                    return;
                }
            }
            if (requestCode == GET_CURRENT_POSITION) {
                getCurrentPosition(this.requestArgs.getJSONObject(0), this.context);
            } else if (requestCode == WATCH_POSITION) {
                watchPosition(this.requestArgs.getJSONObject(0), this.context);
            } else if (requestCode == CLEAR_WATCH) {
                clearWatch(this.context);
            }
        }
    }

    /**
     * 判断是否有对应权限
     */
    public boolean hasPermisssion() {
        for (String p : permissions) {
            if (!PermissionHelper.hasPermission(this, p)) {
                return false;
            }
        }
        return true;
    }

   /*
    * We override this so that we can access the permissions variable, which no longer exists in
    * the parent class, since we can't initialize it reliably in the constructor!
    */

    public void requestPermissions(int requestCode) {
        PermissionHelper.requestPermissions(this, requestCode, permissions);
    }
}
