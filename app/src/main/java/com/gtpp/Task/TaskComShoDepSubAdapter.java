package com.gtpp.Task;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

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

public class TaskComShoDepSubAdapter extends RecyclerView.Adapter <TaskComShoDepSubAdapter.ViewHolder> {

    private JsonArray List, FilteredList;
    private Activity activity;
    private String UserSession;
    private com.gtpp.CommonClasses.Handler Handler= new Handler();
    private int R_ID;
    private JsonObject TaskObject;
    private boolean isBindViewHolderError;
    private Button button;

    private TaskInterface taskInterface = ApiClient.getApiClient().create(TaskInterface.class);

    public TaskComShoDepSubAdapter(JsonArray jsonArray, Activity activity, String UserSession, int R_ID, JsonObject TaskObject, Button button) {
        this.List = jsonArray;
        this.FilteredList = jsonArray;
        this.activity = activity;
        this.UserSession = UserSession;
        this.R_ID = R_ID;
        this.TaskObject = TaskObject;
        this.button = button;
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int ViewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task_comshodepsub,parent,false);
        return new ViewHolder(view);
    }

    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        try {
            JsonObject jsonObject = FilteredList.get(position).getAsJsonObject();

            viewHolder.textViewID.setText(jsonObject.get("id").getAsString());
            viewHolder.textViewDescription.setText(jsonObject.get("description").getAsString());

            if(jsonObject.has("check")){ //Only to Department
                viewHolder.checkBox.setChecked(jsonObject.get("check").getAsBoolean());
                viewHolder.checkBox.setOnClickListener(v->{
                    PutCheck(viewHolder.checkBox, jsonObject.get("id").getAsInt(), position);
                });
            }else {
                viewHolder.checkBox.setVisibility(View.INVISIBLE);
            }

        }catch (Exception e){
            if(!isBindViewHolderError) {
                Handler.ShowSnack("Houve um erro", "TaskComShoDepSubAdapter.onBindViewHolder: " + e.getMessage(), activity, R_ID, true);
                isBindViewHolderError=true;
            }
        }
    }

    public int getItemCount() {
        return FilteredList.size();
    }

    private int getItemChecked(){
        int count = 0;
        for (int i = 0; i < List.size(); i++) {
            if(List.get(i).getAsJsonObject().get("check").getAsBoolean()){
                count+=1;
            }
        }
        return count;
    }

    private String getSingleChecked(){
        for (int i = 0; i < List.size(); i++) {
            if(List.get(i).getAsJsonObject().get("check").getAsBoolean()){
                return (List.get(i).getAsJsonObject().get("description").getAsString());
            }
        }
        return null;
    }

    public JsonObject getItem(int position) {
        return FilteredList.get(position).getAsJsonObject();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView textViewID, textViewDescription;
        CheckBox checkBox;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewID = itemView.findViewById(R.id.itemTaskComShoDepSub_TextViewID);
            textViewDescription = itemView.findViewById(R.id.itemTaskComShoDepSub_TextViewDescription);
            checkBox = itemView.findViewById(R.id.itemTaskComShoDepSub_CheckBox);
        }
    }

    public Filter getFilter() {
        return new Filter()
        {
            protected FilterResults performFiltering(CharSequence charSequence)
            {
                FilterResults results = new FilterResults();

                if(charSequence == null || charSequence.length() == 0)
                {
                    results.values = List;
                    results.count = List.size();
                }
                else
                {

                    JsonArray jsonArray = new JsonArray();

                    int i = 0;
                    try {

                        while (i < FilteredList.size()) {
                            String A = FilteredList.get(i).getAsJsonObject().get("description").getAsString().toLowerCase();
                            String B = charSequence.toString().toLowerCase();
                            if (A.contains(B)) {
                                jsonArray.add(FilteredList.get(i).getAsJsonObject());
                            }
                            i++;
                        }

                        results.values = jsonArray;
                        results.count = jsonArray.size();
                    }catch (Exception e){
                        Handler.ShowSnack("Houve um erro", "TaskComShoDepSubAdapter.getFilter: " + e.getMessage(), activity, R_ID, true);
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

    private void PutCheck(CheckBox checkBox, int DepartmentID, int position){
        try {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("task_id",TaskObject.get("id").getAsInt());
            jsonObject.addProperty("company_id",TaskObject.get("company_id").getAsInt());
            jsonObject.addProperty("shop_id",TaskObject.get("shop_id").getAsInt());
            jsonObject.addProperty("depart_id",DepartmentID);
            Call<JsonObject> call = taskInterface.PostComShoDepSub(getAppID(),
                    UserSession,
                    jsonObject
            );
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    try{
                        if (!Handler.isRequestError(response,activity,R_ID)) {
                            List.get(position).getAsJsonObject().addProperty("check",checkBox.isChecked());
                            if (getItemChecked() == 1){
                                button.setText(getSingleChecked());
                            }else if (getItemChecked() == 0){
                                button.setText("Depart");
                            }else{
                                button.setText(getItemChecked()+" Depart");
                            }

                        }else{
                            checkBox.setChecked(!checkBox.isChecked());
                        }
                    }catch (Exception e) {
                        Handler.ShowSnack("Houve um erro","TaskComShoDepSubAdapter.PutCheck.onResponse: " + e.getMessage(), activity, R_ID,true);
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Handler.ShowSnack("Houve um erro","TaskComShoDepSubAdapter.PutCheck.onFailure: " + t.toString(), activity, R_ID,true);
                }
            });

        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","TaskComShoDepSubAdapter.PutCheck: " + e.getMessage(), activity, R_ID,true);
        }
    }

}
