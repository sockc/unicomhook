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

        // 拦截 OkHttp 网络请求
        try {
            XposedHelpers.findAndHookMethod(
                "okhttp3.ResponseBody", 
                lpparam.classLoader, 
                "string", 
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        String result = (String) param.getResult();
                        if (result != null) {
                            String modified = result;
                            
                            // 1. 破解白名单 (通行证)
                            modified = modified.replaceAll("\"respCode\"\\s*:\\s*\"[^\"]*\"", "\"respCode\":\"0000\"");
                            
                            // 2. 强行把所有业务状态改为“已开通” (完全复刻小火箭脚本逻辑)
                            modified = modified.replaceAll("\"isOpen\"\\s*:\\s*\"0\"", "\"isOpen\":\"1\"");
                            modified = modified.replaceAll("\"isOpen\"\\s*:\\s*\"N\"", "\"isOpen\":\"Y\"");
                            modified = modified.replaceAll("\"status\"\\s*:\\s*\"0\"", "\"status\":\"1\"");
                            modified = modified.replaceAll("\"isSign\"\\s*:\\s*\"0\"", "\"isSign\":\"1\"");
                            modified = modified.replaceAll("\"businessState\"\\s*:\\s*\"[^\"]*\"", "\"businessState\":\"1\"");

                            if (!modified.equals(result)) {
                                param.setResult(modified);
                            }
                        }
                    }
                }
            );
        } catch (Throwable t) {
        }

        // 拦截底层 JSON 解析 (双重保险)
        try {
            XposedHelpers.findAndHookConstructor(
                "org.json.JSONObject", 
                lpparam.classLoader, 
                String.class, 
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        String jsonString = (String) param.args[0];
                        if (jsonString != null) {
                            String modified = jsonString;
                            modified = modified.replaceAll("\"respCode\"\\s*:\\s*\"[^\"]*\"", "\"respCode\":\"0000\"");
                            modified = modified.replaceAll("\"isOpen\"\\s*:\\s*\"0\"", "\"isOpen\":\"1\"");
                            modified = modified.replaceAll("\"isOpen\"\\s*:\\s*\"N\"", "\"isOpen\":\"Y\"");
                            modified = modified.replaceAll("\"status\"\\s*:\\s*\"0\"", "\"status\":\"1\"");
                            modified = modified.replaceAll("\"isSign\"\\s*:\\s*\"0\"", "\"isSign\":\"1\"");
                            modified = modified.replaceAll("\"businessState\"\\s*:\\s*\"[^\"]*\"", "\"businessState\":\"1\"");
                            param.args[0] = modified;
                        }
                    }
                }
            );
        } catch (Throwable t) {
        }
    }
}
