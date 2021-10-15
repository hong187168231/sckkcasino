package com.qianyi.modulecommon;

/**
 * 正则表达式汇总
 */
public enum RegexEnum {

    NAME("^[\\u0391-\\uFFE5a-zA-Z·&\\\\s]{1,20}+$","姓名","长度限制1~20位,并且只能输入中英文");

    private String regex;

    private String name;

    private String desc;

    RegexEnum(String regex, String name, String desc) {
        this.regex = regex;
        this.name = name;
        this.desc = desc;
    }

    public String getRegex() {
        return regex;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }
}
