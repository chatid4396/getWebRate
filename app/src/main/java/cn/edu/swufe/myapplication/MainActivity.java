package cn.edu.swufe.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements Runnable {
    private final String TAG = "Rate";
    private float dollarRate = 0.1f;
    private float euroRate = 0.2f;
    private float wonRate = 0.3f;
    private Button btn_doller;
    private Button btn_euro;
    private Button btn_won;
    private Button btn_config;
    private EditText et_rmb;
    private TextView tv_money;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_doller = findViewById(R.id.btn_doller);
        btn_won = findViewById(R.id.btn_won);
        btn_euro = findViewById(R.id.btn_euor);
        btn_config = findViewById(R.id.btn_config);
        et_rmb = findViewById(R.id.et_rmb);
        tv_money = findViewById(R.id.tv_money);
/*
        SharedPreferences shared = getSharedPreferences("myrate",this.MODE_PRIVATE);

        dollarRate =  shared.getFloat("dollar_rate",0.0f);
        euroRate =  shared.getFloat("euro_rate",0.0f);
        wonRate =  shared.getFloat("won_rate",0.0f);
        Log.i(TAG, "onCreate: sp dollarRate=" + dollarRate);
        Log.i(TAG, "onCreate: sp euroRate=" + euroRate);
        Log.i(TAG, "onCreate: sp wonRate=" + wonRate);
*/
        //开启子线程
        Thread t= new Thread(this);
        t.start();
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == 5){
     //               String str = (String)msg.obj;
     //               Log.i(TAG,"handlerMessage:getMessage = "+str);
                    List<Float> list = (List<Float>) msg.obj;
                    dollarRate=list.get(0);
                    euroRate=list.get(1);
                    wonRate=list.get(2);
                }
                super.handleMessage(msg);
            }
        };

    }
    public void calculate(View view){
        double rmb = 0.0;
        try{
            rmb = Double.parseDouble(et_rmb.getText().toString());
            if(view == btn_doller){
                tv_money.setText(String.format("%.2f",rmb*dollarRate));
            }else if(view == btn_euro){
                tv_money.setText(String.format("%.2f",rmb*euroRate));
            }else {
                tv_money.setText(String.format("%.2f",rmb*wonRate));
            }
        }
        catch (Exception e){
            Toast.makeText(this, "输入错误，请重新输入", Toast.LENGTH_SHORT).show();
        }
    }
    public void openOne(View view){
        config();
    }
    public void config(){
        Intent intent = new Intent(this,ConfigActivity.class);
        intent.putExtra("doller_rate_key",dollarRate);
        intent.putExtra("euro_rate_key",euroRate);
        intent.putExtra("won_rate_key",wonRate);
        Log.i(TAG, "openOne: dollarRate=" + dollarRate);
        Log.i(TAG, "openOne: euroRate=" + euroRate);
        Log.i(TAG, "openOne: wonRate=" + wonRate);
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==1 && resultCode==2){
            Bundle bundle = data.getExtras();
            dollarRate = bundle.getFloat("key_dollar",0.1f);
            euroRate = bundle.getFloat("key_euro",0.1f);
            wonRate = bundle.getFloat("key_won",0.1f);

            SharedPreferences shared = getSharedPreferences("myrate",this.MODE_PRIVATE);
            SharedPreferences.Editor editor = shared.edit();
            editor.putFloat("dollar_rate",dollarRate);
            editor.putFloat("euro_rate",euroRate);
            editor.putFloat("won_rate",wonRate);
            editor.commit();
            Log.i(TAG, "onActivityResult: 数据已保存到sharedPreferences");
        }
        super.onActivityResult(requestCode, resultCode, data);


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.rate,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.menu_set){
            //点击后的事件处理，可填入打开配置汇率页⾯的代码
            config();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void run() {
        Log.i(TAG,"run run().......");


        //获取msg对象用于返回主线程
        Message msg = handler.obtainMessage();
        msg.what = 5;
   //     msg.obj = "Hello from run()";
   //     handler.sendMessage(msg);


        //获取网络数据
        URL url = null;
        try {
            url = new URL("http://www.usd-cny.com/icbc.htm");
    //        HttpURLConnection http = (HttpURLConnection)url.openConnection();
    //        InputStream stream = http.getInputStream();
    //        String html = inputStream2String(stream);
    //        Log.i(TAG,html);
            List<Float> rate = new ArrayList<Float>();
            rate.add(getWebRate("美元",url));
            rate.add(getWebRate("欧元",url));
            rate.add(getWebRate("韩国元",url));
            //将获取到的数据传回主线程
            msg.obj = rate;
            handler.sendMessage(msg);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public float getWebRate(String name,URL url) throws IOException{
        HttpURLConnection http = (HttpURLConnection)url.openConnection();
        InputStream stream = http.getInputStream();
        String str = inputStream2String(stream);
        Document doc = Jsoup.parse(str);
        Elements tr = doc.select("tr");
        //	System.out.println(td);
        for(Element e:tr){
            Elements td = e.getElementsByTag("td");
            if(td.get(0).text().equals(name)){
                return 100/Float.parseFloat(td.get(2).text());

            }
        }
        return 0.0f;

    }
    public String inputStream2String(InputStream inputStream) throws IOException {
        final int bufferSize = 1024;
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();
        Reader in = new InputStreamReader(inputStream, "gb2312");
        for(; ;){
            int rsz = in.read(buffer,0,buffer.length);
            if(rsz < 0)
                break;
            out.append(buffer,0,rsz);
        }
        return out.toString();
    }
}
