package com.gtpp.CommonClasses;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.gtpp.Main.MainActivity;
import com.gtpp.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

import static android.content.Context.NOTIFICATION_SERVICE;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class NotifyListener extends WebSocketListener {

    private Context context;
    private SavedUser SU;
    //private boolean SessionError = false; //Stop error notification if the session is gone
    private WebSocket webSocket;

    public NotifyListener(Context context, SavedUser SU){
        this.context=context;
        this.SU = SU;
    }

    @Override
    public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response){
        super.onOpen(webSocket,response);
        MainActivity.cardViewSession.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorGreen));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
        super.onMessage(webSocket, text);

        try {
            JsonParser jsonParser = new JsonParser();
            JsonObject object = jsonParser.parse(text).getAsJsonObject();

            if (object.get("type").getAsString().contains("user_message") && object.get("task_id").getAsInt() != Handler.SelectedTaskID) {
                String UserName = object.get("user_name").getAsString();
                if (!UserName.equals(SU.getUser())) {
                    String TaskDescription = object.get("task_description").getAsString();
                    Notify(TaskDescription, object.get("user_name").getAsString() + ": " + object.get("description").getAsString());
                }
            }

            //Send params to server during the first connection
            if (object.get("type").getAsString().contains("first_connection")) {
                int resourceID = object.get("resource_id").getAsInt();
                //Handler.ShowSnack("Nova conexão",object.get("message").getAsString()+"\nID:"+ResourceID, activity,R_ID,true);
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("type", "first_connection");
                jsonObject.addProperty("resource_id", resourceID);
                jsonObject.addProperty("task_id", -1);
                jsonObject.addProperty("user_id", SU.getId());
                jsonObject.addProperty("user_name", SU.getUser());
                jsonObject.addProperty("session", SU.getSession());
                webSocket.send(jsonObject.toString());
                //Notify("Conectado", "Resource_id: "+resourceID);
            }

            if (object.get("type").getAsString().contains("error")) {
                //if(!SessionError){
                    if(object.get("description").getAsString().contains("Your session is gone")){
                        Notify("Sua sessão foi perdida", "Talvez você se conectou em outro dispositivo, as notificações foram anuladas, faça login novamente");
                    }else {
                        Notify("Houve um erro", object.get("description").getAsString());
                    }
                    Handler.isLogged = false;
                    MainActivity.cardViewSession.setCardBackgroundColor(Color.RED);
                    //SessionError = true;
                    this.webSocket.cancel();
                //}
            }
        }catch (Exception e){
            Notify("Houve um erro", e.getMessage());
        }
    }

    @Override
    public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
        super.onClosing(webSocket, code, reason);
    }

    @Override
    public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
        super.onClosed(webSocket, code, reason);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
        super.onFailure(webSocket, t, response);
        MainActivity.cardViewSession.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorRed));
        if(Handler.isLogged){
            InstantiateWebSocket(this);//To reconnect WebSocket when connection is lost
        }else{
            this.webSocket.cancel();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void InstantiateWebSocket(NotifyListener notifyListener) {
        try {
            OkHttpClient client = new OkHttpClient();
            //Request request = new Request.Builder().url("ws://192.168.0.99:3333/server.php").build();
            Request request = new Request.Builder().url("ws://187.35.128.157:3333/server.php").build();
            webSocket = client.newWebSocket(request, notifyListener);
        }catch (Exception e){
            Notify("NotifyListener.InstantiateWebSocket", e.getMessage());
            //Handler.ShowSnack("Houve um erro","MainActivity.InstantiateWebSocket: "+e.getMessage(), this, R_ID,true);
        }
    }

    public WebSocket getWebSocket() {
        return webSocket;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void Notify(String UserName, String Message) {
        try {
            String CHANNEL_ID = "MessageNotify";
            CharSequence NAME = "Mensagem";
            String Description = "Para notificar mensagens de cada tarefa";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, NAME, importance);
                channel.setDescription(Description);
                channel.enableLights(true);
                channel.setLightColor(Color.CYAN);
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                channel.setShowBadge(false);

                assert notificationManager != null;
                notificationManager.createNotificationChannel(channel);
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.gopp_white_launcher)
                    .setLights(Color.CYAN,100,100)
                    .setContentTitle(UserName)
                    .setContentText(Message)
                    .setStyle(new NotificationCompat.BigTextStyle());


            int NOTIFICATION_ID = (int) (System.currentTimeMillis()%10000);
            assert null != notificationManager;
            notificationManager.notify(NOTIFICATION_ID, builder.build());

        } catch (Exception e) {
            //Toast.makeText(activity,"Houve um erro na notificação",Toast.LENGTH_LONG);
            //Handler.ShowSnack("Houve um erro","InstantiateWebSocket: "+e.getMessage(), this, R_ID,true);
        }
    }

}
