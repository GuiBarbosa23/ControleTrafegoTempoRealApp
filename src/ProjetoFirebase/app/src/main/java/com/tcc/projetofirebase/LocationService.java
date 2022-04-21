package com.tcc.projetofirebase;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//https://www.youtube.com/watch?v=4_RK_5bCoOY

//https://www.youtube.com/watch?v=o5gdrUliM70
//https://www.youtube.com/watch?v=BXwDM5VVuKA
//https://www.youtube.com/watch?v=CZ575BuLBo4
//https://www.youtube.com/watch?v=4_RK_5bCoOY
//https://www.youtube.com/watch?v=7EL1gLa4yl0

//PROXIMO PASSOS

//Botão de emergência apenas ele deve chavar o evento de gravar na tela principal. - ok
//Botão de parada na notificação. - ok
//chave de gravação ou não no firebase na notificação para testes.
//Passar valor de uma tela para um servico https://stackoverflow.com/questions/36992580/how-to-pass-value-from-activity-to-a-service-in-android
//Chamar o serviço na tela principal ao ser carregada


public class LocationService extends Service implements LocationListener{
    private static final String ACTION_STOP_LISTEN = "action_stop_listen";

    String ACTION_STOP_SERVICE= "STOP";
    boolean SW_FIREBASE;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String usuarioID;
    double latitudeAtual = 0;
    double latitudeUltima= 0;
    double latitudePenultima= 0;
    double longitudeAtual= 0;
    double longitudeUltima= 0;
    double longitudePenultima = 0;
    String address = "";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate(){
        super.onCreate();
        Log.i("Script","onCreate()");


    }

    @Override
    public void onDestroy(){
        stopForeground(true);
        stopSelf();
        super.onDestroy();
    }


    @Override
    public int onStartCommand(Intent intent,int flags,int startId){
        SW_FIREBASE = new Boolean(intent.getExtras().getString("VALOR_CHAVE"));
        //Toast.makeText(this,SW_FIREBASE, Toast.LENGTH_SHORT).show();
        Log.i("Script","CHAVE: " +SW_FIREBASE);
        if (ACTION_STOP_SERVICE.equals(intent.getAction())) {
            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
            managerCompat.cancel(1);
           stopSelf();
           stopForeground(true);
            managerCompat.cancel(1);
            android.os.Process.killProcess(android.os.Process.myPid());
        }

        //Intent stopSelf = new Intent(this, LocationService.class);
        //stopSelf.setAction(ACTION_STOP_SERVICE);

        //PendingIntent pStopSelf = PendingIntent
          //      .getService(this, 0, stopSelf
            //            ,PendingIntent.FLAG_IMMUTABLE);  // That you should change this part in your code


        Log.i("Script","onStartCommand()");
        createNotificationChannel();


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"My Notification");
        builder.setContentTitle("Coletando dados");
        builder.setContentText(address);
        builder.setSmallIcon(R.drawable.ic_launcher_background);
        builder.setAutoCancel(true);
        //builder.addAction(R.drawable.ic_launcher_background,"Close", pStopSelf);


