package com.gtpp.Main;

import android.app.Activity;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gtpp.CommonClasses.Handler;
import com.gtpp.R;

public class StateAdapter extends RecyclerView.Adapter <StateAdapter.ViewHolder> {

    private JsonArray List;
    private com.gtpp.CommonClasses.Handler Handler= new Handler();
    private Activity activity;
    private int R_ID;
    private boolean isBindViewHolderError;

    public StateAdapter(JsonArray list, Activity activity, int R_ID) {
        this.List = list;
        this.activity = activity;
        this.R_ID = R_ID;

        for (int i = 0; i < List.size(); i++) {
            int ID = List.get(i).getAsJsonObject().get("id").getAsInt();
            if(ID == 1 || ID == 2 || ID == 3 || ID == 4 || ID == 5){
                List.get(i).getAsJsonObject().addProperty("check", true);
            }else{
                List.get(i).getAsJsonObject().addProperty("check", false);
            }
        }
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int ViewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task_state,parent,false);
        return new ViewHolder(view);
    }

    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        try {
            String Description = List.get(position).getAsJsonObject().get("description").getAsString();
            viewHolder.checkBox.setText(Description);
            viewHolder.checkBox.setChecked(List.get(position).getAsJsonObject().get("check").getAsBoolean());
            viewHolder.checkBox.setOnClickListener(v->{
                if(viewHolder.checkBox.isChecked()){
                    List.get(position).getAsJsonObject().addProperty("check", true);
                }else{
                    List.get(position).getAsJsonObject().addProperty("check", false);
                }
            });
        }catch (Exception e){
            if(!isBindViewHolderError) {
                Handler.ShowSnack("Houve um erro", "onCreateViewHolder: " + e.getMessage(), activity, R_ID, true);
                isBindViewHolderError=true;
            }
        }
    }
    public int getItemCount() {
        return List.size();
    }

    public JsonObject getItem(int position){
        return List.get(position).getAsJsonObject();
    }

    public JsonArray getItemNotChecked(){
        return List;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        CheckBox checkBox;

        public ViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.itemTaskState_CheckBox);
        }
    }
}
