package com.github.jgzl.bsf.transaction.base;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author huojuncheng
 */
@Data
public class EffortTransactionObject implements Serializable {
    private String uuid;
    private String appName;
    private String className;
    private String method;
    private String exception;
    private String[] param;
    private int reconsumeTimes;
    private int currentTimes;
    private int delayTimeLevel;
    private Date startTime;
    private Date endTime;
    private String tag;
    private int status;
}
