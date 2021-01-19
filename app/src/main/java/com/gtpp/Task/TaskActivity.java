package com.gtpp.Task;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.gtpp.CommonClasses.ApiClient;
import com.gtpp.CommonClasses.Handler;
import com.gtpp.CommonClasses.RecyclerItemClickListener;
import com.gtpp.CommonClasses.SavedUser;
import com.gtpp.Main.MainInterface;
import com.gtpp.Message.MessageActivity;
import com.gtpp.R;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.gtpp.CommonClasses.Handler.getAppID;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class TaskActivity extends AppCompatActivity {

    private final int R_ID = R.id.activityTask_ButtonPriority;
    private Activity This = TaskActivity.this;
    private SavedUser SU;
    private final com.gtpp.CommonClasses.Handler Handler= new Handler();
    private Intent intent;
    private TextView TaskDescription, TaskFullDescription, Exclamation, TextViewPercent, TextViewMessage, TextViewDates;
    private JsonObject TaskObject;
    private JsonArray jsonArrayCSDS;
    private Button Priority;
    private RecyclerView recyclerViewChecklist;
    private RecyclerView recyclerViewGuest;
    private TaskItemAdapter taskItemAdapter;
    private TaskGuestAdapter taskGuestAdapter;
    private TaskCheckUserAdapter taskCheckUserAdapter;
    private TaskHistoricAdapter taskHistoricAdapter;
    private Button buttonCreateItem, buttonState, buttonTaskUser, buttonFullDescription, buttonMessage;
    private TextInputEditText textInputEditTextItem;
    private int taskPosition;

    private Dialog DialogPutDays, DialogTaskUser, DialogPutDescription, DialogComShoDepSub, DialogStateChangeReason, DialogHistoric;

    private Button buttonCompany, buttonShop, buttonDepartment, buttonSubDepartment, buttonHistoric;
    private CardView cardViewCompany, cardViewShop, cardViewDepartment, cardViewSubDepartment;
    private TaskComShoDepSubAdapter taskComShoDepSubAdapter;

    private ProgressBar progressBar;

    private final MainInterface mainInterface = ApiClient.getApiClient().create(MainInterface.class);
    private final TaskInterface taskInterface = ApiClient.getApiClient().create(TaskInterface.class);

    private ConstraintLayout constraintLayoutOrder;
    private FloatingActionButton floatingActionButtonUp, floatingActionButtonDown, floatingActionButtonConfirm;
    private int SelectedItem = 0, constraintLayoutOrderWidth;

    private JsonArray stateList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_task);
        TaskDescription = findViewById(R.id.activityTask_textViewDescription);
        TaskFullDescription = findViewById(R.id.activityTask_textViewFullDescription);
        Priority = findViewById(R.id.activityTask_ButtonPriority);
        TextViewPercent = findViewById(R.id.activityTask_TextViewPercent);
        Exclamation = findViewById(R.id.activityTask_TextViewExclamation);
        recyclerViewChecklist = findViewById(R.id.activityTask_RecyclerViewChecklist);
        recyclerViewGuest = findViewById(R.id.activityTask_RecyclerViewGuest);
        buttonCreateItem = findViewById(R.id.activityTask_ButtonItem);
        textInputEditTextItem = findViewById(R.id.activityTask_TextInputEditTextItem);
        TextViewMessage = findViewById(R.id.activityTask_textViewMessage);
        TextViewDates = findViewById(R.id.activityTask_TextViewDates);
        buttonState = findViewById(R.id.activityTask_ButtonStatus);
        buttonTaskUser = findViewById(R.id.activityTask_ButtonTaskUser);
        buttonFullDescription = findViewById(R.id.activityTask_ButtonFullDescriptionEdit);
        progressBar = findViewById(R.id.activityMain_ProgressBar);

        buttonMessage = findViewById(R.id.activityTask_ButtonMessage);
        buttonHistoric = findViewById(R.id.activityTask_ButtonHistoric);

        buttonCompany = findViewById(R.id.activityTask_ButtonCompany);
        buttonShop = findViewById(R.id.activityTask_ButtonShop);
        buttonDepartment = findViewById(R.id.activityTask_ButtonDepartment);
        buttonSubDepartment= findViewById(R.id.activityTask_ButtonSubDepartment);
        cardViewCompany = findViewById(R.id.activityTask_CardViewCompany);
        cardViewShop = findViewById(R.id.activityTask_CardViewShop);
        cardViewDepartment = findViewById(R.id.activityTask_CardViewDepartment);
        cardViewSubDepartment= findViewById(R.id.activityTask_CardViewSubDepartment);

        constraintLayoutOrder = findViewById(R.id.ConstraintLayout_Order);
        constraintLayoutOrder.setAlpha(0);
        constraintLayoutOrder.setEnabled(false);
        constraintLayoutOrderWidth = constraintLayoutOrder.getMaxWidth();
        constraintLayoutOrder.setMaxWidth(0);
        floatingActionButtonUp = findViewById(R.id.floatingActionButton_Up);
        floatingActionButtonDown = findViewById(R.id.floatingActionButton_Down);
        floatingActionButtonConfirm = findViewById(R.id.floatingActionButton_Confirm);

        cardViewShop.setVisibility(View.INVISIBLE);
        cardViewDepartment.setVisibility(View.INVISIBLE);
        cardViewSubDepartment.setVisibility(View.INVISIBLE);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewChecklist.setLayoutManager(layoutManager);
        recyclerViewChecklist.setHasFixedSize(true);

        StaggeredGridLayoutManager layoutManagerRecyclerViewTaskUser =
                new StaggeredGridLayoutManager(8, StaggeredGridLayoutManager.VERTICAL);
        recyclerViewGuest.setLayoutManager(layoutManagerRecyclerViewTaskUser);
        recyclerViewChecklist.setHasFixedSize(true);

        SU = SavedUser.getSavedUser();
        intent = getIntent();

        taskPosition = intent.getIntExtra("position",0);

        TaskObject = new JsonParser().parse(Objects.requireNonNull(
                intent.getStringExtra("task_object"))).getAsJsonObject();

        TaskDescription.setText(TaskObject.get("description").getAsString());

        stateList = new JsonParser().parse(Objects.requireNonNull(
                intent.getStringExtra("state_list"))).getAsJsonArray();

        String InitialDate = TaskObject.get("initial_date").getAsString();
        InitialDate = InitialDate.substring(8, 10) + "/"
                + InitialDate.substring(5, 7) + "/"
                + InitialDate.substring(0, 4);

        String FinalDate = TaskObject.get("final_date").getAsString();
        FinalDate = FinalDate.substring(8, 10) + "/"
                + FinalDate.substring(5, 7) + "/"
                + FinalDate.substring(0, 4);

        String percent = TaskObject.get("percent").getAsString()+"%";
        TextViewPercent.setText(percent);
        progressBar.setProgress(TaskObject.get("percent").getAsInt(),true);

        String dates = "Iniciou em " + InitialDate + "\nTermina em " + FinalDate;
        TextViewDates.setText(dates);

        SetAlpha();
        SetButtonState();
        GetTask();
        SetButton();
        SetRecyclerView();
        SetButtonLocker();
        SetPriority();
    }

    public void onBackPressed(){
        Intent intent = new Intent();
        intent.putExtra("state_id", TaskObject.get("state_id").getAsInt());
        intent.putExtra("expire", TaskObject.get("expire").getAsInt());
        intent.putExtra("position",taskPosition);
        setResult(RESULT_OK,intent);

        this.finish();
    }

    private void SetAlpha(){

        TextViewMessage.setVisibility(View.INVISIBLE);
        Exclamation.setVisibility(View.INVISIBLE);
        if(TaskObject.get("state_id").getAsInt() == 6 || TaskObject.get("state_id").getAsInt() == 7){
            return;
        }

        AlphaAnimation MessageAnimation = new AlphaAnimation(1.0f, 0.0f);
        MessageAnimation.setDuration(1000);
        MessageAnimation.setStartOffset(2000);
        MessageAnimation.setFillAfter(true);

        AlphaAnimation DescriptionAnimation = new AlphaAnimation(0.0f, 1.0f);
        DescriptionAnimation.setDuration(1000);
        DescriptionAnimation.setStartOffset(3000);
        DescriptionAnimation.setFillAfter(true);

        AlphaAnimation ExclamationAnimation = new AlphaAnimation(0.0f, 1.0f);
        ExclamationAnimation.setDuration(4000);
        ExclamationAnimation.setStartOffset(2000);
        ExclamationAnimation.setFillAfter(true);

        TextViewMessage.setVisibility(View.INVISIBLE);



        if (TaskObject.get("state_id").getAsInt() != 3) {
            if (TaskObject.get("expire").getAsInt() < 8 && TaskObject.get("expire").getAsInt() > 0) {
                Exclamation.setTextColor(ContextCompat.getColor(this, R.color.colorYellow));
                Exclamation.startAnimation(ExclamationAnimation);
                TextViewMessage.setText("Expira em "+TaskObject.get("expire").getAsString()+" dias");
                TextViewMessage.setTextColor(ContextCompat.getColor(this, R.color.colorYellow));
                TextViewMessage.setAnimation(MessageAnimation);
                TaskDescription.setAnimation(DescriptionAnimation);
            } else if (TaskObject.get("expire").getAsInt() < 0) {
                int expire =TaskObject.get("expire").getAsInt();
                expire = expire - expire * 2;
                Exclamation.setTextColor(Color.RED);
                Exclamation.startAnimation(ExclamationAnimation);
                if(expire == 1){
                    TextViewMessage.setText("Atrasou "+expire+" dia");
                }else{
                    TextViewMessage.setText("Atrasou "+expire+" dias");
                }
                TextViewMessage.setTextColor(Color.RED);
                TextViewMessage.setAnimation(MessageAnimation);
                TaskDescription.setAnimation(DescriptionAnimation);
            } else if (TaskObject.get("expire").getAsInt() == 0) {
                Exclamation.setTextColor(Color.RED);
                Exclamation.startAnimation(ExclamationAnimation);
                TextViewMessage.setText("Expira hoje");
                TextViewMessage.setTextColor(Color.RED);
                TextViewMessage.setAnimation(MessageAnimation);
                TaskDescription.setAnimation(DescriptionAnimation);
            }
        }
    }

    private void SetPriority(){

        AlphaAnimation PriorityAnimation = new AlphaAnimation(0.0f, 1.0f);
        PriorityAnimation.setDuration(4000);
        PriorityAnimation.setStartOffset(0);
        PriorityAnimation.setFillAfter(true);

        int priority = TaskObject.get("priority").getAsInt();
        switch (priority) {
            case 2:
                Priority.setBackgroundColor(Color.RED);
                break;
            case 1:
                Priority.setBackgroundResource(R.color.colorYellow);
                break;
            case 0:
                Priority.setBackgroundResource(R.color.colorGreen);
                break;
        }
        Priority.startAnimation(PriorityAnimation);
    }

    private void SetCSDS_Description(){
        if (jsonArrayCSDS.size()>0) {
            String CompanyDescription, ShopDescription;
            CompanyDescription = jsonArrayCSDS.get(0).getAsJsonObject().get("company_description").getAsString();
            ShopDescription = jsonArrayCSDS.get(0).getAsJsonObject().get("shop_description").getAsString();

            buttonCompany.setText(CompanyDescription);
            cardViewCompany.setAlpha(1F);
            buttonShop.setText(ShopDescription);
            cardViewShop.setAlpha(1F);
            cardViewShop.setVisibility(View.VISIBLE);
        }

        if(jsonArrayCSDS.size()==1){
            String DepartDescription = jsonArrayCSDS.get(0).getAsJsonObject().get("depart_description").getAsString();
            buttonDepartment.setText(DepartDescription);
            cardViewDepartment.setAlpha(1F);
            cardViewDepartment.setVisibility(View.VISIBLE);
        }

        if(jsonArrayCSDS.size()>1){
            buttonDepartment.setText(jsonArrayCSDS.size() + " Depart");
            cardViewDepartment.setAlpha(1F);
            cardViewDepartment.setVisibility(View.VISIBLE);
        }
    }

    private void SetButton(){
        try {
            buttonCreateItem.setOnClickListener(v -> {
                PostItem();
            });

            buttonTaskUser.setOnClickListener(v -> {
                DialogTaskUser();
            });

            buttonFullDescription.setOnClickListener(v -> {
                DialogPutFullDescription();
            });

            AlertDialog.Builder alert = new AlertDialog.Builder(This);
            alert.setCancelable(false);
            alert.setNeutralButton("Cancelar", (dialog, which) -> dialog.cancel());
            alert.setTitle("Tarefa");

            cardViewCompany.setAlpha(0.5F);
            cardViewShop.setAlpha(0.5F);
            cardViewDepartment.setAlpha(0.5F);
            cardViewSubDepartment.setAlpha(0.5F);

            buttonCompany.setOnClickListener(v -> DialogTaskComShoDepSub(1));
            /*buttonCompany.setOnLongClickListener(v -> {
                alert.setMessage("Deseja remover desta empresa?");
                alert.setPositiveButton("Remover", (dialog, which) -> {
                    buttonCompany.setText("EMPRESA");
                    cardViewShop.setVisibility(View.INVISIBLE);
                    cardViewDepartment.setVisibility(View.INVISIBLE);
                });
                alert.create();
                alert.show();
                return false;
            });*/

            buttonShop.setOnClickListener(v -> DialogTaskComShoDepSub(2));
            /*buttonShop.setOnLongClickListener(v -> {
                alert.setMessage("Deseja remover desta loja?");
                alert.setPositiveButton("Remover", (dialog, which) -> {
                    buttonShop.setText("LOJA");
                    cardViewDepartment.setVisibility(View.INVISIBLE);
                });
                alert.create();
                alert.show();
                return false;
            });*/

            buttonDepartment.setOnClickListener(v -> DialogTaskComShoDepSub(3));
            /*buttonDepartment.setOnLongClickListener(v -> {
                alert.setMessage("Deseja remover todos os departamentos?");
                alert.setPositiveButton("Remover", (dialog, which) -> {
                    buttonDepartment.setText("DEPART");
                    cardViewSubDepartment.setVisibility(View.INVISIBLE);
                });
                alert.create();
                alert.show();
                return false;
            });*/

            /*buttonSubDepartment.setOnClickListener(v->DialogTaskComShoDepSub(4));
        buttonSubDepartment.setOnLongClickListener(v->{
            alert.setMessage("Deseja remover todos os sub departamentos?");
            alert.setPositiveButton("Remover", (dialog, which) -> {
                buttonSubDepartment.setText("SUB");
            });
            alert.create();
            alert.show();
            return false;
        });*/

            buttonMessage.setOnClickListener(v -> {
                IntentMessage();
            });

            buttonHistoric.setOnClickListener(v -> {
                DialogHistoric();
            });

            floatingActionButtonConfirm.setOnClickListener(v->{
                constraintLayoutOrder.setAlpha(0);
                constraintLayoutOrder.setEnabled(false);
                constraintLayoutOrder.setMaxWidth(0);
            });

            floatingActionButtonDown.setOnClickListener(v->{
                if(SelectedItem >=  taskItemAdapter.getItemCount()-1){
                    return;
                }
                PutTaskItemOrder(taskItemAdapter.getItemID(SelectedItem),"next");
            });

            floatingActionButtonUp.setOnClickListener(v->{
                if(SelectedItem <=  0){
                    return;
                }
                PutTaskItemOrder(taskItemAdapter.getItemID(SelectedItem),"previous");
            });

        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","TaskActivity.SetButton: " + e.getMessage(), This, R_ID);
        }
    }

    private void SetButtonState(){
        try{
            buttonState.setText(intent.getStringExtra("state_description"));
            String color = intent.getStringExtra("state_color");
            buttonState.setBackgroundColor(Color.parseColor(color));

            AlertDialog.Builder alert = new AlertDialog.Builder(This);
            alert.setCancelable(false);
            alert.setNeutralButton("Cancelar", (dialog, which) -> dialog.cancel());
            alert.setTitle("Tarefa");

            buttonState.setOnClickListener(view -> {
                try {
                    int StateID = TaskObject.get("state_id").getAsInt();

                    if (StateID == 1 || StateID == 2) {
                        alert.setMessage("Deseja mudar o estado da tarefa para PARADO?");
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("task_id",TaskObject.get("id").getAsInt());
                        alert.setPositiveButton("Parar", (dialog, which) -> DialogStateChangeReason(jsonObject));
                        alert.create();
                        alert.show();
                        return;
                    }

                    if (StateID == 3) {
                        alert.setMessage("Deseja mudar o estado da tarefa para FINALIZADO?");
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("task_id",TaskObject.get("id").getAsInt());
                        alert.setPositiveButton("Finalizar", (dialog, which) -> PutTaskState(jsonObject));
                        alert.create();
                        alert.show();
                        return;
                    }

                    if (StateID == 4) {
                        alert.setMessage("Deseja retomar esta tarefa?");
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("task_id",TaskObject.get("id").getAsInt());
                        alert.setPositiveButton("Retomar", (dialog, which) -> PutTaskState(jsonObject));
                        alert.create();
                        alert.show();
                        return;
                    }

                    if (StateID == 5) {
                        DialogPutDays();
                        return;
                    }

                    if (StateID == 6) {
                        alert.setMessage("Deseja remotar esta tarefa que estava finalizada?");
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("task_id",TaskObject.get("id").getAsInt());
                        alert.setPositiveButton("Retomar", (dialog, which) -> DialogStateChangeReason(jsonObject));
                        alert.create();
                        alert.show();
                    }
                }catch (Exception e){
                    Handler.ShowSnack("Houve um erro","SetButtonState.setOnClickListener: " + e.getMessage(), This, R_ID);
                }
            });

            buttonState.setOnLongClickListener(v -> {
                try{
                    int StateID = TaskObject.get("state_id").getAsInt();

                    if(StateID == 6) return false;

                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("task_id",TaskObject.get("id").getAsInt());
                    jsonObject.addProperty("cancel",1);

                    if(StateID != 7){
                        alert.setMessage("Deseja cancelar esta tarefa?");
                        alert.setPositiveButton("Cancelar", (dialog, which) -> DialogStateChangeReason(jsonObject));
                    }else{
                        alert.setMessage("Deseja restaurar esta tarefa? Ela voltará como uma tarefa bloqueada");
                        alert.setPositiveButton("Restaurar", (dialog, which) -> DialogStateChangeReason(jsonObject));
                    }
                    alert.create();
                    alert.show();

                }catch (Exception e){
                    Handler.ShowSnack("Houve um erro","SetButtonState.setOnLongClickListener: " + e.getMessage(), This, R_ID);
                }
                return true;
            });
        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","SetButtonState: " + e.getMessage(), This, R_ID);
        }
    }

    private void SetRecyclerView(){
        recyclerViewChecklist.addOnItemTouchListener(new RecyclerItemClickListener(
                this, recyclerViewChecklist, new RecyclerItemClickListener.OnItemClickListener() {

            public void onItemClick(View view, int position) {}

            public boolean onLongItemClick(View view, final int position) {
                try{
                    /*if (TaskObject.get("user_id").getAsInt() != SU.getId() || !SU.isAdministrator()){
                        Handler.ShowSnack("Você não pode fazer isso",
                                "Somente o criador ou administradores podem alterar os itens desta tarefa, caso precise mudar algo entre em contato com seu administrador",
                                This,
                                R_ID,
                                true
                        );
                        return false;
                    }*/

                    final int ID = taskItemAdapter.getItemID(position);
                    SelectedItem = position;

                    DialogPutDescription = new Dialog(This,R.style.Theme_AppCompat_Dialog_MinWidth);
                    DialogPutDescription.setContentView(R.layout.dialog_put_description);

                    TextInputEditText textInputEditText = DialogPutDescription.findViewById(R.id.dialogPutDescription_TextInputEditText);
                    Button buttonOK = DialogPutDescription.findViewById(R.id.dialogPutDescription_ButtonOk);
                    Button buttonCancel = DialogPutDescription.findViewById(R.id.dialogPutDescription_ButtonCancel);
                    Button buttonOrder = DialogPutDescription.findViewById(R.id.dialogPutDescription_ButtonOrder);
                    CheckBox checkBox = DialogPutDescription.findViewById(R.id.dialogPutDescription_CheckBox);

                    textInputEditText.setText(taskItemAdapter.getItemDescription(position));
                    textInputEditText.requestFocus();

                    if(taskItemAdapter.getItem(position).get("yes_no").getAsInt() != 0){
                        checkBox.setChecked(true);
                    }else{
                        checkBox.setChecked(false);
                    }

                    buttonOK.setOnClickListener(v ->{
                        if(textInputEditText.getText().length() < 1){
                            textInputEditText.setError("Digite a descrição");
                            textInputEditText.requestFocus();
                            return;
                        }
                        PutItemDescription(textInputEditText.getText().toString(),position, view);
                        DialogPutDescription.cancel();
                    });

                    buttonCancel.setOnClickListener(v -> {
                        DialogPutDescription.cancel();
                    });

                    buttonOrder.setOnClickListener(v -> {
                        constraintLayoutOrder.setAlpha(1f);
                        constraintLayoutOrder.setEnabled(true);
                        constraintLayoutOrder.setMaxWidth(constraintLayoutOrderWidth);
                        DialogPutDescription.cancel();
                    });

                    checkBox.setOnClickListener(v->{
                        int YesNo = 0;
                        if(checkBox.isChecked()){
                            YesNo = -1;
                        }
                        DialogPutDescription.cancel();
                        PutTaskItemYesNoOnDialog(position,YesNo);
                    });

                    DialogPutDescription.show();

                }catch (Exception e){
                    Handler.ShowSnack("Houve um erro","TaskActivity.SetRecyclerView.onLongItemClick: " + e.getMessage(), This, R_ID);
                }

                return false;
            }
        }));
    }

    private void SetComShoDepSubRecyclerView(RecyclerView recyclerView, int Type){
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(
                this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {

            public void onItemClick(View view, int position) {
                switch (Type){
                    case 1:
                        buttonCompany.setText(taskComShoDepSubAdapter.getItem(position).get("description").getAsString());
                        cardViewCompany.setAlpha(1F);
                        cardViewShop.setVisibility(View.VISIBLE);
                        cardViewShop.setAlpha(0.5F);
                        buttonShop.setText("LOJA");
                        TaskObject.addProperty("company_id", taskComShoDepSubAdapter.getItem(position).get("id").getAsInt());
                        DialogComShoDepSub.cancel();
                        break;
                    case 2:
                        buttonShop.setText(taskComShoDepSubAdapter.getItem(position).get("description").getAsString());
                        cardViewShop.setAlpha(1F);
                        cardViewDepartment.setVisibility(View.VISIBLE);
                        cardViewDepartment.setAlpha(0.5F);
                        buttonDepartment.setText("DEPART");
                        TaskObject.addProperty("shop_id", taskComShoDepSubAdapter.getItem(position).get("id").getAsInt());
                        DialogComShoDepSub.cancel();
                        break;
                    case 3:
                        /*
                        buttonDepartment.setText(taskComShoDepSubAdapter.getItem(position).get("description").getAsString());
                        cardViewDepartment.setAlpha(1F);
                        TaskObject.addProperty("department_id", taskComShoDepSubAdapter.getItem(position).get("id").getAsInt());
*/
                        break;
                }
            }

            public boolean onLongItemClick(View view, final int position) { return false; }
        }));
    }

    private void GetTask(){
        try {
            Call<JsonObject> call = taskInterface.GetTask(getAppID(), SU.getSession(), TaskObject.get("id").getAsInt());
            call.enqueue(new Callback<JsonObject>() {
                @SuppressLint("SetTextI18n")
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    try {
                        if (!Handler.isRequestError(response,This,R_ID)){
                            JsonObject jsonObject = response.body();
                            assert jsonObject != null;
                            JsonObject data = jsonObject.get("data").getAsJsonObject();
                            if(data.get("csds") != JsonNull.INSTANCE){
                                jsonArrayCSDS = data.get("csds").getAsJsonArray();
                            }else{
                                jsonArrayCSDS = new JsonArray();
                            }
                            SetCSDS_Description();
                            if(data.get("full_description") != JsonNull.INSTANCE) {
                                TaskFullDescription.setText(data.get("full_description").getAsString());
                            }else{
                                TaskFullDescription.setText("Sem descrição");
                            }
                            //TextViewPercent.setText(data.get("percent").getAsString()+"%");
                            //progressBar.setProgress(data.get("percent").getAsInt(),true);

                            boolean Lock = TaskObject.get("state_id").getAsInt() == 4 || TaskObject.get("state_id").getAsInt() == 5
                                    || TaskObject.get("state_id").getAsInt() == 6 || TaskObject.get("state_id").getAsInt() == 7
                                    /*|| TaskObject.get("user_id").getAsInt() != SU.getId() && !SU.isAdministrator()*/;

                            if(data.get("task_item") != JsonNull.INSTANCE){
                                JsonArray CheckListArray = data.get("task_item").getAsJsonArray();
                                taskItemAdapter = new TaskItemAdapter(
                                        CheckListArray,
                                        TaskActivity.this,taskInterface,
                                        SU,
                                        R_ID,
                                        TextViewPercent,
                                        progressBar,
                                        buttonState,
                                        TaskObject,
                                        recyclerViewChecklist,
                                        stateList,
                                        Lock
                                );

                                recyclerViewChecklist.setAdapter(taskItemAdapter);
                                recyclerViewChecklist.smoothScrollToPosition(taskItemAdapter.getItemCount());
                            }

                            JsonArray UserArray = new JsonArray();
                            JsonObject FirstUserObject = new JsonObject();
                            FirstUserObject.addProperty("user_id",TaskObject.get("user_id").getAsInt());
                            FirstUserObject.addProperty("task_id",TaskObject.get("id").getAsInt());
                            FirstUserObject.addProperty("status",true);
                            UserArray.add(FirstUserObject);

                            if(data.get("task_user") != JsonNull.INSTANCE){
                                for (int i = 0; i < data.get("task_user").getAsJsonArray().size(); i++) {
                                    JsonObject object =  data.get("task_user").getAsJsonArray().get(i).getAsJsonObject();
                                    UserArray.add(object);
                                }
                            }

                            if (Lock) {
                                taskGuestAdapter = new TaskGuestAdapter(
                                        UserArray,
                                        TaskActivity.this,
                                        SU.getSession(),
                                        R_ID,
                                        true
                                );
                            }else{
                                taskGuestAdapter = new TaskGuestAdapter(
                                        UserArray,
                                        TaskActivity.this,
                                        SU.getSession(),
                                        R_ID,
                                        false
                                );
                            }

                            recyclerViewGuest.setAdapter(taskGuestAdapter);
                        }
                    }catch (Exception e){
                        Handler.ShowSnack("Houve um erro","TaskActivity.GetTask.onResponse: " + e.getMessage(), This, R_ID);
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Handler.ShowSnack("Houve um erro","TaskActivity.GetTask.onFailure: " + t.toString(), This, R_ID);
                }
            });

        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","TaskActivity.GetTask: " + e.getMessage(), This, R_ID);
        }
    }

    private void GetTaskUser(RecyclerView recyclerView){
        try {
            Call<JsonObject> call = taskInterface.GetTaskUser(getAppID(), SU.getSession(),1, TaskObject.get("id").getAsInt());
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    try {
                        if (!Handler.isRequestError(response,This,R_ID)){
                            JsonObject jsonObject = response.body();
                            assert jsonObject != null;
                            JsonArray data = jsonObject.get("data").getAsJsonArray();

                            taskCheckUserAdapter = new TaskCheckUserAdapter(
                                    data,
                                    TaskActivity.this,taskInterface,
                                    SU.getSession(),
                                    R_ID,
                                    TaskObject.get("id").getAsInt(),
                                    taskGuestAdapter
                            );
                            recyclerView.setAdapter(taskCheckUserAdapter);
                        }
                    }catch (Exception e){
                        Handler.ShowSnack("Houve um erro","TaskActivity.GetTaskUser.onResponse: " + e.getMessage(), This, R_ID);
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Handler.ShowSnack("Houve um erro","TaskActivity.GetTaskUser.onFailure: " + t.toString(), This, R_ID);
                }
            });

        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","TaskActivity.GetTaskUser: " + e.getMessage(), This, R_ID);
        }
    }

    private void GetCompany(RecyclerView recyclerView){
        try {
            Call<JsonObject> call = mainInterface.GetCompany(getAppID(), SU.getSession());
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    try {
                        if (!Handler.isRequestError(response, This, R_ID)) {
                            JsonObject jsonObject = response.body();
                            JsonArray companyList = jsonObject.get("data").getAsJsonArray();

                            taskComShoDepSubAdapter = new TaskComShoDepSubAdapter(companyList, TaskActivity.this, SU.getSession(), R_ID, TaskObject, null);
                            recyclerView.setAdapter(taskComShoDepSubAdapter);

                            SetComShoDepSubRecyclerView(recyclerView, 1);
                        }
                    }catch (Exception e){
                        Handler.ShowSnack("Houve um erro", "TaskActivity.GetCompany.onResponse: " + e.getMessage(), This, R_ID);
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Handler.ShowSnack("Houve um erro","TaskActivity.GetCompany.onFailure: " + t.toString(), This, R_ID);
                }
            });

        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","TaskActivity.GetCompany: " + e.getMessage(), This, R_ID);
        }
    }

    private void GetShop(RecyclerView recyclerView){
        try {
            Call<JsonObject> call = mainInterface.GetShop(getAppID(), SU.getSession(),TaskObject.get("company_id").getAsInt());
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    try {
                        if (!Handler.isRequestError(response, This, R_ID)) {
                            JsonObject jsonObject = response.body();
                            JsonArray companyList = jsonObject.get("data").getAsJsonArray();

                            taskComShoDepSubAdapter = new TaskComShoDepSubAdapter(companyList, TaskActivity.this, SU.getSession(), R_ID, TaskObject, null);
                            recyclerView.setAdapter(taskComShoDepSubAdapter);

                            SetComShoDepSubRecyclerView(recyclerView, 2);
                        }
                    }catch (Exception e){
                        Handler.ShowSnack("Houve um erro", "TaskActivity.GetShop.onResponse: " + e.getMessage(), This, R_ID);
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Handler.ShowSnack("Houve um erro","TaskActivity.GetShop.onFailure: " + t.toString(), This, R_ID);
                }
            });

        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","TaskActivity.GetShop: " + e.getMessage(), This, R_ID);
        }
    }

    private void GetDepartment(RecyclerView recyclerView){
        try {
            JsonObject jsonCSDS = TaskObject.get("csds").getAsJsonArray().get(0).getAsJsonObject();

            Call<JsonObject> call = mainInterface.GetDepartmentCheck(getAppID(),
                    SU.getSession(),
                    jsonCSDS.get("company_id").getAsInt(),
                    jsonCSDS.get("shop_id").getAsInt(),
                    TaskObject.get("id").getAsInt()
            );

            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    try{
                        if (!Handler.isRequestError(response,This,R_ID)){
                            JsonObject jsonObject = response.body();
                            JsonArray departmentList = jsonObject.get("data").getAsJsonArray();

                            taskComShoDepSubAdapter = new TaskComShoDepSubAdapter(departmentList, TaskActivity.this, SU.getSession(), R_ID, TaskObject, buttonDepartment);
                            recyclerView.setAdapter(taskComShoDepSubAdapter);

                            SetComShoDepSubRecyclerView(recyclerView,3);
                        }
                    }catch (Exception e) {
                        Handler.ShowSnack("Houve um erro","TaskActivity.GetDepartment.onResponse: " + e.getMessage(), This, R_ID);
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Handler.ShowSnack("Houve um erro","TaskActivity.GetDepartment.onFailure: " + t.toString(), This, R_ID);
                }
            });

        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","TaskActivity.GetDepartment: " + e.getMessage(), This, R_ID);
        }
    }

    private void GetHistoric(RecyclerView recyclerView){
        try {
            Call<JsonObject> call = taskInterface.GetHistoric(getAppID(), SU.getSession(),TaskObject.get("id").getAsInt());
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    try {
                        if(!Handler.isRequestError(response,This,R_ID)){
                            JsonObject jsonObject = response.body();
                            JsonArray jsonArray = jsonObject.get("data").getAsJsonArray();

                            taskHistoricAdapter = new TaskHistoricAdapter(jsonArray,TaskActivity.this,R_ID);

                            recyclerView.setAdapter(taskHistoricAdapter);
                            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(TaskActivity.this);
                            recyclerView.setLayoutManager(layoutManager);
                            recyclerView.setHasFixedSize(true);
                        }else{
                            DialogHistoric.cancel();
                        }
                    }catch (Exception e){
                        Handler.ShowSnack("Houve um erro","TaskActivity.GetHistoric.onResponse: " + e.getMessage(), This, R_ID);
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Handler.ShowSnack("Houve um erro","TaskActivity.GetHistoric.onFailure: " + t.toString(), This, R_ID);
                }
            });

        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","TaskActivity.GetHistoric: " + e.getMessage(), This, R_ID);
        }
    }

    private void PostItem(){
        try {
            if(textInputEditTextItem.getText().length()<1){
                textInputEditTextItem.setError("Insira a descrição do item");
                return;
            }

            hideKeyboard(This,textInputEditTextItem);
            Handler.ShowSnack("Aguarde...",null, This, R_ID);

            JsonObject ItemObject = new JsonObject();
            ItemObject.addProperty("description",textInputEditTextItem.getText().toString());
            ItemObject.addProperty("task_id",TaskObject.get("id").getAsInt());
            Call<JsonObject> call = taskInterface.PostTaskItem(getAppID(), SU.getSession(),ItemObject);
            call.enqueue(new Callback<JsonObject>() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    try {
                        if (!Handler.isRequestError(response,This,R_ID)){
                            JsonObject jsonObject = response.body().get("data").getAsJsonObject();

                            assert jsonObject != null;
                            int ID = jsonObject.get("last_id").getAsInt();
                            int StateID = jsonObject.get("state_id").getAsInt();
                            int percent = jsonObject.get("percent").getAsInt();
                            int order = jsonObject.get("order").getAsInt();
                            int yes_no = jsonObject.get("yes_no").getAsInt();

                            String s_percent = percent+"%";
                            TextViewPercent.setText(s_percent);
                            progressBar.setProgress(percent,true);

                            ItemObject.addProperty("check",false);
                            ItemObject.addProperty("id",ID);
                            ItemObject.addProperty("order",order);
                            ItemObject.addProperty("yes_no",yes_no);

                            JsonObject jsonState = new JsonObject();
                            for (int i = 0; i < stateList.size(); i++) {
                                if(stateList.get(i).getAsJsonObject().get("id").getAsInt() == StateID){
                                    jsonState = stateList.get(i).getAsJsonObject();
                                    i=stateList.size();
                                }
                            }

                            String StateDescription = jsonState.get("description").getAsString();
                            String StateColor = "#"+jsonState.get("color").getAsString();
                            buttonState.setText(StateDescription);
                            buttonState.setBackgroundColor(Color.parseColor(StateColor));
                            TaskObject.addProperty("state_id", StateID);

                            boolean Lock = TaskObject.get("state_id").getAsInt() == 4 || TaskObject.get("state_id").getAsInt() == 5
                                    || TaskObject.get("state_id").getAsInt() == 6 || TaskObject.get("state_id").getAsInt() == 7
                                    || TaskObject.get("user_id").getAsInt() != SU.getId() && !SU.isAdministrator();

                            if(taskItemAdapter == null){
                                JsonArray CheckListArray = new JsonArray();
                                CheckListArray.add(ItemObject);
                                taskItemAdapter = new TaskItemAdapter(
                                        CheckListArray,
                                        TaskActivity.this,taskInterface,
                                        SU,
                                        R_ID,
                                        TextViewPercent,
                                        progressBar,
                                        buttonState,
                                        TaskObject,
                                        recyclerViewChecklist,
                                        stateList,
                                        Lock
                                );
                                recyclerViewChecklist.setAdapter(taskItemAdapter);
                            }else{
                                taskItemAdapter.setNewItem(ItemObject);
                                taskItemAdapter.notifyItemInserted(taskItemAdapter.getItemCount());
                            }

                            recyclerViewChecklist.smoothScrollToPosition(taskItemAdapter.getItemCount());
                            textInputEditTextItem.setText("");

                            Handler.ShowSnack("Novo item inserido",null, This, R_ID);
                        }
                    }catch (Exception e){
                        Handler.ShowSnack("Houve um erro","TaskActivity.PostItem.onResponse: " + e.getMessage(), This, R_ID);
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Handler.ShowSnack("Houve um erro","TaskActivity.PostItem.onFailure: " + t.toString(), This, R_ID);
                }
            });

        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","TaskActivity.PostItem: " + e.getMessage(), This, R_ID);
        }
    }

    private void PutTaskState(JsonObject jsonObject){
        try {

            Call<JsonObject> call = taskInterface.PutTaskState(getAppID(), SU.getSession(),jsonObject);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    try {
                        if (!Handler.isRequestError(response,This,R_ID)){
                            JsonObject jsonObject = response.body();
                            JsonArray jsonArray = jsonObject.get("data").getAsJsonArray();
                            int ID = jsonArray.get(0).getAsJsonObject().get("id").getAsInt();
                            String Description = jsonArray.get(0).getAsJsonObject().get("description").getAsString();
                            String StateColor = "#"+jsonArray.get(0).getAsJsonObject().get("color").getAsString();

                            TaskObject.addProperty("state_id",ID);
                            TaskObject.addProperty("state_description", Description);

                            buttonState.setText(Description);
                            buttonState.setBackgroundColor(Color.parseColor(StateColor));

                            SetButtonLocker();
                            Handler.ShowSnack("Estado atualizado",null, This, R_ID);
                        }
                    }catch (Exception e){
                        Handler.ShowSnack("Houve um erro","TaskActivity.PutTaskState.onResponse: " + e.getMessage(), This, R_ID);
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Handler.ShowSnack("Houve um erro","TaskActivity.PutTaskState.onFailure: " + t.toString(), This, R_ID);
                }
            });

        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","TaskActivity.PutTaskState: " + e.getMessage(), This, R_ID);
        }
    }

    private void PutTaskItemOrder(int ID, String NextOrPrevious){
        try {
            JsonObject ItemObject = new JsonObject();
            ItemObject.addProperty("id",ID);
            ItemObject.addProperty("task_id",TaskObject.get("id").getAsInt());
            ItemObject.addProperty("next_or_previous",NextOrPrevious);

            constraintLayoutOrder.setEnabled(false);
            constraintLayoutOrder.setAlpha(0.2f);
            Call<JsonObject> call = taskInterface.PutTaskItemOrder(getAppID(), SU.getSession(),ItemObject);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    try {
                        if(!Handler.isRequestError(response,This,R_ID)){
                            if(NextOrPrevious.equalsIgnoreCase("next")){
                                taskItemAdapter.ItemDown(SelectedItem);
                                SelectedItem+=1;
                            }else{
                                taskItemAdapter.ItemUp(SelectedItem);
                                SelectedItem-=1;
                            }
                        }

                    }catch (Exception e){
                        Handler.ShowSnack("Houve um erro","TaskActivity.PutTaskItemOrder.onResponse: " + e.getMessage(), This, R_ID);
                    }

                    constraintLayoutOrder.setEnabled(true);
                    constraintLayoutOrder.setAlpha(1f);
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Handler.ShowSnack("Houve um erro","TaskActivity.PutTaskItemOrder.onFailure: " + t.toString(), This, R_ID);
                    constraintLayoutOrder.setEnabled(true);
                    constraintLayoutOrder.setAlpha(1f);
                }
            });

        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","TaskActivity.PutTaskItemOrder: " + e.getMessage(), This, R_ID);
            constraintLayoutOrder.setEnabled(true);
            constraintLayoutOrder.setAlpha(1f);
        }
    }

    private void PutTaskItemYesNoOnDialog(int position, int YesNo){
        try {
            JsonObject ItemObject = new JsonObject();
            ItemObject.addProperty("id",taskItemAdapter.getItemID(position));
            ItemObject.addProperty("yes_no",YesNo);
            ItemObject.addProperty("task_id",TaskObject.get("id").getAsInt());

            Call<JsonObject> call = taskInterface.PutTaskItemYesNo(getAppID(), SU.getSession(),ItemObject);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    try {
                        if(!Handler.isRequestError(response,This,R_ID)){
                            JsonObject jsonObject = response.body();
                            int percent = jsonObject.get("data").getAsJsonObject().get("percent").getAsInt();
                            int StateID = jsonObject.get("data").getAsJsonObject().get("state_id").getAsInt();

                            String StateDescription = stateList.get(StateID-1).getAsJsonObject().get("description").getAsString();
                            String StateColor = "#"+stateList.get(StateID-1).getAsJsonObject().get("color").getAsString();
                            TextViewPercent.setText(percent+"%");
                            progressBar.setProgress(percent,true);

                            buttonState.setText(StateDescription);
                            buttonState.setBackgroundColor(Color.parseColor(StateColor));

                            TaskObject.addProperty("state_id",StateID);

                            taskItemAdapter.getItem(position).getAsJsonObject().addProperty("yes_no", YesNo);

                            taskItemAdapter.getItem(position).getAsJsonObject().addProperty("check", YesNo == 1 || YesNo == 2);

                            taskItemAdapter.notifyItemChanged(position);
                        }
                    }catch (Exception e){
                        Handler.ShowSnack("Houve um erro","TaskActivity.PutTaskItemYesNoOnDialog.onResponse: " + e.getMessage(), This, R_ID);
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Handler.ShowSnack("Houve um erro","TaskActivity.PutTaskItemYesNoOnDialog.onFailure: " + t.toString(), This, R_ID);
                }
            });

        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","TaskActivity.PutTaskItemYesNoOnDialog: " + e.getMessage(), This, R_ID);
        }
    }

    private void DonePutDays(TextInputEditText textInputEditTextPutDays){
        try{
            String days = textInputEditTextPutDays.getText().toString();

            if (days.trim().length() < 1) {
                textInputEditTextPutDays.setError("Insira a quantidade de dias");
                textInputEditTextPutDays.requestFocus();
                return;
            }
            int iDays = Integer.parseInt(days);
            if(iDays < 1){
                textInputEditTextPutDays.setError("Insira mais dias para extender");
                textInputEditTextPutDays.requestFocus();
                return;
            }

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("days",textInputEditTextPutDays.getText().toString());
            jsonObject.addProperty("task_id",TaskObject.get("id").getAsInt());

            Call<JsonObject> call = taskInterface.PutDays(getAppID(),
                    SU.getSession(),
                    jsonObject
            );

            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    try {
                        if (!Handler.isRequestError(response, This, R_ID)) {
                            Handler.ShowSnack("Data final e estado da tarefa atualizados",null, This, R_ID);
                            DialogPutDays.cancel();
                            JsonObject jsonObject = response.body();
                            JsonObject jsonData = jsonObject.get("data").getAsJsonObject();

                            int StateID = jsonData.get("id").getAsInt();
                            String FinalDate = jsonData.get("final_date").getAsString();

                            String StateDescription = stateList.get(StateID-1).getAsJsonObject().get("description").getAsString();
                            String StateColor = "#"+stateList.get(StateID-1).getAsJsonObject().get("color").getAsString();

                            buttonState.setText(StateDescription);
                            buttonState.setBackgroundColor(Color.parseColor(StateColor));

                            String InitialDate = TaskObject.get("initial_date").getAsString();
                            InitialDate = InitialDate.substring(8,10)+"/"+InitialDate.substring(5,7)+"/"+InitialDate.substring(0,4);
                            FinalDate = FinalDate.substring(8,10)+"/"+FinalDate.substring(5,7)+"/"+FinalDate.substring(0,4);
                            String setDate = "Iniciou em " + InitialDate + "\nTermina em " + FinalDate;
                            TextViewDates.setText(setDate);

                            TaskObject.addProperty("final_date", FinalDate);
                            TaskObject.addProperty("state_id", StateID);
                            TaskObject.addProperty("expire", iDays);

                            SetButtonLocker();
                            SetAlpha();
                        }
                    }catch (Exception e){
                        Handler.ShowSnack("Houve um erro","TaskActivity.DonePutDays.onResponse: " + e.getMessage(), This, R_ID);
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Handler.ShowSnack("Houve um erro","TaskActivity.DonePutDays.onFailure: " + t.toString(), This, R_ID);
                }
            });
        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","TaskActivity.DonePutDays: " + e.getMessage(), This, R_ID);
        }
    }

    private void PutItemDescription(String Description, int position, View view){
        try{
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("task_id",TaskObject.get("id").getAsInt());
            jsonObject.addProperty("description",Description);
            jsonObject.addProperty("id",taskItemAdapter.getItemID(position));
            Call<JsonObject> call = taskInterface.PutTaskItem(2,SU.getSession(),jsonObject);

            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    try {
                        if (!Handler.isRequestError(response, This, R_ID)) {
                            taskItemAdapter.setItemDescription(Description, position);
                            hideKeyboard(getApplicationContext(),view);
                            Handler.ShowSnack("Descrição do item foi atualizada",null, This, R_ID);
                        }
                    }catch (Exception e){
                        Handler.ShowSnack("Houve um erro","TaskActivity.PutItemDescription.onResponse: " + e.getMessage(), This, R_ID);
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Handler.ShowSnack("Houve um erro","TaskActivity.PutItemDescription.onFailure: " + t.toString(), This, R_ID);
                }
            });
        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","TaskActivity.PutItemDescription: " + e.getMessage(), This, R_ID);
        }
    }

    private void SetButtonLocker(){
        if ((TaskObject.get("state_id").getAsInt() == 4 || TaskObject.get("state_id").getAsInt() == 5
                || TaskObject.get("state_id").getAsInt() == 6 || TaskObject.get("state_id").getAsInt() == 7)
                || (TaskObject.get("user_id").getAsInt() != SU.getId() && !SU.isAdministrator())) {
            buttonTaskUser.setEnabled(false);
            buttonTaskUser.setAlpha(0.1f);
            buttonFullDescription.setEnabled(false);
            buttonFullDescription.setAlpha(0.1f);
            buttonCompany.setEnabled(false);
            buttonShop.setEnabled(false);
            buttonDepartment.setEnabled(false);
            if(taskGuestAdapter != null){
                taskGuestAdapter.SetLock(true);
            }
            if(taskItemAdapter != null){
                taskItemAdapter.SetLock(true);
            }
        }else{
            textInputEditTextItem.setEnabled(true);
            textInputEditTextItem.setAlpha(1.0f);
            buttonCreateItem.setEnabled(true);
            buttonCreateItem.setAlpha(1.0f);
            buttonTaskUser.setEnabled(true);
            buttonTaskUser.setAlpha(1.0f);
            buttonFullDescription.setEnabled(true);
            buttonFullDescription.setAlpha(1.0f);
            buttonCompany.setEnabled(true);
            buttonShop.setEnabled(true);
            buttonDepartment.setEnabled(true);
            if(taskGuestAdapter != null){
                taskGuestAdapter.SetLock(false);
            }
            if(taskItemAdapter != null){
                taskItemAdapter.SetLock(false);
            }
        }
        if ((TaskObject.get("state_id").getAsInt() == 4 || TaskObject.get("state_id").getAsInt() == 5
                || TaskObject.get("state_id").getAsInt() == 6 || TaskObject.get("state_id").getAsInt() == 7)) {

            textInputEditTextItem.setEnabled(false);
            textInputEditTextItem.setAlpha(0.1f);
            buttonCreateItem.setEnabled(false);
            buttonCreateItem.setAlpha(0.1f);
        }else{
            textInputEditTextItem.setEnabled(true);
            textInputEditTextItem.setAlpha(1f);
            buttonCreateItem.setEnabled(true);
            buttonCreateItem.setAlpha(1f);
        }

    }

    private void PutFullDescription(String FullDescription){
        try{
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("full_description",FullDescription);
            jsonObject.addProperty("id",TaskObject.get("id").getAsInt());
            Call<JsonObject> call = mainInterface.PutTask(2,SU.getSession(),jsonObject);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    try {
                        if (!Handler.isRequestError(response,This,R_ID)){
                            TaskFullDescription.setText(FullDescription);
                            Handler.ShowSnack("Descrição completa foi atualizada",null, This, R_ID);
                        }
                    }catch (Exception e){
                        Handler.ShowSnack("Houve um erro","TaskActivity.PutFullDescription.onResponse: " + e.getMessage(), This, R_ID);
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Handler.ShowSnack("Houve um erro","TaskActivity.PutFullDescription.onFailure: " + t.toString(), This, R_ID);
                }
            });
        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","TaskActivity.PutFullDescription: " + e.getMessage(), This, R_ID);
        }
    }

    private void DialogStateChangeReason(JsonObject jsonObject){
        try {
            DialogStateChangeReason = new Dialog(This,R.style.Theme_AppCompat_Dialog_MinWidth);
            DialogStateChangeReason.setContentView(R.layout.dialog_state_change_reason);

            TextInputEditText textInputEditTextPutDays = DialogStateChangeReason.findViewById(R.id.dialogStateChangeReason_TextInputEditText);

            Button buttonOK = DialogStateChangeReason.findViewById(R.id.dialogStateChangeReason_ButtonOk);
            Button buttonCancel = DialogStateChangeReason.findViewById(R.id.dialogStateChangeReason_ButtonCancel);

            buttonCancel.setOnClickListener(v -> {
                DialogStateChangeReason.cancel();
            });

            buttonOK.setOnClickListener(v ->{
                if(textInputEditTextPutDays.length() < 1){
                    textInputEditTextPutDays.setError("Descreva um motivo");
                    return;
                }
                jsonObject.addProperty("reason",textInputEditTextPutDays.getText().toString());
                PutTaskState(jsonObject);
                DialogStateChangeReason.cancel();
            });

            DialogStateChangeReason.create();
            DialogStateChangeReason.show();

        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","TaskActivity.DialogStateChangeReason: " + e.getMessage(), This, R_ID);
        }
    }

    private void DialogHistoric(){
        try {
            DialogHistoric = new Dialog(This,R.style.Theme_AppCompat_Dialog_MinWidth);
            DialogHistoric.setContentView(R.layout.dialog_historic);

            RecyclerView recyclerView = DialogHistoric.findViewById(R.id.dialogHistoric_RecyclerView);

            GetHistoric(recyclerView);

            DialogHistoric.create();
            DialogHistoric.show();

        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","TaskActivity.DialogStateChangeReason: " + e.getMessage(), This, R_ID);
        }
    }

    private void DialogPutFullDescription(){
        try {
            DialogPutDescription = new Dialog(This,R.style.Theme_AppCompat_Dialog_MinWidth);
            DialogPutDescription.setContentView(R.layout.dialog_put_description);

            TextInputEditText textInputEditText = DialogPutDescription.findViewById(R.id.dialogPutDescription_TextInputEditText);
            Button buttonOK = DialogPutDescription.findViewById(R.id.dialogPutDescription_ButtonOk);
            Button buttonCancel = DialogPutDescription.findViewById(R.id.dialogPutDescription_ButtonCancel);
            textInputEditText.setText(TaskFullDescription.getText());
            buttonOK.setOnClickListener(v ->{
                PutFullDescription(textInputEditText.getText().toString());
                DialogPutDescription.cancel();
            });

            buttonCancel.setOnClickListener(v->{
                DialogPutDescription.cancel();
            });

            DialogPutDescription.create();
            DialogPutDescription.show();

        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","TaskActivity.DialogPutFullDescription: " + e.getMessage(), This, R_ID);
        }
    }

    private void DialogPutDays(){
        try {
            DialogPutDays = new Dialog(This,R.style.Theme_AppCompat_Dialog_MinWidth);
            DialogPutDays.setContentView(R.layout.dialog_put_days);

            TextInputEditText textInputEditTextPutDays = DialogPutDays.findViewById(R.id.dialogPutDays_TextInputEditText);

            Button buttonOK = DialogPutDays.findViewById(R.id.dialogPutDays_buttonOk);
            Button buttonCancel = DialogPutDays.findViewById(R.id.dialogPutDays_buttonCancel);

            buttonCancel.setOnClickListener(v -> {
                DialogPutDays.cancel();
            });

            buttonOK.setOnClickListener(v ->{
                DonePutDays(textInputEditTextPutDays);
            });

            DialogPutDays.create();
            DialogPutDays.show();

        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","TaskActivity.DialogPutDays: " + e.getMessage(), This, R_ID);
        }
    }

    private void DialogTaskUser(){
        try {
            DialogTaskUser = new Dialog(This,R.style.Theme_AppCompat_Dialog_MinWidth);
            DialogTaskUser.setContentView(R.layout.dialog_task_user);

            RecyclerView recyclerView = DialogTaskUser.findViewById(R.id.dialogTaskUser_RecyclerView);
            Button buttonOK = DialogTaskUser.findViewById(R.id.dialogTaskUser_buttonOk);

            SearchView searchView = DialogTaskUser.findViewById(R.id.dialogTaskUser_SearchView);

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    try {
                        if (!(taskCheckUserAdapter == null)) {
                            taskCheckUserAdapter.getFilter().filter(s);
                        }
                    }catch (Exception e){
                        Handler.ShowSnack("Houve um erro","TaskActivity.DialogTaskUser.onQueryTextChange: " + e.getMessage(), This, R_ID);
                    }
                    return false;
                }
            });

            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setHasFixedSize(true);

            buttonOK.setOnClickListener(v ->{
                DialogTaskUser.cancel();
            });

            GetTaskUser(recyclerView);

            DialogTaskUser.create();
            DialogTaskUser.show();

        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","TaskActivity.DialogTaskUser: " + e.getMessage(), This, R_ID);
        }
    }

    private void DialogTaskComShoDepSub(int Type){
        try {
            DialogComShoDepSub = new Dialog(This,R.style.Theme_AppCompat_Dialog_MinWidth);
            DialogComShoDepSub.setContentView(R.layout.dialog_task_comshodepsub);

            RecyclerView recyclerView = DialogComShoDepSub.findViewById(R.id.dialogTaskComShoDepSub_RecyclerView);
            SearchView searchView = DialogComShoDepSub.findViewById(R.id.dialogTaskComShoDepSub_SearchView);

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    if (!(taskComShoDepSubAdapter == null)) {
                        taskComShoDepSubAdapter.getFilter().filter(s);
                    }
                    return false;
                }
            });

            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setHasFixedSize(true);

            switch (Type){
                case 1:
                    GetCompany(recyclerView);
                    break;
                case 2:
                    GetShop(recyclerView);
                    break;
                case 3:
                    GetDepartment(recyclerView);
                    break;
            }

            DialogComShoDepSub.create();
            DialogComShoDepSub.show();

        }catch (Exception e){
            Handler.ShowSnack("Houve um erro","TaskActivity.DialogTaskComShoDepSub: " + e.getMessage(), This, R_ID);
        }
    }

    public void IntentMessage(){
        Intent intent = new Intent(this, MessageActivity.class);
        intent.putExtra("task_id", TaskObject.get("id").getAsInt());
        intent.putExtra("task_description", TaskObject.get("description").getAsString());
        startActivity(intent);
    }

    public static void hideKeyboard(Context context, View view) {
        try {
            InputMethodManager keyboard = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}