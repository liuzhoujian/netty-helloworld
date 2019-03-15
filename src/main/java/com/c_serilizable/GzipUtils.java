package com.c_serilizable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 压缩、解压工具
 */
public class GzipUtils {
    /**
     * 解压缩
     * @param source 源数据
     * @return 解压后恢复的数据
     * @throws IOException
     */
    public static byte[] unzip(byte[] source) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(source);
        GZIPInputStream zipIn = new GZIPInputStream(in);
        byte[] temp = new byte[256];
        int length = 0;
        while ((length = zipIn.read(temp, 0, temp.length)) != -1) {
            out.write(temp, 0, length);
        }

        byte[] target = out.toByteArray();

        zipIn.close();
        out.close();

        return target;
    }

    /**
     * 解压缩
     * @param source 源数据
     * @return 压缩后的数据
     * @throws Exception
     */
    public static byte[] zip(byte[] source) throws Exception{
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream zipOut = new GZIPOutputStream(out);
        zipOut.write(source);
        zipOut.finish();
        byte[] target = out.toByteArray();

        zipOut.close();
        return target;
    }
}
