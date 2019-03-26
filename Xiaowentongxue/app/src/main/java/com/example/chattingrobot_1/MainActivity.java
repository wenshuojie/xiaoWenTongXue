package com.example.chattingrobot_1;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;


import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Random;
import android.util.Log;

import Speek.Speek;

public class MainActivity extends AppCompatActivity{


    private static Context context;

    private List<Msg> msgList = new ArrayList<>();

    private EditText inputText;

    private ImageButton sendRequest;

    private ImageButton play;

    private ImageButton luyin;

    //存放听写分析结果文本
    private HashMap<String, String> hashMapTexts = new LinkedHashMap<String, String>();

    SpeechRecognizer hearer;  //听写对象

    RecognizerDialog dialog;  //讯飞提示框

    private RecyclerView msgRecyclerView;

    private MsgAdapter adapter;

    private TextView textView;

    private Speek speek;

    private String voice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initMsgs();//初始化消息数据
        inputText = (EditText)findViewById(R.id.input_text);
        sendRequest = (ImageButton)findViewById(R.id.send_request);
        luyin = (ImageButton)findViewById(R.id.luyin);
        play = (ImageButton)findViewById(R.id.play);
        msgRecyclerView = (RecyclerView)findViewById(R.id.msg_recycler_view);
        textView = (TextView)findViewById(R.id.left_msg);
        speek = new Speek(this);//创建Speek对像

    }

    @Override
    protected void onResume() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        msgRecyclerView.setLayoutManager(layoutManager);
        adapter = new MsgAdapter(msgList);
        msgRecyclerView.setAdapter(adapter);
        sendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = inputText.getText().toString();
                Log.d("content", content);//ok
                String content_1 = content.replace(" ","");
                String content_2 = content_1.replace("\n","");
//                Log.d("content_2", content_2);//ok

                String message = null;
                try{
                    message = java.net.URLEncoder.encode(content_2, "UTF-8");
                }catch (Exception e){
                    e.printStackTrace();
                }

                String contentUrl = "http://www.tuling123.com/openapi/api?key=eb717ddaf771432f8ba93c2faeb6c1ba&info=" + message;
//                Log.d("contentUrl", contentUrl);//ok
                final String Url =contentUrl;
