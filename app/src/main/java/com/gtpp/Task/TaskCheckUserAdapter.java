package com.gtpp.Task;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.gtpp.CommonClasses.ApiClient;
import com.gtpp.CommonClasses.Handler;
import com.gtpp.Main.MainInterface;
import com.gtpp.R;

import java.io.File;
import java.io.FileOutputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.gtpp.CommonClasses.Handler.getAppID;

public class TaskCheckUserAdapter extends RecyclerView.Adapter <TaskCheckUserAdapter.ViewHolder> {

    private JsonArray List, FilteredList;
    private Activity activity;
    private TaskInterface taskInterface;
    private String UserSession;
    private com.gtpp.CommonClasses.Handler Handler= new Handler();
    private int R_ID, TaskID;
    private boolean isBindViewHolderError;
    private TaskGuestAdapter taskGuestAdapter;
    private MainInterface mainInterface = ApiClient.getApiClient().create(MainInterface.class);

    public TaskCheckUserAdapter(JsonArray list, Activity activity, TaskInterface taskInterface, String UserSession, int R_ID, int TaskID, TaskGuestAdapter taskGuestAdapter) {
        this.List = list;
        this.FilteredList = list;
        this.activity = activity;
        this.taskInterface = taskInterface;
        this.UserSession = UserSession;
        this.R_ID = R_ID;
        this.TaskID = TaskID;
        this.taskGuestAdapter = taskGuestAdapter;
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int ViewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task_check_user,parent,false);
        return new ViewHolder(view);
    }

    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        try {
            JsonObject jsonObject = FilteredList.get(position).getAsJsonObject();
            int UserID = jsonObject.get("user_id").getAsInt();
            viewHolder.checkBox.setText(jsonObject.get("name").getAsString());
            viewHolder.checkBox.setChecked(jsonObject.get("check").getAsBoolean());

            viewHolder.imageView.setImageBitmap(null);
            GetEmployeePhoto(viewHolder.imageView,UserID);

            viewHolder.checkBox.setOnClickListener(v->{
                PutUser(UserID, viewHolder.checkBox,position);
            });

        }catch (Exception e){
            if(!isBindViewHolderError) {
                Handler.ShowSnack("Houve um erro", "TaskCheckUserAdapter.onBindViewHolder: " + e.getMessage(), activity, R_ID);
                isBindViewHolderError=true;
            }
        }
    }

    public int getItemCount() {
        try {
            if(FilteredList == null){
                FilteredList = List;
            }
            return FilteredList.size();
        }catch (Exception e){
            Handler.ShowSnack("Houve um erro", "TaskCheckUserAdapter.getItemCount: " + e.getMessage(), activity, R_ID);
        }
        return 0;
    }

    private void UpdateCheck(JsonObject newJsonObject){
        for (int i = 0; i < List.size(); i++) {
            if(newJsonObject.get("user_id").getAsInt() == List.get(i).getAsJsonObject().get("user_id").getAsInt()){
                List.set(i,newJsonObject);
                FilteredList = List;
                return;
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;
        CheckBox checkBox;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.itemTaskCheckUser_ImageView);
            checkBox = itemView.findViewById(R.id.itemTaskCheckUser_CheckBox);
        }
    }

    private void GetEmployeePhoto(ImageView imageView, int UserID){
        try {
            String path = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath();
            File file = new File(path + "/" + UserID + "_" + ".jpg");

            if(file.exists()){
                imageView.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
                return;
            }

            Call<JsonObject> call = mainInterface.GetEmployeePhoto(getAppID(), UserSession,UserID);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    try {
                        if (!Handler.isRequestError(response,activity,R_ID)){
                            JsonObject jsonObject = response.body();

                                if(jsonObject.get("photo") != JsonNull.INSTANCE){
                                    String photo = jsonObject.get("photo").getAsString();
                                    Bitmap bitmap = Handler.ImageDecode(photo);
                                    FileOutputStream fos = new FileOutputStream(file);
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                                    bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                                    imageView.setImageBitmap(bitmap);
                                }

                        }
                    }catch (Exception e){
                        if(!isBindViewHolderError) {
                            Handler.ShowSnack("Houve um erro", "TaskCheckUserAdapter.GetEmployeePhoto.onResponse: " + e.getMessage(), activity, R_ID);
                            isBindViewHolderError=true;
                        }
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    if(!isBindViewHolderError) {
                        Handler.ShowSnack("Houve um erro", "TaskCheckUserAdapter.GetEmployeePhoto.onFailure: " + t.toString(), activity, R_ID);
                        isBindViewHolderError=true;
                    }
                }
            });

        }catch (Exception e){
            if(!isBindViewHolderError) {
                Handler.ShowSnack("Houve um erro", "TaskCheckUserAdapter.GetEmployeePhoto: " + e.getMessage(), activity, R_ID);
                isBindViewHolderError=true;
            }
        }
    }

    private void PutUser(int UserID, CheckBox checkBox, int position){
        try {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("task_id",TaskID);
            jsonObject.addProperty("user_id",UserID);
            Call<JsonObject> call = taskInterface.PutTaskUser(getAppID(),
                    UserSession,
                    jsonObject
            );

            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    try{
                        if (!Handler.isRequestError(response, activity, R_ID)){
                            JsonObject jsonObject = response.body();

                            JsonObject newItemToGuest = new JsonObject();
                            newItemToGuest.addProperty("name",checkBox.getText().toString());
                            newItemToGuest.addProperty("user_id", UserID);
                            newItemToGuest.addProperty("task_id", TaskID);
                            newItemToGuest.addProperty("status", checkBox.isChecked());

                            if(jsonObject.get("message").getAsString().contains("Add data success")) {
                                taskGuestAdapter.setNewItem(newItemToGuest);
                            }else if(jsonObject.get("message").getAsString().contains("Delete success")){
                                taskGuestAdapter.removeItem(UserID);
                            }

                            JsonObject newJsonObject = new JsonObject();
                            newJsonObject.addProperty("name",checkBox.getText().toString());
                            newJsonObject.addProperty("user_id", UserID);
                            newJsonObject.addProperty("check", checkBox.isChecked());
                            //FilteredList.set(position, newItem);
                            //notifyDataSetChanged();
                            UpdateCheck(newJsonObject);

                        }else{
                            checkBox.setChecked(!checkBox.isChecked());
                        }
                    }catch (Exception e) {
                        Handler.ShowSnack("Houve um erro","TaskCheckUserAdapter.PutUser.onResponse: " + e.getMessage(), activity, R_ID);
                        checkBox.setChecked(!checkBox.isChecked());
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Handler.ShowSnack("Houve um erro","TaskCheckUserAdapter.PutUser.onFailure: " + t.toString(), activity, R_ID);
                    checkBox.setChecked(!checkBox.isChecked());
                }
            });
        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","TaskCheckUserAdapter.PutUser: " + e.getMessage(), activity, R_ID);
            checkBox.setChecked(!checkBox.isChecked());
        }
    }

    public Filter getFilter() {
        return new Filter()
        {
            protected FilterResults performFiltering(CharSequence charSequence)
            {
                FilterResults results = new FilterResults();

                if(charSequence == null || charSequence.length() == 0){
                    results.values = List;
                    results.count = List.size();
                }else{
                    JsonArray jsonArray = new JsonArray();

                    int i = 0;
                    try {

                        while (i < List.size()) {
                            String A = List.get(i).getAsJsonObject().get("name").getAsString().toLowerCase();
                            String B = charSequence.toString().toLowerCase();
                            if (A.contains(B)) {
                                jsonArray.add(List.get(i).getAsJsonObject());
                            }
                            i++;
                        }

                        results.values = jsonArray;
                        results.count = jsonArray.size();
                    }catch (Exception e){
                        Handler.ShowSnack("Houve um erro", "TaskCheckUserAdapter.getFilter: " + e.getMessage(), activity, R_ID);
                    }
                }
                return results;
            }

            protected void publishResults(CharSequence charSequence, FilterResults filterResults)
            {
                FilteredList = (JsonArray) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

}
