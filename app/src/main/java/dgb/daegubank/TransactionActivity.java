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
    private TextView storeName;
    private EditText priceStr;

    private Button okBtn;
    private Button cancelBtn;

    private String merchantStr;
    private String customerStr;
    private int price;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaction);

        Intent fromQRIntent = new Intent(this.getIntent());
        merchantStr = fromQRIntent.getStringExtra("merchant");
        customerStr = fromQRIntent.getStringExtra("customer");

        storeName = (TextView)findViewById(R.id.storeName);
        priceStr = (EditText)findViewById(R.id.priceEdt);

        okBtn = (Button)findViewById(R.id.transactionOkBtn);
        cancelBtn = (Button)findViewById(R.id.transactionCancelBtn);

        storeName.setText(merchantStr);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                price = Integer.parseInt(priceStr.getText().toString());
                if(price > 0){
                    Intent goCheckPinIntent = new Intent(TransactionActivity.this, CheckPinActivity.class);
                    goCheckPinIntent.putExtra("merchant_name", merchantStr);
                    goCheckPinIntent.putExtra("customer_name", customerStr);
                    goCheckPinIntent.putExtra("price", price);
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
