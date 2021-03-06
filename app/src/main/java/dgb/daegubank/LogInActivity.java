package dgb.daegubank;

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
 * Created by machina on 28/10/2018.
 */

public class LogInActivity extends AppCompatActivity{
    private Button signUpButton;
    private Button signInButton;

    private EditText eIdText;
    private EditText ePwdText;

    private static final String SERVER_ADDRESS = "http://capstone.gonetis.com/daegubank/server.php";
    private HttpURLConnection serverConnection;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_in);

        signInButton = (Button)findViewById(R.id.SignInBtn);
        signUpButton = (Button)findViewById(R.id.SignUpBtn);

        eIdText = (EditText)findViewById(R.id.editId);
        ePwdText = (EditText)findViewById(R.id.editPwd);

        signUpButton.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent signUpIntent = new Intent(LogInActivity.this, SignUpActivity.class);
                startActivity(signUpIntent);
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String idStr = eIdText.getText().toString();
                String pwdStr;
                try {
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");
                    String strPassword = ePwdText.getText().toString();
                    digest.update(strPassword.getBytes(Charset.forName("UTF-8")));
                    byte[] password = digest.digest();
                    StringBuffer buffer = new StringBuffer();
                    for(int i = 0; i < password.length; i++){
                        buffer.append(Integer.toString((password[i]&0xff) + 0x100, 1).substring(1));
                    }
                    pwdStr = buffer.toString();
                }
                catch(NoSuchAlgorithmException e) {
                    e.printStackTrace();
                    return;
                }

                sendSignInInfo task = new sendSignInInfo();
                task.execute(SERVER_ADDRESS, idStr, pwdStr);
            }
        });
    }

    class sendSignInInfo extends AsyncTask<String, Void, String>{
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(LogInActivity.this, "접속 중..", null, true, true);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();

            try{
                JSONObject result = new JSONObject(s);
                String respondMsg = result.getString("respond"); //"Error" or "Success"

                if(respondMsg.equals("Success")){
                    String userId = result.getString("id"); //User's ID
                    String userName = result.getString("name"); // User's name
                    String userType = result.getString("type"); // User's type (상인 or 고객)
                    String userAccount = result.getString("account"); // User's bank account

                    Intent goLobbyIntent = new Intent(LogInActivity.this, UserLobbyActivity.class);
                    goLobbyIntent.putExtra("user_id", userId);
                    goLobbyIntent.putExtra("user_name", userName);
                    goLobbyIntent.putExtra("user_type", userType);
                    goLobbyIntent.putExtra("user_account", userAccount);
//                    if(userType.equals("Merchant")){
//                        String storeName = result.getString("store_name");
//                        goLobbyIntent.putExtra("store_name", storeName);
//                    }
                    startActivity(goLobbyIntent);
                    finish();
                }else{
                    Toast.makeText(LogInActivity.this, result.getString("message"), Toast.LENGTH_LONG).show();
                }

                Toast.makeText(LogInActivity.this, respondMsg, Toast.LENGTH_LONG).show();

            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            String strID = (String)strings[1];
            String strPWD = (String)strings[2];
            String severalURL = (String)strings[0];

            try{
                URL url = new URL(severalURL);

                serverConnection = (HttpURLConnection)url.openConnection();
                serverConnection.setRequestProperty("Content-Type", "application/json");

                serverConnection.setRequestMethod("POST");

                JSONObject requestObject = new JSONObject();
                requestObject.put("request", "sign_in");
                requestObject.put("id", strID);
                requestObject.put("password", strPWD);

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

    /*
        HttpURLConnection serverConnection;
        serverConnection = .........;
        JSONObject requestObject = new JSONObject();
        requestObject.put("type", "sign_in");
        requestObject.put("id", $(DATA));
        ...
        byte[] postDataBytes = requestObject.toString().getBytes(NetworkInterface.ENCODE);
        ...
        serverConnection.setRequestMethod("POST");
        serverConnection.setRequestProperty("Content-Type", "application/json");
        serverConnection.SetDoOutput(true);
        serverConnection.getOutputStream().write(postDataBytes);
    */
}
