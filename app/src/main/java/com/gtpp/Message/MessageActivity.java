package com.gtpp.Message;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
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
import com.gtpp.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Objects;
import java.util.Random;

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
            com.gtpp.CommonClasses.Handler.SelectedTaskID = TaskID;
            InstantiateWebSocket();

            SU = SavedUser.getSavedUser();

            SetButton();

            recyclerView.requestFocus();
        }catch (Exception e){
            Handler.ShowSnack("Houve um erro", "MessageActivity.onCreate: "+e.getMessage(), this, R_ID);
        }
    }

    private void InstantiateWebSocket() {
        try {
            messageSocketListener = new MessageSocketListener(MessageActivity.this,R_ID,recyclerView, TaskID, textViewOffline);
            messageSocketListener.InstantiateWebSocket(messageSocketListener);
        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","MessageActivity.InstantiateWebSocket: "+e.getMessage(), this, R_ID);
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
                    String description = editText.getText().toString();

                    JsonObject jsonObject = new JsonObject();

                    jsonObject.addProperty("description",description);
                    jsonObject.addProperty("task_id",getIntent().getIntExtra("task_id", 0));
                    jsonObject.addProperty("user_id",SU.getId());

                    if (imageView.getDrawable() == null) {
                        if (!description.isEmpty()) {
                            PostMessage(jsonObject);
                            editText.setText("");
                        }
                    }else{
                        Bitmap bitmap = Handler.ImageOrientation(MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri), file);
                        String EncodedImage = Handler.ImageEncode(bitmap);
                        jsonObject.addProperty("image", EncodedImage);
                        PostMessage(jsonObject);
                        editText.setText("");
                        imageView.setImageBitmap(null);
                    }
                }catch (Exception e){
                    Handler.ShowSnack("Houve um erro","MessageActivity.SetButton.buttonSend: "+e.getMessage(), this, R_ID);
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
                    Handler.ShowSnack("Houve um erro", "MessageActivity.SetButton.buttonCam.setOnClickListener: "+e.getMessage(), this, R_ID);
                }
            });

        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","MessageActivity.SetButton: "+e.getMessage(), this, R_ID);
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
                Handler.ShowSnack("Houve um erro","MessageActivity.onActivityResult: "+e.getMessage(), this, R_ID);
            }
        }
    }

    @Override
    public void onBackPressed() {
        try {
            messageSocketListener.Stop();
            messageSocketListener.getWebSocket().cancel();
            com.gtpp.CommonClasses.Handler.SelectedTaskID = 0;
            finish();
        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","MessageActivity.onBackPressed: " + e.getMessage(), MessageActivity.this, R_ID);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        com.gtpp.CommonClasses.Handler.SelectedTaskID = 0;
    }

    private void PostMessage(JsonObject jsonMessage){
        try {
            progressBar.setVisibility(View.VISIBLE);
            buttonCam.setEnabled(false);
            buttonCam.setAlpha(0.2f);
            buttonSend.setEnabled(false);
            buttonSend.setAlpha(0.2f);
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
                            JsonObject jsonData = jsonObject.get("data").getAsJsonObject();

                            int ID = jsonData.get("last_id").getAsInt();
                            jsonMessage.addProperty("id", ID);
                            jsonMessage.addProperty("date_time", jsonData.get("date_time").getAsString());

                            JsonObject jsonSocket = new JsonObject();
                            jsonSocket.addProperty("user_id", SU.getId());
                            jsonSocket.addProperty("user_name", SU.getUser());
                            jsonSocket.addProperty("task_id", getIntent().getIntExtra("task_id", 0));
                            jsonSocket.addProperty("type",1);

                            if(jsonMessage.has("image")){
                                if(file != null && file.exists()) {
                                    String Name = "/Message_" + TaskID + "_" + ID + "_" + SU.getId();
                                    File NewFile = createImageFile(Name);
                                    file.renameTo(NewFile);
                                }

                                jsonMessage.addProperty("image","1");
                            }else{
                                jsonMessage.addProperty("image","0");
                            }

                            jsonSocket.add("object", jsonMessage);
                            messageSocketListener.getWebSocket().send(jsonSocket.toString());
                        }
                    }catch (Exception e){
                        Handler.ShowSnack("Houve um erro","MessageActivity.PostMessage.onResponse: " + e.getMessage(), MessageActivity.this, R_ID);
                    }
                    progressBar.setVisibility(View.INVISIBLE);
                    buttonCam.setEnabled(true);
                    buttonCam.setAlpha(1f);
                    buttonSend.setEnabled(true);
                    buttonSend.setAlpha(1f);
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Handler.ShowSnack("Houve um erro","MessageActivity.PostMessage.onFailure: " + t.toString(), MessageActivity.this, R_ID);
                    progressBar.setVisibility(View.INVISIBLE);
                    buttonCam.setEnabled(true);
                    buttonCam.setAlpha(1f);
                }
            });

        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","MessageActivity.PostMessage: " + e.getMessage(), MessageActivity.this, R_ID);
            progressBar.setVisibility(View.INVISIBLE);
            buttonCam.setEnabled(true);
            buttonCam.setAlpha(1f);
        }
    }

}