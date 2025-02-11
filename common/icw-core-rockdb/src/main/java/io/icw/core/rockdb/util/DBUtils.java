/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.icw.core.rockdb.util;

import io.icw.core.model.StringUtils;
import io.icw.core.log.Log;
import io.icw.core.parse.config.IniEntity;
import org.ini4j.Config;
import org.ini4j.Ini;

import java.io.File;
import java.net.URL;

/**
 * @author qinyf
 * @desription: rocksdb utils
 * @date 2018/10/10
 */
public class DBUtils {

    public static File loadDataPath(String path) {
        File dir;
        String pathSeparator = System.getProperty("path.separator");
        String unixPathSeparator = ":";
        String rootPath;
        if (unixPathSeparator.equals(pathSeparator)) {
            rootPath = "/";
            if (path.startsWith(rootPath)) {
                dir = new File(path);
            } else {
                Log.debug("path=" + path);
                Log.debug("genAbsolutePath(path)=" + genAbsolutePath(path));
                dir = new File(genAbsolutePath(path));
            }
        } else {
            rootPath = "^[c-zC-Z]:.*";
            if (path.matches(rootPath)) {
                dir = new File(path);
            } else {
                dir = new File(genAbsolutePath(path));
            }
        }

        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    private static String getProjectDbPath() throws Exception {
        Config cfg = new Config();
        cfg.setMultiSection(true);
        Ini ini = new Ini();
        ini.setConfig(cfg);
        ini.load(new File("module.ncf"));  //可以读取到nuls_2.0项目根目录下的module.ncf,在生产环境读到jar同目录下的module.ncf
        IniEntity ie = new IniEntity(ini);
        String filePath = ie.getCfgValue("Module", "DataPath");
//        Log.debug(filePath); //读取配置的data文件夹路径
        return filePath;
    }

    public static String genAbsolutePath(String path) {
        File file = new File(path);
        if(file.exists()){
            if(file.isAbsolute()){
                return path;
            }
        }
        String[] paths = path.split("/|\\\\");
        URL resource = ClassLoader.getSystemClassLoader().getResource(".");
        String classPath = "";
        if (resource == null) {
            URL url = DBUtils.class.getProtectionDomain().getCodeSource().getLocation();
            classPath = url.getPath();
//            Log.debug("2.classPath = {}", classPath);
        } else {
            classPath = resource.getPath();
//            Log.debug("3.classPath = {}", classPath);
        }
        file = new File(classPath);
        String resultPath = null;
        boolean isFileName = false;
        for (String p : paths) {
            if (StringUtils.isBlank(p)) {
                continue;
            }
            if (!isFileName) {
                if ("..".equals(p)) {
                    file = file.getParentFile();
                } else if (".".equals(p)) {
                    continue;
                } else {
                    isFileName = true;
                    resultPath = file.getPath() + File.separator + p;
                }
            } else {
                resultPath += File.separator + p;
            }
        }
        return resultPath;
    }

    public static String getAreaNameFromDbPath(String dbPath) {
        int end = dbPath.lastIndexOf(File.separator);
        int start = dbPath.lastIndexOf(File.separator, end - 1) + 1;
        return dbPath.substring(start, end);
    }

    public static boolean checkPathLegal(String areaName) {
        if (StringUtils.isBlank(areaName)) {
            return false;
        }
        String regex = "^[a-zA-Z0-9_\\-]+$";
        return areaName.matches(regex);
    }
    /*public static void main(String []args){
        System.out.println(  DBUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath());
    }*/
}
