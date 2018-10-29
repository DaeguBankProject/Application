package dgb.daegubank;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;
/**
 * Created by machina on 28/10/2018
 */

public class UserLobbyActivity extends AppCompatActivity{
    private TextView userText;

    private String userName;
    private String userType;
    private String userAccount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_lobby);

        Intent fromLogInIntent = new Intent(this.getIntent());
        userName = fromLogInIntent.getStringExtra("user_name");
        userType = fromLogInIntent.getStringExtra("user_type");
        userAccount = fromLogInIntent.getStringExtra("user_account");

        Toast.makeText(this,userName + " " + userType + " ", Toast.LENGTH_LONG).show();


        userText = (TextView)findViewById(R.id.userName);

    }
}
