package hook.xposed;

import java.util.ArrayList;
import java.util.List;

import util.Util;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class XURL extends XHook {
	private static final String className = "java.net.URL";
	private static String localpkgName = null;
	private static ClassLoader localcl = null;
	private static List<String> logList = null;
	private static XURL classLoadHook;

	public static XURL getInstance() {
		if (classLoadHook == null) {
			classLoadHook = new XURL();
		}
		return classLoadHook;
	}

	@Override
	String getClassName() {
		// TODO Auto-generated method stub
		return className;
	}

	@Override
	void hook(String pkgName, ClassLoader classLoader) {
		// TODO Auto-generated method stub
		localpkgName = pkgName;
		localcl = classLoader;
		logList = new ArrayList<String>();

		XposedHelpers.findAndHookConstructor(className, classLoader, String.class,
				new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param) {
						// TODO Auto-generated method stub
						String time = Util.getSystemTime();
						logList.add("time:" + time);
						logList.add("action:--new url--");
						logList.add("function:URL");
						logList.add("target:" + param.args[0].toString());
						logList.add("call class:"+localcl.getClass().toString());
						for (String log : logList) {
							XposedBridge.log(log);
						}
						Util.writeLog(localpkgName, logList);
						logList.clear();
					}
				});

		XposedHelpers.findAndHookMethod(className, classLoader,
				"openConnection", new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param) {
						// TODO Auto-generated method stub
						String time = Util.getSystemTime();
						logList.add("time:" + time);
						logList.add("action:--connect url--");
						logList.add("function:openConnection");
						logList.add("call class:"+localcl.getClass().toString());
						for (String log : logList) {
							XposedBridge.log(log);
						}
						Util.writeLog(localpkgName, logList);
						logList.clear();
					}
				});
	}

}