//                Log.d("URL", Url);//ok
                if (!"".equals(content)){
                    Msg msg = new Msg(content, Msg.TYPE_SENT);
                    msgList.add(msg);
                    //当有新消息时，刷新listview中的显示
                    adapter.notifyItemInserted(msgList.size() - 1);
                    //将ListView定位到最后一行
                    msgRecyclerView.scrollToPosition(msgList.size() - 1);
                    //清空输入框中的内容
                    inputText.setText("");
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        HttpURLConnection connection = null;
                        BufferedReader reader = null;
                        try{
                            URL url = new URL(Url);
                            Log.d("url:", url.toString());//ok
                            connection = (HttpURLConnection)url.openConnection();
                            InputStream in = connection.getInputStream();
//                            Log.d("in", in.toString());//ok
                            //下面对获取到的输入流进行读取
                            reader = new BufferedReader(new InputStreamReader(in));
//                            Log.d("reader:", reader.toString());//ok
                            StringBuilder response = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null){
                                response.append(line);
                            }
                            Log.d("response", response.toString());//ok
                            //按指定模式在字符串查找,只需要XXXX内容
                            //{"code":100000,"text":"XXXX"}
                            String str = response.toString();
                            String pattern = "\"(.*?)\"";//表示匹配所有字符串
                            Pattern r = Pattern.compile(pattern);
                            Matcher m = r.matcher(str);
                            int i=0;
                            while (m.find()){
//                                System.out.println(m.group(1));
                                str = m.group(0);
                            }
                            voice = str;
                            Log.d("voice",voice);

//                           Log.d("str", str);//ok
                            Msg msg1 = new Msg(str, Msg.TYPE_RECEIVED);
                            msgList.add(msg1);
                        }catch (Exception e){
                            e.printStackTrace();
                        }finally {
                            if (reader != null){
                                try {
                                    reader.close();
                                }catch (IOException e){
                                    e.printStackTrace();
                                }
                            }
                            if (connection != null){
                                connection.disconnect();
                            }
                        }
                    }
                }).start();//线程启动

            }
        });
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!voice.equals("")){
                    speek.Speeking(voice);
                }else {
                    speek.Speeking("小文同学无话可说");
                }
            }
        });
        super.onResume();
        luyin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 语音配置对象初始化
                SpeechUtility.createUtility(MainActivity.this, SpeechConstant.APPID + "=5c2df014");

                // 1.创建SpeechRecognizer对象，第2个参数：本地听写时传InitListener
                hearer = SpeechRecognizer.createRecognizer( MainActivity.this, null);
                // 交互动画
                dialog = new RecognizerDialog(MainActivity.this, null);
                // 2.设置听写参数，详见《科大讯飞MSC API手册(Android)》SpeechConstant类
                hearer.setParameter(SpeechConstant.DOMAIN, "iat"); // domain:域名
                hearer.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
                hearer.setParameter(SpeechConstant.ACCENT, "mandarin"); // mandarin:普通话

                //3.开始听写
                dialog.setListener(new RecognizerDialogListener() {  //设置对话框

                    @Override
                    public void onResult(RecognizerResult results, boolean isLast) {
                        // TODO 自动生成的方法存根
                        Log.d("Result", results.getResultString());
                        //(1) 解析 json 数据<< 一个一个分析文本 >>
                        StringBuffer strBuffer = new StringBuffer();
                        try {
                            JSONTokener tokener = new JSONTokener(results.getResultString());
                            Log.i("TAG", "Test"+results.getResultString());
                            Log.i("TAG", "Test"+results.toString());
                            JSONObject joResult = new JSONObject(tokener);

                            JSONArray words = joResult.getJSONArray("ws");
                            for (int i = 0; i < words.length(); i++) {
                                // 转写结果词，默认使用第一个结果
                                JSONArray items = words.getJSONObject(i).getJSONArray("cw");
                                JSONObject obj = items.getJSONObject(0);
                                strBuffer.append(obj.getString("w"));

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
//            		String text = strBuffer.toString();
                        // (2)读取json结果中的sn字段
                        String sn = null;

                        try {
                            JSONObject resultJson = new JSONObject(results.getResultString());
                            sn = resultJson.optString("sn");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        //(3) 解析语音文本<< 将文本叠加成语音分析结果  >>
                        hashMapTexts.put(sn, strBuffer.toString());
                        StringBuffer resultBuffer = new StringBuffer();  //最后结果
                        for (String key : hashMapTexts.keySet()) {
                            resultBuffer.append(hashMapTexts.get(key));
                        }

                        inputText.setText(resultBuffer.toString());
                        inputText.requestFocus();//获取焦点
                        inputText.setSelection(1);//将光标定位到文字最后，以便修改

                    }

                    @Override
                    public void onError(SpeechError error) {
                        // TODO 自动生成的方法存根
                        error.getPlainDescription(true);
                    }
                });

                dialog.show();  //显示对话框

            }
        });//录音按钮
    }

    private void initMsgs(){
        Msg msg1;
        int max=5;
        int min=1;
        Random random = new Random();
        int s = random.nextInt(max)%(max-min+1) + min;//生成指定随机数
        switch (s){
            case 1:
                msg1 = new Msg("小文同学在线答疑", Msg.TYPE_RECEIVED);
                msgList.add(msg1);
                break;
            case 2:
                 msg1 = new Msg("哟，人类你来了", Msg.TYPE_RECEIVED);
                msgList.add(msg1);
                break;
            case 3:
                 msg1 = new Msg("有什么问题快问吧，我不一定答得上来", Msg.TYPE_RECEIVED);
                msgList.add(msg1);
                break;
            case 4:
                 msg1 = new Msg("今天也是元气满满的一天", Msg.TYPE_RECEIVED);
                msgList.add(msg1);
                break;
            case 5:
                 msg1 = new Msg("##￥%*）&%￥@#", Msg.TYPE_RECEIVED);
                msgList.add(msg1);
                break;
        }

    }

}
