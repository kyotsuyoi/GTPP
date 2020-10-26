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

public class TaskHistoricAdapter extends RecyclerView.Adapter <TaskHistoricAdapter.ViewHolder> {

    private JsonArray List;
    private Activity activity;
    private com.gtpp.CommonClasses.Handler Handler= new Handler();
    private int R_ID;
    private boolean isBindViewHolderError;

    public TaskHistoricAdapter(JsonArray jsonArray, Activity activity, int R_ID) {
        this.List = jsonArray;
        this.activity = activity;
        this.R_ID = R_ID;
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int ViewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_historic,parent,false);
        return new ViewHolder(view);
    }

    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        try {
            viewHolder.textViewDescription.setText(List.get(position).getAsJsonObject().get("description").getAsString());
            viewHolder.textViewDateTime.setText(List.get(position).getAsJsonObject().get("date_time").getAsString());
        }catch (Exception e){
            if(!isBindViewHolderError) {
                Handler.ShowSnack("Houve um erro", "TaskHistoricAdapter.onBindViewHolder: " + e.getMessage(), activity, R_ID, true);
                isBindViewHolderError=true;
            }
        }
    }

    public int getItemCount() {
        return List.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView textViewDescription, textViewDateTime;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewDescription = itemView.findViewById(R.id.itemHistoric_TextViewDescription);
            textViewDateTime = itemView.findViewById(R.id.itemHistoric_TextViewDateTime);
        }
    }

}
