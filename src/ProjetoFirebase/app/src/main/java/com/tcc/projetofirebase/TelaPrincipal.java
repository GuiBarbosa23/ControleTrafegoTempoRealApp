package com.tcc.projetofirebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.opengl.Visibility;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TelaPrincipal extends AppCompatActivity implements LocationListener {
    //https://www.youtube.com/watch?v=Ak1O9Gip-pg&list=WL

    private TextView nomeUsuario, emailUsuario;
    private Button bt_deslogar;
    private Switch sw_emergencia,sw_gravar;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String usuarioID;
    TextView textViewLatitude, textViewLongitude, textViewEndereco, textViewVelocidade;
    FusedLocationProviderClient fusedLocationProviderClient;
    double latitudeAtual = 0;
    double latitudeUltima= 0;
    double latitudePenultima= 0;
    double longitudeAtual= 0;
    double longitudeUltima= 0;
    double longitudePenultima = 0;
    // private GoogleMap mMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_principal);

        getSupportActionBar().hide();
        IniciarComponentes();


        //  SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
        //        .findFragmentById(R.id.map);
        // mapFragment.getMapAsync(this);

        //check permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
            //ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 1000);
        } else {
            //start the program if permission is granted
            getLocation();
        }


        bt_deslogar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(TelaPrincipal.this, FormLogin.class);
                startActivity(intent);
                finish();
            }
        });

        sw_emergencia.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SimpleDateFormat formatter = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss");
                Date curDate = new Date(System.currentTimeMillis());
                String strdatetimenow = formatter.format(curDate);
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                DocumentReference documentReference = db.collection("APP_REAL_TIME").document(usuarioID);
                documentReference.update("emergencia",sw_emergencia.isChecked());
                documentReference.update("data_hora_atualizacao",strdatetimenow);
            }
        });

        sw_gravar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                startService();
            }
        });
    }

    //  @Override
    // public void onMapReady(GoogleMap googleMap) {
    //     mMap = googleMap;

        // Add a marker in Sydney and move the camera
    //   LatLng sydney = new LatLng(-34, 151);
    //    mMap.addMarker(new MarkerOptions()
    //         .position(sydney)
    //        .title("Marker in Sydney"));
    //   mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    //  }

    @Override
    protected void onStart() {
        super.onStart();
        startService();
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DocumentReference documentReference = db.collection("Usuarios").document(usuarioID);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if (documentSnapshot != null){
                    nomeUsuario.setText(documentSnapshot.getString("Nome"));
                    emailUsuario.setText(email);
                    String tipoVeiculo = documentSnapshot.getString("Tipo de Veículo");
                    if (tipoVeiculo.equals("Policial") || tipoVeiculo.equals("Bombeiro") || tipoVeiculo.equals("Ambulância")  ) {
                        sw_emergencia.setVisibility(View.VISIBLE);
                    } else {
                        sw_emergencia.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    public void startService(){
        Intent serviceIntent = new Intent(this,LocationService.class);
        Log.i("Script","CHAVE: " + new Boolean(sw_gravar.isChecked()).toString());
        serviceIntent.putExtra("VALOR_CHAVE",new Boolean(sw_gravar.isChecked()).toString());
        this.startService(serviceIntent);
    }

    private void IniciarComponentes(){
        nomeUsuario = findViewById(R.id.textNameUser);
        emailUsuario = findViewById(R.id.textEmailUser);
        bt_deslogar = findViewById(R.id.bt_deslogar);
        sw_emergencia = findViewById(R.id.sw_emergencia);
        sw_gravar = findViewById(R.id.sw_gravar);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //Assign variable
        textViewLatitude = findViewById(R.id.textViewLatitude);
        textViewLongitude = findViewById(R.id.textViewLongitude);
        textViewEndereco = findViewById(R.id.textViewEndereco);
        textViewVelocidade = findViewById(R.id.textViewVelocidade);

    }


    @Override
    public void onLocationChanged(Location location) {
        if (location==null){

            textViewVelocidade.setText("-.- m/s");
            textViewLatitude.setText("nulo");
            textViewLongitude.setText("nulo");
            textViewEndereco.setText("nulo");


        } else {


            //float nCurrentSpeed = location.getSpeed();
            float nCurrentSpeed = location.getSpeed() * 3.6f;

            int velocidadeArredondada = Math.round(nCurrentSpeed);
            //Get velocidade
            //float nCurrentSpeed = location.getSpeed();
            textViewVelocidade.setText("Velocidade: " + velocidadeArredondada + " km/h" );
            //textViewVelocidade.setText("Velocidade: "+ String.format("%.2f", nCurrentSpeed)+ " m/s" );

            latitudeUltima = latitudePenultima;
            latitudePenultima = latitudeAtual;
            latitudeAtual = location.getLatitude();

            longitudeUltima = longitudePenultima;
            longitudePenultima = longitudeAtual;
            longitudeAtual = location.getLongitude();

            //Get Latitude
            double nCurrentLatitude = latitudeAtual;
            textViewLatitude.setText( "Lat: "+String.format("%.9f", nCurrentLatitude));
            //Get Longitude
            double nCurrentLongitude = longitudeAtual;
            textViewLongitude.setText("Lon: "+String.format("%.9f", nCurrentLongitude));

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

            //Get Longitude
            textViewEndereco.setText("Endereço :"+ String.format(address));

            //if (sw_gravar.isChecked()) {
            if (false) {
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
                leitura_app.put("emergencia", sw_emergencia.isChecked());

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

                //android.os.Handler handler = new android.os.Handler();
                //handler.postDelayed(new Runnable() {
                  //  public void run() {
                        // yourMethod();
                    //}
                //}, 5000);   //5 seconds
            }
        }


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {

                finish();
            }

        }

    }

    private void getLocation(){
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (lm != null){
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,0,this);
            //commented, this is from the old version
            // this.onLocationChanged(null);
        }
        Toast.makeText(this,"Aguardando conexão com GPS!", Toast.LENGTH_SHORT).show();


    }

}