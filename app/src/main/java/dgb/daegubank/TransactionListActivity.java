package dgb.daegubank;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by machina on 30/10/2018.
 */

public class TransactionListActivity extends AppCompatActivity {
    private Button okButton;
    private List<History> history_data;
    private ListView list;

    private static final String SERVER_ADDRESS = "http://capstone.gonetis.com/daegubank/server.php";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaction_list);

        Intent fromLobbyIntent = new Intent(this.getIntent());
        String id = fromLobbyIntent.getStringExtra("user_id");

        history_data = new ArrayList<>();
        list = (ListView)findViewById(R.id.livTransaction);
        okButton = (Button)findViewById(R.id.listOkBtn);

        list.setAdapter(new HistoryAdapter());
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        RequestHistory request = new RequestHistory();
        request.execute(SERVER_ADDRESS, id);
    }

    class HistoryAdapter extends ArrayAdapter<History> {
        HistoryAdapter() {
            super(TransactionListActivity.this, R.layout.list_history, history_data);
        }

        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = TransactionListActivity.this.getLayoutInflater();
            View row = inflater.inflate(R.layout.list_history, null, true);
            TextView time = (TextView) row.findViewById(R.id.txtTime);
            TextView name = (TextView) row.findViewById(R.id.txtName);
            TextView money = (TextView) row.findViewById(R.id.txtMoney);
            time.setText(history_data.get(position).getTime());
            name.setText(history_data.get(position).getName());
            money.setText(history_data.get(position).getMoney());
            return row;
        }
    }

    class History {
        private String strTime;
        private String strName;
        private String strMoney;

        History(String strTime, String strName, String strMoney) {
            this.strTime = strTime;
            this.strName = strName;
            this.strMoney = strMoney;
        }
        String getTime() {
            return this.strTime;
        }
        String getName() {
            return this.strName;
        }
        String getMoney() {
            return this.strMoney;
        }
    }

    class RequestHistory extends AsyncTask<String, Void, String> {
        private ProgressDialog progressDialog;
        private HttpURLConnection serverConnection;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(TransactionListActivity.this, "질의 중..", null, true, true);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();

            try{
                JSONObject result = new JSONObject(s);
                String respondMsg = result.getString("respond"); //"Error" or "Success"

                if(respondMsg.equals("Success")){
                    JSONArray array = result.getJSONArray("message");
                    history_data.clear();
                    for(int i = 0; i < array.length(); ++i) {
                        JSONObject object = array.getJSONObject(i);

                        String time = object.getString("date");
                        String name = object.getString("name");
                        String money = Integer.toString(object.getInt("money"));

                        History history = new History(time, name, money);
                        history_data.add(history);
                    }
                    list.invalidateViews();
                }else{
                    Toast.makeText(TransactionListActivity.this, result.getString("message"), Toast.LENGTH_LONG).show();
                }

                Toast.makeText(TransactionListActivity.this, respondMsg, Toast.LENGTH_LONG).show();

            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            String severalURL = strings[0];
            String strID = strings[1];

            try {
                URL url = new URL(severalURL);

                serverConnection = (HttpURLConnection)url.openConnection();
                serverConnection.setRequestProperty("Content-Type", "application/json");
                serverConnection.setRequestMethod("POST");

                JSONObject requestObject = new JSONObject();
                requestObject.put("request", "history");
                requestObject.put("id", strID);

                Log.e("Req", requestObject.toString());

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
                e.printStackTrace();
            }
            return null;
        }
    }
}
