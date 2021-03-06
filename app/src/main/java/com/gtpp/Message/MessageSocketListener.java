package com.gtpp.Message;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.gtpp.CommonClasses.ApiClient;
import com.gtpp.CommonClasses.Handler;
import com.gtpp.CommonClasses.RecyclerItemClickListener;
import com.gtpp.CommonClasses.SavedUser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import retrofit2.Call;
import retrofit2.Callback;

import static com.gtpp.CommonClasses.Handler.getAppID;

public class MessageSocketListener extends WebSocketListener {
    public MessageActivity activity;
    public MessageAdapter adapter;
    private com.gtpp.CommonClasses.Handler Handler= new Handler();
    private SavedUser SU;
    private int R_ID;
    private RecyclerView recyclerView;
    private TextView textViewOffline;
    private int TaskID;
    private int ResourceID;
    private MessageInterface messageInterface = ApiClient.getApiClient().create(MessageInterface.class);
    private WebSocket webSocket;

    private boolean Stop = false;
    private boolean isConnected = false;

    public MessageSocketListener(MessageActivity activity, int R_ID, RecyclerView recyclerView, int TaskID, TextView textViewOffline){
        this.activity = activity;
        this.R_ID = R_ID;
        this.recyclerView = recyclerView;
        this.textViewOffline = textViewOffline;
        SU = SavedUser.getSavedUser();
        JsonArray jsonArray = new JsonArray();
        adapter = new MessageAdapter(jsonArray,activity,SU,R_ID, TaskID);

        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        this.recyclerView.setLayoutManager(layoutManager);
        this.recyclerView.setHasFixedSize(true);
        this.TaskID = TaskID;

        GetMessage();
        SetList();
    }

