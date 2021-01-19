package com.gtpp.Task;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.gtpp.CommonClasses.Handler;
import com.gtpp.CommonClasses.SavedUser;
import com.gtpp.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.gtpp.CommonClasses.Handler.getAppID;

public class TaskItemAdapter extends RecyclerView.Adapter <TaskItemAdapter.ViewHolder> {

    private JsonArray List, FilteredList;
    private Activity activity;
    private TaskInterface taskInterface;
    private SavedUser SU;
    private com.gtpp.CommonClasses.Handler Handler = new Handler();
    private int R_ID;
    private boolean isBindViewHolderError, Lock;
    private TextView textViewPercent;
    private ProgressBar progressBar;
    private JsonObject taskObject;
    private Button buttonState;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView recyclerView;
    private JsonArray stateList;

    private int LayoutHeight = 80;

    public TaskItemAdapter(JsonArray list, Activity activity, TaskInterface taskInterface, SavedUser SU, int R_ID,
                           TextView textViewPercent, ProgressBar progressBar, Button buttonState, JsonObject taskObject, RecyclerView recyclerView, JsonArray stateList, boolean Lock) {
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
        this.stateList = stateList;
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int ViewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task_checklist_recycler,parent,false);
        return new ViewHolder(view);
    }

    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        try {
            JsonObject jsonObject = FilteredList.get(position).getAsJsonObject();
            int ID =  jsonObject.get("id").getAsInt();
            int YesNo = jsonObject.get("yes_no").getAsInt();
            viewHolder.textViewDescription.setText(jsonObject.get("description").getAsString());
            viewHolder.textViewOrder.setText(jsonObject.get("order").getAsString());

            //int StateID = taskObject.get("state_id").getAsInt();

            if(Lock){
                viewHolder.checkBox.setEnabled(false);
                viewHolder.checkBoxYes.setEnabled(false);
                viewHolder.checkBoxNo.setEnabled(false);
            }else{
                viewHolder.checkBox.setEnabled(true);
                viewHolder.checkBoxYes.setEnabled(true);
                viewHolder.checkBoxNo.setEnabled(true);
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

            if(YesNo == 1){
                viewHolder.checkBoxYes.setChecked(true);
                viewHolder.checkBoxNo.setChecked(false);
                viewHolder.constraintLayout.setMaxHeight(LayoutHeight);
                viewHolder.checkBox.setVisibility(View.INVISIBLE);
            }else if(YesNo == 2){
                viewHolder.checkBoxYes.setChecked(false);
                viewHolder.checkBoxNo.setChecked(true);
                viewHolder.constraintLayout.setMaxHeight(LayoutHeight);
                viewHolder.checkBox.setVisibility(View.INVISIBLE);
            }else if(YesNo == -1) {
                viewHolder.checkBoxYes.setChecked(false);
                viewHolder.checkBoxNo.setChecked(false);
                viewHolder.constraintLayout.setMaxHeight(LayoutHeight);
                viewHolder.checkBox.setVisibility(View.INVISIBLE);
            }else{
                viewHolder.checkBoxYes.setChecked(false);
                viewHolder.checkBoxNo.setChecked(false);
                viewHolder.constraintLayout.setMaxHeight(0);
                viewHolder.checkBox.setVisibility(View.VISIBLE);
            }

            boolean Yes = viewHolder.checkBoxYes.isChecked();
            boolean No = viewHolder.checkBoxNo.isChecked();

            viewHolder.checkBoxYes.setOnClickListener(v->{
                if(Yes) {
                    PutItemYesNo(ID, -1, viewHolder.checkBoxYes, viewHolder.checkBox, position);
                }else{
                    PutItemYesNo(ID, 1, viewHolder.checkBoxYes, viewHolder.checkBox, position);
                }
            });
            viewHolder.checkBoxNo.setOnClickListener(v->{
                if(No) {
                    PutItemYesNo(ID, -1, viewHolder.checkBoxNo, viewHolder.checkBox, position);
                }else{
                    PutItemYesNo(ID, 2, viewHolder.checkBoxNo, viewHolder.checkBox, position);
                }
            });

        }catch (Exception e){
            if(!isBindViewHolderError) {
                Handler.ShowSnack("Houve um erro", "TaskItemAdapter.onBindViewHolder: " + e.getMessage(), activity, R_ID);
                isBindViewHolderError=true;
            }
        }
    }

    public int getItemCount() {
        return FilteredList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        CheckBox checkBox, checkBoxYes, checkBoxNo;
        Button button;
        TextView textViewOrder, textViewDescription;
        ConstraintLayout constraintLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.taskChecklistRecyclerItem_CheckBox);
            button = itemView.findViewById(R.id.taskChecklistRecyclerItem_Button);
            textViewOrder = itemView.findViewById(R.id.taskChecklistRecyclerItem_TextViewOrder);
            textViewDescription = itemView.findViewById(R.id.taskChecklistRecyclerItem_TextViewDescription);
            checkBoxYes = itemView.findViewById(R.id.taskChecklistRecyclerItem_CheckBoxYes);
            checkBoxNo = itemView.findViewById(R.id.taskChecklistRecyclerItem_CheckBoxNo);
            constraintLayout = itemView.findViewById(R.id.taskChecklistRecyclerItem_ConstraintLayout);
        }
    }

    private void DialogDeleteTaskItem(int ID, int position){
        try {
            AlertDialog.Builder alert = new AlertDialog.Builder(activity);
            alert.setMessage("Deseja realmente excluir?");
            alert.setCancelable(false);

            alert.setNegativeButton("Não", (dialog, which) -> dialog.cancel());
            alert.setPositiveButton("Sim", (dialog, which) -> DeleteItem(ID,position));
            alert.create();
            alert.setTitle("Excluir");
            alert.show();

        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","TaskItemAdapter.DeleteChecklist: " + e.getMessage(), activity, R_ID);
        }
    }

    private void PutChecklist(int ID, CheckBox checkBox, int position){
        try {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("task_id",taskObject.get("id").getAsInt());
            jsonObject.addProperty("check",checkBox.isChecked());
            jsonObject.addProperty("id",ID);
            Call<JsonObject> call = taskInterface.PutTaskItem(getAppID(),
                    SU.getSession(),
                    jsonObject
            );

            call.enqueue(new Callback<JsonObject>() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    try{
                        if (!Handler.isRequestError(response, activity, R_ID)){
                            JsonObject jsonObject = response.body().get("data").getAsJsonObject();
                            int percent = jsonObject.get("percent").getAsInt();
                            int StateID = jsonObject.get("state_id").getAsInt();

                            JsonObject jsonState = new JsonObject();
                            for (int i = 0; i < stateList.size(); i++) {
                                if(stateList.get(i).getAsJsonObject().get("id").getAsInt() == StateID){
                                    jsonState = stateList.get(i).getAsJsonObject();
                                    i=stateList.size();
                                }
                            }

                            String StateDescription = jsonState.get("description").getAsString();
                            String StateColor = "#"+jsonState.get("color").getAsString();

                            String s_percent = percent+"%";
                            textViewPercent.setText(s_percent);
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
                        Handler.ShowSnack("Houve um erro","TaskItemAdapter.PutChecklist.onResponse: " + e.getMessage(), activity, R_ID);
                        checkBox.setChecked(!checkBox.isChecked());
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Handler.ShowSnack("Houve um erro","TaskItemAdapter.PutChecklist.onFailure: " + t.toString(), activity, R_ID);
                    checkBox.setChecked(!checkBox.isChecked());
                }
            });
        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","TaskItemAdapter.PutChecklist: " + e.getMessage(), activity, R_ID);
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

    public void ItemDown(int position){
        JsonObject jsonObject = FilteredList.get(position).getAsJsonObject();
        int order = jsonObject.get("order").getAsInt();
        JsonObject jsonObjectNext = FilteredList.get(position+1).getAsJsonObject();
        int orderNext = jsonObjectNext.get("order").getAsInt();
        jsonObject.addProperty("order",orderNext);
        jsonObjectNext.addProperty("order",order);
        FilteredList.set(position, jsonObjectNext);
        FilteredList.set(position+1,jsonObject);
        notifyItemChanged(position);
        notifyItemChanged(position+1);
    }

    public void ItemUp(int position){
        JsonObject jsonObject = FilteredList.get(position).getAsJsonObject();
        int order = jsonObject.get("order").getAsInt();
        JsonObject jsonObjectPrevious = FilteredList.get(position-1).getAsJsonObject();
        int orderNext = jsonObjectPrevious.get("order").getAsInt();
        jsonObject.addProperty("order",orderNext);
        jsonObjectPrevious.addProperty("order",order);
        FilteredList.set(position, jsonObjectPrevious);
        FilteredList.set(position-1,jsonObject);
        notifyItemChanged(position);
        notifyItemChanged(position-1);
    }

    private void DeleteItem(int ID, int position){
        try {
            Call<JsonObject> call = taskInterface.DeleteTaskItem(getAppID(),
                    SU.getSession(),
                    taskObject.get("id").getAsInt(),
                    ID
            );
            call.enqueue(new Callback<JsonObject>() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (!Handler.isRequestError(response, activity, R_ID)){
                        try{
                            JsonObject jsonObject = response.body().get("data").getAsJsonObject();

                            int percent = jsonObject.get("percent").getAsInt();
                            int StateID = jsonObject.get("state_id").getAsInt();

                            JsonObject jsonState = new JsonObject();
                            for (int i = 0; i < stateList.size(); i++) {
                                if(stateList.get(i).getAsJsonObject().get("id").getAsInt() == StateID){
                                    jsonState = stateList.get(i).getAsJsonObject();
                                    i=stateList.size();
                                }
                            }

                            String StateDescription = jsonState.get("state_description").getAsString();
                            String StateColor = "#"+jsonState.get("state_color").getAsString();
                            buttonState.setText(StateDescription);
                            buttonState.setBackgroundColor(Color.parseColor(StateColor));
                            taskObject.addProperty("state_id", StateID);

                            String s_percent = percent+"%";
                            textViewPercent.setText(s_percent);
                            progressBar.setProgress(percent,true);
                            RemoveItem(ID);
                            notifyItemRemoved(position);
                            layoutManager = new LinearLayoutManager(activity);
                            recyclerView.setLayoutManager(layoutManager);
                            recyclerView.setHasFixedSize(true);
                            //notifyDataSetChanged();
                            Handler.ShowSnack("Item removido",null, activity, R_ID);

                        }catch (Exception e) {
                            Handler.ShowSnack("Houve um erro","TaskItemAdapter.DeleteItem.onResponse: " + e.getMessage(), activity, R_ID);
                        }
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Handler.ShowSnack("Houve um erro","TaskItemAdapter.DeleteItem.onFailure: " + t.toString(), activity, R_ID);
                }
            });
        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","TaskItemAdapter.DeleteItem: " + e.getMessage(), activity, R_ID);
        }
    }

    public void SetLock(boolean Lock){
        this.Lock = Lock;
        notifyDataSetChanged();
    }

    public int getItemID(int position){ return FilteredList.get(position).getAsJsonObject().get("id").getAsInt(); }

    public JsonObject getItem(int position){ return FilteredList.get(position).getAsJsonObject(); }

    public String getItemDescription(int position){
        return FilteredList.get(position).getAsJsonObject().get("description").getAsString();
    }

    public void setItemDescription(String Description, int position){
        FilteredList.get(position).getAsJsonObject().addProperty("description", Description);
        notifyItemChanged(position);
    }

    private void PutItemYesNo(int ID, int YesNo, CheckBox checkBoxYesNo, CheckBox checkBox, int position){
        try {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id",ID);
            jsonObject.addProperty("yes_no",YesNo);
            jsonObject.addProperty("task_id",taskObject.get("id").getAsInt());

            Call<JsonObject> call = taskInterface.PutTaskItemYesNo(getAppID(), SU.getSession(),jsonObject);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    try {
                        if(!Handler.isRequestError(response,activity,R_ID)){
                            JsonObject jsonObject = response.body().get("data").getAsJsonObject();
                            int percent = jsonObject.get("percent").getAsInt();
                            int StateID = jsonObject.get("state_id").getAsInt();

                            JsonObject jsonState = new JsonObject();
                            for (int i = 0; i < stateList.size(); i++) {
                                if(stateList.get(i).getAsJsonObject().get("id").getAsInt() == StateID){
                                    jsonState = stateList.get(i).getAsJsonObject();
                                    i=stateList.size();
                                }
                            }

                            String StateDescription = jsonState.get("state_description").getAsString();
                            String StateColor = "#"+jsonState.get("state_color").getAsString();

                            String s_percent = percent+"%";
                            textViewPercent.setText(s_percent);
                            progressBar.setProgress(jsonObject.get("percent").getAsInt(),true);

                            buttonState.setText(StateDescription);
                            buttonState.setBackgroundColor(Color.parseColor(StateColor));

                            taskObject.addProperty("state_id",StateID);

                            List.get(position).getAsJsonObject().addProperty("check",checkBox.isChecked());
                            List.get(position).getAsJsonObject().addProperty("yes_no",YesNo);
                            FilteredList.get(position).getAsJsonObject().addProperty("check",checkBox.isChecked());
                            FilteredList.get(position).getAsJsonObject().addProperty("yes_no",YesNo);

                            notifyItemChanged(position);
                        }else{
                            checkBoxYesNo.setChecked(!checkBoxYesNo.isChecked());
                        }
                    }catch (Exception e){
                        Handler.ShowSnack("Houve um erro","TaskActivity.PutTaskItemYesNo.onResponse: " + e.getMessage(), activity, R_ID);
                        checkBoxYesNo.setChecked(!checkBoxYesNo.isChecked());
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Handler.ShowSnack("Houve um erro","TaskActivity.PutTaskItemYesNo.onFailure: " + t.toString(), activity, R_ID);
                    checkBoxYesNo.setChecked(!checkBoxYesNo.isChecked());
                }
            });

        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","TaskActivity.PutTaskItemYesNo: " + e.getMessage(), activity, R_ID);
            checkBoxYesNo.setChecked(!checkBoxYesNo.isChecked());
        }
    }
}
