package com.sockc.unicomhook;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class MainHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        // 只针对联通 APP
        if (!lpparam.packageName.equals("com.sinovatech.unicom.ui")) {
            return;
        }

        XposedBridge.log("UnicomHook: 启动精准拦截模式");

        // 拦截 OkHttp (这是绝大多数请求的出口)
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
                            // 我们只在确定这个 JSON 包含白名单关键字时才修改
                            String modified = result.replaceAll("\"respCode\"\\s*:\\s*(\"[^\"]*\"|[^,\\}\\]\\s]+)", "\"respCode\":\"0000\"");
                            
                            // 针对你之前说的“未开通业务”，我们也精准修补这两个字段
                            modified = modified.replaceAll("\"isOpen\"\\s*:\\s*(\"[^\"]*\"|[^,\\}\\]\\s]+)", "\"isOpen\":\"1\"");
                            modified = modified.replaceAll("\"status\"\\s*:\\s*(\"[^\"]*\"|[^,\\}\\]\\s]+)", "\"status\":\"1\"");

                            if (!modified.equals(result)) {
                                param.setResult(modified);
                                XposedBridge.log("UnicomHook: 已成功将接口数据修正为合法状态");
                            }
                        }
                    }
                }
            );
        } catch (Throwable t) {
            XposedBridge.log("UnicomHook: 拦截出错 -> " + t.getMessage());
        }
    }
}
