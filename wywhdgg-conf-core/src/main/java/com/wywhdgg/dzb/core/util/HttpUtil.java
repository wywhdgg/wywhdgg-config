package com.wywhdgg.dzb.core.util;

import com.wywhdgg.dzb.enums.OperateEnums;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: dongzhb
 * @date: 2019/7/24
 * @Description: http链接
 */
@Slf4j
public class HttpUtil {
    /**
     * post
     *
     * @param timeout 秒
     */
    public static String postBody(String url, String requestBody, int timeout) {
        HttpURLConnection connection = null;
        BufferedReader bufferedReader = null;
        try {
            // connection
            URL realUrl = new URL(url);
            connection = (HttpURLConnection) realUrl.openConnection();

            // connection setting
            connection.setRequestMethod(OperateEnums.POST.getValue());
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setReadTimeout(timeout * 1000);
            connection.setConnectTimeout(3 * 1000);
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setRequestProperty("Accept-Charset", "application/json;charset=UTF-8");

            // do connection
            connection.connect();

            // write requestBody
            DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
            dataOutputStream.writeBytes(requestBody);
            dataOutputStream.flush();
            dataOutputStream.close();

            /*byte[] requestBodyBytes = requestBody.getBytes("UTF-8");
            connection.setRequestProperty("Content-Length", String.valueOf(requestBodyBytes.length));
            OutputStream outwritestream = connection.getOutputStream();
            outwritestream.write(requestBodyBytes);
            outwritestream.flush();
            outwritestream.close();*/

            // valid StatusCode
            int statusCode = connection.getResponseCode();
            if (statusCode != 200) {
                throw new RuntimeException("http request StatusCode(" + statusCode + ") invalid. for url : " + url);
            }

            // result
            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            close(bufferedReader,connection);
        }
        return null;
    }

    /**
     * get
     *
     * @param timeout second
     */
    public static String get(String url, int timeout) {
        HttpURLConnection connection = null;
        BufferedReader bufferedReader = null;
        try {
            // connection
            URL realUrl = new URL(url);
            connection = (HttpURLConnection) realUrl.openConnection();

            // connection setting
            connection.setRequestMethod(OperateEnums.GET.getValue());
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setReadTimeout(timeout * 1000);
            connection.setConnectTimeout(3 * 1000);
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setRequestProperty("Accept-Charset", "application/json;charset=UTF-8");
            // do connection
            connection.connect();
            //Map<String, List<String>> map = connection.getHeaderFields();
            // valid StatusCode
            int statusCode = connection.getResponseCode();
            if (statusCode != 200) {
                throw new RuntimeException("Http Request StatusCode(" + statusCode + ") Invalid.");
            }
            // result
            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            close(bufferedReader,connection);
        }
        return null;
    }

   /**
    * 关闭流操作
    * */
    private static void close(BufferedReader bufferedReader,HttpURLConnection connection){
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