    public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response){
        super.onOpen(webSocket,response);
        activity.runOnUiThread(() -> {
            //textViewOffline.setVisibility(View.INVISIBLE);
            textViewOffline.setText("Conectado");
            textViewOffline.setTextColor(Color.GREEN);
        });
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("auth", SU.getSession());
        jsonObject.addProperty("app_id",2);

        this.webSocket.send(jsonObject.toString());

        isConnected = true;
    }

    @Override
    public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
        super.onMessage(webSocket, text);

        activity.runOnUiThread(() -> {
            try {
                JsonParser jsonParser = new JsonParser();
                JsonObject object = jsonParser.parse(text).getAsJsonObject();

                if (Handler.isMessageError(object,activity,R_ID)){
                    textViewOffline.setText(object.get("message").getAsString());
                    textViewOffline.setTextColor(Color.YELLOW);
                    return;
                }

                if(!object.has("type")){
                    Handler.ShowSnack("Houve um erro","Message type is NULL", activity, R_ID);
                    return;
                }

                int Type = object.get("type").getAsInt();

                textViewOffline.setText("Conectado");
                textViewOffline.setTextColor(Color.GREEN);

                if(Type == 1) {
                    JsonObject jsonMessage = jsonParser.parse(object.get("object").getAsString()).getAsJsonObject();
                    jsonMessage.addProperty("user_name", object.get("user_name").getAsString());

                    adapter.setNewItem(jsonMessage);
                    recyclerView.smoothScrollToPosition(adapter.getItemCount());
                    if (adapter.getItemCount() == 1) {
                        recyclerView.setAdapter(adapter);
                    }
                }

            }catch (Exception e){
                Handler.ShowSnack("Houve um erro","MessageSocketListener.onMessage: "+e.getMessage(), activity, R_ID);
            }
        });
    }

    @Override
    public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
        super.onClosing(webSocket, code, reason);
        activity.runOnUiThread(() -> {
            textViewOffline.setText("Desconectando...");
            textViewOffline.setTextColor(Color.RED);
        });
    }

    @Override
    public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
        super.onClosed(webSocket, code, reason);
        activity.runOnUiThread(() -> {
            textViewOffline.setText("Desconectado");
            textViewOffline.setTextColor(Color.RED);
        });
    }

    @Override
    public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
        try {
            super.onFailure(webSocket, t, response);
            //Handler.ShowSnack("Houve um erro","MessageSocketListener.onFailure(WebSocket): "+t.getMessage(), activity, R_ID,true);
            //activity.runOnUiThread(() -> textViewOffline.setVisibility(View.VISIBLE));
            activity.runOnUiThread(() -> {
                //textViewOffline.setVisibility(View.INVISIBLE);
                textViewOffline.setText("Error (onFailure): " + t.getMessage());
                textViewOffline.setTextColor(Color.RED);
            });
            if(!Stop){
                this.webSocket.cancel();
                InstantiateWebSocket(this);
            }
        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","MessageSocketListener.onFailure(WebSocket): "+e.getMessage(), activity, R_ID);
        }
    }

    public void InstantiateWebSocket(MessageSocketListener messageSocketListener) {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url("ws://192.168.0.99:3333/server.php").build();
            //Request request = new Request.Builder().url("ws://187.35.128.157:3333/server.php").build();
            webSocket = client.newWebSocket(request, messageSocketListener);
        }catch (Exception e){
            //Handler.ShowSnack("Houve um erro","InstantiateWebSocket: "+e.getMessage(), this, R_ID,true);
        }
    }

    public int getResourceID(){
        return ResourceID;
    }

    private void GetMessage(){
        try {
            Call<JsonObject> call = messageInterface.GetMessage(getAppID(),
                    SU.getSession(),
                    TaskID
            );
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                    if (!Handler.isRequestError(response,activity,R_ID)){
                        JsonObject task = response.body();
                        JsonArray data = task.get("data").getAsJsonArray();
                        adapter = new MessageAdapter(data,activity,SU,R_ID,TaskID);
                        recyclerView.setAdapter(adapter);
                        recyclerView.smoothScrollToPosition(adapter.getItemCount());
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Handler.ShowSnack("Houve um erro","MessageSocketListener.onFailure: " + t.toString(), activity, R_ID);
                }
            });

        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","MessageSocketListener.GetMessage: " + e.getMessage(), activity, R_ID);
        }
    }

    private void SetList(){

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(
                activity, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {

            public void onItemClick(View view, int position) {
                try {

                }catch (Exception e){
                    Handler.ShowSnack("Houve um erro","MessageSocketListener.SetList.onItemClick: " + e.getMessage(), activity, R_ID);
                }
            }

            public boolean onLongItemClick(View view, final int position) {
                try{
                    if (adapter.getItem(position).getAsJsonObject().get("user_id").getAsInt() != SU.getId()){
                        return false;
                    }

                    AlertDialog.Builder alert = new AlertDialog.Builder(activity);
                    alert.setMessage("Deseja apagar esta mensagem?");
                    alert.setCancelable(false);

                    alert.setNeutralButton("Cancelar", (dialog, which) -> dialog.cancel());
                    alert.setPositiveButton("Apagar", (dialog, which) -> DeleteMessage(position));

                    alert.create();
                    alert.setTitle("Tarefa");
                    alert.show();

                }catch (Exception e){
                    Handler.ShowSnack("Houve um erro","MessageSocketListener.SetList.onLongItemClick: " + e.getMessage(), activity, R_ID);
                }
                return false;
            }
        }));
    }

    private void DeleteMessage(int position){
        try {
            Call<JsonObject> call = messageInterface.DeleteMessage(getAppID(),
                    SU.getSession(),
                    adapter.getItem(position).getAsJsonObject().get("id").getAsInt(),
                    TaskID
            );
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                    if (!Handler.isRequestError(response,activity,R_ID)){
                        //adapter.RemoveItem(position);
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("type","message_delete");
                        jsonObject.addProperty("message_id",adapter.getItem(position).getAsJsonObject().get("id").getAsInt());
                        jsonObject.addProperty("task_id",TaskID);
                        jsonObject.addProperty("user_id",adapter.getItem(position).getAsJsonObject().get("user_id").getAsInt());
                        jsonObject.addProperty("resource_id",ResourceID);
                        webSocket.send(jsonObject.toString());

                        int TaskID = adapter.getItem(position).get("task_id").getAsInt();
                        int UserID = adapter.getItem(position).get("user_id").getAsInt();
                        int MessageID = adapter.getItem(position).get("id").getAsInt();

                        String path = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath();
                        File file = new File(path + "/Message_" + TaskID + "_" + MessageID + "_" + UserID + ".jpg");

                        if(file.exists()) {
                            file.delete();
                        }
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Handler.ShowSnack("Houve um erro","MessageSocketListener.DeleteMessage.onFailure: " + t.toString(), activity, R_ID);
                }
            });

        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","MessageSocketListener.DeleteMessage: " + e.getMessage(), activity, R_ID);
        }
    }

    public void Stop(){
        Stop = true;
    }

    public WebSocket getWebSocket(){
        return webSocket;
    }

}
