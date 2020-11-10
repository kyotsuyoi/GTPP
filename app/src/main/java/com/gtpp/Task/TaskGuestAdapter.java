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
import android.widget.CheckBox;
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

public class TaskGuestAdapter extends RecyclerView.Adapter <TaskGuestAdapter.ViewHolder> {

    private JsonArray List;
    private Activity activity;
    private String UserSession;
    private com.gtpp.CommonClasses.Handler Handler= new Handler();
    private int R_ID;
    private boolean isBindViewHolderError, Lock;
    private MainInterface mainInterface = ApiClient.getApiClient().create(MainInterface.class);
    private TaskInterface taskInterface = ApiClient.getApiClient().create(TaskInterface.class);

    private String EmployeeName, CompanyDescription, ShopDescription, SubDepartDescription, Type;

    public TaskGuestAdapter(JsonArray jsonArray, Activity activity, String UserSession, int R_ID, boolean Lock) {
        this.List = jsonArray;
        this.activity = activity;
        this.UserSession = UserSession;
        this.R_ID = R_ID;
        this.Lock = Lock;
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int ViewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task_guest,parent,false);
        return new ViewHolder(view);
    }

    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        try {
            JsonObject jsonObject = List.get(position).getAsJsonObject();
            int UserID = jsonObject.get("user_id").getAsInt();
            int TaskID = jsonObject.get("task_id").getAsInt();
            boolean StatusOK = jsonObject.get("status").getAsBoolean();

            GetEmployeePhoto(viewHolder.imageView,UserID);

            if (StatusOK){
                viewHolder.imageView.setAlpha(1.0f);
            }else{
                viewHolder.imageView.setAlpha(0.2f);
            }

            viewHolder.imageView.setOnClickListener(view -> GetEmployee(UserID));

            if(!Lock && position > 0){
                viewHolder.imageView.setOnLongClickListener(view -> SetDialogDelete(TaskID,UserID,position));
            }else{
                viewHolder.imageView.setOnLongClickListener(view -> false);
            }

        }catch (Exception e){
            if(!isBindViewHolderError) {
                Handler.ShowSnack("Houve um erro", "TaskGuestAdapter.onBindViewHolder: " + e.getMessage(), activity, R_ID, true);
                isBindViewHolderError=true;
            }
        }
    }

    public int getItemCount() {
        return List.size();
    }

    public void setNewItem(JsonObject jsonObject){
        List.add(jsonObject);
        notifyDataSetChanged();
    }

    public void removeItem(int UserID){
        int i = 0;
        while (i < List.size()){
            if(List.get(i).getAsJsonObject().get("user_id").getAsInt()== UserID){
                List.remove(i);
                notifyItemRemoved(i);
                return;
            }
            i++;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.itemTaskGuest_ImageView);
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
                            Handler.ShowSnack("Houve um erro", "TaskGuestAdapter.GetEmployeePhoto.onResponse: " + e.getMessage(), activity, R_ID, true);
                            isBindViewHolderError=true;
                        }
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    if(!isBindViewHolderError) {
                        Handler.ShowSnack("Houve um erro", "TaskGuestAdapter.GetEmployeePhoto.onFailure: " + t.toString(), activity, R_ID, true);
                        isBindViewHolderError=true;
                    }
                }
            });

        }catch (Exception e){
            if(!isBindViewHolderError) {
                Handler.ShowSnack("Houve um erro", "TaskGuestAdapter.GetEmployeePhoto: " + e.getMessage(), activity, R_ID, true);
                isBindViewHolderError=true;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void onImageClick(int UserID){
        try {
            final Dialog dialog = new Dialog(activity, R.style.Theme_AppCompat_Dialog_MinWidth);
            dialog.setContentView(R.layout.dialog_employee);
            ImageView imageView = dialog.findViewById(R.id.dialogEmployee_ImageView);
            TextView Name = dialog.findViewById(R.id.dialogEmployee_TextViewName);
            TextView Company = dialog.findViewById(R.id.dialogEmployee_TextViewCompany);
            TextView SubDepart = dialog.findViewById(R.id.dialogEmployee_TextViewSubDepart);
            TextView textViewType = dialog.findViewById(R.id.dialogEmployee_TextViewType);
            CheckBox checkBox = dialog.findViewById(R.id.dialogEmployee_CheckBox);

            Name.setText(EmployeeName);
            Company.setText(CompanyDescription + " - " + ShopDescription);
            SubDepart.setText(SubDepartDescription);
            textViewType.setText(Type);
            checkBox.setVisibility(View.INVISIBLE);
            checkBox.setEnabled(false);
            checkBox.setHeight(0);

            String path = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath();
            File file = new File(path + "/" + UserID + "_" + ".jpg");

            if (file.exists()) {
                imageView.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
            }

            dialog.create();
            dialog.show();
        }catch (Exception e){
            Handler.ShowSnack("Houve um erro", "TaskGuestAdapter.onImageClick: " + e.getMessage(), activity, R_ID, true);
        }
    }

    private void GetEmployee(int UserID){
        try {
            Call<JsonObject> call = mainInterface.GetEmployee(getAppID(), UserSession,UserID);
            call.enqueue(new Callback<JsonObject>() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        try {
                            if (!Handler.isRequestError(response,activity,R_ID)){
                                JsonObject jsonObject = response.body();
                                JsonArray data = jsonObject.get("data").getAsJsonArray();
                                EmployeeName = data.get(0).getAsJsonObject().get("name").getAsString();
                                CompanyDescription = data.get(0).getAsJsonObject().get("company").getAsString();
                                ShopDescription = data.get(0).getAsJsonObject().get("shop").getAsString();
                                SubDepartDescription = data.get(0).getAsJsonObject().get("sub").getAsString();
                                boolean isAdmin = data.get(1).getAsJsonObject().get("administrator").getAsBoolean();

                                if(isAdmin){
                                    Type = "Administrador";
                                }else{
                                    Type = "Usuário";
                                }

                                onImageClick(UserID);
                            }
                        }catch (Exception e){
                            Handler.ShowSnack("Houve um erro","TaskGuestAdapter.GetEmployee.onResponse: " + e.getMessage(), activity, R_ID,true);
                        }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Handler.ShowSnack("Houve um erro","TaskGuestAdapter.GetEmployee.onFailure: " + t.toString(), activity, R_ID,true);
                }
            });

        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","TaskGuestAdapter.GetEmployee: " + e.getMessage(), activity, R_ID,true);
        }
    }

    private boolean SetDialogDelete(int TaskID, int UserID, int position){
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setCancelable(false);
        alert.setNeutralButton("Cancelar", (dialog, which) -> dialog.cancel());
        alert.setTitle("Tarefa");

        alert.setMessage("Deseja remover este participante?");
        alert.setPositiveButton("SIM", (dialog, which) -> PutUser(TaskID, UserID, position));
        alert.setNeutralButton("NÃO", (dialog, which) -> dialog.cancel());
        alert.create();
        alert.show();
        return false;
    }

    public void SetLock(boolean Lock){
        this.Lock = Lock;
        notifyDataSetChanged();
    }

    private void PutUser(int TaskID, int UserID, int position){
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
                            List.remove(position);
                            notifyItemRemoved(position);
                            Handler.ShowSnack("Participante removido",null, activity, R_ID,false);
                        }
                    }catch (Exception e) {
                        Handler.ShowSnack("Houve um erro","TaskCheckUserAdapter.PutUser.onResponse: " + e.getMessage(), activity, R_ID,true);
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Handler.ShowSnack("Houve um erro","TaskCheckUserAdapter.PutUser.onFailure: " + t.toString(), activity, R_ID,true);
                }
            });
        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","TaskCheckUserAdapter.PutUser: " + e.getMessage(), activity, R_ID,true);
        }
    }

}
