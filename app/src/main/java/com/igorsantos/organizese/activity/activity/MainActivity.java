package com.igorsantos.organizese.activity.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.FragmentSlide;
import com.igorsantos.organizese.R;
import com.igorsantos.organizese.activity.config.ConfiguracaoFirebase;

public class MainActivity extends IntroActivity {

    private FirebaseAuth autenticacaoFirebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        setButtonBackVisible(false);
        setButtonNextVisible(false);

        addSlide(new FragmentSlide.Builder()
                .background(android.R.color.white)
                .fragment(R.layout.intro_1)
                .build());

        addSlide(new FragmentSlide.Builder()
                .background(android.R.color.white)
                .fragment(R.layout.intro_2)
                .build());

        addSlide(new FragmentSlide.Builder()
                .background(android.R.color.white)
                .fragment(R.layout.intro_3)
                .build());

        addSlide(new FragmentSlide.Builder()
                .background(android.R.color.white)
                .fragment(R.layout.intro_4)
                .build());

        addSlide(new FragmentSlide.Builder()
                .background(android.R.color.white)
                .fragment(R.layout.intro_cadastro)
                .canGoForward(false)
                .build());
    }

    @Override
    protected void onStart() {
        super.onStart();
        verificarUsuarioLogado();
    }

    public void criarCadastro(View view){
        Intent intent = new Intent(getApplicationContext(), Activity_cadastro.class);
        startActivity(intent);
    }
    public void logar(View view){
        Intent intent = new Intent(getApplicationContext(), Activity_login.class);
        startActivity(intent);
    }
    public void verificarUsuarioLogado(){
        autenticacaoFirebase = ConfiguracaoFirebase.getAutenticacaoFirebase();
        //autenticacaoFirebase.signOut();
        if(autenticacaoFirebase.getCurrentUser() != null){
            abrirTelaPrincipal();
        }
    }

    public void abrirTelaPrincipal(){
        Intent intent = new Intent(getApplicationContext(), Activity_principal.class);
        startActivity(intent);
    }
}
