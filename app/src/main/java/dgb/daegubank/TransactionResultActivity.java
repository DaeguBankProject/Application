package dgb.daegubank;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by machina on 30/10/2018.
 */

public class TransactionResultActivity extends AppCompatActivity{
    private TextView merchantTxt;
    private TextView priceTxt;
    private Button okBtn;

    private String merchantName;
    private int price;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaction_result);

        Intent fromCheckPinIntent = new Intent(this.getIntent());
        merchantName = fromCheckPinIntent.getStringExtra("storeName");
        price = fromCheckPinIntent.getIntExtra("price", 0);

        merchantTxt = (TextView)findViewById(R.id.result_merchantTxt);
        priceTxt = (TextView)findViewById(R.id.result_priceTxt);

        okBtn = (Button)findViewById(R.id.result_okBtn);

        merchantTxt.setText(merchantName);
        priceTxt.setText(price + "원");

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
