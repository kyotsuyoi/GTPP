package com.gtpp.Main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.util.Calendar;
import android.os.Environment;
import android.support.design.widget.TextInputEditText;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.gtpp.CommonClasses.ApiClient;
import com.gtpp.CommonClasses.AppService;
import com.gtpp.CommonClasses.Handler;
import com.gtpp.CommonClasses.NotifyListener;
import com.gtpp.CommonClasses.RecyclerItemClickListener;
import com.gtpp.CommonClasses.SavedUser;
import com.gtpp.Login.LoginActivity;
import com.gtpp.R;
import com.gtpp.Task.TaskActivity;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static com.gtpp.CommonClasses.Handler.getAppID;

public class MainActivity extends AppCompatActivity implements com.wdullaer.materialdatetimepicker.date.DatePickerDialog.OnDateSetListener {

    private static final String GTPP_PREFERENCES = "GTPP_PREFERENCES";
    @SuppressLint("StaticFieldLeak")
    public static CardView cardViewSession;
    private MainAdapter adapter;
    private SavedUser SU;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private int R_ID = R.id.activityMain_SwipeRefresh;
    private Activity This = MainActivity.this;
    private com.gtpp.CommonClasses.Handler Handler = new Handler();

    private ImageView imageView;

    private MainInterface mainInterface = ApiClient.getApiClient().create(MainInterface.class);

    private JsonArray companyList;
    private JsonArray shopList;
    private JsonArray departmentList;
    private JsonArray stateList;
    private StateAdapter stateAdapter;

    private int SelectedCompany, SelectedShop, SelectedDepartment;
    private String EmployeeName, CompanyDescription, ShopDescription, SubDepartDescription;

    private Dialog DialogTaskCreate, DialogTaskUpdate;
    private int Check;
    private int CheckedRadioButton = 0;
    private TextInputEditText TaskTextInputEditText;
    private TextInputEditText TaskTextInputInitialDate;
    private TextInputEditText TaskTextInputFinalDate;
    private RadioButton radioButtonLow;

    private TextView textViewShow;
    private Button buttonCreate, buttonFilter, buttonSort;
    private SearchView searchView;

    private Intent serviceIntent;

    private boolean emu = false;
    NotifyListener notifyListener;

    private String orderBy = "state_id"; //Default filter

    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            Objects.requireNonNull(getSupportActionBar()).hide();

            cardViewSession = findViewById(R.id.cardViewMBorder);

            swipeRefresh = findViewById(R.id.activityMain_SwipeRefresh);
            recyclerView = findViewById(R.id.activityMain_RecyclerView);
            imageView = findViewById(R.id.activityMain_ImageView);
            layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setHasFixedSize(true);

            textViewShow = findViewById(R.id.activityMain_TextViewShow);
            buttonCreate = findViewById(R.id.activityMain_ButtonCreate);
            buttonFilter = findViewById(R.id.activityMain_ButtonFilter);
            buttonSort = findViewById(R.id.activityMain_ButtonSort);
            searchView = findViewById(R.id.activityMain_SearchView);

            SU = SavedUser.getSavedUser();

            SetSwipeRefresh();
            GetEmployee();
            GetEmployeePhoto();
            GetTaskState(); //Call GetTask()
            SetRecyclerView();
            SetAlpha();
            SetSearchView();

            com.gtpp.CommonClasses.Handler.isLogged = true;

            /*serviceIntent = new Intent(getBaseContext(), AppService.class);
            if(emu){
                //notifyListener = new NotifyListener(getApplicationContext(), SU);
                //notifyListener.InstantiateWebSocket(notifyListener);
            }else {
                try {
                    startForegroundService(serviceIntent);
                }catch (Throwable t){
                    Handler.ShowSnack("Shit...","All fucked up!!: " + t.getMessage(), This, R_ID,true);
                }
                //notifyListener = new NotifyListener(getApplicationContext(), SU);
                //notifyListener.InstantiateWebSocket(notifyListener);
            }*/

