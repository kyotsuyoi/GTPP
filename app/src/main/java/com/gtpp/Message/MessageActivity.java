package com.gtpp.Message;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.gtpp.CommonClasses.ApiClient;
import com.gtpp.CommonClasses.ApiClientForImage;
import com.gtpp.CommonClasses.Handler;
import com.gtpp.CommonClasses.SavedUser;
import com.gtpp.Main.MainActivity;
import com.gtpp.Main.MainInterface;
import com.gtpp.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import okhttp3.WebSocket;
import retrofit2.Call;
import retrofit2.Callback;

import static com.gtpp.CommonClasses.Handler.getAppID;

public class MessageActivity extends AppCompatActivity {

    private SavedUser SU;
    private int R_ID = R.id.activityMessage_Button;
    private com.gtpp.CommonClasses.Handler Handler= new Handler();

    private TextView textView, textViewOffline;
    private RecyclerView recyclerView;
    private EditText editText;
    private Button buttonSend, buttonCam;
    private ImageView imageView;
    private ProgressBar progressBar;
    private WebSocket webSocket;
    private MessageSocketListener messageSocketListener;
    private MessageInterface messageInterface = ApiClient.getApiClient().create(MessageInterface.class);
    private MessageInterface messageInterfaceForImage = ApiClientForImage.getApiClient().create(MessageInterface.class);

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private File file;
    private Uri uri;

