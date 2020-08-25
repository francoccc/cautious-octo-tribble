package com.octo.util;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * FileUtils
 *
 * jdk: 1.8
 * @author chenxy
 */
public class FileUtils {

    /**
     * 获取系统正规后的路径
     * @param args
     * @return
     */
    public static String getJoinPath(String... args) {
        return Arrays.stream(args).collect(Collectors.joining(File.separator));
    }
}
