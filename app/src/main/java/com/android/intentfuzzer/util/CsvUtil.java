package com.android.intentfuzzer.util;


import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangsj
 * @date 2021-08-16 17:00
 */
public class CsvUtil {

    /**
     * 读取csv文件内容
     * @param inputStream
     * @param code csv文件的编码,如utf8,,gbk
     *
     * @return 返回csv文件中的数据
     * @throws Exception
     */
    public static List<String[]> read(InputStream inputStream, String code) throws Exception{
        // 存储csv文件中的内容
        List<String[]> csvList = new ArrayList<String[]>();

        // 创建CsvReader
        CsvReader reader = new CsvReader(inputStream, ',', Charset.forName(code));

        // 逐行读入除表头的数据
        while(reader.readRecord()){
            csvList.add(reader.getValues());
        }

        // 释放资源
        reader.close();
        return csvList;
    }

    /**
     * 数据写入csv文件
     * @param list UTF-8编码写入csv文件的内容
//     * @param filePath 写入的csv文件的指定路劲
     *
     * @throws Exception
     */
    public static ByteArrayOutputStream write(List<String[]> list) throws Exception{
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CsvWriter wr = new CsvWriter(outputStream, ',', Charset.forName("UTF-8"));
        for (int i = 0; i < list.size(); i++) {
            wr.writeRecord(list.get(i));
        }
        wr.close();
        return outputStream;
    }
}
