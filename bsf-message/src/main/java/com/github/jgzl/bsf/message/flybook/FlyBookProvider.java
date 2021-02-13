package com.github.jgzl.bsf.message.flybook;

import com.github.jgzl.bsf.core.http.DefaultHttpClient;
import com.github.jgzl.bsf.core.http.HttpClient;
import lombok.Data;
import org.apache.http.entity.ContentType;

import java.net.SocketTimeoutException;


/**
 * @author: huojuncheng
 * @version: 2020-08-25 15:10
 **/
@Data
public class FlyBookProvider {

    private String getUrl()
    {
        return FlyBookProperties.Domain+"{access_token}";
    }


    public  void send(String[] tokens, FlyBookBody content)  {
        HttpClient.Params params = HttpClient.Params.custom().setContentType(ContentType.APPLICATION_JSON).add(content.text).build();
        sendToken(tokens,params);
    }

    public void sendText(String[] tokens,String subject,String text){
        FlyBookBody.Text text1 = new FlyBookBody.Text();
        text1.setText(text);
        FlyBookBody flyBookBody = new FlyBookBody();
        flyBookBody.setText(text1);
        flyBookBody.setMsgtype("text");
        send(tokens,flyBookBody);
    }

    private void sendToken(String[] tokens,HttpClient.Params params)
    {
        if(tokens!=null) {
            for (String token : tokens) {
                try {
                    DefaultHttpClient.Default.post(getUrl().replace("{access_token}", token), params);
                }catch (Exception e){
                    if(e.getCause() instanceof SocketTimeoutException)
                    {
                        //网关不一定稳定
                        return;
                    }
                    throw e;
                }
            }
        }
    }

    @Data
    public static class FlyBookBody
    {
        @Data
        public static class MarkDown
        {
            private String title;
            private String text;
        }
        @Data
        public static class Text
        {
            private String text;
        }
        /**
         * "markdown","text"
         */
        private String msgtype="markdown";
        private MarkDown markdown;
        private Text text;
    }
}
