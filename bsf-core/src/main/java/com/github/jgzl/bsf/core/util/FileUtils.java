package com.github.jgzl.bsf.core.util;

import com.github.jgzl.bsf.core.base.BsfException;
import lombok.var;

import java.io.*;

/**
 * @author: lihaifeng
 * @version: 2019-09-25 15:35
 **/
public class FileUtils {
    public static boolean fileExsit(String filepath) {
        File file = new File(filepath);
        return file.exists();
    }

    public static String getDirectoryPath(String path) {
        File file = new File(path);
        return file.getAbsolutePath();
    }

    public static String getDirectoryPath(Class cls) {
        File file = getJarFile(cls);
        if (file == null) {
            return null;
        }
        if (!file.isDirectory()) {
            file = file.getParentFile();
        }
        return file.getAbsolutePath();
    }

    public static File getJarFile(Class cls) {
        String path = cls.getProtectionDomain().getCodeSource().getLocation().getFile();
        try {
            // 转换处理中文及空格
            path = java.net.URLDecoder.decode(path, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return null;
        }
        return new File(path);
    }

    public static String getFilePath(String... paths) {
        StringBuffer sb = new StringBuffer();
        for (String path : paths) {
            sb.append(org.springframework.util.StringUtils.trimTrailingCharacter(path, File.separatorChar));
            sb.append(File.separator);
        }
        return org.springframework.util.StringUtils.trimTrailingCharacter(sb.toString(), File.separatorChar);
    }

    public static void createDirectory(String path) {
        File file = new File(path);
        if (!file.isDirectory()) {
            file = file.getParentFile();
        }
        //如果文件夹不存在则创建
        if (!file.exists()) {
            file.mkdirs();
        } else {
        }
    }

    public static void saveStream(InputStream is,String fileName){
        try(var in=new BufferedInputStream(is)){
           try(var out=new FileOutputStream(fileName)) {
               int len = -1;
               byte[] b = new byte[1024];
               while ((len = in.read(b)) != -1) {
                   out.write(b, 0, len);
               }
               out.flush();
           }
        }catch (Exception e){
            throw new BsfException("保存流出错",e);
        }
    }

    public static void appendAllText(String path, String contents) {
        try {
            //如果文件存在，则追加内容；如果文件不存在，则创建文件
            File f = new File(path);
            try (FileWriter fw = new FileWriter(f, true)) {
                try (PrintWriter pw = new PrintWriter(fw)) {
                    pw.println(contents);
                    pw.flush();
                    fw.flush();
                }
            }
        } catch (IOException exp) {
            throw new BsfException("追加文件异常", exp);
        }

    }

    public static void writeAllText(String path, String contents) {
        try {
            File f = new File(path);
            if (f.exists()) {
                f.delete();
            } else {
            }
            //f.mkdirs();
            //不存在则创建
            f.createNewFile();
            try (BufferedWriter output = new BufferedWriter(new FileWriter(f))) {
                output.write(contents);
            }
        } catch (IOException exp) {
            throw new BsfException("写文件异常", exp);
        }
    }

    public static String readAllText(String path) {
        try {
            File f = new File(path);
            if (f.exists()) {
                //获取文件长度
                Long filelength = f.length();
                byte[] filecontent = new byte[filelength.intValue()];
                try (FileInputStream in = new FileInputStream(f)) {
                    in.read(filecontent);
                }
                //返回文件内容,默认编码
                return new String(filecontent);
            } else {
                throw new FileNotFoundException(path);
            }
        } catch (IOException exp) {
            throw new BsfException("读文件异常", exp);
        }
    }

    public static String lineSeparator() {
        return System.getProperty("line.separator");
    }

    /**
     * 根据文件路径获取文件名
     *
     * @param filePath 文件路径
     * @return filename 文件名
     */
    public static String getFileName(String filePath) {
        String path = filePath.replaceAll("\\\\", "/");
        return path.substring(path.lastIndexOf("/") + 1, path.length());
    }

    /**
     * 复制目录
     *
     * @param fromDir
     * @param toDir
     */
    public static void copyDir(String fromDir, String toDir) {
        //创建目录的File对象
        File dirSouce = new File(fromDir);
        //判断源目录是不是一个目录
        if (!dirSouce.isDirectory()) {
            //如果不是目录那就不复制
            return;
        }
        //创建目标目录的File对象
        File destDir = new File(toDir);
        //如果目的目录不存在
        if (!destDir.exists()) {
            //创建目的目录
            destDir.mkdir();
        }
        //获取源目录下的File对象列表
        File[] files = dirSouce.listFiles();
        for (File file : files) {
            //拼接新的fromDir(fromFile)和toDir(toFile)的路径
            String strFrom = fromDir + File.separator + file.getName();
            String strTo = toDir + File.separator + file.getName();
            //判断File对象是目录还是文件
            //判断是否是目录
            if (file.isDirectory()) {
                //递归调用复制目录的方法
                copyDir(strFrom, strTo);
            }
            //判断是否是文件
            if (file.isFile()) {
                //递归调用复制文件的方法
                copyFile(strFrom, strTo);
            }
        }
    }

    /**
     * 复制文件
     *
     * @param fromFile
     * @param toFile
     */
    public static void copyFile(String fromFile, String toFile) {
        try {
            //字节输入流——读取文件
            try (FileInputStream in = new FileInputStream(fromFile)) {
                //字节输出流——写入文件
                try (FileOutputStream out = new FileOutputStream(toFile)) {
                    //把读取到的内容写入新文件
                    //把字节数组设置大一些   1*1024*1024=1M
                    byte[] bs = new byte[1 * 1024 * 1024];
                    int count = 0;
                    while ((count = in.read(bs)) != -1) {
                        out.write(bs, 0, count);
                    }
                    //关闭流
                    out.flush();
                }
            }
        } catch (Exception e) {
            throw new BsfException("复制文件异常:" + StringUtils.nullToEmpty(fromFile) + "->" + StringUtils.nullToEmpty(toFile));
        }
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
            //递归删除目录中的子目录下
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }
}
