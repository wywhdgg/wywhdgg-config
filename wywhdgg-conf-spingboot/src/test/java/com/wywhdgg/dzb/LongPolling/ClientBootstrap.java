package com.wywhdgg.dzb.LongPolling;

import com.wywhdgg.dzb.core.util.HttpUtil;
import com.wywhdgg.dzb.core.vo.ConfParamVO;
import com.wywhdgg.dzb.enums.OperateEnums;
import com.wywhdgg.dzb.util.json.BasicJson;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/***
 *@author dzb
 *@date 2019/7/28 10:15
 *@Description:
 *@version 1.0

Long Polling的实现很简单，可分为四个过程：
发起Polling
发起Polling很简单，只需向服务器发起请求，此时服务端还未应答，所以客户端与服务端之间一直处于连接状态。
数据推送
如果服务器端有相关数据，此时服务端会将数据通过此前建立的通道发回客户端。
Polling终止
Polling终止情况有三种：
若服务端返回相关数据，此时客户端收到数据后，关闭请求连接，结束此次Polling过程。
若客户端等待设定的超时时间后，服务端依然没有返回数据，此时客户端需要主动终止此次Polling请求。
若客户端收到网络故障或异常，此时客户端自然也是需要主动终止此次Polling请求。
重新Polling
终止上次Polling后，客户端需要立即再次发起Polling请求。这样才能保证拉取数据的及时性。

 *  客户端
 */
@Slf4j
public class ClientBootstrap {
    public static final String url = "http://localhost:8080/wywhdgg-conf-admin/conf/find";

    public static void main(String[] args) {
        ConfParamVO paramVO = new ConfParamVO();
        paramVO.setAccessToken("123456");
        paramVO.setEnv("test");
        List list = new ArrayList(1);
        list.add("default.key01");
        paramVO.setKeys(list);
        String requestBody = BasicJson.toJson(paramVO);
        int i = 0;
       while (true) {
            //log.info("第" + (++i) + "次 longpolling");
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                // connection
                URL realUrl = new URL(url);
                connection = (HttpURLConnection) realUrl.openConnection();
                connection.setRequestMethod(OperateEnums.POST.getValue());
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setUseCaches(false);
                connection.setReadTimeout(5 * 1000);
                connection.setConnectTimeout(3 * 1000);
                connection.setRequestProperty("connection", "Keep-Alive");
                connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                connection.setRequestProperty("Accept-Charset", "application/json;charset=UTF-8");
                connection.connect();

                // write requestBody
                DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
                dataOutputStream.writeBytes(requestBody);
                dataOutputStream.flush();
                dataOutputStream.close();
                int statusCode = connection.getResponseCode();
                if (statusCode != 200) {
                    throw new RuntimeException("http request StatusCode(" + statusCode + ") invalid. for url : " + url);
                }
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                log.info("result={}", result);
            } catch (IOException e) {
                log.error("error:", e);
            } finally {
                close(reader,connection);
            }
        }
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
