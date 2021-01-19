package com.gtpp.Main;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.Filter;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gtpp.CommonClasses.Handler;
import com.gtpp.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainAdapter extends RecyclerView.Adapter <MainAdapter.ViewHolder> {

    private JsonArray List, FilteredList, stateList;
    private com.gtpp.CommonClasses.Handler Handler= new Handler();
    private Activity activity;
    private int R_ID;
    private boolean isBindViewHolderError;

    public MainAdapter(JsonArray list, JsonArray stateList, Activity activity, int R_ID) {
        this.List = list;
        this.FilteredList = list;
        this.stateList = stateList;
        this.activity = activity;
        this.R_ID = R_ID;
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int ViewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_recycler_item,parent,false);
        return new ViewHolder(view);
    }

    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        try {
            viewHolder.Exclamation.setVisibility(View.INVISIBLE);

            String Description = FilteredList.get(position).getAsJsonObject().get("description").getAsString();
            if(Description.length() > 20){
                Description = Description.substring(0,20) + "...";
            }
            viewHolder.Description.setText(Description);
        /*if(position % 2 == 0){
            viewHolder.Layout.setBackgroundResource(R.color.colorPrimary);
        }else{
            viewHolder.Layout.setBackgroundResource(R.color.colorPrimaryDark);
        }*/

            int StateID = FilteredList.get(position).getAsJsonObject().get("state_id").getAsInt();
            if (stateList != null) {
                int size = stateList.size();
                for (int i = 0; i < size; i++) {
                    JsonObject jsonObject = stateList.get(i).getAsJsonObject();
                    if (jsonObject.get("id").getAsInt() == StateID) {
                        String color = "#" + jsonObject.get("color").getAsString();
                        viewHolder.Status.setBackgroundColor(Color.parseColor(color));
                        viewHolder.Status.setText(jsonObject.get("description").getAsString());
                        i = size;
                    }
                }
            }

            AlphaAnimation PriorityAnimation = new AlphaAnimation(0.0f, 1.0f);
            PriorityAnimation.setDuration(2000);
            PriorityAnimation.setStartOffset(0);
            PriorityAnimation.setFillAfter(true);

            AlphaAnimation ExclamationAnimation = new AlphaAnimation(0.0f, 1.0f);
            ExclamationAnimation.setDuration(2000);
            ExclamationAnimation.setStartOffset(0);
            ExclamationAnimation.setFillAfter(true);

            int priority = FilteredList.get(position).getAsJsonObject().get("priority").getAsInt();
            switch (priority) {
                case 2:
                    viewHolder.Priority.setBackgroundColor(Color.RED);
                    break;
                case 1:
                    viewHolder.Priority.setBackgroundResource(R.color.colorYellow);
                    break;
                case 0:
                    viewHolder.Priority.setBackgroundResource(R.color.colorGreen);
                    break;
            }
            viewHolder.Priority.startAnimation(PriorityAnimation);

            int expire = FilteredList.get(position).getAsJsonObject().get("expire").getAsInt();

            if (StateID != 6 && StateID != 7 && expire <= 8) {
                if (expire < 8 && expire > 0) {
                    viewHolder.Exclamation.setTextColor(ContextCompat.getColor(activity, R.color.colorYellow));
                } else {
                    viewHolder.Exclamation.setTextColor(Color.RED);
                }
                viewHolder.Exclamation.setVisibility(View.VISIBLE);
                viewHolder.Exclamation.startAnimation(ExclamationAnimation);
            }

        }catch (Exception e){
            if(!isBindViewHolderError) {
                Handler.ShowSnack("Houve um erro", "onCreateViewHolder: " + e.getMessage(), activity, R_ID);
                isBindViewHolderError=true;
            }
        }
    }

    public void setNewItem(JsonObject jsonObject){
            List.add(jsonObject);
            FilteredList = List;
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

    public int getItemCount() {
        return FilteredList.size();
    }

    public void ResetFilter() {
        FilteredList = List;
    }

    public int getItemID(int position){
        return FilteredList.get(position).getAsJsonObject().get("id").getAsInt();
    }

    public int getItemState(int position){
        return FilteredList.get(position).getAsJsonObject().get("state_id").getAsInt();
    }

    public int getItemUserID(int position){
        JsonObject jsonObject = FilteredList.get(position).getAsJsonObject();
        return jsonObject.get("user_id").getAsInt();
    }

    /*public String getItemDescription(int TaskID){
        for (int i = 0; i < List.size(); i++) {
            JsonObject jsonObject = List.get(i).getAsJsonObject();
            if(jsonObject.get("id").getAsInt()==TaskID){
                return jsonObject.get("description").getAsString();
            }
        }
        return "null";
    }*/

    public void OrderBy(String tag){
        java.util.List<JsonObject> jsonObjectList = new ArrayList<JsonObject>();
        for (int i = 0; i < FilteredList.size(); i++) {
            jsonObjectList.add(FilteredList.get(i).getAsJsonObject());
        }
        Collections.sort(jsonObjectList, (a, b) -> {
            String valA = "";
            String valB = "";
            try {
                valA = a.get(tag).getAsString();
                valB = b.get(tag).getAsString();
            }
            catch (Exception e) {
                //do something
            }

            return valA.compareTo(valB);
        });
        for (int i = 0; i < FilteredList.size(); i++) {
            FilteredList.set(i,jsonObjectList.get(i).getAsJsonObject());
        }

        notifyDataSetChanged();
    }

    public JsonObject getItem(int position){
        return FilteredList.get(position).getAsJsonObject();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView Description, Exclamation;
        Button Status, Priority;
        ConstraintLayout Layout;

        public ViewHolder(View itemView) {
            super(itemView);
            Description = itemView.findViewById(R.id.mainRecyclerItem_TextViewDescription);
            Exclamation = itemView.findViewById(R.id.mainRecyclerItem_TextViewExclamation);
            Status = itemView.findViewById(R.id.mainRecyclerItem_ButtonStatus);
            Priority = itemView.findViewById(R.id.mainRecyclerItem_ButtonPriority);
            Layout = itemView.findViewById(R.id.mainRecyclerItem_Layout);
        }
    }

    public void getFilterState(JsonArray jsonArray) {
        for (int i = 0; i < FilteredList.size(); i++) {

            int StateID = FilteredList.get(i).getAsJsonObject().get("state_id").getAsInt();
            boolean checked = false;

            for (int j = 0; j < jsonArray.size(); j++) {
                if(StateID == jsonArray.get(j).getAsJsonObject().get("id").getAsInt()){
                    checked = jsonArray.get(j).getAsJsonObject().get("check").getAsBoolean();
                    j = jsonArray.size();
                }
            }

            if(!checked){
                FilteredList.remove(i);
                notifyItemRemoved(i);
                i-=1;
            }
        }
    }

    public Filter getFilterAmount(int company_id, int shop_id, int department_id) {
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

                        if(company_id!=0 && shop_id == 0 && department_id == 0){
                            while (i < FilteredList.size()) {
                                String A = FilteredList.get(i).getAsJsonObject().get("company_id").getAsString();
                                String B = String.valueOf(company_id);
                                if (A.contains(B)) {
                                    jsonArray.add(FilteredList.get(i).getAsJsonObject());
                                }
                                i++;
                            }
                        }

                        if(company_id !=0 && shop_id != 0 && department_id == 0){
                            while (i < FilteredList.size()) {
                                String A = FilteredList.get(i).getAsJsonObject().get("company_id").getAsString()
                                        + "-" + FilteredList.get(i).getAsJsonObject().get("shop_id").getAsString();
                                String B = company_id + "-" +shop_id;
                                if (A.contains(B)) {
                                    jsonArray.add(FilteredList.get(i).getAsJsonObject());
                                }
                                i++;
                            }
                        }

                        if(company_id !=0 && shop_id != 0 && department_id != 0){
                            while (i < FilteredList.size()) {
                                JsonArray depart_id = FilteredList.get(i).getAsJsonObject().get("department_id").getAsJsonArray();
                                int dept_id = 0;
                                for (int j = 0; j < depart_id.size(); j++) {
                                    if(depart_id.get(j).getAsInt()==department_id){
                                        dept_id = depart_id.get(j).getAsInt();
                                    }
                                }
                                String A = FilteredList.get(i).getAsJsonObject().get("company_id").getAsString()
                                        + "-" + FilteredList.get(i).getAsJsonObject().get("shop_id").getAsString()
                                        + "-" + dept_id;
                                String B = company_id + "-" +shop_id + "-" + department_id;
                                if (A.contains(B)) {
                                    jsonArray.add(FilteredList.get(i).getAsJsonObject());
                                }
                                i++;
                            }
                        }

                        results.values = jsonArray;
                        results.count = jsonArray.size();
                    }catch (Exception e){
                        e.getMessage();
                        //Toast.makeText(activity.getApplicationContext(), "Handler.ShowSnack: \n"+e.getMessage(), Toast.LENGTH_LONG).show();
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
                            String A = List.get(i).getAsJsonObject().get("description").getAsString().toLowerCase();
                            String B = charSequence.toString().toLowerCase();
                            if (A.contains(B)) {
                                jsonArray.add(List.get(i).getAsJsonObject());
                            }
                            i++;
                        }
                        results.values = jsonArray;
                        results.count = jsonArray.size();

                    }catch (Exception e){
                        //Toast.makeText(activity.getApplicationContext(), "Handler.ShowSnack: \n"+e.getMessage(), Toast.LENGTH_LONG).show();
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
