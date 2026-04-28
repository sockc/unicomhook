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
                        if (result != null && result.contains("respCode")) {
                            
                            // 【核心修复】：完美复刻小火箭的 jq 逻辑！
                            // 无论 respCode 的值带不带引号，嵌套在第几层，统统替换为 "0000"
                            String modified = result.replaceAll("\"respCode\"\\s*:\\s*(\"[^\"]*\"|[^,\\}\\]]+)", "\"respCode\":\"0000\"");
                            
                            // 保留其他状态字段作为兜底保险
                            modified = modified.replaceAll("\"isOpen\"\\s*:\\s*(\"[^\"]*\"|[^,\\}\\]]+)", "\"isOpen\":\"1\"");
                            modified = modified.replaceAll("\"status\"\\s*:\\s*(\"[^\"]*\"|[^,\\}\\]]+)", "\"status\":\"1\"");

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
                        if (jsonString != null && jsonString.contains("respCode")) {
                            
                            String modified = jsonString.replaceAll("\"respCode\"\\s*:\\s*(\"[^\"]*\"|[^,\\}\\]]+)", "\"respCode\":\"0000\"");
                            modified = modified.replaceAll("\"isOpen\"\\s*:\\s*(\"[^\"]*\"|[^,\\}\\]]+)", "\"isOpen\":\"1\"");
                            modified = modified.replaceAll("\"status\"\\s*:\\s*(\"[^\"]*\"|[^,\\}\\]]+)", "\"status\":\"1\"");
                            
                            param.args[0] = modified;
                        }
                    }
                }
            );
        } catch (Throwable t) {
        }
    }
}
