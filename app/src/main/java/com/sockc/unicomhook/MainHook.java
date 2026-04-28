package com.sockc.unicomhook;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class MainHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.sinovatech.unicom.ui")) {
            return;
        }

        XposedBridge.log("UnicomHook: 成功注入联通 APP");

        try {
            XposedHelpers.findAndHookMethod(
                "okhttp3.ResponseBody", 
                lpparam.classLoader, 
                "string", 
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        String result = (String) param.getResult();
                        if (result != null && result.contains("respCode")) {
                            String modified = result.replaceAll("\"respCode\"\\s*:\\s*\"[^\"]*\"", "\"respCode\":\"0000\"");
                            param.setResult(modified);
                            XposedBridge.log("UnicomHook: OkHttp 响应修改为 0000");
                        }
                    }
                }
            );
        } catch (Throwable t) {
            XposedBridge.log("UnicomHook: OkHttp 拦截失败");
        }

        try {
            XposedHelpers.findAndHookConstructor(
                "org.json.JSONObject", 
                lpparam.classLoader, 
                String.class, 
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        String jsonString = (String) param.args[0];
                        if (jsonString != null && jsonString.contains("respCode")) {
                            param.args[0] = jsonString.replaceAll("\"respCode\"\\s*:\\s*\"[^\"]*\"", "\"respCode\":\"0000\"");
                            XposedBridge.log("UnicomHook: JSONObject 解析修改为 0000");
                        }
                    }
                }
            );
        } catch (Throwable t) {
        }
    }
}
