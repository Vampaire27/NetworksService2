package com.wwc2.networks.server.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.Base64;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FileUtils {

    public static final int SIZETYPE_B = 1;
    public static final int SIZETYPE_KB = 2;
    public static final int SIZETYPE_MB = 3;
    public static final int SIZETYPE_GB = 4;

    /**
     * the nand flash storage device.
     */
    public static final int NAND_FLASH = 1;

    /**
     * the nand flash storage device.
     */
    public static final int MEDIA_CARD = 2;

    public static final int MEDIA_USB = 3;
    public static final int MEDIA_USB1 = 4;

    private static final Map<Integer, String> mStorageDevices = new ConcurrentHashMap<>();

    static {
        mStorageDevices.put(NAND_FLASH, "/storage/emulated/0/");//与Main中的路径同步，避免从DVR列表进视频播放不正常的问题。2019-10-31
        mStorageDevices.put(MEDIA_CARD, "/storage/sdcard1/");
        mStorageDevices.put(MEDIA_USB, "/storage/usbotg/");
        mStorageDevices.put(MEDIA_USB1, "/storage/usbotg1/");
    }

    public static double getFileOrFilesSize(String filePath, int sizeType) {
        File file = new File(filePath);
        long blockSize = 0;
        try {
            if (file.isDirectory()) {
                blockSize = getFileSizes(file);
            } else {
                blockSize = getFileSize(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return FormetFileSize(blockSize, sizeType);
    }

    private static long getFileSize(File file) throws Exception {
        long size = 0;
        if (file.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            size = fis.available();
        }
        return size;
    }

    private static long getFileSizes(File f) throws Exception {
        long size = 0;
        File flist[] = f.listFiles();
        for (int i = 0; i < flist.length; i++) {
            if (flist[i].isDirectory()) {
                size = size + getFileSizes(flist[i]);
            } else {
                size = size + getFileSize(flist[i]);
            }
        }
        return size;
    }

    private static double FormetFileSize(long fileS, int sizeType) {
        DecimalFormat df = new DecimalFormat("#.00");
        double fileSizeLong = 0;
        switch (sizeType) {
            case SIZETYPE_B:
                fileSizeLong = Double.valueOf(df.format((double) fileS));
                break;
            case SIZETYPE_KB:
                fileSizeLong = Double.valueOf(df.format((double) fileS / 1024));
                break;
            case SIZETYPE_MB:
                fileSizeLong = Double.valueOf(df.format((double) fileS / 1048576));
                break;
            case SIZETYPE_GB:
                fileSizeLong = Double.valueOf(df.format((double) fileS / 1073741824));
                break;
            default:
                break;
        }
        return fileSizeLong;
    }

    public static boolean isSDCardPresent() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static boolean checkParent(String path) {
        boolean isOK;
        if (isSDCardPresent()) {
            File fileDir = new File(path);
            if (!fileDir.exists()) {
                fileDir.mkdirs();
            }
            isOK = true;
        } else {
            isOK = false;
        }
        return isOK;
    }

    public static long copyFile(final File srcFile, final File destDir,
                                String newFileName) {
        long copySizes = 0;
        if (!srcFile.exists()) {
            copySizes = -1;
        } else if (newFileName == null) {
            copySizes = -1;
        } else {
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            FileChannel fcin = null;
            FileChannel fcout = null;
            try {
                fcin = new FileInputStream(srcFile).getChannel();
                fcout = new FileOutputStream(new File(destDir, newFileName))
                        .getChannel();
                long size = fcin.size();
                fcin.transferTo(0, fcin.size(), fcout);
                copySizes = size;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fcin != null) {
                        fcin.close();
                    }
                    if (fcout != null) {
                        fcout.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        LogUtils.d("---copyFile---destDir="
                + destDir.getAbsolutePath() + ",,---newFileName=" + newFileName);
        return copySizes;
    }

    public static boolean deleteFile(String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            final File file = new File(filePath);
            if (file.exists()) {
                return file.delete();
            }
        }
        return false;
    }

    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     * @param dir 将要删除的文件目录
     * @return boolean Returns "true" if all deletions were successful.
     *                 If a deletion fails, the method stops attempting to
     *                 delete and returns "false".
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    public static boolean fileIsExists(String strFile) {
        try {
            File f = new File(strFile);
            if (!f.exists()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static void writeFile(Map<String, String> values, File file) {
        if (null != file) {
            try {
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, false));
                for (Map.Entry<String, String> entry : values.entrySet()) {
                    final String key = entry.getKey();
                    final String value = entry.getValue().toString();
                    if (null != key && null != value) {
                        bufferedWriter.write(entry.getKey());
                        bufferedWriter.write("=");
                        bufferedWriter.write(entry.getValue().toString());
                        bufferedWriter.newLine();
                    }
                }
                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean writeFile(String values, File file) {
        if (null != file) {
            try {
                BufferedWriter bufferedWriter =
                        new BufferedWriter(new FileWriter(file, false));
                bufferedWriter.write(values);
                bufferedWriter.newLine();
                bufferedWriter.close();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public static Map<String, String> readFile(String sFileName) {
        if (TextUtils.isEmpty(sFileName)) {
            return null;
        }
        Map<String, String> sDest = new HashMap<String, String>(16);
        final File f = new File(sFileName);
        if (!f.exists()) {
            return null;
        }
        try {
            FileInputStream is = new FileInputStream(f);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            try {
                String data = null;
                while ((data = br.readLine()) != null) {
                    String[] tmp = data.split("=");
                    sDest.put(tmp[0].trim(), tmp[1].trim());
                }
            } catch (IOException ioex) {
                return null;
            } finally {
                is.close();
                is = null;
                br.close();
                br = null;
            }
        } catch (Exception ex) {
            return null;
        } catch (OutOfMemoryError ex) {
            return null;
        }
        return sDest;
    }

    public static String readFiles(String sFileName) {
        if (TextUtils.isEmpty(sFileName)) {
            return null;
        }
        final File f = new File(sFileName);
        if (!f.exists()) {
            return null;
        }
        String data = null;
        try {
            FileInputStream is = new FileInputStream(f);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            try {
                data = br.readLine();
            } catch (IOException ioex) {
                return null;
            } finally {
                is.close();
                is = null;
                br.close();
                br = null;
            }
        } catch (Exception ex) {
            return null;
        } catch (OutOfMemoryError ex) {
            return null;
        }
        return data;
    }

    public static String getImageStr(String imgFile) {
        InputStream inputStream = null;
        // 加密
        String imgBase64 = null;
        byte[] data = null;
        try {
            inputStream = new FileInputStream(imgFile);
            data = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();

            imgBase64 = Base64.encodeToString(data,Base64.DEFAULT);
        } catch (IOException e) {
            LogUtils.d("getImageStr e=" + e.toString());
        }
        return "data:image/jpeg;base64," + imgBase64;
    }

    /**
     * 获取设备路径
     */
    public static String getPath(int storage) {
        String env = "";
        String ret = "";

        for (Map.Entry<Integer, String> entry : mStorageDevices.entrySet()) {
            if (storage == entry.getKey()) {
                env = entry.getValue();
                break;
            }
        }
        ret = env;
        return ret;
    }


    /**
     * 获取设备ID
     */
    public static int getDeviceId(String name) {
        int storage = -1;
        if (null != name) {
            for (Map.Entry<Integer, String> entry : mStorageDevices.entrySet()) {
                final int _storage = entry.getKey();
                final String path = getPath(_storage);
                if (null != path) {
                    if (name.startsWith(path)) {
                        storage = _storage;
                        break;
                    }
                }
            }
        }
        return storage;
    }

    //设备检测
    /**
     * Check whether the letter has been successfully mount
     */
    public static boolean isDiskMounted(Context context, String path) {
        boolean ret = false;
        if (null != path) {
            if (null != context) {
                StorageManager mStorageManager = (StorageManager) context.getSystemService(Activity.STORAGE_SERVICE);
                if (null != mStorageManager) {
                    final int size = path.length();
                    if (size > 1) {
                        final String _path = path.substring(0, size - 1);
                        Class classMethod;
                        Method method;
                        Object object;
                        try {
                            classMethod = Class.forName(StorageManager.class.getName());
                            if (null != classMethod) {
                                method = classMethod.getMethod("getVolumeState", String.class);
                                if (null != method) {
                                    object = method.invoke(mStorageManager, _path);
                                    if (object instanceof String) {
                                        String state = (String) object;
                                        ret = Environment.MEDIA_MOUNTED.equals(state);
                                        if (ret) {
                                            File file = new File(path);
                                            ret = file.exists();
                                        }
                                    }
                                }
                            }
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Check whether the letter has been successfully mount
     */
    public static boolean isPathMounted(Context context, String path) {
        int type = getDeviceId(path);
        if (!FileUtils.isDiskMounted(context, FileUtils.getPath(type))) {
            LogUtils.e("path " +  path+ " is not exist!");
            return false;
        }
        return true;
    }

}
