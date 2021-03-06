package me.mikusjelly.amon.hook;

import android.os.Binder;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import me.mikusjelly.amon.utils.Global;
import me.mikusjelly.amon.utils.LogWriter;
import me.mikusjelly.amon.utils.Util;

/**
 * TODO 读文件的时候，需要DUMP到SDCARD
 */
public class XIoBridge extends MethodHook {
    private static final String mClassName = "libcore.io.IoBridge";
    private Methods mMethod = null;
    private ArrayList<String> paths = new ArrayList<String>() {
        {
            add("/amon/");
            add("/proc/net/if_inet");
            add("/sys/class/net/");
            add("/system/etc/security/cacerts/");
            add("/dev/urandom");
            add("/proc/meminfo");
        }
    };

    private XIoBridge(Methods method) {
        super(mClassName, method.name());
        mMethod = method;
    }


    // public static FileDescriptor open(String path, int flags)
    // public static int read(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount)
    // public static void write(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount)
    // libcore/luni/src/main/java/libcore/io/IoBridge.java

    public static List<MethodHook> getMethodHookList() {
        List<MethodHook> methodHookList = new ArrayList<MethodHook>();
        for (Methods method : Methods.values())
            methodHookList.add(new XIoBridge(method));

        return methodHookList;
    }

    private ArrayList<byte[]> extractData(MethodHookParam param) {
        ArrayList<byte[]> dataSlices = new ArrayList<>();

        byte[] bytes = (byte[]) param.args[1];
        int byteOffset = (Integer) param.args[2];
        int byteCount = (Integer) param.args[3];

        while (byteCount > 0) {
            int targetDataLen = byteCount > Global.DATA_BYTES_TO_LOG ? Global.DATA_BYTES_TO_LOG : byteCount;
            byte[] targetData = new byte[targetDataLen];
            for (int i = 0; i < targetDataLen; i++)
                targetData[i] = bytes[byteOffset + i];
            byteOffset += targetDataLen;
            byteCount -= targetDataLen;

            dataSlices.add(targetData);

        }

        return dataSlices;
    }

    @Override
    public void after(MethodHookParam param) throws Throwable {
        int uid = Binder.getCallingUid();
        int pid = Binder.getCallingPid();

        if (uid <= 1000)
            return;

        if (mMethod == Methods.open) {
            if (param.args.length >= 1) {

                String argNames = null;
                if (mMethod == Methods.open) {
                    argNames = "path|flags";
                }

                String path = param.args[0].toString();
                for (String str : paths) {
                    if (path.contains(str)) {
                        return;
                    }
                }

                log(0, param, argNames);
            }
        } else if (mMethod == Methods.read) {
            if (param.args.length >= 4) {
                if (Global.pathConvertorClass == null || Global.logFilePathMethod == null)
                    return;

                FileDescriptor fileDescriptor = (FileDescriptor) param.args[0];
                int fdInt = Util.getFd(fileDescriptor);
                int fdId = Util.getTimeId();

                if ((Boolean) Global.logFilePathMethod.invoke(Global.pathConvertorClass,
                        uid, pid, fdInt, fdId) == false)
                    return;

                ArrayList<byte[]> dataSlices = extractData(param);
                for (int i = 0; i < dataSlices.size(); i++) {
                    String logMsg = String.format("{\"operation\": \"read\",\"data\": \"%s\", \"id\": \"%d\", ",
                            Util.toHex(dataSlices.get(i)), fdId);
                    LogWriter.logStack(logMsg);
                }

            }
        } else if (mMethod == Methods.write) {
            if (param.args.length >= 4) {
                if (Global.pathConvertorClass == null || Global.logFilePathMethod == null)
                    return;

                FileDescriptor fileDescriptor = (FileDescriptor) param.args[0];
                int fdInt = Util.getFd(fileDescriptor);
                int fdId = Util.getTimeId();

                if ((Boolean) Global.logFilePathMethod.invoke(Global.pathConvertorClass,
                        uid, pid, fdInt, fdId) == false)
                    return;
                ArrayList<byte[]> dataSlices = extractData(param);
                for (int i = 0; i < dataSlices.size(); i++) {
                    String logMsg = String.format("{\"operation\": \"write\", \"data\": \"%s\", \"id\": \"%d\", ",
                            Util.toHex(dataSlices.get(i)), fdId);
                    LogWriter.logStack(logMsg);
                }
            }
        }

    }

    private enum Methods {
        open, read, write
    }
}
