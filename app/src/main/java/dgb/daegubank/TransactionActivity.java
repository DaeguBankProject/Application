package dgb.daegubank;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by machina on 30/10/2018.
 */

public class TransactionActivity extends AppCompatActivity {
    private TextView storeNameTxt;
    private EditText priceStr;

    private Button okBtn;
    private Button cancelBtn;

    private String storeName;
    private String customerName;
    private String merchantId;
    private String customerId;
    private int price;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaction);

        Intent fromQRIntent = new Intent(this.getIntent());
        storeName = fromQRIntent.getStringExtra("qr_store_name");
        customerName = fromQRIntent.getStringExtra("qr_customer_name");
        merchantId = fromQRIntent.getStringExtra("qr_merchant_id");
        customerId = fromQRIntent.getStringExtra("qr_customer_id");

        storeNameTxt = (TextView)findViewById(R.id.storeName);
        priceStr = (EditText)findViewById(R.id.priceEdt);

        okBtn = (Button)findViewById(R.id.transactionOkBtn);
        cancelBtn = (Button)findViewById(R.id.transactionCancelBtn);

        storeNameTxt.setText(storeName);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                price = Integer.parseInt(priceStr.getText().toString());
                if(price > 0){
                    Intent goCheckPinIntent = new Intent(TransactionActivity.this, CheckPinActivity.class);
                    goCheckPinIntent.putExtra("tr_store_name", storeName);
                    goCheckPinIntent.putExtra("tr_customer_name", customerName);
                    goCheckPinIntent.putExtra("tr_merchant_id", merchantId);
                    goCheckPinIntent.putExtra("tr_customer_id", customerId);
                    goCheckPinIntent.putExtra("tr_price", price);
                    startActivity(goCheckPinIntent);
                    finish();
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}
