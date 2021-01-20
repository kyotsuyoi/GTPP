package com.gtpp.Message;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.gtpp.CommonClasses.ApiClient;
import com.gtpp.CommonClasses.ApiClientForImage;
import com.gtpp.CommonClasses.Handler;
import com.gtpp.CommonClasses.SavedUser;
import com.gtpp.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

import static com.gtpp.CommonClasses.Handler.getAppID;

public class MessageAdapter extends RecyclerView.Adapter <MessageAdapter.ViewHolder> {

    private JsonArray List;
    private Activity activity;
    private SavedUser SU;
    private com.gtpp.CommonClasses.Handler Handler= new Handler();
    private int R_ID;
    private boolean isBindViewHolderError;
    private int TaskID;
    private MessageInterface messageInterface = ApiClient.getApiClient().create(MessageInterface.class);
    private MessageInterface messageInterfaceForImage = ApiClientForImage.getApiClient().create(MessageInterface.class);

    public MessageAdapter(JsonArray list, Activity activity, SavedUser SU, int R_ID, int TaskID) {
        this.List = list;
        this.activity = activity;
        this.SU = SU;
        this.R_ID = R_ID;
        this.TaskID = TaskID;
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int ViewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message,parent,false);
        return new ViewHolder(view);
    }

    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        try {
            JsonObject jsonObject = List.get(position).getAsJsonObject();

            viewHolder.cardView.setVisibility(View.INVISIBLE);
            viewHolder.cardViewUser.setVisibility(View.INVISIBLE);

            viewHolder.textViewName.setText("");
            viewHolder.textViewMessage.setText("");
            viewHolder.textViewDateTime.setText("");
            viewHolder.imageViewPhoto.setImageBitmap(null);
            viewHolder.progressBar.setVisibility(View.INVISIBLE);

            viewHolder.textViewUserName.setText("");
            viewHolder.textViewUserMessage.setText("");
            viewHolder.textViewUserDateTime.setText("");
            viewHolder.imageViewUserPhoto.setImageBitmap(null);
            viewHolder.progressBarUser.setVisibility(View.INVISIBLE);

            if (jsonObject.has("user_name")){
                int MessageID = jsonObject.get("id").getAsInt();
                //int UserID = jsonObject.get("user_id").getAsInt();

                if(jsonObject.get("user_name").getAsString().equals(SU.getUser())){
                    viewHolder.textViewUserName.setText(jsonObject.get("user_name").getAsString() + "           ");

                    if(jsonObject.get("description") != JsonNull.INSTANCE) {
                        viewHolder.textViewUserMessage.setText(jsonObject.get("description").getAsString());
                    }
                    viewHolder.cardViewUser.setVisibility(View.VISIBLE);

                    FormatDateTime(jsonObject.get("date_time").getAsString(),viewHolder.textViewUserDateTime);

                    if(jsonObject.get("image").getAsString().equals("1")){
                        GetMessageImage(MessageID, viewHolder.imageViewUserPhoto,viewHolder.progressBarUser);
                    }
                }else {
                    viewHolder.textViewName.setText(jsonObject.get("user_name").getAsString() + "           ");

                    if(jsonObject.get("description") != JsonNull.INSTANCE) {
                        viewHolder.textViewMessage.setText(jsonObject.get("description").getAsString());
                    }
                    viewHolder.cardView.setVisibility(View.VISIBLE);

                    FormatDateTime(jsonObject.get("date_time").getAsString(),viewHolder.textViewDateTime);

                    if(jsonObject.get("image").getAsString() .equals("1")){
                        GetMessageImage(MessageID, viewHolder.imageViewPhoto,viewHolder.progressBar);
                    }
                }

            }else{
                viewHolder.textViewName.setText("Sistema: ");
                viewHolder.textViewMessage.setText(jsonObject.get("description").getAsString());
                viewHolder.cardView.setVisibility(View.VISIBLE);
            }

        }catch (Exception e){
            if(!isBindViewHolderError) {
                Handler.ShowSnack("Houve um erro", "MessageAdapter.onBindViewHolder: " + e.getMessage()+"\n"+position, activity, R_ID);
                isBindViewHolderError=true;
            }
        }
    }

    public int getItemCount() {
        return List.size();
    }

    public JsonObject getItem(int position) {
        return List.get(position).getAsJsonObject();
    }

    public void RemoveItem(int position) {
        List.remove(position);
        notifyItemRemoved(position);
    }

    public void RemoveItemByID(int ID) {
        int count = 0;
        while (count < getItemCount()){
            int MessageID = getItem(count).getAsJsonObject().get("id").getAsInt();
            if(MessageID == ID){
                RemoveItem(count);
                return;
            }
            count++;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView textViewName, textViewMessage, textViewDateTime, textViewUserName, textViewUserMessage, textViewUserDateTime;
        CardView cardView, cardViewUser;
        ImageView imageViewUserPhoto, imageViewPhoto;
        ProgressBar progressBar, progressBarUser;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.itemMessage_TextViewName);
            textViewMessage = itemView.findViewById(R.id.itemMessage_TextViewMessage);
            textViewDateTime = itemView.findViewById(R.id.itemMessage_TextViewDateTime);
            textViewUserName = itemView.findViewById(R.id.itemMessage_TextViewUserName);
            textViewUserMessage = itemView.findViewById(R.id.itemMessage_TextViewUserMessage);
            textViewUserDateTime = itemView.findViewById(R.id.itemMessage_TextViewUserDateTime);
            cardView = itemView.findViewById(R.id.itemMessage_CardView);
            cardViewUser = itemView.findViewById(R.id.itemMessage_CardViewUser);
            imageViewUserPhoto = itemView.findViewById(R.id.itemMessage_ImageViewUserPhoto);
            imageViewPhoto = itemView.findViewById(R.id.itemMessage_ImageViewPhoto);

            progressBar = itemView.findViewById(R.id.itemMessage_ProgressBar);
            progressBarUser = itemView.findViewById(R.id.itemMessage_ProgressBarUser);
        }
    }

    public void setNewItem(JsonObject jsonObject){
        List.add(jsonObject);
        notifyItemInserted(List.size());
    }

    private void GetMessageImage(int MessageID, ImageView imageView, ProgressBar progressBar){
        try {
            progressBar.setVisibility(View.VISIBLE);
            String path = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath();
            File file = new File(path + "/Message_" + TaskID + "_" + MessageID + ".jpg");

            if(file.exists()){
                /*Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                bitmap = Handler.ImageOrientation(bitmap,file);
                imageView.setImageBitmap(bitmap);*/
                Picasso.get()
                        .load(file.getAbsoluteFile())
                        .resize(500,500)
                        .centerCrop()
                        .into(imageView);
                progressBar.setVisibility(View.INVISIBLE);
                return;
            }

            Call<JsonObject> call = messageInterfaceForImage.GetMessageImage(getAppID(),
                    SU.getSession(),
                    MessageID
            );

            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                    if (!Handler.isRequestError(response,activity,R_ID)){
                        try {
                            JsonObject jsonObject = response.body();

                            String image = jsonObject.get("data").getAsString();
                            if (image != "") {
                                Bitmap bitmap = Handler.ImageDecode(image);
                                FileOutputStream fos = new FileOutputStream(file);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                                //bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                                //imageView.setImageBitmap(bitmap);
                                Picasso.get()
                                        .load(file.getAbsoluteFile())
                                        .resize(500,500)
                                        .centerCrop()
                                        .into(imageView);
                            }
                        }catch (Exception e){
                            Handler.ShowSnack("Houve um erro","MessageAdapter.GetMessageImage.onResponse: " + e.getMessage(), activity, R_ID);
                        }
                    }
                    progressBar.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Handler.ShowSnack("Houve um erro","MessageAdapter.GetMessageImage.onFailure: " + t.toString(), activity, R_ID);
                }
            });

        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","MessageAdapter.GetMessageImage: " + e.getMessage(), activity, R_ID);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void FormatDateTime(String StringDateTime, TextView textView){
        try {
            SimpleDateFormat curDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date date = curDateFormat.parse(StringDateTime);
            SimpleDateFormat postDateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String newDateString = postDateFormat.format(date);

            Date time = curDateFormat.parse(StringDateTime);
            SimpleDateFormat postTimeFormat = new SimpleDateFormat("hh:mm:ss");
            String newTimeString = postTimeFormat.format(time);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String NOW = sdf.format(new Date());

            if (StringDateTime.contains(NOW)) {
                textView.setText(newTimeString);
            } else {
                textView.setText(newDateString);
            }
        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","MessageAdapter.FormatDateTime: " + e.getMessage(), activity, R_ID);
        }
    }
}