        Intent stopSelf = new Intent(this, LocationService.class);
        stopSelf.setAction(this.ACTION_STOP_SERVICE);
        PendingIntent pStopSelf = PendingIntent.getService(this, 0, stopSelf,PendingIntent.FLAG_MUTABLE);
        builder.addAction(R.drawable.ic_launcher_background, "Parar App", pStopSelf);


        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);

        managerCompat.notify(1,builder.build());


        startForeground(1, builder.build());

        //check permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            // Permission is not granted
            //ActivityCompat.requestPermissions(Activity.Tela,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        } else {
            //start the program if permission is granted
            getLocation();
        }


        return START_STICKY;
    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("My Notification","My Notification",NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        Intent intent = new Intent(this, LocationService.class);
        //Intent intent_2 = new Intent(this, TelaPrincipal.class);
        //SW_FIREBASE = new Boolean(intent_2.getExtras().getString("VALOR_CHAVE"));
        if (ACTION_STOP_SERVICE.equals(intent.getAction())) {
            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
            managerCompat.cancel(1);
            stopSelf();
            stopForeground(true);
            managerCompat.cancel(1);
            android.os.Process.killProcess(android.os.Process.myPid());
        }

        //Toast.makeText(this,"ESTOU RODANDO!", Toast.LENGTH_SHORT).show();
        if (location==null){
        } else {
            //float nCurrentSpeed = location.getSpeed();
            float nCurrentSpeed = location.getSpeed() * 3.6f;

            int velocidadeArredondada = Math.round(nCurrentSpeed);


            latitudeUltima = latitudePenultima;
            latitudePenultima = latitudeAtual;
            latitudeAtual = location.getLatitude();

            longitudeUltima = longitudePenultima;
            longitudePenultima = longitudeAtual;
            longitudeAtual = location.getLongitude();

            //Get Latitude
            double nCurrentLatitude = latitudeAtual;
            //Get Longitude
            double nCurrentLongitude = longitudeAtual;

            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this);

            String address = "";
            String zip = "";
            String number = "";
            try {
                addresses = geocoder.getFromLocation(nCurrentLatitude, nCurrentLongitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                address = addresses.get(0).getAddressLine(0);
                zip = addresses.get(0).getPostalCode();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.i("Script","latitude_atual: " +latitudeAtual);
            Log.i("Script","latitude_ultima: " +latitudeUltima);
            Log.i("Script","latitude_penultima: " +latitudePenultima);
            Log.i("Script","longitude_atual: " +longitudeAtual);
            Log.i("Script","longitude_ultima: " +longitudeUltima);
            Log.i("Script","longitude_penultima: " +longitudePenultima);
            Log.i("Script","velocidade: " +velocidadeArredondada);
            Log.i("Script","Endereco: " +address);
            Log.i("Script","cep: " +zip);

            //NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"My Notification");
            //builder.setContentTitle("Capturando Dados");
            //builder.setContentText(String.format(address));
            //builder.setSmallIcon(R.drawable.ic_launcher_background);
            //builder.setAutoCancel(true);
            //NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
            //managerCompat.notify(1,builder.build());


            NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"My Notification");
            builder.setContentTitle("Coletando dados");
            builder.setContentText(address);
            builder.setSmallIcon(R.drawable.ic_launcher_background);
            builder.setAutoCancel(true);
            //builder.addAction(R.drawable.ic_launcher_background,"Close", pStopSelf);


            Intent stopSelf = new Intent(this, LocationService.class);
            stopSelf.setAction(this.ACTION_STOP_SERVICE);
            PendingIntent pStopSelf = PendingIntent.getService(this, 0, stopSelf,PendingIntent.FLAG_MUTABLE);
            builder.addAction(R.drawable.ic_launcher_background, "Parar App", pStopSelf);

            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
            managerCompat.notify(1,builder.build());


            if (SW_FIREBASE) {
                //Atualiza Banco de Dados
                SimpleDateFormat formatter = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss");
                Date curDate = new Date(System.currentTimeMillis());
                String strdatetimenow = formatter.format(curDate);
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                Map<String, Object> leitura_app = new HashMap<>();
                leitura_app.put("data_hora_atualizacao", strdatetimenow);
                leitura_app.put("latitude_atual", latitudeAtual);
                leitura_app.put("latitude_ultima", latitudeUltima);
                leitura_app.put("latitude_penultima", latitudePenultima);
                leitura_app.put("longitude_atual", longitudeAtual);
                leitura_app.put("longitude_ultima", longitudeUltima);
                leitura_app.put("longitude_penultima", longitudePenultima);
                leitura_app.put("velocidade", velocidadeArredondada);
                leitura_app.put("Endereco", address);
                leitura_app.put("cep", zip);

                usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                DocumentReference documentReference = db.collection("APP_REAL_TIME").document(usuarioID);
                documentReference.set(leitura_app).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("db", "Sucesso ao salvar os dados");
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("db_error", "Erro ao salvar os dados" + e.toString());
                            }
                        });

            }
        }


    }


    private void getLocation(){
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (lm != null){
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
            //commented, this is from the old version
            // this.onLocationChanged(null);
        }
        //Toast.makeText(this,"Aguardando conexão com GPS!", Toast.LENGTH_SHORT).show();


    }
}