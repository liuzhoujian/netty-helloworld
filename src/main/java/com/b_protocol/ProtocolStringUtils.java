package com.b_protocol;


/**
 * 特定格式字符串解析
 */
public class ProtocolStringUtils {
    /**
     * 解析自定义的消息格式，取出内容信息
     * @param message  HEADcontent-length:xxxHEADBODYxxxxxxxxxxxBODY
     * @return
     */
    public static String parse(String message) {
        String[] split = message.split("HEADBODY");
        int contentLength = Integer.parseInt(split[0].split(":")[1]);
        String content = split[1].substring(0, split[1].length() - 4);

        if(contentLength != content.length()) {
            return null;
        }

        return content;
    }

    /**
     * 将字符串组装成自定义的消息格式
     * @param message
     * @return
     */
    public static String transferTo(String message) {
        StringBuilder sb = new StringBuilder();
        sb.append("HEADcontent-length:")
          .append(message.length())
          .append("HEADBODY")
          .append(message)
          .append("BODY");

        return sb.toString();
    }
}
