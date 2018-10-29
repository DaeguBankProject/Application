package dgb.daegubank;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by machina on 28/10/2018
 */

public class UserLobbyActivity extends AppCompatActivity{
    private TextView userText;

    private static String IP_ADDRESS = "192.168.0.4";
    private HttpURLConnection serverConnection;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_lobby);

        loadUserData task = new loadUserData();
        task.execute("http://" + IP_ADDRESS + "/Test/Server/server.php");

        userText = (TextView)findViewById(R.id.userName);

    }
}
