package dgb.daegubank;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

public class SignUpActivity extends AppCompatActivity{
    private EditText editID;
    private EditText editPWD;
    private EditText editPWDcheck;
    private EditText editName;
    private Spinner userTypeSpinner;

    private Button completeButton;
    private Button cancelButton;

    private static final String SERVER_ADDRESS = "http://capstone.gonetis.com/daegubank/server.php";
    private HttpURLConnection serverConnection;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        editID = (EditText)findViewById(R.id.inputID);
        editPWD = (EditText)findViewById(R.id.inputPwd);
        editPWDcheck = (EditText)findViewById(R.id.inputPwdCheck);
        editName = (EditText)findViewById(R.id.inputName);

        completeButton  = (Button)findViewById(R.id.completeBtn);
        cancelButton = (Button)findViewById(R.id.cancelBtn);

        userTypeSpinner = (Spinner)findViewById(R.id.inputType);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.user_type,
                android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userTypeSpinner.setAdapter(adapter);

        completeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String idStr = editID.getText().toString();
                String pwdStr = editPWD.getText().toString();

                String pwdStrCheck = editPWDcheck.getText().toString();
                String nameStr = editName.getText().toString();
                String userTypeStr = userTypeSpinner.getSelectedItem().toString();

                if(!idStr.equals("") && !nameStr.equals("") && pwdStr.equals(pwdStrCheck)){
                    try {
                        MessageDigest digest = MessageDigest.getInstance("SHA-256");
                        String strPassword = editPWD.getText().toString();
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
                    sendSignUpInfo task = new sendSignUpInfo();
                    task.execute(SERVER_ADDRESS, idStr, pwdStr, nameStr, userTypeStr);
                }else{
                    Toast.makeText(getApplicationContext(), "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT);
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    class sendSignUpInfo extends AsyncTask<String, Void, String>{

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(SignUpActivity.this, "회원 등록 중...", null, true, true);
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
                    Toast.makeText(getApplicationContext(), "가입 성공", Toast.LENGTH_LONG).show();
                    finish();
                }else{
                    Toast.makeText(getApplicationContext(), resultMsg, Toast.LENGTH_LONG).show();
                }
            }catch (Exception e){

            }

            finish();
            Toast.makeText(SignUpActivity.this, s, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String strID = (String)strings[1];
            String strPWD = (String)strings[2];
            String strName = (String)strings[3];
            int strUserType = strings[4].equals("상인")? 1 : 0;

            String severalURL = (String)strings[0];


            try{
                URL url = new URL(severalURL);

                serverConnection = (HttpURLConnection)url.openConnection();
                serverConnection.setRequestProperty("Content-Type", "application/json");

                serverConnection.setRequestMethod("POST");

                JSONObject requestObject = new JSONObject();
                requestObject.put("request", "sign_up");
                requestObject.put("id", strID);
                requestObject.put("password", strPWD);
                requestObject.put("name", strName);
                requestObject.put("type", strUserType);

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
