package com.igorsantos.organizze.activity.config;

import com.google.firebase.auth.FirebaseAuth;

public class ConfigutacaoFirebase {

    private static FirebaseAuth autenticacaoFirebase;

    //retorna a instancia do FirebaseAuth
    public static FirebaseAuth getAutenticacaoFirebase(){
        if(autenticacaoFirebase == null) {
            autenticacaoFirebase = FirebaseAuth.getInstance();
        }
        return autenticacaoFirebase;
    }
}
