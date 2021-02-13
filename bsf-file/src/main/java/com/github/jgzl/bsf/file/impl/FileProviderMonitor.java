package com.github.jgzl.bsf.file.impl;

import com.github.jgzl.bsf.core.common.Collector;

/**
 * @author Huang Zhaoping
 */
public class FileProviderMonitor {

    private static String name = "file.info";

    public static Collector.Hook hook() {
        return Collector.Default.hook(name + ".hook");
    }
}
