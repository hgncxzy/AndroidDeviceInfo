package com.xzy.deviceinfo.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.WindowManager;

import java.io.*;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * 系统相关的工具类。另外可以参考 RomUtils.java
 *
 * @author xzy
 */
@SuppressWarnings("all")
public class SystemUtils {

    private static final String SYS_EMUI = "sys_emui";
    private static final String SYS_MIUI = "sys_miui";
    private static final String SYS_FLYME = "sys_flyme";
    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";
    private static final String KEY_EMUI_API_LEVEL = "ro.build.hw_emui_api_level";
    private static final String KEY_EMUI_VERSION = "ro.build.version.emui";
    private static final String KEY_EMUI_CONFIG_HW_SYS_VERSION = "ro.confg.hw_systemversion";

    public static String getSystem() {
        String system = "otherSystem";
        try {
            Properties prop = new Properties();
            prop.load(new FileInputStream(new File(Environment.getRootDirectory(),
                    "build.prop")));
            if (prop.getProperty(KEY_MIUI_VERSION_CODE, null) != null
                    || prop.getProperty(KEY_MIUI_VERSION_NAME, null) != null
                    || prop.getProperty(KEY_MIUI_INTERNAL_STORAGE, null) != null) {
                system = SYS_MIUI;//小米
            } else if (prop.getProperty(KEY_EMUI_API_LEVEL, null) != null
                    || prop.getProperty(KEY_EMUI_VERSION, null) != null
                    || prop.getProperty(KEY_EMUI_CONFIG_HW_SYS_VERSION, null) != null) {
                system = SYS_EMUI;//华为
            } else if (getMeizuFlymeOSFlag().toLowerCase().contains("flyme")) {
                system = SYS_FLYME;//魅族
            }
        } catch (IOException e) {
            e.printStackTrace();
            return system;
        }
        return system;
    }

    public static String getMeizuFlymeOSFlag() {
        return getSystemProperty();
    }

    /**
     * @return 系统属性
     */
    private static String getSystemProperty() {
        try {
            @SuppressLint("PrivateApi")
            Class<?> clz = Class.forName("android.os.SystemProperties");
            Method get = clz.getMethod("get", String.class, String.class);
            return (String) get.invoke(clz, "ro.build.display.id", "otherSystem");
        } catch (Exception e) {
            Log.e("", e.getMessage());
        }
        return "otherSystem";
    }

