package dgb.daegubank;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

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
 * Created by machina on 30/10/2018.
 */

public class CheckPinActivity extends AppCompatActivity {
    private static final String SERVER_ADDRESS = "http://capstone.gonetis.com/daegubank/server.php";
    private HttpURLConnection serverConnection;

    private String merchantId;
    private String customerId;
    private String storeName;
    private String customerName;
    private String pinNumber;
    private int price;

    private EditText pinEdt;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pin_check);

        Intent fromTransactionIntent = new Intent(this.getIntent());
        storeName = fromTransactionIntent.getStringExtra("tr_store_name");
        customerName = fromTransactionIntent.getStringExtra("tr_customer_name");
        merchantId = fromTransactionIntent.getStringExtra("tr_merchant_id");
        customerId = fromTransactionIntent.getStringExtra("tr_customer_id");
        price = fromTransactionIntent.getIntExtra("tr_price", 0);

        pinEdt = (EditText)findViewById(R.id.inputPIN);
        pinEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                pinNumber = s.toString();

                try {
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");
                    String strPassword = pinNumber;
                    digest.update(strPassword.getBytes(Charset.forName("UTF-8")));
                    byte[] password = digest.digest();
                    StringBuffer buffer = new StringBuffer();
                    for(int i = 0; i < password.length; i++){
                        buffer.append(Integer.toString((password[i]&0xff) + 0x100, 1).substring(1));
                    }
                    pinNumber = buffer.toString();
                }
                catch(NoSuchAlgorithmException e) {
                    e.printStackTrace();
                    return;
                }
                sendTransactionInfo task = new sendTransactionInfo();
                task.execute(SERVER_ADDRESS, customerId, merchantId, String.valueOf(price), pinNumber);
            }
        });
    }

    class sendTransactionInfo extends AsyncTask<String, Void, String>{
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(getApplicationContext(), "거래 중...", null, true, true);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            progressDialog.dismiss();

            try{
                JSONObject result = new JSONObject(s);
                String respondMsg = result.getString("respond");
                String resultMsg = result.getString("message");

                if(respondMsg.equals("Success")){
                    Intent goResultIntent = new Intent(CheckPinActivity.this, TransactionResultActivity.class);
                    goResultIntent.putExtra("pin_storeName", storeName);
                    goResultIntent.putExtra("pin_price", price);
                    startActivity(goResultIntent);
                }else{

                }
            }catch (Exception e){
                e.printStackTrace();
            }

            finish();
            Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String strSender = (String)strings[1];
            String strReceiver = (String)strings[2];
            int money = Integer.parseInt((String)strings[3]);
            String strPinNumber = (String)strings[4];

            String severalURL = (String)strings[0];


            try{
                URL url = new URL(severalURL);

                serverConnection = (HttpURLConnection)url.openConnection();
                serverConnection.setRequestProperty("Content-Type", "application/json");

                serverConnection.setRequestMethod("POST");

                JSONObject requestObject = new JSONObject();
                requestObject.put("request", "transaction");
                requestObject.put("sender", strSender);
                requestObject.put("receiver", strReceiver);
                requestObject.put("pin_number", strPinNumber);
                requestObject.put("money", money);

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

                return sb.toString();
            }catch (Exception e){
                return new String("Error: " + e.getMessage());
            }
        }
    }
}
