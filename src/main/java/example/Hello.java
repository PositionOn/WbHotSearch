package example;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

public class Hello {
    public Hello() throws UnsupportedEncodingException {
    }

//    public String mainHandler(KeyValueClass kv) {
////        System.out.println(String.format("key1 = %s", kv.getKey1()));
////        System.out.println(String.format("key2 = %s", kv.getKey2()));
//        Date date2 = DateUtil.date(Calendar.getInstance());
//        System.out.println("当前时间:"+ date2);
//        return String.format("Hello Yangkeke");
//    }

    //前10纯文本
    //后面所有热搜
    public String wbHotSearch(KeyValueClass kv) throws UnsupportedEncodingException {
        StringBuilder top10Message = new StringBuilder();
        StringBuilder allMessage  = new StringBuilder();
        String jsonResult = HttpUtil.createGet("https://weibo.com/ajax/statuses/hot_band").execute().charset("utf-8").body();
        JSONObject jsonObject = JSON.parseObject(jsonResult);
        Integer code = jsonObject.getInteger("http_code");
        if (code == null || code != 200){
            return jsonResult;
        }
        int j = 1;
        JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("band_list");
        for (int i=0;i<jsonArray.size();i++){
            JSONObject data = jsonArray.getJSONObject(i);
            String title = data.getString("word");
            String icon =data.getString("icon_desc");
            String raw_hot = data.getString("raw_hot");
            if (StringUtils.isBlank(raw_hot) || "null".equals(raw_hot)){
                raw_hot = "-";
            }
            String searchUrl = "https://s.weibo.com/weibo?q=%23"+URLEncoder.encode(title,"utf-8")+"%23";
            if (icon == null){
                icon = "-";
            }
            //top 10热搜
            if (i < 10){
                top10Message.append(j).append(".").append(title).append(" ").append("[").append(icon).append("]").append("\n");
            }
            allMessage.append(j).append(".").append(title).append(" ").append("(").append(raw_hot).append(")").append("[查看](").append(searchUrl).append(")").append("\n");
            j++;
        }
//        System.out.println(top10Message);
//        System.out.println(allMessage);
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("HH");
        SimpleDateFormat md = new SimpleDateFormat("MMdd");
        String str = df.format(date);
        String mmddStr = md.format(date);

        String historyToday = "### [历史上的今天(点我查看)](https://api.comm.miui.com/calendar-history-today/index.html?date="+mmddStr+")";

        Map<String, Object> param = new HashMap<>();
        String head = "---";
        param.put("key","542b8e80c78f46f48ce923d520b8588f");
        param.put("head","实时推送微博热点");


        int time = Integer.parseInt(str);
        //12点才推送的图片早报
        if(time <= 12){
            String pictureUrl = HttpUtil.createGet("http://dwz.2xb.cn/zaob").execute().charset("utf-8").body();
            JSONObject pictureJson = JSON.parseObject(pictureUrl);
            String purl = pictureJson.getString("imageUrl");
            String msg1 = "### [每天60秒读懂世界(点我查看)]("+purl+")";
            param.put("body", top10Message + "\n" +head+ "\n"+msg1+"\n"+historyToday+"\n"+allMessage);
        } else {
            param.put("body", top10Message + "\n" +head+ "\n"+historyToday+"\n" + allMessage);
        }

            String url = "http://push.ijingniu.cn/send";
            String result = HttpUtil.post(url,param);
//
        System.out.println(result);

        return result;
    }
}
