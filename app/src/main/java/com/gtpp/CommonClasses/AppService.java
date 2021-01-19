package com.gtpp.CommonClasses;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

public class AppService extends Service {

    private SavedUser SU = SavedUser.getSavedUser();
    private NotifyListener notifyListener;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        try {
            super.onCreate();
            SetWebSocket();
            int NOTIFICATION_ID = (int) (System.currentTimeMillis() % 10000);
            this.startForeground(NOTIFICATION_ID, new Notification.Builder(this).build());
        }catch (Exception e){

        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void SetWebSocket() {
        try {
            notifyListener = new NotifyListener(getApplicationContext(), SU);
            notifyListener.InstantiateWebSocket(notifyListener);
        }catch (Exception e){
            //Handler.ShowSnack("Houve um erro","MainActivity.SetWebSocket: "+e.getMessage(), this, R_ID,true);
        }
    }

    @Override
    public void onDestroy() {
        notifyListener.getWebSocket().cancel();
    }
}
