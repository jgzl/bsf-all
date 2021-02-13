package com.github.jgzl.bsf.transaction.config;

/**
 * @author huojuncheng
 */
public class EffortTransactionProperties {

    public static String project = "EffortTransaction";
    public static String springApplicationName = "spring.application.name";
    public static String springProActive = "spring.profiles.active";
    public static String topic="EFFORTTRANSACTION-TOPIC";
    public static String tagBsf="$$BSF";
    public static String tagElse="$$ELSE";
    public static int locktime = 60 * 60 * 2;
    public static int methodSecond=10;
    public static int executeSucess=1;
    public static int executeFail=0;
}
