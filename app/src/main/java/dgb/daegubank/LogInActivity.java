package dgb.daegubank;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

/**
 * Created by machina on 28/10/2018.
 */

public class LogInActivity extends AppCompatActivity{
    Button signUpButton;
    Button signInButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_in);

        signInButton = (Button)findViewById(R.id.SignInBtn);
        signUpButton = (Button)findViewById(R.id.SignUpBtn);
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
