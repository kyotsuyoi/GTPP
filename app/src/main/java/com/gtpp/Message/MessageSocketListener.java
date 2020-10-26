package com.gtpp.Message;

import android.app.AlertDialog;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.gtpp.CommonClasses.ApiClient;
import com.gtpp.CommonClasses.ApiClientForImage;
import com.gtpp.CommonClasses.Handler;
import com.gtpp.CommonClasses.RecyclerItemClickListener;
import com.gtpp.CommonClasses.SavedUser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import retrofit2.Call;
import retrofit2.Callback;

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

    @Override
    public void onOpen(WebSocket webSocket, Response response){
        super.onOpen(webSocket,response);
        activity.runOnUiThread(() -> textViewOffline.setVisibility(View.INVISIBLE));
    }

    @Override
    public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
        super.onMessage(webSocket, text);

        activity.runOnUiThread(() -> {
            try {
                JsonParser jsonParser = new JsonParser();
                JsonObject object = jsonParser.parse(text).getAsJsonObject();

                if(object.get("type").getAsString().contains("user_message")){
                    Log.i("onMessage",object.toString());
                    JsonObject jsonMessage = new JsonObject();
                    jsonMessage.addProperty("type", object.get("type").getAsString());
                    jsonMessage.addProperty("id", object.get("message_id").getAsInt());
                    jsonMessage.addProperty("description", object.get("description").getAsString());
                    jsonMessage.addProperty("user_name", object.get("user_name").getAsString());
                    jsonMessage.addProperty("task_id", object.get("task_id").getAsInt());
                    jsonMessage.addProperty("user_id", object.get("user_id").getAsInt());
                    jsonMessage.addProperty("task_description", object.get("task_description").getAsString());
                    jsonMessage.addProperty("date_time", object.get("date_time").getAsString());
                    jsonMessage.addProperty("image", object.get("image").getAsInt());

                    adapter.setNewItem(jsonMessage);
                    recyclerView.smoothScrollToPosition(adapter.getItemCount());
                    if (adapter.getItemCount()==1){
                        recyclerView.setAdapter(adapter);
                    }
                }

                if(object.get("type").getAsString().contains("first_connection")){
                    ResourceID = object.get("resource_id").getAsInt();
                    //Handler.ShowSnack("Nova conexão",object.get("message").getAsString()+"\nID:"+ResourceID, activity,R_ID,true);
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("type","first_connection");
                    jsonObject.addProperty("resource_id",ResourceID);
                    jsonObject.addProperty("task_id",TaskID);
                    jsonObject.addProperty("user_id",SU.getId());
                    jsonObject.addProperty("user_name",SU.getUser());
                    jsonObject.addProperty("session",SU.getSession());
                    webSocket.send(jsonObject.toString());
                }

                if(object.get("type").getAsString().contains("message_delete")){
                    int MessageID = object.get("message_id").getAsInt();
                    int TaskID = object.get("task_id").getAsInt();
                    int UserID = object.get("user_id").getAsInt();
                    adapter.RemoveItemByID(MessageID);

                    String path = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath();
                    File file = new File(path + "/Message_" + TaskID + "_" + MessageID + "_" + UserID + ".jpg");

                    if(file.exists()) {
                        file.delete();
                    }
                }

                if(object.get("type").getAsString().contains("error")){
                    Handler.ShowSnack("Houve um erro",object.get("message").getAsString(), activity,R_ID,true);
                }

            }catch (Exception e){
                Handler.ShowSnack("Houve um erro","MessageSocketListener.onMessage: "+e.getMessage(), activity, R_ID,true);
            }
        });
    }

    @Override
    public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
        super.onClosing(webSocket, code, reason);
    }

    @Override
    public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
        super.onClosed(webSocket, code, reason);
    }

    @Override
    public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
        try {
            super.onFailure(webSocket, t, response);
            //Handler.ShowSnack("Houve um erro","MessageSocketListener.onFailure(WebSocket): "+t.getMessage(), activity, R_ID,true);
            activity.runOnUiThread(() -> textViewOffline.setVisibility(View.VISIBLE));
            InstantiateWebSocket(this);
        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","MessageSocketListener.onFailure(WebSocket): "+e.getMessage(), activity, R_ID,true);
        }
    }

    public WebSocket InstantiateWebSocket(MessageSocketListener messageSocketListener) {
        try {
            OkHttpClient client = new OkHttpClient();
            //Request request = new Request.Builder().url("ws://192.168.0.99:3333/server.php").build();
            Request request = new Request.Builder().url("ws://187.35.128.157:3333/server.php").build();
            webSocket = client.newWebSocket(request, messageSocketListener);
            return webSocket;
        }catch (Exception e){
            //Handler.ShowSnack("Houve um erro","InstantiateWebSocket: "+e.getMessage(), this, R_ID,true);
            return null;
        }
    }

    public int getResourceID(){
        return ResourceID;
    }

    private void GetMessage(){
        try {
            Call<JsonObject> call = messageInterface.GetMessage(
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
                    Handler.ShowSnack("Houve um erro","MessageSocketListener.onFailure: " + t.toString(), activity, R_ID,true);
                }
            });

        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","MessageSocketListener.GetMessage: " + e.getMessage(), activity, R_ID,true);
        }
    }

    private void SetList(){

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(
                activity, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {

            public void onItemClick(View view, int position) {
                try {

                }catch (Exception e){
                    Handler.ShowSnack("Houve um erro","MessageSocketListener.SetList.onItemClick: " + e.getMessage(), activity, R_ID,true);
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
                    Handler.ShowSnack("Houve um erro","MessageSocketListener.SetList.onLongItemClick: " + e.getMessage(), activity, R_ID,true);
                }
                return false;
            }
        }));
    }

    private void DeleteMessage(int position){
        try {
            Call<JsonObject> call = messageInterface.DeleteMessage(
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
                    Handler.ShowSnack("Houve um erro","MessageSocketListener.DeleteMessage.onFailure: " + t.toString(), activity, R_ID,true);
                }
            });

        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","MessageSocketListener.DeleteMessage: " + e.getMessage(), activity, R_ID,true);
        }
    }

}
