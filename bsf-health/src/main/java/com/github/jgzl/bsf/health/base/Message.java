package com.github.jgzl.bsf.health.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 报警消息
 * @author: lihaifeng
 * @version: 2019-07-24 13:44
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message{
    private EnumWarnType warnType;
    private String title;
    private String content;
    private EnumLevelType levelType=EnumLevelType.LOW;

    public Message(EnumWarnType warnType, String title, String content) {
        this.warnType = warnType;
        this.title = title;
        this.content = content;
    }
}