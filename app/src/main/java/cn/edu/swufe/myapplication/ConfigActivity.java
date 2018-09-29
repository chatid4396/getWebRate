package cn.edu.swufe.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class ConfigActivity extends Activity {
    private final String TAG = "ConfigActivity";
    EditText dollarText;
    EditText euroText;
    EditText wonText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        Intent intent = getIntent();
        float dollar2 = intent.getFloatExtra("doller_rate_key",0.0f);
        float euro2 = intent.getFloatExtra("euro_rate_key",0.0f);
        float won2 = intent.getFloatExtra("won_rate_key",0.0f);
        Log.i(TAG, "onCreate: dollar2=" + dollar2);
        Log.i(TAG, "onCreate: euro2=" + euro2);
        Log.i(TAG, "onCreate: won2=" + won2);

        dollarText = findViewById(R.id.dollar_rate);
        euroText = findViewById(R.id.euro_rate);
        wonText = findViewById(R.id.won_rate);

        dollarText.setText(String.valueOf(dollar2));
        euroText.setText(String.valueOf(euro2));
        wonText.setText(String.valueOf(won2));

    }

    public void save(View view){
        Log.i(TAG, "save: ");
        float newDollar = Float.parseFloat(dollarText.getText().toString());
        float newEuro = Float.parseFloat(euroText.getText().toString());
        float newWon = Float.parseFloat(wonText.getText().toString());
        Log.i(TAG, "save: 获取到新的值");
        Log.i(TAG, "save: newDollar=" + newDollar);
        Log.i(TAG, "save: newEuroi=" + newEuro);
        Log.i(TAG, "save: newWon=" + newWon);

        Intent intent = getIntent();
        Bundle db = new Bundle();
        db.putFloat("key_dollar",newDollar);
        db.putFloat("key_euro",newEuro);
        db.putFloat("key_won",newWon);
        intent.putExtras(db);
        setResult(2,intent);

        finish();
    }
}