            //MainActivity.cardViewSession.setCardBackgroundColor(ContextCompat.getColor(this, R.color.colorGreen));
        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","MainActivity.onCreate: " + e.getMessage(), This, R_ID);
        }
    }

    public void onBackPressed() {
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setMessage("Deseja realmente efetuar logoff?");
        alert.setCancelable(false);
        alert.setPositiveButton("Sim", (dialog, which) -> {
            getSharedPreferences(GTPP_PREFERENCES, 0).edit().clear().apply();
            SavedUser.setSavedUser(null);

            com.gtpp.CommonClasses.Handler.isLogged = false;
            /*if(emu){
                notifyListener.getWebSocket().cancel();
            }else {
                stopService(serviceIntent);
            }*/
            Intent intent  = new Intent(getApplicationContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        alert.setNegativeButton("Não", (dialog, which) -> dialog.cancel());
        alert.setTitle("GTPP");
        alert.create();
        alert.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        try {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == 1 && resultCode == RESULT_OK) {
                int position = data.getIntExtra("position", 0);
                int StateID = data.getIntExtra("state_id", 0);
                int Expire = data.getIntExtra("expire", 0);
                adapter.getItem(position).addProperty("state_id", StateID);
                adapter.getItem(position).addProperty("expire", Expire);
                adapter.notifyItemChanged(position);
            }
        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","MainActivity.onActivityResult: " + e.getMessage(), This, R_ID);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @SuppressLint("SetTextI18n")
    private void SetAlpha(){
        try {
            AlphaAnimation ButtonAnimation = new AlphaAnimation(0.0f, 1.0f);
            ButtonAnimation.setDuration(1000);
            ButtonAnimation.setStartOffset(5000);
            ButtonAnimation.setFillAfter(true);

            AlphaAnimation TextViewShowAnimationDown = new AlphaAnimation(1.0f, 0.0f);
            TextViewShowAnimationDown.setDuration(1000);
            TextViewShowAnimationDown.setStartOffset(4000);
            TextViewShowAnimationDown.setFillAfter(true);

            textViewShow.setText("Conectado como "+SU.getUser());
            textViewShow.startAnimation(TextViewShowAnimationDown);
            buttonCreate.startAnimation(ButtonAnimation);
            buttonFilter.startAnimation(ButtonAnimation);
            buttonSort.startAnimation(ButtonAnimation);
            searchView.startAnimation(ButtonAnimation);
        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","MainActivity.SetAlpha: " + e.getMessage(), This, R_ID);
        }
    }

    private void SetDialogTaskUpdate(int position){
        DialogTaskUpdate = new Dialog(MainActivity.this,R.style.Theme_AppCompat_Dialog_MinWidth);
        DialogTaskUpdate.setContentView(R.layout.dialog_task_update);
        TextInputEditText textInputEditText = DialogTaskUpdate.findViewById(R.id.dialogTaskUpdate_TextInputEditText);
        Button ButtonUpdate = DialogTaskUpdate.findViewById(R.id.dialogTaskUpdate_ButtonUpdate);
        Button ButtonCancel = DialogTaskUpdate.findViewById(R.id.dialogTaskUpdate_ButtonCancel);

        RadioGroup radioGroup = DialogTaskUpdate.findViewById(R.id.dialogTaskUpdate_RadioGroup);
        RadioButton radioButtonLow = DialogTaskUpdate.findViewById(R.id.dialogTaskUpdate_RadioButtonLow);
        RadioButton radioButtonMid = DialogTaskUpdate.findViewById(R.id.dialogTaskUpdate_RadioButtonMid);
        RadioButton radioButtonHi = DialogTaskUpdate.findViewById(R.id.dialogTaskUpdate_RadioButtonHi);

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId){
                case R.id.dialogTaskUpdate_RadioButtonHi:
                    CheckedRadioButton = 2;
                    break;
                case R.id.dialogTaskUpdate_RadioButtonMid:
                    CheckedRadioButton = 1;
                    break;
                case R.id.dialogTaskUpdate_RadioButtonLow:
                    CheckedRadioButton = 0;
                    break;
                default:
                    CheckedRadioButton = -1;
            }
        });
        textInputEditText.setText(adapter.getItem(position).get("description").getAsString());

        switch (adapter.getItem(position).get("priority").getAsInt()){
            case 0:
                radioButtonLow.setChecked(true);
                break;
            case 1:
                radioButtonMid.setChecked(true);
                break;
            case 2:
                radioButtonHi.setChecked(true);
                break;
        }

        ButtonUpdate.setOnClickListener(v->{
            if(textInputEditText.getText().length() < 1){
                textInputEditText.setError("Insira o nome corretamente");
                textInputEditText.requestFocus();
                return;
            }
            DoneTaskUpdate(textInputEditText.getText().toString(),CheckedRadioButton,position);
        });

        ButtonCancel.setOnClickListener(v->{
            DialogTaskUpdate.cancel();
        });

        DialogTaskUpdate.create();
        DialogTaskUpdate.show();
    }

    private void SetSearchView(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        searchView.setMaxWidth(width-150);

        searchView.setOnCloseListener(()->{
            buttonCreate.setAlpha(1f);
            buttonCreate.setEnabled(true);
            buttonFilter.setAlpha(1f);
            buttonFilter.setEnabled(true);
            buttonSort.setAlpha(1f);
            buttonSort.setEnabled(true);
            return false;
        });
        searchView.setOnSearchClickListener(view -> {
            buttonCreate.setAlpha(0f);
            buttonCreate.setEnabled(false);
            buttonFilter.setAlpha(0f);
            buttonFilter.setEnabled(false);
            buttonSort.setAlpha(0f);
            buttonSort.setEnabled(false);
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (!(adapter == null)) {
                    adapter.getFilter().filter(s);
                }
                return false;
            }
        });
    }

    private void SetFilterSpinner(Spinner spinnerCompany, Spinner spinnerShop, Spinner spinnerDepartment){

        spinnerCompany.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    if(position!=0) {
                        SelectedCompany = companyList.get(position-1).getAsJsonObject().get("id").getAsInt();
                        if (adapter != null){
                            GetShop(spinnerShop,SelectedCompany);
                            departmentList = null;
                            spinnerDepartment.setAdapter(null);
                        }
                    }else{
                        SelectedCompany = 0;
                    }
                } catch (Exception e) {
                    Handler.ShowSnack("Houve um erro","MainActivity.SetFilterSpinner.spinnerCompany: " + e.getMessage(), This, R_ID);
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerShop.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    if(position!=0) {
                        SelectedShop = shopList.get(position-1).getAsJsonObject().get("id").getAsInt();
                        if (adapter != null){
                            GetDepartment(spinnerDepartment,SelectedCompany,SelectedShop);
                        }
                    }else{
                        SelectedShop = 0;
                    }
                } catch (Exception e) {
                    Handler.ShowSnack("Houve um erro","MainActivity.SetFilterSpinner..spinnerShop: " + e.getMessage(), This, R_ID);
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerDepartment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    if(position!=0) {
                        SelectedDepartment = departmentList.get(position-1).getAsJsonObject().get("id").getAsInt();
                    }else{
                        SelectedDepartment = 0;
                    }
                } catch (Exception e) {
                    Handler.ShowSnack("Houve um erro","MainActivity.SetFilterSpinner..spinnerDepartment: " + e.getMessage(), This, R_ID);
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    private void SetSwipeRefresh(){
        try{
            swipeRefresh.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.colorAccent));
            swipeRefresh.setColorSchemeColors(getResources().getColor(R.color.colorDarkBackground));
            swipeRefresh.setOnRefreshListener(this::GetTaskState);
        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","MainActivity.SetSwipeRefresh: " + e.getMessage(), This, R_ID);
        }
    }

    private void SetRecyclerView(){
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(
                this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {

            public void onItemClick(View view, int position) {
                try {
                    int state_id = adapter.getItemState(position);
                    int user = adapter.getItemUserID(position);

                    /*if(state_id == 5 && user != SU.getId() && !SU.isAdministrator()){
                        Handler.ShowSnack("Tarefa bloqueada","Esta tarefa atrasou sua entrega, somente o criador da tarefa pode extender o praso", This, R_ID,true);
                        return;
                    }*/

                    Intent intent = new Intent(MainActivity.this, TaskActivity.class);
                    intent.putExtra("task_object", adapter.getItem(position).toString());
                    intent.putExtra("position", position);
                    intent.putExtra("state_list", stateList.toString());

                    if (stateList != null) {
                        int size = stateList.size();
                        for (int i = 0; i < size; i++) {
                            JsonObject jsonObject = stateList.get(i).getAsJsonObject();
                            if (jsonObject.get("id").getAsInt() == state_id) {
                                intent.putExtra("state_description", stateList.get(i).getAsJsonObject().get("description").getAsString());
                                intent.putExtra("state_color", "#"+stateList.get(i).getAsJsonObject().get("color").getAsString());
                                i = size;
                            }
                        }
                    }

                    startActivityForResult(intent,1);
                }catch (Exception e){
                    Handler.ShowSnack("Houve um erro","MainActivity.SetList.onItemClick: " + e.getMessage(), This, R_ID);
                }
            }

            public boolean onLongItemClick(View view, final int position) {
                try{
                    if (adapter.getItem(position).getAsJsonObject().get("user_id").getAsInt() != SU.getId() && !SU.isAdministrator()){
                        Handler.ShowSnack("Você não pode fazer isso",
                                "Somente o criador ou administradores podem apagar ou alterar esta tarefa, caso precise mudar algo entre em contato com seu administrador",
                                This,
                                R_ID
                        );
                        return false;
                    }

                    final int ID = adapter.getItemID(position);

                    if(adapter.getItemState(position) == 5 && adapter.getItemUserID(position) != SU.getId() && !SU.isAdministrator()){
                        Handler.ShowSnack("Tarefa bloqueada","Esta tarefa atrasou sua entrega, somente o criador da tarefa pode extender o praso", This, R_ID);
                        return false;
                    }
                    AlertDialog.Builder alert = new AlertDialog.Builder(This);
                    alert.setMessage("O que deseja fazer?");
                    alert.setCancelable(false);
                    alert.setNeutralButton("Cancelar", (dialog, which) -> dialog.cancel());

                    int state_id = adapter.getItemState(position);
                    if(state_id != 5 && state_id != 6 && state_id != 7){
                        alert.setNegativeButton("Alterar", (dialog, which) -> SetDialogTaskUpdate(position));
                    }

                    alert.setPositiveButton("Excluir", (dialog, which) -> DialogTaskDelete(ID,position));

                    alert.create();
                    alert.setTitle("Tarefa");
                    alert.show();

                }catch (Exception e){
                    Handler.ShowSnack("Houve um erro","MainActivity.SetList.onLongItemClick: " + e.getMessage(), This, R_ID);
                }
                return false;
            }
        }));
    }

    private void GetEmployee(){
        try {
            Call<JsonObject> call = mainInterface.GetEmployee(getAppID(), SU.getSession(),SU.getId());
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (!Handler.isRequestError(response,This,R_ID)){
                        try {
                            JsonObject jsonObject = response.body();
                            assert jsonObject != null;
                            JsonArray data = jsonObject.get("data").getAsJsonArray();
                            EmployeeName = data.get(0).getAsJsonObject().get("name").getAsString();
                            CompanyDescription = data.get(0).getAsJsonObject().get("company").getAsString();
                            ShopDescription = data.get(0).getAsJsonObject().get("shop").getAsString();
                            SubDepartDescription = data.get(0).getAsJsonObject().get("sub").getAsString();
                        }catch (Exception e){
                            Handler.ShowSnack("Houve um erro","MainActivity.GetEmployee.onResponse: " + e.getMessage(), This, R_ID);
                        }
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Handler.ShowSnack("Houve um erro","MainActivity.GetEmployee.onFailure: " + t.toString(), This, R_ID);
                    swipeRefresh.setRefreshing(false);
                }
            });

        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","MainActivity.GetEmployee: " + e.getMessage(), This, R_ID);
            swipeRefresh.setRefreshing(false);
        }
    }

    private void GetEmployeePhoto(){
        try {
            String path = Objects.requireNonNull(getExternalFilesDir(Environment.DIRECTORY_PICTURES)).getPath();
            File file = new File(path + "/" + SU.getId() + "_" + ".jpg");

            if(file.exists()){
                imageView.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
                return;
            }

            Call<JsonObject> call = mainInterface.GetEmployeePhoto(getAppID(), SU.getSession(),SU.getId());
            call.enqueue(new Callback<JsonObject>()  {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (!Handler.isRequestError(response,This,R_ID)){
                        JsonObject jsonObject = response.body();

                        try {
                            assert jsonObject != null;
                            if(jsonObject.get("photo") == JsonNull.INSTANCE){
                                Handler.ShowSnack("Usuário sem foto",null, This, R_ID);
                                return;
                            }
                            String photo = jsonObject.get("photo").getAsString();

                            if(photo != null){

                                Bitmap bitmap = Handler.ImageDecode(photo);
                                FileOutputStream fos = new FileOutputStream(file);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                                bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                                imageView.setImageBitmap(bitmap);
                            }
                        }catch (Exception e){
                            Handler.ShowSnack("Houve um erro","MainActivity.GetEmployeePhoto.onResponse: " + e.getMessage(), This, R_ID);
                        }
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Handler.ShowSnack("Houve um erro","MainActivity.GetEmployeePhoto.onFailure: " + t.toString(), This, R_ID);
                    swipeRefresh.setRefreshing(false);
                }
            });

        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","MainActivity.GetEmployeePhoto: " + e.getMessage(), This, R_ID);
            swipeRefresh.setRefreshing(false);
        }
    }

    private void GetTask(){
        try {
            int admin = 0;
            if(SU.isAdminVisualization()){
                admin = 1;
            }
            Call<JsonObject> call = mainInterface.GetTask(getAppID(),
                    SU.getSession(),
                    SU.getId(),
                    1,
                    admin
            );
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (!Handler.isRequestError(response,This,R_ID)){
                        JsonObject task = response.body();
                        JsonArray data = task.get("data").getAsJsonArray();
                        adapter = new MainAdapter(data, stateList,MainActivity.this,R_ID);
                        recyclerView.setAdapter(adapter);
                    }else{
                        recyclerView.setAdapter(null);
                    }
                    swipeRefresh.setRefreshing(false);
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Handler.ShowSnack("Houve um erro","GetTask.onFailure: " + t.toString(), This, R_ID);
                    swipeRefresh.setRefreshing(false);
                }
            });

        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","MainActivity.GetTask: " + e.getMessage(), This, R_ID);
            swipeRefresh.setRefreshing(false);
        }
    }

    private void GetCompany(Spinner spinner){
        try {
            Call<JsonObject> call = mainInterface.GetCompany(getAppID(), SU.getSession());
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (!Handler.isRequestError(response,This,R_ID)){
                        try{
                            JsonObject jsonObject = response.body();
                            companyList = jsonObject.get("data").getAsJsonArray();

                            ArrayList arrayList;
                            arrayList = new ArrayList<>();
                            arrayList.add("");
                            for (int i = 0; i < companyList.size(); i++) {

                                int id = companyList.get(i).getAsJsonObject().get("id").getAsInt();
                                String description = companyList.get(i).getAsJsonObject().get("description").getAsString();

                                arrayList.add(id + " - " + description);
                            }

                            ArrayAdapter arrayAdapter = new ArrayAdapter<>(This,R.layout.support_simple_spinner_dropdown_item,arrayList);

                            spinner.setAdapter(arrayAdapter);
                        }catch (Exception e) {
                            Handler.ShowSnack("Houve um erro","MainActivity.GetCompany.onResponse: " + e.getMessage(), This, R_ID);
                        }
                    }else{
                        spinner.setAdapter(null);
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Handler.ShowSnack("Houve um erro","MainActivity.GetCompany.onFailure: " + t.toString(), This, R_ID);
                }
            });

        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","MainActivity.GetCompany: " + e.getMessage(), This, R_ID);
        }
    }

    private void GetShop(Spinner spinner, int CompanyID){
        try {
            Call<JsonObject> call = mainInterface.GetShop(getAppID(), SU.getSession(),CompanyID);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (!Handler.isRequestError(response,This,R_ID)){
                        try{
                            JsonObject jsonObject = response.body();
                            shopList = jsonObject.get("data").getAsJsonArray();

                            ArrayList arrayList = new ArrayList<>();
                            arrayList.add("");
                            for (int i = 0; i < shopList.size(); i++) {

                                int id = shopList.get(i).getAsJsonObject().get("id").getAsInt();
                                String description = shopList.get(i).getAsJsonObject().get("description").getAsString();

                                arrayList.add(id + " - " + description);
                            }

                            ArrayAdapter<String> arrayAdapter= new ArrayAdapter<>(This,R.layout.support_simple_spinner_dropdown_item,arrayList);

                            spinner.setAdapter(arrayAdapter);
                        }catch (Exception e) {
                            Handler.ShowSnack("Houve um erro","MainActivity.GetShop.onResponse: " + e.getMessage(), This, R_ID);
                        }
                    }else{
                        spinner.setAdapter(null);
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Handler.ShowSnack("Houve um erro","MainActivity.GetShop.onFailure: " + t.toString(), This, R_ID);
                }
            });

        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","MainActivity.GetShop: " + e.getMessage(), This, R_ID);
        }
    }

    private void GetDepartment(Spinner spinner, int CompanyID, int ShopID){
        try {
            Call<JsonObject> call = mainInterface.GetDepartment(getAppID(), SU.getSession(),CompanyID,ShopID);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    try{
                        if (!Handler.isRequestError(response,This,R_ID)){
                            JsonObject jsonObject = response.body();
                            departmentList = jsonObject.get("data").getAsJsonArray();

                            ArrayList arrayList = new ArrayList<>();
                            arrayList.add("");
                            for (int i = 0; i < departmentList.size(); i++) {

                                int id = departmentList.get(i).getAsJsonObject().get("id").getAsInt();
                                String description = departmentList.get(i).getAsJsonObject().get("description").getAsString();

                                arrayList.add(id + " - " + description);
                            }

                            ArrayAdapter<String> arrayAdapter= new ArrayAdapter<>(This,R.layout.support_simple_spinner_dropdown_item,arrayList);

                            spinner.setAdapter(arrayAdapter);
                        }else{
                            spinner.setAdapter(null);
                        }
                    }catch (Exception e) {
                        Handler.ShowSnack("Houve um erro","MainActivity.GetDepartment.onResponse: " + e.getMessage(), This, R_ID);
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Handler.ShowSnack("Houve um erro","MainActivity.GetDepartment.onFailure: " + t.toString(), This, R_ID);
                }
            });

        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","MainActivity.GetDepartment: " + e.getMessage(), This, R_ID);
        }
    }

    private void GetTaskState(){
        try {
            Call<JsonObject> call = mainInterface.GetTaskState(getAppID(), SU.getSession());
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    try{
                        if (!Handler.isRequestError(response,This,R_ID)){
                            JsonObject jsonObject = response.body();
                            assert jsonObject != null;
                            stateList = jsonObject.get("data").getAsJsonArray();
                            GetTask();
                        }else{
                            swipeRefresh.setRefreshing(false);
                        }
                    }catch (Exception e) {
                        Handler.ShowSnack("Houve um erro","MainActivity.GetTaskState.onResponse: " + e.getMessage(), This, R_ID);
                        swipeRefresh.setRefreshing(false);
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Handler.ShowSnack("Houve um erro","MainActivity.GetTaskState.onFailure: " + t.toString(), This, R_ID);
                    swipeRefresh.setRefreshing(false);
                }
            });

        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","MainActivity.GetTaskState: " + e.getMessage(), This, R_ID);
            swipeRefresh.setRefreshing(false);
        }
    }

    public void showStartDatePicker(View v) {
        DatePickerDialog DPD = DatePickerDialog.newInstance(
                MainActivity.this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        DPD.setOnDateSetListener(this);
        DPD.show(getFragmentManager(), "startDatepickerdialog");
    }

    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {

        int monthAddOne = monthOfYear + 1;

        String s = monthAddOne < 10 ? "0" + monthAddOne : "" + monthAddOne;
        switch(Check){
            case 0:
                String dateInitial = (year + "-" + s + "-" +
                        (dayOfMonth < 10 ? "0" + dayOfMonth : "" + dayOfMonth));

                TaskTextInputInitialDate.setText(dateInitial);
                break;
            case 1:
                String dateFinal = (year + "-" + s + "-" +
                        (dayOfMonth < 10 ? "0" + dayOfMonth : "" + dayOfMonth));

                TaskTextInputFinalDate.setText(dateFinal);
                break;
        }
    }

    public void OnTaskCreateClick(View view){

        try {
            DialogTaskCreate = new Dialog(MainActivity.this,R.style.Theme_AppCompat_Dialog_MinWidth);
            DialogTaskCreate.setContentView(R.layout.dialog_task_create);
            TaskTextInputEditText = DialogTaskCreate.findViewById(R.id.dialogTaskCreate_TextInputEditText);
            TaskTextInputInitialDate = DialogTaskCreate.findViewById(R.id.dialogTaskCreate_InitialDate);
            TaskTextInputFinalDate = DialogTaskCreate.findViewById(R.id.dialogTaskCreate_FinalDate);
            Button taskButtonCreate = DialogTaskCreate.findViewById(R.id.dialogTaskCreate_ButtonCreate);
            radioButtonLow = DialogTaskCreate.findViewById(R.id.dialogTaskCreate_RadioButtonLow);

            RadioGroup radioGroup = DialogTaskCreate.findViewById(R.id.dialogTaskCreate_RadioGroup);
            Button buttonCancel = DialogTaskCreate.findViewById(R.id.dialogTaskCreate_ButtonCancel);

            TaskTextInputInitialDate.setOnClickListener(v -> {
                if (TaskTextInputInitialDate.isFocused()) {
                    hideKeyboard(This, TaskTextInputInitialDate.getRootView());
                }
                TaskTextInputInitialDate.setText(" ");
                TaskTextInputInitialDate.getText().clear();
                Check = 0;
                showStartDatePicker(v);
            });

            TaskTextInputFinalDate.setOnClickListener(v -> {
                hideKeyboard(This,TaskTextInputInitialDate.getRootView());
                TaskTextInputFinalDate.setText(" ");
                TaskTextInputFinalDate.getText().clear();
                Check = 1;
                showStartDatePicker(v);
            });

            radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                switch (checkedId){
                    case R.id.dialogTaskCreate_RadioButtonHi:
                        CheckedRadioButton = 2;
                        break;
                    case R.id.dialogTaskCreate_RadioButtonMid:
                        CheckedRadioButton = 1;
                        break;
                    case R.id.dialogTaskCreate_RadioButtonLow:
                        CheckedRadioButton = 0;
                        break;
                    default:
                        CheckedRadioButton = -1;
                }
            });

            buttonCancel.setOnClickListener(v -> {
                TaskTextInputEditText.requestFocus();
                DialogTaskCreate.cancel();
            });

            taskButtonCreate.setOnClickListener(v ->{
                DoneTaskAdd();
            });

            DialogTaskCreate.create();
            DialogTaskCreate.show();

        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","MainActivity.OnTaskCreateClick: " + e.getMessage(), This, R_ID);
        }
    }

    public void OnTaskFilterClick(View view){
        try {
            final Dialog dialog = new Dialog(MainActivity.this, R.style.Theme_AppCompat_Dialog_MinWidth);
            dialog.setContentView(R.layout.dialog_task_filter);
            Spinner spinnerFilterCompany = dialog.findViewById(R.id.dialogTaskFilter_SpinnerCompany);
            Spinner spinnerFilterShop = dialog.findViewById(R.id.dialogTaskFilter_SpinnerShop);
            Spinner spinnerFilterDepart = dialog.findViewById(R.id.dialogTaskFilter_SpinnerDepart);
            RecyclerView recyclerView = dialog.findViewById(R.id.dialogTaskFilter_RecyclerView);
            Button buttonFilter = dialog.findViewById(R.id.dialogTaskFilter_Button);

            GetCompany(spinnerFilterCompany);
            GetTaskState();

            stateAdapter = new StateAdapter(stateList,this,R_ID);
            recyclerView.setAdapter(stateAdapter);

            layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setHasFixedSize(true);

            SetFilterSpinner(
                    spinnerFilterCompany,
                    spinnerFilterShop,
                    spinnerFilterDepart
            );

            buttonFilter.setOnClickListener(view1 -> {
                try {
                    if (adapter.getItemCount() == 0) {
                        Handler.ShowSnack("Nada encontrado", null, This, R_ID);
                    }

                    JsonArray jsonArray = stateAdapter.getItemNotChecked();
                    adapter.getFilterState(jsonArray);

                    if (SelectedCompany == 0) {
                        dialog.cancel();
                        return;
                    }
                    if (SelectedShop == 0) {
                        adapter.getFilterAmount(SelectedCompany, 0, 0).filter("NULL");
                        dialog.cancel();
                        return;
                    }
                    if (SelectedDepartment == 0) {
                        adapter.getFilterAmount(SelectedCompany, SelectedShop, 0).filter("NULL");
                        dialog.cancel();
                        return;
                    }
                    if (SelectedDepartment > 0) {
                        adapter.getFilterAmount(SelectedCompany, SelectedShop, SelectedDepartment).filter("NULL");
                    }

                }catch (Exception e){
                    Handler.ShowSnack("Houve um erro", "MainActivity.onTaskFilterClick.buttonFilter: "+e.getMessage(), This, R_ID);
                }
                dialog.cancel();
            });

            dialog.create();
            dialog.show();

            adapter.ResetFilter();
            adapter.notifyDataSetChanged();
            SelectedCompany=0;
            SelectedShop=0;
            SelectedDepartment=0;
        }catch (Exception e){
            Handler.ShowSnack("Houve um erro", "MainActivity.onTaskFilterClick: "+e.getMessage(), This, R_ID);
        }
    }

    public void OnTaskSortClick(View view){
        switch (orderBy){
            case "state_id":
                orderBy="description";
                adapter.OrderBy(orderBy);
                Handler.ShowSnack("Ordenado por descrição",null, this,R_ID);
                break;
            case "description":
                orderBy="priority";
                adapter.OrderBy(orderBy);
                Handler.ShowSnack("Ordenado por prioridade",null, this,R_ID);
                break;
            case "priority":
                orderBy="final_date";
                adapter.OrderBy(orderBy);
                Handler.ShowSnack("Ordenado por vencimento",null, this,R_ID);
                break;
            case "final_date":
                orderBy="state_id";
                adapter.OrderBy(orderBy);
                Handler.ShowSnack("Ordenado por estado da tarefa",null, this,R_ID);
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    public void dialogEmployee(View view){
        try {
            final Dialog dialog = new Dialog(MainActivity.this, R.style.Theme_AppCompat_Dialog_MinWidth);
            dialog.setContentView(R.layout.dialog_employee);
            ImageView imageView = dialog.findViewById(R.id.dialogEmployee_ImageView);
            TextView Name = dialog.findViewById(R.id.dialogEmployee_TextViewName);
            TextView Company = dialog.findViewById(R.id.dialogEmployee_TextViewCompany);
            TextView SubDepart = dialog.findViewById(R.id.dialogEmployee_TextViewSubDepart);
            TextView Type = dialog.findViewById(R.id.dialogEmployee_TextViewType);
            CheckBox checkBox = dialog.findViewById(R.id.dialogEmployee_CheckBox);

            TextView textViewScore = dialog.findViewById(R.id.dialogEmployee_TextView_Score);

            Name.setText(EmployeeName);
            Company.setText(CompanyDescription + " - " + ShopDescription);
            SubDepart.setText(SubDepartDescription);

            String path =  Objects.requireNonNull(getExternalFilesDir(Environment.DIRECTORY_PICTURES)).getPath();
            File file = new File(path + "/" + SU.getId() + "_" + ".jpg");

            if(file.exists()){
                imageView.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
            }

            if(!SU.isAdministrator()){
                Type.setText("Usuário");
                checkBox.setEnabled(false);
                checkBox.setHeight(0);
            }else{
                Type.setText("Administrador");
                checkBox.setChecked(SU.isAdminVisualization());
                checkBox.setOnClickListener(v->{
                    SU.setAdminVisualization(checkBox.isChecked());
                    SharedPreferences settings = getSharedPreferences(GTPP_PREFERENCES, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean("adminVisualization",SU.isAdminVisualization());
                    editor.apply();
                });
            }

            getScore(textViewScore);

            dialog.create();
            dialog.show();
        }catch (Exception e){
            Handler.ShowSnack("Houve um erro", "MainActivity.OnTaskEmployeeClick: "+e.getMessage(), This, R_ID);
        }
    }

    private void DialogTaskDelete(int ID, int position){
        try{
            AlertDialog.Builder alert = new AlertDialog.Builder(This);
            alert.setMessage("Deseja realmente excluir?");
            alert.setCancelable(false);

            alert.setNegativeButton("Não", (dialog13, which13) -> dialog13.cancel());
            alert.setPositiveButton("Sim", (dialog14, which14) -> DeleteTask(ID,position));
            alert.create();
            alert.setTitle("Excluir");
            alert.show();
        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","MainActivity.CreateDialogForDelete: " + e.getMessage(), This, R_ID);
        }
    }

    private void DeleteTask(int ID, int position){
        try {
            Call<JsonObject> call = mainInterface.DeleteTask(getAppID(),
                    SU.getSession(),
                    ID
            );

            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    try{
                        if (!Handler.isRequestError(response,This,R_ID)){
                            adapter.RemoveItem(ID);
                            adapter.notifyItemRemoved(position);
                            Handler.ShowSnack("Tarefa removida",null, This, R_ID);
                        }
                    }catch (Exception e) {
                        Handler.ShowSnack("Houve um erro","MainActivity.DeleteTask.onResponse: " + e.getMessage(), This, R_ID);
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Handler.ShowSnack("Houve um erro","MainActivity.DeleteTask.onFailure: " + t.toString(), This, R_ID);
                }
            });
        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","MainActivity.DeleteTask: " + e.getMessage(), This, R_ID);
        }
    }

    public static void hideKeyboard(Context context, View view) {
        try {
            InputMethodManager keyboard = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            assert keyboard != null;
            keyboard.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void DoneTaskAdd(){
        try{
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

            String name = TaskTextInputEditText.getText().toString();
            String initialDate = TaskTextInputInitialDate.getText().toString();
            String finalDate = TaskTextInputFinalDate.getText().toString();

            AtomicInteger errorStep = new AtomicInteger();

            if (finalDate.trim().length() < 10) {
                errorStep.getAndIncrement();
                TaskTextInputFinalDate.setError("Insira a data final");
                TaskTextInputFinalDate.requestFocus();
            }
            if (initialDate.trim().length() < 10) {
                errorStep.getAndIncrement();
                TaskTextInputInitialDate.setError("Insira a data inicial");
                TaskTextInputInitialDate.requestFocus();
                hideKeyboard(This,TaskTextInputInitialDate.getRootView());
            }
            if (name.trim().length() < 1) {
                errorStep.getAndIncrement();
                TaskTextInputEditText.setError("Insira um nome para a tarefa");
                TaskTextInputEditText.requestFocus();
                hideKeyboard(This,TaskTextInputInitialDate.getRootView());
            }
            if (errorStep.get() == 0) {
                Date initial = format.parse(initialDate);
                Date Final = format.parse(finalDate);

                if (initial.after(Final)) {
                    errorStep.getAndIncrement();
                    TaskTextInputFinalDate.setError("Data final não pode ser antes da data inicial");
                    TaskTextInputFinalDate.requestFocus();
                    hideKeyboard(This,TaskTextInputInitialDate.getRootView());
                    return;
                }
            }

            if (errorStep.get() != 0) {
                Handler.ShowSnack("Verifique todos os campo",null,This,R_ID);
                return;
            }

            JsonObject InsertObject = new JsonObject();
            InsertObject.addProperty("description",TaskTextInputEditText.getText().toString());
            InsertObject.addProperty("initial_date",TaskTextInputInitialDate.getText().toString());
            InsertObject.addProperty("final_date",TaskTextInputFinalDate.getText().toString());
            InsertObject.addProperty("priority",CheckedRadioButton);
            InsertObject.addProperty("user_id",SU.getId());

            DialogTaskCreate.cancel();
            Handler.ShowSnack("Aguarde...",null, This, R_ID);

            Call<JsonObject> call = mainInterface.PostTask(getAppID(),
                    SU.getSession(),
                    1,
                    InsertObject
            );

            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    try {
                        if (!Handler.isRequestError(response, This, R_ID)) {
                            JsonObject jsonObject = response.body();

                            assert jsonObject != null;
                            int ID = jsonObject.get("last_id").getAsInt();
                            int expire = jsonObject.get("expire").getAsInt();
                            JsonObject NewItemObject = new JsonObject();
                            NewItemObject.addProperty("id", ID);
                            NewItemObject.addProperty("description", InsertObject.get("description").getAsString());
                            NewItemObject.addProperty("state_description", "Fazer");
                            NewItemObject.addProperty("initial_date",InsertObject.get("initial_date").getAsString());
                            NewItemObject.addProperty("final_date",InsertObject.get("final_date").getAsString());
                            NewItemObject.addProperty("state_id", 1);
                            NewItemObject.addProperty("priority", InsertObject.get("priority").getAsString());
                            NewItemObject.addProperty("expire", expire);
                            NewItemObject.addProperty("company_id", 0);
                            NewItemObject.addProperty("shop_id", 0);
                            NewItemObject.addProperty("departament_id", 0);
                            NewItemObject.addProperty("sub_departament_id", 0);
                            NewItemObject.addProperty("user_id", SU.getId());

                            if(adapter==null){
                                JsonArray jsonArray = new JsonArray();
                                adapter = new MainAdapter(jsonArray,stateList,MainActivity.this,R_ID);
                                adapter.setNewItem(NewItemObject);
                                recyclerView.setAdapter(adapter);
                            }else{
                                adapter.setNewItem(NewItemObject);
                            }
                            adapter.notifyDataSetChanged();
                            Handler.ShowSnack("Tarefa criada", null, This, R_ID);
                        }
                    }catch (Exception e){
                        Handler.ShowSnack("Houve um erro","MainActivity.DoneAddTask.onResponse: " + e.getMessage(), This, R_ID);
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Handler.ShowSnack("Houve um erro","MainActivity.DoneAddTask.onFailure: " + t.toString(), This, R_ID);
                }
            });
            TaskTextInputEditText.setText("");
            TaskTextInputInitialDate.setText("");
            TaskTextInputFinalDate.setText("");
            radioButtonLow.setChecked(true);
            CheckedRadioButton=0;
        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","MainActivity.DoneAddTask: " + e.getMessage(), This, R_ID);
        }
    }

    private void DoneTaskUpdate(String Description, int Priority, int position){
        try {
            DialogTaskUpdate.cancel();
            Handler.ShowSnack("Aguarde...",null, This, R_ID);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("description",Description);
            jsonObject.addProperty("priority",Priority);
            jsonObject.addProperty("id",adapter.getItem(position).get("id").getAsInt());

            Call<JsonObject> call = mainInterface.PutTask(getAppID(),
                    SU.getSession(),
                    jsonObject
            );

            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    try{
                        if (!Handler.isRequestError(response,This,R_ID)){
                            adapter.getItem(position).addProperty("description",Description);
                            adapter.getItem(position).addProperty("priority",Priority);
                            adapter.notifyItemChanged(position);
                            Handler.ShowSnack("Tarefa atualizada",null, This, R_ID);
                        }
                    }catch (Exception e) {
                        Handler.ShowSnack("Houve um erro","MainActivity.DoneTaskUpdate.callDescription.onResponse: " + e.getMessage(), This, R_ID);
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Handler.ShowSnack("Houve um erro","MainActivity.DoneTaskUpdate.callDescription.onFailure: " + t.toString(), This, R_ID);
                }
            });

        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","MainActivity.DoneTaskUpdate: " + e.getMessage(), This, R_ID);
        }
    }

    private void getScore(TextView textView){
        try {
            Call<JsonObject> call = mainInterface.GetScore(getAppID(), SU.getSession(), SU.getId(),"no");
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    try{
                        if (!Handler.isRequestError(response,This,R_ID)){
                            JsonObject jsonObject = response.body();

                            textView.setText(jsonObject.get("data").getAsJsonArray().get(0).getAsJsonObject().get("score").getAsString());
                        }
                    }catch (Exception e) {
                        Handler.ShowSnack("Houve um erro","MainActivity.GetDepartment.onResponse: " + e.getMessage(), This, R_ID);
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Handler.ShowSnack("Houve um erro","MainActivity.GetDepartment.onFailure: " + t.toString(), This, R_ID);
                }
            });

        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","MainActivity.GetDepartment: " + e.getMessage(), This, R_ID);
        }
    }
}