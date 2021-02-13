package com.github.jgzl.bsf.file.impl;

import com.github.jgzl.bsf.core.util.ContextUtils;
import com.github.jgzl.bsf.core.util.LogUtils;
import com.github.jgzl.bsf.core.util.PropertyUtils;
import com.github.jgzl.bsf.file.FileException;
import com.github.jgzl.bsf.file.FileProvider;
import com.github.jgzl.bsf.file.config.FileProperties;
import com.github.jgzl.bsf.health.base.AbstractCollectTask;
import com.github.jgzl.bsf.health.base.EnumWarnType;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * @author Huang Zhaoping
 */
public abstract class AbstractFileProvider implements FileProvider {
    private static final String IMAGES = "images/", DOCS = "docs/", EXCELS = "excels/", FILES = "files/", TEMP = "temp/";
    private static Map<String, String> defaultPrefix = new HashMap<>();

    static {
        defaultPrefix.put("jpg", IMAGES);
        defaultPrefix.put("png", IMAGES);
        defaultPrefix.put("jpeg", IMAGES);
        defaultPrefix.put("gif", IMAGES);
        defaultPrefix.put("bmp", IMAGES);
        defaultPrefix.put("doc", DOCS);
        defaultPrefix.put("docx", DOCS);
        defaultPrefix.put("pdf", DOCS);
        defaultPrefix.put("txt", DOCS);
        defaultPrefix.put("xlsx", EXCELS);
        defaultPrefix.put("xls", EXCELS);
        defaultPrefix.put("csv", EXCELS);
        defaultPrefix.put("tmp", TEMP);
        defaultPrefix.put("temp", TEMP);
    }

    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    protected String createFileKey(String path, String name) {
        String suffix = null;
        if (name != null && name.lastIndexOf('.') >= 0) {
            suffix = name.substring(name.lastIndexOf('.') + 1).trim().toLowerCase();
        }
        if (path != null) {
            while (path.startsWith("/")) {
                path = path.substring(1);
            }
        }
        if (path == null || (path = path.trim()).length() == 0) {
            path = suffix == null ? FILES : defaultPrefix.getOrDefault(suffix, FILES);
        }
        StringBuilder builder = new StringBuilder(path);
        if (!path.endsWith("/")) {
            builder.append("/");
        }
        builder.append(dateTimeFormatter.format(LocalDateTime.now())).append("/");
        builder.append(randomName());
        if (suffix != null && suffix.length() > 0) {
            builder.append('.').append(suffix);
        }
        return builder.toString();
    }

    private String randomName() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }


    @Override
    public String upload(InputStream input, String name) {
        return FileProviderMonitor.hook().run("upload", () -> retryUpload(()->upload(input, null, name)));
    }
    @Override
    public String upload(String filePath,String name) {
        return FileProviderMonitor.hook().run("upload", () -> retryUpload(()->upload(filePath, null, name)));
    }

    @Override
    public String uploadTemp(InputStream input, String name) {
        return FileProviderMonitor.hook().run("uploadTemp", () -> retryUpload(()->upload(input, TEMP, name)));
    }
    @Override
    public String uploadTemp(String filePath, String name) {
        return FileProviderMonitor.hook().run("uploadTemp", () -> retryUpload(()->upload(filePath, TEMP, name)));
    }

    private String retryUpload(Callable<String> call)  {
        int retryUpload=PropertyUtils.getPropertyCache("bsf.file.retryUpload", 3);
        String result= null;
        for (int i = 0; i <=retryUpload; i++) {
            try{
                result=call.call();break;
            }catch(Exception e){
                if(i==retryUpload){
                    sendWarning(retryUpload);
                    throw new FileException("努力上传["+retryUpload+"]次后，仍然失败，错误原因：七牛文件服务异常", e);
                }else{
                    try {Thread.sleep(1000);} catch (InterruptedException ex) {}
                    LogUtils.error(AbstractFileProvider.class, FileProperties.Project, "努力上传["+(i+1)+"]次后失败，重试上传", e);
                }
            }
        }
        return  result;
    }
    private void sendWarning(int retryUpload){
        FileProperties fileProperties= ContextUtils.getBean(FileProperties.class, true);
        if(fileProperties.isWarningEnabled()){
            AbstractCollectTask.notifyMessage(EnumWarnType.ERROR,"七牛云上传失败","努力上传["+retryUpload+"]次后，仍然失败，错误原因：七牛文件服务异常");
        }
    }
    /**
     * 	上传文件
     *
     * @param input 文件流
     * @param path  存储路径
     * @param name  文件名
     * @return
     */
    public abstract String upload(InputStream input, String path, String name);
    /**
     * 	上传文件
     *
     * @param input 文件流
     * @param path  存储路径
     * @param name  文件名
     * @return
     */
    public abstract String upload(String filePath, String path, String name);
    
    public abstract String info();


}
