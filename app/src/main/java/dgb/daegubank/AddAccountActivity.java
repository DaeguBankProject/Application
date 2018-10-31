package dgb.daegubank;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

public class AddAccountActivity extends AppCompatActivity{
    private static final String SERVER_ADDRESS = "http://capstone.gonetis.com/daegubank/server.php";
    private HttpURLConnection serverConnection;

    private EditText editAccount;
    private EditText editPin;
    private EditText editPinCheck;

    private Button okBtn;
    private Button cancelBtn;

    private String userId;
    private String userAccount;
    private String pinNumber;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_account);

        Intent fromLobby = new Intent(this.getIntent());
        userId = fromLobby.getStringExtra("userId");

        editAccount = (EditText)findViewById(R.id.editAccount);
        editPin = (EditText)findViewById(R.id.editPin);
        editPinCheck = (EditText)findViewById(R.id.editPinCheck);

        okBtn = (Button)findViewById(R.id.okBtn);
        cancelBtn = (Button)findViewById(R.id.accountCancelBtn);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String idStr = userId;
                String accountStr = editAccount.getText().toString();
                String pinStr = editPin.getText().toString();
                String pinStrCheck = editPinCheck.getText().toString();

                if(!accountStr.equals("") && !pinStr.equals("") && pinStr.equals(pinStrCheck)){
                    try {
                        MessageDigest digest = MessageDigest.getInstance("SHA-256");
                        String strPassword = editPin.getText().toString();
                        digest.update(strPassword.getBytes(Charset.forName("UTF-8")));
                        byte[] password = digest.digest();
                        StringBuffer buffer = new StringBuffer();
                        for(int i = 0; i < password.length; i++){
                            buffer.append(Integer.toString((password[i]&0xff) + 0x100, 1).substring(1));
                        }
                        pinStr = buffer.toString();
                    }
                    catch(NoSuchAlgorithmException e) {
                        e.printStackTrace();
                        return;
                    }
                    AccountInfo task = new AccountInfo();
                    task.execute(SERVER_ADDRESS, idStr, accountStr, pinStr);
                }else{
                    Toast.makeText(getApplicationContext(), "PIN번호가 일치하지 않습니다.", Toast.LENGTH_SHORT);
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
            }
        });
    }

    class AccountInfo extends AsyncTask<String, Void, String>{
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(AddAccountActivity.this, "등록 중..", null, true, true);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();

            try{
                JSONObject result = new JSONObject(s);
                String respondMsg = result.getString("respond"); //"Error" or "Success"

                if(respondMsg.equals("Success")){
                    userAccount = editAccount.getText().toString();
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("account", userAccount);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();

                }else{
                    Toast.makeText(getApplicationContext(), result.getString("message"), Toast.LENGTH_LONG).show();
                }

            }catch (Exception e){
                e.printStackTrace();
            }

        }

        @Override
        protected String doInBackground(String... strings) {
            String strID = (String)strings[1];
            String strAccount = (String)strings[2];
            String strPIN = (String)strings[3];

            String severalURL = (String)strings[0];


            try{
                URL url = new URL(severalURL);

                serverConnection = (HttpURLConnection)url.openConnection();
                serverConnection.setRequestProperty("Content-Type", "application/json");

                serverConnection.setRequestMethod("POST");

                JSONObject requestObject = new JSONObject();
                requestObject.put("request", "account_associate");
                requestObject.put("id", strID);
                requestObject.put("account", strAccount);
                requestObject.put("pin_number", strPIN);

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
}
