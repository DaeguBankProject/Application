package dgb.daegubank;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
/**
 * Created by machina on 28/10/2018
 */

public class UserLobbyActivity extends AppCompatActivity{
    private TextView userText;
    private Button accountBtn;
    private Button transactionListBtn;
    private Button transactionBtn;

    private String userId;
    private String userName;
    private String userType;
    private String userAccount;

    private boolean isAddAccount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_lobby);

        Intent fromLogInIntent = new Intent(this.getIntent());
        userId = fromLogInIntent.getStringExtra("user_id");
        userName = fromLogInIntent.getStringExtra("user_name");
        userType = fromLogInIntent.getStringExtra("user_type");
        userAccount = fromLogInIntent.getStringExtra("user_account");

        userText = (TextView)findViewById(R.id.userName);
        userText.setText(userName);

        accountBtn = (Button)findViewById(R.id.accountBtn);
        transactionListBtn = (Button)findViewById(R.id.listBtn);
        transactionBtn = (Button)findViewById(R.id.transactionBtn);

        if(userAccount.equals("null")){
            accountBtn.setText("+");
            isAddAccount = true;
        }else{
            accountBtn.setText(userAccount);
            isAddAccount = false;
        }

        if(isAddAccount){
            accountBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent addIntent = new Intent(UserLobbyActivity.this, AddAccountActivity.class);
                    addIntent.putExtra("userId", userId);
                    startActivityForResult(addIntent, 1);
                }
            });
        }

        transactionListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent listIntent = new Intent(UserLobbyActivity.this, TransactionListActivity.class);
                startActivity(listIntent);
            }
        });

        transactionBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent transactionIntent = new Intent(UserLobbyActivity.this, MainActivity.class);
                startActivity(transactionIntent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1){
            if(resultCode == Activity.RESULT_OK){
                userAccount = data.getStringExtra("account");
            }
        }
    }
}
