package Speek;

import android.app.Activity;
import android.util.Log;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;



public class Speek {
    protected SpeechSynthesizer mTts;

    private Activity activity;

    private static final String TAG = "Speek";

    public Speek(Activity activity){
        this.activity=activity;
        initTTs();
    }
    public void Speeking(String text)
    {
        //3.开始合成
        mTts.startSpeaking(text, null);
    }
    public void initTTs()
    {

        // 初始化SDK
        SpeechUtility.createUtility(activity, SpeechConstant.APPID +"=5c2df014");
        //创建 SpeechSynthesizer 对象, 第二个参数： 本地合成时传 InitListener
         mTts= SpeechSynthesizer.createSynthesizer(activity, null);
        //设置发音人
        mTts.setParameter(SpeechConstant.VOICE_NAME, "vixy"); //设置发音人
        mTts.setParameter(SpeechConstant.SPEED, "45");//设置语速
        mTts.setParameter(SpeechConstant.VOLUME, "80");//设置音量，范围 0~100
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //设置云端
        //设置合成音频保存位置（可自定义保存位置），保存在“./sdcard/iflytek.pcm”
        //保存在 SD 卡需要在 AndroidManifest.xml 添加写 SD 卡权限
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, "./sdcard/iflytek.pcm");

    }
    private void print(String message) {
        Log.i(TAG, message);
    }
    private void checkResult(int result, String method) {
        if (result != 0) {
            print("error code :" + result + " method:" + method + ", 错误码文档:http://yuyin.baidu.com/docs/tts/122 ");
        }
    }
    public void Destory(){
        if (mTts != null){
            mTts.stopSpeaking();
            mTts.destroy();
            mTts = null;
            print("释放资源成功");
        }
    }

}
