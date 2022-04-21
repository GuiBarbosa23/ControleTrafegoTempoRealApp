package com.tcc.projetofirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormCadastro extends AppCompatActivity {

    //Declaração de todas as variavéis
    private EditText edit_nome, edit_email, edit_senha, edit_placa, edit_tag;
    private CheckBox check_pedestre, check_motorista;
    private Spinner spinner_def, spinner_veiculos;
    private Button bt_cadastrar;
    private TextView text_def, text_veiculo;
    private ImageView im_help;
    String[] mensagens = {"Preencha todos os campos", "Cadastro realizado com sucesso", "A placa deve conter 7 caracteres", "Selecione uma ou as duas opções", "Selecione um tipo de veículo","Possui mobilidade reduzida?"};
    String[] def = {"Selecione...", "Sim", "Não"};
    String[] veiculo = {"Selecione...", "Passeio", "Moto","Caminhão", "Ônibus", "Táxi/Uber", "Policial", "Ambulância", "Bombeiro"};
    String usuarioID;

    //Principal
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_cadastro);

        getSupportActionBar().hide();
        IniciarComponentes();

        //Check Pedestre
        check_pedestre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (check_pedestre.isChecked()){
                    edit_nome.setVisibility(View.VISIBLE);
                    edit_email.setVisibility(View.VISIBLE);
                    edit_senha.setVisibility(View.VISIBLE);
                    edit_tag.setVisibility(View.VISIBLE);
                    spinner_def.setVisibility(View.VISIBLE);
                    text_def.setVisibility(View.VISIBLE);
                }else if (check_motorista.isChecked()){
                    edit_nome.setVisibility(View.VISIBLE);
                    edit_email.setVisibility(View.VISIBLE);
                    edit_senha.setVisibility(View.VISIBLE);
                    edit_tag.setVisibility(View.INVISIBLE);
                    spinner_def.setVisibility(View.INVISIBLE);
                    text_def.setVisibility(View.INVISIBLE);
                }else{
                    edit_nome.setVisibility(View.INVISIBLE);
                    edit_email.setVisibility(View.INVISIBLE);
                    edit_senha.setVisibility(View.INVISIBLE);
                    edit_tag.setVisibility(View.INVISIBLE);
                    spinner_def.setVisibility(View.INVISIBLE);
                    text_def.setVisibility(View.INVISIBLE);
                }
            }
        });

        //Check Motorista
        check_motorista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (check_motorista.isChecked()){
                    edit_nome.setVisibility(View.VISIBLE);
                    edit_email.setVisibility(View.VISIBLE);
                    edit_senha.setVisibility(View.VISIBLE);
                    edit_placa.setVisibility(View.VISIBLE);
                    spinner_veiculos.setVisibility(View.VISIBLE);
                    text_veiculo.setVisibility(View.VISIBLE);
                }else if (check_pedestre.isChecked()){
                    edit_nome.setVisibility(View.VISIBLE);
                    edit_email.setVisibility(View.VISIBLE);
                    edit_senha.setVisibility(View.VISIBLE);
                    edit_tag.setVisibility(View.VISIBLE);
                    edit_placa.setVisibility(View.INVISIBLE);
                    spinner_veiculos.setVisibility(View.INVISIBLE);
                    text_veiculo.setVisibility(View.INVISIBLE);
                }else{
                    edit_nome.setVisibility(View.INVISIBLE);
                    edit_email.setVisibility(View.INVISIBLE);
                    edit_senha.setVisibility(View.INVISIBLE);
                    edit_placa.setVisibility(View.INVISIBLE);
                    spinner_veiculos.setVisibility(View.INVISIBLE);
                    text_veiculo.setVisibility(View.INVISIBLE);
                }
            }
        });

        //Linka Spinner com a String-Array (def) e desabilita o primeiro item
        final ArrayAdapter<String> spinnerArrayAdapter_1 = new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,def){
            @Override
            public boolean isEnabled(int position){
                if(position == 0){
                    //Desabilita a primeira posição (hint)
                    return false;
                }else{
                   return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent){
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    //Deixa o hint com a cor cinza (efeito de desabilitado)
                    tv.setTextColor(Color.GRAY);
                }else{
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        //Linka o setAdapter_1 ao Spinner de Mobilidade Reduzida
        spinnerArrayAdapter_1.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner_def.setAdapter(spinnerArrayAdapter_1);

        //Linka Spinner com a String-Array (veiculo) e desabilita o primeiro item
        final ArrayAdapter<String> spinnerArrayAdapter_2 = new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,veiculo){
            @Override
            public boolean isEnabled(int position){
                if(position == 0){
                    //Desabilita a primeira posição (hint)
                    return false;
                }else{
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent){
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    //Deixa o hint com a cor cinza ( efeito de desabilitado)
                    tv.setTextColor(Color.GRAY);
                }else{
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        //Linka o setAdapter_2 ao Spinner de Tipo de Veículo
        spinnerArrayAdapter_2.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner_veiculos.setAdapter(spinnerArrayAdapter_2);

        //Pop-up Ajuda
        im_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder ajuda = new AlertDialog.Builder(FormCadastro.this);
                ajuda.setTitle("Ajuda!");
                ajuda.setMessage("1º - Escolha uma das opções, ou as duas (Pedestre e Motorista);\n\n2º - Preencha os campos de entrada (Nome, Email e Senha);\n\n3º - Escolhendo a opção Pedestre, aparecerá o campo de Mobilidade Reduzida, esse campo será utilizado para reduzir o tempo de semáforo em verde, caso haja um usuário que possua mobilidade reduzida no trecho da via.\nSendo que a mobilidade reduzida poderá ser auditiva, visual e física;\n\n4º - Escolhendo a opção Motorista, aparecerão os campos de placa e tipo de veículo.\nPara o tipo de veículo, selecione o tipo que será utilizado.");

                ajuda.create().show();
            }
        });

        //Botão Cadastrar
        bt_cadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String nome = edit_nome.getText().toString();
                String email = edit_email.getText().toString();
                String senha = edit_senha.getText().toString();
                String tag = edit_tag.getText().toString();
                String placa = edit_placa.getText().toString();
                int car = spinner_veiculos.getLastVisiblePosition();
                int mob = spinner_def.getLastVisiblePosition();

                if(check_pedestre.isChecked() || check_motorista.isChecked()){

                    if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || (tag.isEmpty() & check_pedestre.isChecked()) || (placa.isEmpty() & check_motorista.isChecked())) {
                        Snackbar snackbar = Snackbar.make(view, mensagens[0], Snackbar.LENGTH_SHORT);
                        snackbar.setBackgroundTint(Color.WHITE);
                        snackbar.setTextColor(Color.BLACK);
                        snackbar.show();
                    }else if (check_motorista.isChecked() & ((placa.length() != 7 ))){
                        Snackbar snackbar = Snackbar.make(view, mensagens[2], Snackbar.LENGTH_SHORT);
                        snackbar.setBackgroundTint(Color.WHITE);
                        snackbar.setTextColor(Color.BLACK);
                        snackbar.show();
                    }else if(check_motorista.isChecked() & car == 0){
                        Snackbar snackbar = Snackbar.make(view, mensagens[4], Snackbar.LENGTH_SHORT);
                        snackbar.setBackgroundTint(Color.WHITE);
                        snackbar.setTextColor(Color.BLACK);
                        snackbar.show();
                    }else if(check_pedestre.isChecked() & mob == 0){
                        Snackbar snackbar = Snackbar.make(view, mensagens[5], Snackbar.LENGTH_SHORT);
                        snackbar.setBackgroundTint(Color.WHITE);
                        snackbar.setTextColor(Color.BLACK);
                        snackbar.show();
                    }else{
                        CadastrarUsusario(view);
                    }

                }else{
                    Snackbar snackbar = Snackbar.make(view, mensagens[3], Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(Color.WHITE);
                    snackbar.setTextColor(Color.BLACK);
                    snackbar.show();
                }
            }
        });
    }

    //Firebase Authetication - Cadastro de usuário e senha para autenticação
    private void CadastrarUsusario(View view){

        String email = edit_email.getText().toString();
        String senha = edit_senha.getText().toString();

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, senha).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){

                    SalvarDadosUsuario();

                    Snackbar snackbar = Snackbar.make(view, mensagens[1], Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(Color.WHITE);
                    snackbar.setTextColor(Color.BLACK);
                    snackbar.show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            TelaLogin();
                        }
                    }, 3000);
                }else{
                    String erro;
                    try {
                        throw task.getException();

                    }catch (FirebaseAuthWeakPasswordException e) {
                        erro = "Digite uma senha com no mínimo 6 caracteres";
                    }catch (FirebaseAuthUserCollisionException e) {
                        erro = "Essa conta já possui cadastro";
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        erro = "E-mail inválido";
                    }catch (Exception e){
                        erro = "Erro ao cadastrar usuário";
                    }

                    Snackbar snackbar = Snackbar.make(view, erro, Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(Color.WHITE);
                    snackbar.setTextColor(Color.BLACK);
                    snackbar.show();
                }
            }
        });

    }

    //Firestore Database - Salva informações dos usuários
    private void SalvarDadosUsuario(){

        if (check_pedestre.isChecked() & check_motorista.isChecked()){

            String nome = edit_nome.getText().toString();
            String placa = edit_placa.getText().toString();
            String tag = edit_tag.getText().toString();
            String mob = spinner_def.getSelectedItem().toString();
            String car = spinner_veiculos.getSelectedItem().toString();
            String w = check_pedestre.getText().toString();
            String d = check_motorista.getText().toString();

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            Map<String, Object> usuarios = new HashMap<>();
            usuarios.put("Nome", nome);
            usuarios.put("Placa", placa);
            usuarios.put("Tag", tag);
            usuarios.put("Mobilidade Reduzida", mob);
            usuarios.put("Tipo de Veículo", car);
            usuarios.put("Modo de Locomoção:", w + " e " + d);

            usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DocumentReference documentReference = db.collection("Usuarios").document(usuarioID);
            documentReference.set(usuarios).addOnSuccessListener(new OnSuccessListener<Void>() {
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
        }else if (check_pedestre.isChecked()) {

            String nome = edit_nome.getText().toString();
            String tag = edit_tag.getText().toString();
            String mob = spinner_def.getSelectedItem().toString();
            String w = check_pedestre.getText().toString();

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            Map<String, Object> usuarios = new HashMap<>();
            usuarios.put("Nome", nome);
            usuarios.put("Tag", tag);
            usuarios.put("Mobilidade Reduzida", mob);
            usuarios.put("Modo de Locomoção:", w);

            usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DocumentReference documentReference = db.collection("Usuarios").document(usuarioID);
            documentReference.set(usuarios).addOnSuccessListener(new OnSuccessListener<Void>() {
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
        }else{

            String nome = edit_nome.getText().toString();
            String placa = edit_placa.getText().toString();
            String car = spinner_veiculos.getSelectedItem().toString();
            String d = check_motorista.getText().toString();

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            Map<String, Object> usuarios = new HashMap<>();
            usuarios.put("Nome", nome);
            usuarios.put("Placa", placa);
            usuarios.put("Tipo de Veículo", car);
            usuarios.put("Modo de Locomoção:", d);

            usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DocumentReference documentReference = db.collection("Usuarios").document(usuarioID);
            documentReference.set(usuarios).addOnSuccessListener(new OnSuccessListener<Void>() {
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

    //Inicia a tela de login
    private void TelaLogin(){
        Intent intent = new Intent(FormCadastro.this, FormLogin.class);
        startActivity(intent);
        finish();
    }

    //Linka as variavéis da activity_form_cadastro.xml as variáveis de FormCadastro.java
    private void IniciarComponentes(){

        edit_nome = findViewById(R.id.edit_nome);
        edit_email = findViewById(R.id.edit_email);
        edit_senha = findViewById(R.id.edit_senha);
        edit_tag = findViewById(R.id.edit_tag);
        edit_placa = findViewById(R.id.edit_placa);
        bt_cadastrar = findViewById(R.id.bt_cadastrar);
        spinner_def = findViewById(R.id.spinner_def);
        spinner_veiculos = findViewById(R.id.spinner_veiculos);
        check_pedestre = findViewById(R.id.check_pedestre);
        check_motorista = findViewById(R.id.check_motorista);
        text_veiculo = findViewById(R.id.text_veiculo);
        text_def = findViewById(R.id.text_def);
        im_help = findViewById(R.id.im_help);

    }

}