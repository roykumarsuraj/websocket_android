package com.roykumarsuraj.chat;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class MainActivity extends AppCompatActivity {

    private WebSocket webSocket;
    private MessageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView messageList = findViewById(R.id.messageList);
        final EditText edit_msg = findViewById(R.id.edit_msg);
        MaterialButton btn_send = findViewById(R.id.btn_send);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("ws://192.168.233.55:81/Ratchet/insert_msg.php").build();
       // SocketListener socketListener = new SocketListener(this);

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                super.onClosed(webSocket, code, reason);
            }

            @Override
            public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                super.onClosing(webSocket, code, reason);
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Failed - "+response, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
               runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("message", text);
                            jsonObject.put("byServer", true);
                            adapter.addItem(jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Connected - "+response, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        adapter = new MessageAdapter();
        messageList.setAdapter(adapter);

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = edit_msg.getText().toString();
                if (!message.isEmpty()) {
                    if(webSocket!=null){
                        if(webSocket.send(message)){
                            edit_msg.setText("");
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("message", message);
                                jsonObject.put("byServer", false);
                                adapter.addItem(jsonObject);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }else{
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "Failed - Not Sent", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Failed - WS Null", Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                }
            }
        });

    }

    public class MessageAdapter extends BaseAdapter {
        List<JSONObject> messagesList = new ArrayList<>();
        @Override
        public int getCount() {
            return messagesList.size();
        }

        @Override
        public Object getItem(int i) {
            return messagesList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            if (view == null)
                view = getLayoutInflater().inflate(R.layout.message_list_item, viewGroup, false);
            TextView sentMessage = view.findViewById(R.id.sentMessage);
            TextView receivedMessage = view.findViewById(R.id.receivedMessage);
            JSONObject item = messagesList.get(i);

            try {

                if (item.getBoolean("byServer")) {

                    receivedMessage.setVisibility(View.VISIBLE);
                    receivedMessage.setText(item.getString("message"));
                    sentMessage.setVisibility(View.INVISIBLE);

                } else {

                    sentMessage.setVisibility(View.VISIBLE);
                    sentMessage.setText(item.getString("message"));
                    receivedMessage.setVisibility(View.INVISIBLE);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return view;
        }


        void addItem(JSONObject item) {
            messagesList.add(item);
            notifyDataSetChanged();
        }
    }

}