package dgb.daegubank;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.qrcode.QRCodeWriter;

public class MainActivity extends AppCompatActivity {
    private Button btnCreateQR, btnReadQR;
    private EditText txtContent;
    private ImageView imgQRCode;

    private final int QR_CODE_WIDTH = 100;
    private final int QR_CODE_HEIGHT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCreateQR = (Button)findViewById(R.id.btnCreateQR);
        btnReadQR = (Button)findViewById(R.id.btnReadQR);
        txtContent = (EditText)findViewById(R.id.txtContent);
        imgQRCode = (ImageView)findViewById(R.id.imgQRCode);

        // Create Button Event
        btnCreateQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                QRCodeWriter writer = new QRCodeWriter();
                try {
                    BitMatrix matrix = writer.encode(txtContent.getText().toString(), BarcodeFormat.QR_CODE, QR_CODE_WIDTH, QR_CODE_HEIGHT);
                    Bitmap bitmap = Bitmap.createBitmap(matrix.getWidth(), matrix.getHeight(), Bitmap.Config.RGB_565);
                    for (int i = 0; i < matrix.getWidth(); ++i) {
                        for (int j = 0; j < matrix.getHeight(); ++j) {
                            bitmap.setPixel(i, j, matrix.get(i, j) ? Color.BLACK : Color.WHITE);
                        }
                    }
                    imgQRCode.setImageBitmap(bitmap);
                }
                catch (WriterException e) {
                    e.printStackTrace();
                }
            }
        });

        // Read Button Event
        btnReadQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new IntentIntegrator(MainActivity.this).initiateScan();
            }
        });
    }

    // After QR Code Read
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
