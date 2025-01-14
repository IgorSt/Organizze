package com.igorsantos.organizese.activity.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.igorsantos.organizese.R;
import com.igorsantos.organizese.activity.config.ConfiguracaoFirebase;
import com.igorsantos.organizese.activity.helper.Base64Custom;
import com.igorsantos.organizese.activity.model.Usuario;

public class Activity_cadastro extends AppCompatActivity {

    private EditText campoNome, campoEmail, campoSenha;
    private Button botaoCadastrar;

    private FirebaseAuth autenticacaoFireBase;

    private Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        getSupportActionBar().setTitle("Cadastro");

        campoNome = findViewById(R.id.editTextNome);
        campoEmail = findViewById(R.id.editTextEmail);
        campoSenha = findViewById(R.id.editTextSenha);
        botaoCadastrar = findViewById(R.id.buttonCadastrar);

        botaoCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String textoNome = campoNome.getText().toString();
                String textoEmail = campoEmail.getText().toString();
                String textoSenha = campoSenha.getText().toString();

                //validar se os campos foram preenchidos
                if(!textoNome.isEmpty()){

                    if(!textoEmail.isEmpty()){

                        if(!textoSenha.isEmpty()){

                            usuario = new Usuario();
                            usuario.setNome(textoNome);
                            usuario.setEmail(textoEmail);
                            usuario.setSenha(textoSenha);

                            cadastrarUsuario();

                        }else{
                            Toast.makeText(Activity_cadastro.this, "Preencha a senha!", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(Activity_cadastro.this, "Preencha o email!", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(Activity_cadastro.this, "Preencha o nome!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void cadastrarUsuario(){

        autenticacaoFireBase = ConfiguracaoFirebase.getAutenticacaoFirebase();
        autenticacaoFireBase.createUserWithEmailAndPassword(usuario.getEmail(), usuario.getSenha()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    String idUsuario = Base64Custom.codificarBase64(usuario.getEmail());
                    usuario.setIdUsuario(idUsuario);
                    usuario.salvaNoFirebase();

                    abrirTelaPrincipal();
                    Toast.makeText(Activity_cadastro.this, "Sucesso ao cadastrar usuário", Toast.LENGTH_SHORT).show();
                }else{
                    String excecao = "";
                    try{
                        throw task.getException();
                    }catch (FirebaseAuthWeakPasswordException e){
                        excecao = "Digite uma senha mais forte!";
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        excecao = "Por favor, digite um email válido";
                    }catch (FirebaseAuthUserCollisionException e){
                        excecao = "Usuário já cadastrado";
                    }catch (Exception e){
                        excecao = "Erro ao cadastrar usuário" + e.getMessage();
                        e.printStackTrace();
                    }
                    Toast.makeText(Activity_cadastro.this, excecao, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void abrirTelaPrincipal(){
        Intent intent = new Intent(getApplicationContext(), Activity_principal.class);
        startActivity(intent);
    }
}
