package com.igorsantos.organizze.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.igorsantos.organizze.R;
import com.igorsantos.organizze.activity.config.ConfigutacaoFirebase;
import com.igorsantos.organizze.activity.model.Usuario;

public class Activity_login extends AppCompatActivity {

    private EditText campoEmail, campoSenha;
    private Button botaoEntrar;

    private Usuario usuario;

    private FirebaseAuth autenticacaoFirebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        campoEmail = findViewById(R.id.editTextEmail);
        campoSenha = findViewById(R.id.editTextSenha);
        botaoEntrar = findViewById(R.id.buttonEntrar);

        botaoEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String textoEmail = campoEmail.getText().toString();
                String textoSenha = campoSenha.getText().toString();

                if(!textoEmail.isEmpty()){

                    if(!textoSenha.isEmpty()){
                        usuario = new Usuario();
                        usuario.setEmail(textoEmail);
                        usuario.setSenha(textoSenha);
                        validarLogin();

                    }else{
                        Toast.makeText(Activity_login.this, "Digite a senha!", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(Activity_login.this, "Digite o email!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void validarLogin(){
        autenticacaoFirebase = ConfigutacaoFirebase.getAutenticacaoFirebase();
        autenticacaoFirebase.signInWithEmailAndPassword(usuario.getEmail(), usuario.getSenha()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(Activity_login.this, "Sucesso ao fazer login", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(Activity_login.this, "Erro ao fazer login", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
