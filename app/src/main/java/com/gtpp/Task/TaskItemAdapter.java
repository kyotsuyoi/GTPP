package com.gtpp.Task;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gtpp.CommonClasses.Handler;
import com.gtpp.CommonClasses.SavedUser;
import com.gtpp.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskItemAdapter extends RecyclerView.Adapter <TaskItemAdapter.ViewHolder> {

    private JsonArray List, FilteredList;
    private Activity activity;
    private TaskInterface taskInterface;
    private SavedUser SU;
    private com.gtpp.CommonClasses.Handler Handler= new Handler();
    private int R_ID;
    private boolean isBindViewHolderError, Lock;
    private TextView textViewPercent;
    private ProgressBar progressBar;
    private JsonObject taskObject;
    private Button buttonState;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView recyclerView;

    public TaskItemAdapter(JsonArray list, Activity activity, TaskInterface taskInterface, SavedUser SU, int R_ID,
                           TextView textViewPercent, ProgressBar progressBar, Button buttonState, JsonObject taskObject, RecyclerView recyclerView, boolean Lock) {
        this.List = list;
        this.FilteredList = list;
        this.activity = activity;
        this.taskInterface = taskInterface;
        this.SU = SU;
        this.R_ID = R_ID;
        this.textViewPercent = textViewPercent;
        this.progressBar = progressBar;
        this.taskObject = taskObject;
        this.buttonState = buttonState;
        this.Lock = Lock;
        this.recyclerView = recyclerView;
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int ViewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_checklist_recycler_item,parent,false);
        return new ViewHolder(view);
    }

    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        try {
            JsonObject jsonObject = FilteredList.get(position).getAsJsonObject();
            int ID =  jsonObject.get("id").getAsInt();
            viewHolder.checkBox.setText(jsonObject.get("description").getAsString());

            //int StateID = taskObject.get("state_id").getAsInt();

            if(Lock){
                viewHolder.checkBox.setEnabled(false);
            }else{
                viewHolder.checkBox.setEnabled(true);
            }

            if((taskObject.get("user_id").getAsInt() != SU.getId() && !SU.isAdministrator()) || Lock){
                viewHolder.button.setEnabled(false);
                viewHolder.button.setAlpha(0.1f);
            }else{
                viewHolder.button.setEnabled(true);
                viewHolder.button.setAlpha(1.0f);
            }

            viewHolder.checkBox.setOnClickListener(v->{
                PutChecklist(ID, viewHolder.checkBox, position);
            });

            viewHolder.button.setOnClickListener(v->{
                DialogDeleteTaskItem(ID, position);
            });

            boolean check =jsonObject.get("check").getAsBoolean();

            viewHolder.checkBox.setChecked(check);

        }catch (Exception e){
            if(!isBindViewHolderError) {
                Handler.ShowSnack("Houve um erro", "CheckListAdapter.onBindViewHolder: " + e.getMessage(), activity, R_ID, true);
                isBindViewHolderError=true;
            }
        }
    }

    public int getItemCount() {
        return FilteredList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        CheckBox checkBox;
        Button button;

        public ViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.taskChecklistRecyclerItem_checkBox);
            button = itemView.findViewById(R.id.taskChecklistRecyclerItem_button);
        }
    }

    private void DialogDeleteTaskItem(int ID, int position){
        try {
            AlertDialog.Builder alert = new AlertDialog.Builder(activity);
            alert.setMessage("Deseja realmente excluir?");
            alert.setCancelable(false);

            alert.setNegativeButton("Não", (dialog, which) -> dialog.cancel());
            alert.setPositiveButton("Sim", (dialog, which) -> DeleteTaskItem(ID,position));
            alert.create();
            alert.setTitle("Excluir");
            alert.show();

        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","CheckListAdapter.DeleteChecklist: " + e.getMessage(), activity, R_ID,true);
        }
    }

    private void PutChecklist(int ID, CheckBox checkBox, int position){
        try {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("task_id",taskObject.get("id").getAsInt());
            jsonObject.addProperty("check",checkBox.isChecked());
            jsonObject.addProperty("id",ID);
            Call<JsonObject> call = taskInterface.PutTaskItem(
                    SU.getSession(),
                    jsonObject
            );

            call.enqueue(new Callback<JsonObject>() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    try{
                        if (!Handler.isRequestError(response, activity, R_ID)){
                        JsonObject jsonObject = response.body();
                        int percent = jsonObject.get("percent").getAsInt();
                        int StateID = jsonObject.get("state_id").getAsInt();
                        String StateDescription = jsonObject.get("state_description").getAsString();
                        String StateColor = "#"+jsonObject.get("state_color").getAsString();
                        textViewPercent.setText(percent+"%");
                        progressBar.setProgress(jsonObject.get("percent").getAsInt(),true);

                        buttonState.setText(StateDescription);
                        buttonState.setBackgroundColor(Color.parseColor(StateColor));

                        taskObject.addProperty("state_id",StateID);

                        List.get(position).getAsJsonObject().addProperty("check",checkBox.isChecked());
                        FilteredList.get(position).getAsJsonObject().addProperty("check",checkBox.isChecked());

                        }else{
                            checkBox.setChecked(!checkBox.isChecked());
                        }
                    }catch (Exception e) {
                        Handler.ShowSnack("Houve um erro","CheckListAdapter.PutChecklist.onResponse: " + e.getMessage(), activity, R_ID,true);
                        checkBox.setChecked(!checkBox.isChecked());
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Handler.ShowSnack("Houve um erro","CheckListAdapter.PutChecklist.onFailure: " + t.toString(), activity, R_ID,true);
                    checkBox.setChecked(!checkBox.isChecked());
                }
            });
        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","CheckListAdapter.PutChecklist: " + e.getMessage(), activity, R_ID,true);
            checkBox.setChecked(!checkBox.isChecked());
        }
    }

    public void RemoveItem(int ID){
        int i = 0;
        while (i < List.size()){
            if(List.get(i).getAsJsonObject().get("id").getAsInt()== ID){
                List.remove(i);
                return;
            }
            i++;
        }
        i=0;
        while (i < FilteredList.size()){
            if(FilteredList.get(i).getAsJsonObject().get("id").getAsInt()== ID){
                FilteredList.remove(i);
                return;
            }
            i++;
        }
    }

    public void setNewItem(JsonObject jsonObject){
        List.add(jsonObject);
        FilteredList = List;
    }

    private void DeleteTaskItem(int ID, int position){
        try {
            Call<JsonObject> call = taskInterface.DeleteTaskItem(
                    SU.getSession(),
                    taskObject.get("id").getAsInt(),
                    ID
            );
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (!Handler.isRequestError(response, activity, R_ID)){
                        try{
                            JsonObject jsonObject = response.body();

                            int StateID = jsonObject.get("state_id").getAsInt();
                            String StateDescription = jsonObject.get("state_description").getAsString();
                            String StateColor = "#"+jsonObject.get("state_color").getAsString();
                            buttonState.setText(StateDescription);
                            buttonState.setBackgroundColor(Color.parseColor(StateColor));
                            taskObject.addProperty("state_id", StateID);

                            textViewPercent.setText(jsonObject.get("percent").getAsString()+"%");
                            RemoveItem(ID);
                            notifyItemRemoved(position);
                            layoutManager = new LinearLayoutManager(activity);
                            recyclerView.setLayoutManager(layoutManager);
                            recyclerView.setHasFixedSize(true);
                            //notifyDataSetChanged();
                            Handler.ShowSnack("Item removido",null, activity, R_ID,false);

                        }catch (Exception e) {
                            Handler.ShowSnack("Houve um erro","CheckListAdapter.DeleteTaskItem.onResponse: " + e.getMessage(), activity, R_ID,true);
                        }
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Handler.ShowSnack("Houve um erro","CheckListAdapter.DeleteTaskItem.onFailure: " + t.toString(), activity, R_ID,true);
                }
            });
        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","CheckListAdapter.DeleteTaskItem: " + e.getMessage(), activity, R_ID,true);
        }
    }

    public void SetLock(boolean Lock){
        this.Lock = Lock;
        notifyDataSetChanged();
    }

    public int getItemID(int position){ return FilteredList.get(position).getAsJsonObject().get("id").getAsInt(); }

    public String getItemDescription(int position){
        return FilteredList.get(position).getAsJsonObject().get("description").getAsString();
    }

    public void setItemDescription(String Description, int position){
        FilteredList.get(position).getAsJsonObject().addProperty("description", Description);
        notifyItemChanged(position);
    }
}
