package dgb.daegubank;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.qrcode.QRCodeWriter;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by machina on 31/10/2018.
 */

public class QRcodeActivity extends AppCompatActivity {
    private static final String SERVER_ADDRESS = "http://capstone.gonetis.com/daegubank/server.php";
    private HttpURLConnection serverConnection;

    private String userId;
    private String userName;
    private String userType;
    private String userAccount;
    private String storeName;
    private String receiverId;

    private ImageView imgQRCode;

    private final int QR_CODE_WIDTH = 100;
    private final int QR_CODE_HEIGHT = 100;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_code);

        Intent fromLobbyIntent = new Intent(this.getIntent());
        userId = fromLobbyIntent.getStringExtra("transaction_user_id");
        userName = fromLobbyIntent.getStringExtra("transaction_user_name");
        userAccount = fromLobbyIntent.getStringExtra("transaction_user_account");
        userType = fromLobbyIntent.getStringExtra("transaction_user_type");

        if(userType.equals("Merchant")){
            storeName = fromLobbyIntent.getStringExtra("transaction_store_name");
        }

        imgQRCode = (ImageView)findViewById(R.id.imgQRCode);

        if(userType.equals("Merchant")){
            QRCodeWriter writer = new QRCodeWriter();
            try {
                BitMatrix matrix = writer.encode(userId, BarcodeFormat.QR_CODE, QR_CODE_WIDTH, QR_CODE_HEIGHT);
                Bitmap bitmap = Bitmap.createBitmap(matrix.getWidth(), matrix.getHeight(), Bitmap.Config.RGB_565);
                for (int i = 0; i < matrix.getWidth(); ++i) {
                    for (int j = 0; j < matrix.getHeight(); ++j) {
                        bitmap.setPixel(i, j, matrix.get(i, j) ? Color.BLACK : Color.WHITE);
                    }
                }
                imgQRCode.setImageBitmap(bitmap);

                saveStoreInfo();

            }
            catch (WriterException e) {
                e.printStackTrace();
            }
        }else{
            new IntentIntegrator(QRcodeActivity.this).initiateScan();
        }
    }

    public void saveStoreInfo(){
        String merchantId = userId;

        QRcode task = new QRcode();
        task.execute(SERVER_ADDRESS, merchantId);
    }

    public void searchStoreInfo(String result){
        String merchantId = result;

        QRcode task = new QRcode();
        task.execute(SERVER_ADDRESS, merchantId);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result == null) {
                Toast.makeText(this, "Canceled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                receiverId = result.getContents();
                searchStoreInfo(receiverId);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    class QRcode extends AsyncTask<String, Void, String>{
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(QRcodeActivity.this, "등록 중...", null, true, true);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            progressDialog.dismiss();

            try{
                JSONObject result = new JSONObject(s);
                String respondMsg = result.getString("respond");
                String resultMsg = result.getString("message");
                if(userType.equals("Customer")){
                    String store = result.getString("store_name");

                    Intent goTransaction = new Intent(QRcodeActivity.this, TransactionActivity.class);
                    goTransaction.putExtra("qr_merchant_id", receiverId);
                    goTransaction.putExtra("qr_customer_id", userId);
                    goTransaction.putExtra("qr_store_name", store);
                    goTransaction.putExtra("qr_customer_name", userName);
                    startActivity(goTransaction);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            String strID = (String)strings[1];

            String severalURL = (String)strings[0];


            try{
                URL url = new URL(severalURL);

                serverConnection = (HttpURLConnection)url.openConnection();
                serverConnection.setRequestProperty("Content-Type", "application/json");

                serverConnection.setRequestMethod("POST");

                JSONObject requestObject = new JSONObject();
                requestObject.put("request", "store");
                requestObject.put("id", strID);

                byte[] postDataBytes = requestObject.toString().getBytes("UTF-8");

                serverConnection.setDoOutput(true);
                serverConnection.getOutputStream().write(postDataBytes);

                int responseStatusCode = serverConnection.getResponseCode();

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK){
                    inputStream = serverConnection.getInputStream();
                }else{
                    inputStream = serverConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");

                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }

                bufferedReader.close();
                Log.e("Request", sb.toString());
                return sb.toString();
            }catch (Exception e){
                return new String("Error: " + e.getMessage());
            }
        }
    }

//    class QRScan extends AsyncTask<String, Void, String>{
//
//        ProgressDialog progressDialog;
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//
//            progressDialog = ProgressDialog.show(QRcodeActivity.this, "등록 중...", null, true, true);
//        }
//
//        @Override
//        protected void onPostExecute(String s) {
//            super.onPostExecute(s);
//
//            progressDialog.dismiss();
//
//            try{
//                JSONObject result = new JSONObject(s);
//                String respondMsg = result.getString("respond");
//                String resultMsg = result.getString("message");
//                String store = result.getString("store_name");
//
//                Intent goTransaction = new Intent(QRcodeActivity.this, TransactionActivity.class);
//                goTransaction.putExtra("qr_merchant_id", receiverId);
//                goTransaction.putExtra("qr_customer_id", userId);
//                goTransaction.putExtra("qr_store_name", store);
//                goTransaction.putExtra("qr_customer_name", userName);
//                startActivity(goTransaction);
//
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//        }
//
//        @Override
//        protected String doInBackground(String... strings) {
//            String strID = (String)strings[1];
//
//            String severalURL = (String)strings[0];
//
//
//            try{
//                URL url = new URL(severalURL);
//
//                serverConnection = (HttpURLConnection)url.openConnection();
//                serverConnection.setRequestProperty("Content-Type", "application/json");
//
//                serverConnection.setRequestMethod("POST");
//
//                JSONObject requestObject = new JSONObject();
//                requestObject.put("request", "store");
//                requestObject.put("id", strID);
//
//                byte[] postDataBytes = requestObject.toString().getBytes("UTF-8");
//
//                serverConnection.setDoOutput(true);
//                serverConnection.getOutputStream().write(postDataBytes);
//
//                int responseStatusCode = serverConnection.getResponseCode();
//
//                InputStream inputStream;
//                if(responseStatusCode == HttpURLConnection.HTTP_OK){
//                    inputStream = serverConnection.getInputStream();
//                }else{
//                    inputStream = serverConnection.getErrorStream();
//                }
//
//                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
//
//                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
//
//                StringBuilder sb = new StringBuilder();
//                String line = null;
//
//                while((line = bufferedReader.readLine()) != null){
//                    sb.append(line);
//                }
//
//                bufferedReader.close();
//                Log.e("Request", sb.toString());
//                return sb.toString();
//            }catch (Exception e){
//                return new String("Error: " + e.getMessage());
//            }
//        }
//    }
}
