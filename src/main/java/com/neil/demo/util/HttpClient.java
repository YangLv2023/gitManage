package com.neil.demo.util;

import com.alibaba.fastjson.JSON;
import okhttp3.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpClient {

    private final static Logger log = LoggerFactory.getLogger(HttpClient.class);

    public static final MediaType jsonType = MediaType.parse("application/json; charset=utf-8");

    public static final MediaType mediaType = MediaType.parse("application/octet-stream");

    public final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(8000, TimeUnit.MILLISECONDS)
            .readTimeout(8000, TimeUnit.MILLISECONDS)
            .build();

    public static void main(String[] args) {
        new HttpClient().getHttp();
    }

    public void getHttp(){
        while (true){
            Scanner scan = new Scanner(System.in);
            String str = scan.nextLine();
            String page = this.sendPostByForm(str,new HashMap<>());
            page = delHTMLTag(delHTMLTag(page));
            System.out.println(page);
        }
    }

    public static String stripHtml(String content) {
        // <p>段落替换为换行
        content = content.replaceAll("<p .*?>", "\r\n");
        // <br><br/>替换为换行
        content = content.replaceAll("<br\\s*/?>", "\r\t");
        // 去掉其它的<>之间的东西
        content = content.replaceAll("\\<.*?>", "");
        // 还原HTML
        // content = HTMLDecoder.decode(content);
        return content;
    }

    public static String delHTMLTag(String htmlStr){
        String regEx_script="<script[^>]*?>[\\s\\S]*?<\\/script>"; //定义script的正则表达式
        String regEx_style="<style[^>]*?>[\\s\\S]*?<\\/style>"; //定义style的正则表达式
        String regEx_html="<[^>]+>"; //定义HTML标签的正则表达式

        Pattern p_script=Pattern.compile(regEx_script,Pattern.CASE_INSENSITIVE);
        Matcher m_script=p_script.matcher(htmlStr);
        htmlStr=m_script.replaceAll(""); //过滤script标签

        Pattern p_style=Pattern.compile(regEx_style,Pattern.CASE_INSENSITIVE);
        Matcher m_style=p_style.matcher(htmlStr);
        htmlStr=m_style.replaceAll(""); //过滤style标签

        Pattern p_html=Pattern.compile(regEx_html,Pattern.CASE_INSENSITIVE);
        Matcher m_html=p_html.matcher(htmlStr);
        htmlStr=m_html.replaceAll(""); //过滤html标签

        return htmlStr.trim(); //返回文本字符串
    }

    /**
     * 发送post请求通过Form表单形式
     *
     * @param reqUrl 请求url
     * @param mapParam 请求参数
     *
     */
    private String sendPostByForm(String reqUrl, Map<String,String> mapParam){
        try {
            long startTime = System.currentTimeMillis();
            //循环form表单，将表单内容添加到form builder中
            FormBody.Builder formBody = new FormBody.Builder();
            for (Map.Entry<String, String> m : mapParam.entrySet()) {
                String name = m.getKey();
                String value = m.getValue()+"";
                formBody.add(name, value);
            }
            //构建formBody(formBody.build())，将其传入Request请求中
            Request.Builder builder = new Request.Builder().url(reqUrl).addHeader("Origin","https://www.toutiao.com")
                    .addHeader("sec-ch-ua","\" Not;A Brand\";v=\"99\", \"Google Chrome\";v=\"91\", \"Chromium\";v=\"91\"")
                    .addHeader("sec-ch-ua-mobile","?0")
                    .addHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36")
                    .post(formBody.build());
            try(Response response = okHttpClient.newCall(builder.build()).execute()){
                String body = response.body().string();
                /*log.info("{} response body:{}", reqUrl.substring(reqUrl.lastIndexOf("/") + 1),
                        body);*/
                return body;
            }catch(Exception e){
                log.error("调用接口出错\n"+ ExceptionUtils.getMessage(e));
            }finally{
                long endTime = System.currentTimeMillis();
                log.info("{} cost time:{}", reqUrl.substring(reqUrl.lastIndexOf("/") + 1),
                        (endTime - startTime));
            }
        } catch (Exception e) {
            log.error("error", e);
        }
        return null;
    }

    /**
     * 发送post请求通过JSON参数
     *
     * @param reqUrl 请求url
     * @param param 请求参数
     *
     */
    public String sendPostByJson(String reqUrl, Object param){
        try {
            String paramStr = JSON.toJSONString(param);

            RequestBody requestBody = RequestBody.create(jsonType, paramStr);
            long startTime = System.currentTimeMillis();
            Request.Builder builder = new Request.Builder().url(reqUrl).post(requestBody);

            try(Response response = okHttpClient.newCall(builder.build()).execute()){
                String body = response.body().string();
                /*log.info("{} response body:{}", reqUrl.substring(reqUrl.lastIndexOf("/") + 1),
                        body);*/
                return body;
            }catch(Exception e){
                log.error("调用接口出错\n"+ ExceptionUtils.getMessage(e));
            }finally{
                long endTime = System.currentTimeMillis();
                log.info("{} cost time:{}", reqUrl.substring(reqUrl.lastIndexOf("/") + 1),
                        (endTime - startTime));
            }
        } catch (Exception e) {
            log.error("error", e);
        }
        return null;
    }
}