    /**
     * @return cpu 信息
     */
    public static String getCpuInfo() {
        String cpuCount = "cpu 核心数：" + Runtime.getRuntime().availableProcessors() + "\n";
        String cpuFreq = "cpu 频率范围:" + getMinCpuFreq() + "Hz~" + getMaxCpuFreq() + "Hz" + "\n";
        String cpuName = "cpu 名称:" + getCpuName() + "\n";
        ProcessBuilder processBuilder;
        String[] DATA = {"/system/bin/cat", "/proc/cpuinfo"};
        InputStream inputStream;
        Process process;
        byte[] byteArry;
        String cpuInfo = cpuCount + cpuFreq + cpuName + "cpu 详细信息如下：" + "\n";
        ;
        byteArry = new byte[1024];
        try {
            processBuilder = new ProcessBuilder(DATA);
            process = processBuilder.start();
            inputStream = process.getInputStream();
            while (inputStream.read(byteArry) != -1) {
                cpuInfo = cpuInfo + new String(byteArry);
            }
            inputStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return cpuInfo + "\n";
    }

    /**
     * 获取手机IMEI(需要“android.permission.READ_PHONE_STATE”权限)
     *
     * @return 手机IMEI
     */
    @SuppressLint("MissingPermission")
    public static String getIMEI(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Activity.TELEPHONY_SERVICE);
        try {
            return tm.getDeviceId() + "\n";
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取内存信息。
     *
     * @param context 上下文
     * @return 内存信息
     */
    public static String getMemoInfo(Context context) {
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(memoryInfo);

        Runtime runtime = Runtime.getRuntime();

        String strMemInfo =
                "MemoryInfo.availMem = " +
                        getFileSizeDescription(memoryInfo.availMem) + "\n"
                        + "MemoryInfo.totalMem = " + getFileSizeDescription(memoryInfo.totalMem) + "\n" // API 16
                        + "\n"
                        + "Runtime.maxMemory() = " + getFileSizeDescription(runtime.maxMemory()) + "\n"
                        + "Runtime.totalMemory() = " + getFileSizeDescription(runtime.totalMemory()) + "\n"
                        + "Runtime.freeMemory() = " + getFileSizeDescription(runtime.freeMemory()) + "\n";

        return strMemInfo;
    }


    /**
     * 获取系统相关的信息。
     *
     * @return 系统信息。
     */
    public static String getSysVersionInfo(Context context) {
        String sysVersionInfo = "SERIAL: " + Build.SERIAL + "\n" +
                "品牌: " + Build.BRAND + "\n" +
                "型号: " + Build.MODEL + "\n" +
                "SDK:  " + Build.VERSION.SDK + "\n" +
                "分辨率: " + getScreenWidth(context) + "*" + getScreenHeight(context) + "\n" +
                "APP 分辨率(去掉状态栏高度和导航栏): " + getAppScreenWidth(context) + "*" + getAppScreenHeight(context) + "\n" +
                "DPI: " + getScreenDensityDpi() + "\n" +
                "系统版本: Android " + Build.VERSION.RELEASE + "\n";
        return sysVersionInfo;
    }

    /**
     * 得到关于文件大小的描述信息。
     *
     * @param size 文件大小 long 值
     * @return 文件大小的描述信息字符串。
     */
    public static String getFileSizeDescription(long size) {
        StringBuilder bytes = new StringBuilder();
        DecimalFormat format = new DecimalFormat("###.0");
        if (size >= 1024 * 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0 * 1024.0));
            bytes.append(format.format(i)).append("GB");
        } else if (size >= 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0));
            bytes.append(format.format(i)).append("MB");
        } else if (size >= 1024) {
            double i = (size / (1024.0));
            bytes.append(format.format(i)).append("KB");
        } else {
            if (size <= 0) {
                bytes.append("0B");
            } else {
                bytes.append((int) size).append("B");
            }
        }
        return bytes.toString();
    }

    /**
     * Return the width of screen, in pixel.
     *
     * @return the width of screen, in pixel
     */
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) return -1;
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            wm.getDefaultDisplay().getRealSize(point);
        } else {
            wm.getDefaultDisplay().getSize(point);
        }
        return point.x;
    }

    /**
     * Return the height of screen, in pixel.
     *
     * @return the height of screen, in pixel
     */
    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) return -1;
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            wm.getDefaultDisplay().getRealSize(point);
        } else {
            wm.getDefaultDisplay().getSize(point);
        }
        return point.y;
    }

    /**
     * Return the density of screen.
     *
     * @return the density of screen
     */
    public static float getScreenDensity() {
        return Resources.getSystem().getDisplayMetrics().density;
    }

    /**
     * Return the screen density expressed as dots-per-inch.
     *
     * @return the screen density expressed as dots-per-inch
     */
    public static int getScreenDensityDpi() {
        return Resources.getSystem().getDisplayMetrics().densityDpi;
    }


    // 获取CPU最大频率（单位KHZ）

    // "/system/bin/cat" 命令行

    // "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq" 存储最大频率的文件的路径

    public static String getMaxCpuFreq() {
        String result = "";
        ProcessBuilder cmd;
        try {
            String[] args = {"/system/bin/cat",
                    "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq"};
            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[24];
            while (in.read(re) != -1) {
                result = result + new String(re);
            }
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            result = "N/A";
        }
        return result.trim();
    }

    // 获取CPU最小频率（单位KHZ）
    public static String getMinCpuFreq() {
        String result = "";
        ProcessBuilder cmd;
        try {
            String[] args = {"/system/bin/cat",
                    "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq"};
            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[24];
            while (in.read(re) != -1) {
                result = result + new String(re);
            }
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            result = "N/A";
        }
        return result.trim();
    }

    // 实时获取CPU当前频率（单位KHZ）
    public static String getCurCpuFreq() {
        String result = "N/A";
        try {
            FileReader fr = new FileReader(
                    "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq");
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            result = text.trim();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    // 获取CPU名字
    public static String getCpuName() {
        try {
            FileReader fr = new FileReader("/proc/cpuinfo");
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            String[] array = text.split(":\\s+", 2);
            for (int i = 0; i < array.length; i++) {
            }
            return array[1];
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Return the application's width of screen, in pixel.
     *
     * @return the application's width of screen, in pixel
     */
    public static int getAppScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) return -1;
        Point point = new Point();
        wm.getDefaultDisplay().getSize(point);
        return point.x;
    }

    /**
     * Return the application's height of screen, in pixel.
     *
     * @return the application's height of screen, in pixel
     */
    public static int getAppScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) return -1;
        Point point = new Point();
        wm.getDefaultDisplay().getSize(point);
        return point.y;
    }

    /**
     * 获取手机内部空间总大小
     *
     * @return 大小，字节为单位
     */
    public static String getTotalInternalMemorySize() {
        //获取内部存储根目录
        File path = Environment.getDataDirectory();
        //系统的空间描述类
        StatFs stat = new StatFs(path.getPath());
        //每个区块占字节数
        long blockSize = stat.getBlockSize();
        //区块总数
        long totalBlocks = stat.getBlockCount();
        return "手机内部总空间: " + getFileSizeDescription(totalBlocks * blockSize) + "\n";
    }

    /**
     * 获取手机内部可用空间大小
     *
     * @return 大小，字节为单位
     */
    public static String getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        //获取可用区块数量
        long availableBlocks = stat.getAvailableBlocks();
        return "手机内部剩余空间: " + getFileSizeDescription(availableBlocks * blockSize) + "\n";
    }

    /**
     * 判断SD卡是否可用
     *
     * @return true : 可用<br>false : 不可用
     */
    public static boolean isSDCardEnable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * 获取手机外部总空间大小
     *
     * @return 总大小，字节为单位
     */
    static public String getTotalExternalMemorySize() {
        if (isSDCardEnable()) {
            //获取SDCard根目录
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();
            return "SD 卡总空间: " + getFileSizeDescription(totalBlocks * blockSize) + "\n";
        } else {
            return "SD 卡总空间: UNKNOW" + "\n";
        }
    }

    /**
     * 获取SD卡剩余空间
     *
     * @return SD卡剩余空间
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static String getAvailableExternalMemorySize() {
        if (isSDCardEnable()) {
            //获取SDCard根目录
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize, availableBlocks;
            availableBlocks = stat.getAvailableBlocksLong();
            blockSize = stat.getBlockSizeLong();
            return "SD 卡剩余空间:" + getFileSizeDescription(availableBlocks * blockSize) + "\n";
        } else {
            return "sdcard unable!" + "\n";
        }
    }

    public static String showSensorInfo(Context context) {
        SensorManager mSensroMgr;
        String[] mSensorType = {
                "加速度", "磁场", "方向", "陀螺仪", "光线",
                "压力", "温度", "距离", "重力", "线性加速度",
                "旋转矢量", "湿度", "环境温度", "无标定磁场", "无标定旋转矢量",
                "未校准陀螺仪", "特殊动作", "步行检测", "计步器", "地磁旋转矢量",
                "心跳", "倾斜检测", "唤醒手势", "瞥一眼", "捡起来"};
        Map<Integer, String> mapSensor = new HashMap<Integer, String>();
        mSensroMgr = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensorList = mSensroMgr.getSensorList(Sensor.TYPE_ALL);
        String showContent = "当前支持的传感器包括：\n";
        for (Sensor sensor : sensorList) {
            if (sensor.getType() >= mSensorType.length) {
                continue;
            }
            mapSensor.put(sensor.getType(), sensor.getName());
        }
        for (Map.Entry<Integer, String> item_map : mapSensor.entrySet()) {
            int type = item_map.getKey();
            String name = item_map.getValue();
            String content = String.format("%d %s：%s\n", type, mSensorType[type - 1], name);
            showContent += content;
        }
        return showContent;
    }

}