    private int TaskID, TempID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_message);
            Objects.requireNonNull(getSupportActionBar()).hide();

            textView = findViewById(R.id.activityMessage_TextView);
            recyclerView = findViewById(R.id.activityMessage_RecyclerView);
            editText = findViewById(R.id.activityMessage_EditTextText);
            buttonSend = findViewById(R.id.activityMessage_Button);
            buttonCam = findViewById(R.id.activityMessage_ButtonCam);
            imageView = findViewById(R.id.activityMessage_ImageView);
            textViewOffline = findViewById(R.id.activityMessage_TextViewOffline);
            progressBar = findViewById(R.id.activityMessage_ProgressBar);

            progressBar.setVisibility(View.INVISIBLE);

            TaskID = getIntent().getIntExtra("task_id",0);
            textView.setText(getIntent().getStringExtra("task_description"));
            Handler.SelectedTaskID = TaskID;
            InstantiateWebSocket();

            SU = SavedUser.getSavedUser();

            SetButton();

            recyclerView.requestFocus();
        }catch (Exception e){
            Handler.ShowSnack("Houve um erro", "MessageActivity.onCreate: "+e.getMessage(), this, R_ID,true);
        }
    }

    private void InstantiateWebSocket() {
        try {
            messageSocketListener = new MessageSocketListener(MessageActivity.this,R_ID,recyclerView, TaskID, textViewOffline);
            webSocket = messageSocketListener.InstantiateWebSocket(messageSocketListener);
        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","MessageActivity.InstantiateWebSocket: "+e.getMessage(), this, R_ID,true);
        }
    }

    private File createImageFile(String name) {
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        return new File(storageDir,name+".jpg");
    }

    private void SetButton(){
        try{
            buttonSend.setOnClickListener(v -> {
                try {
                    String message = editText.getText().toString();

                    JsonObject jsonMessage = new JsonObject();
                    JsonObject jsonMessageWebSocket = new JsonObject();

                    jsonMessage.addProperty("description",message);
                    jsonMessage.addProperty("task_id",getIntent().getIntExtra("task_id", 0));
                    jsonMessage.addProperty("user_id",SU.getId());

                    jsonMessageWebSocket.addProperty("type", "user_message");
                    jsonMessageWebSocket.addProperty("resource_id", messageSocketListener.getResourceID());
                    jsonMessageWebSocket.addProperty("task_description", getIntent().getStringExtra("task_description"));
                    jsonMessageWebSocket.addProperty("user_name", SU.getUser());
                    jsonMessageWebSocket.addProperty("user_id", SU.getId());
                    jsonMessageWebSocket.addProperty("task_id", getIntent().getIntExtra("task_id", 0));
                    jsonMessageWebSocket.addProperty("description", message);

                    if (imageView.getDrawable() == null) {
                        if (!message.isEmpty()) {
                            jsonMessageWebSocket.addProperty("image", 0);
                            PostMessage(jsonMessage,jsonMessageWebSocket);
                            //object.addProperty("image_temp_id", -1);
                            //webSocket.send(object.toString());
                            editText.setText("");
                        }
                    }else{
                        Bitmap bitmap = Handler.ImageOrientation(MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri), file);
                        //bitmap = Handler.ImageOrientation(bitmap,file);
                        String EncodedImage = Handler.ImageEncode(bitmap);
                        jsonMessageWebSocket.addProperty("image", 1);
                        jsonMessage.addProperty("image", EncodedImage);
                        //object.addProperty("image_temp_id", TempID);
                        PostMessage(jsonMessage, jsonMessageWebSocket);
                        editText.setText("");
                        imageView.setImageBitmap(null);
                        //webSocket.send(object.toString());
                    }
                }catch (Exception e){
                    Handler.ShowSnack("Houve um erro","MessageActivity.SetButton.buttonSend: "+e.getMessage(), this, R_ID,true);
                }
            });

            buttonCam.setOnClickListener(v->{
                try {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    TempID = (int) (System.currentTimeMillis()%10000);
                    int TaskID = getIntent().getIntExtra("task_id",0);
                    String name = "/Message_" + TaskID + "_Temp" + TempID + "_" + SU.getId();
                    file = createImageFile(name);
                    uri = FileProvider.getUriForFile(this, "com.gtpp", file);

                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                }catch (Exception e){
                    Handler.ShowSnack("Houve um erro", "MessageActivity.SetButton.buttonCam.setOnClickListener: "+e.getMessage(), this, R_ID,true);
                }
            });

        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","MessageActivity.SetButton: "+e.getMessage(), this, R_ID,true);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            try{
                Picasso.get()
                        .load(file.getAbsoluteFile())
                        .resize(500,800)
                        .centerCrop()
                        .into(imageView);

                //Bitmap bitmap = Handler.ImageOrientation(MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri), file);
                //imageView.setImageBitmap(bitmap);
            }catch (Exception e){
                Handler.ShowSnack("Houve um erro","MessageActivity.onActivityResult: "+e.getMessage(), this, R_ID,true);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        webSocket.cancel();
        Handler.SelectedTaskID = 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Handler.SelectedTaskID = 0;
    }

    private void PostMessage(JsonObject jsonMessage, JsonObject jsonMessageWebSocket){
        try {
            progressBar.setVisibility(View.VISIBLE);
            buttonCam.setEnabled(false);
            buttonCam.setAlpha(0.2f);
            Call<JsonObject> call = messageInterfaceForImage.PostMessage(getAppID(),
                    SU.getSession(),
                    jsonMessage
            );
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                    try {
                        if (!Handler.isRequestError(response, MessageActivity.this, R_ID)) {
                            JsonObject jsonObject = response.body();
                            int ID = jsonObject.get("last_id").getAsInt();
                            jsonMessageWebSocket.addProperty("id", ID);
                            webSocket.send(jsonMessageWebSocket.toString());

                            if(jsonMessageWebSocket.get("image").getAsInt() == 1){
                                if(file != null && file.exists()) {
                                    String Name = "/Message_" + TaskID + "_" + ID + "_" + SU.getId();
                                    File NewFile = createImageFile(Name);
                                    file.renameTo(NewFile);
                                }
                            }
                        }
                    }catch (Exception e){
                        Handler.ShowSnack("Houve um erro","MessageActivity.PostMessage.onResponse: " + e.getMessage(), MessageActivity.this, R_ID,true);
                    }
                    progressBar.setVisibility(View.INVISIBLE);
                    buttonCam.setEnabled(true);
                    buttonCam.setAlpha(1f);
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Handler.ShowSnack("Houve um erro","MessageActivity.PostMessage.onFailure: " + t.toString(), MessageActivity.this, R_ID,true);
                    progressBar.setVisibility(View.INVISIBLE);
                    buttonCam.setEnabled(true);
                    buttonCam.setAlpha(1f);
                }
            });

        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","MessageActivity.PostMessage: " + e.getMessage(), MessageActivity.this, R_ID,true);
            progressBar.setVisibility(View.INVISIBLE);
            buttonCam.setEnabled(true);
            buttonCam.setAlpha(1f);
        }
    }

}