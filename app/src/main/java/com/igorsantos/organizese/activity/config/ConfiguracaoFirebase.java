package com.igorsantos.organizese.activity.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ConfiguracaoFirebase {

    private static FirebaseAuth autenticacaoFirebase;
    private static DatabaseReference referenciaDatabase;

    //retorna a instancia do FirebaseDatabase
    public static DatabaseReference getReferenciaDatabaseFirebase(){
        if(referenciaDatabase == null){
            referenciaDatabase = FirebaseDatabase.getInstance().getReference();
        }
        return referenciaDatabase;
    }

    //retorna a instancia do FirebaseAuth
    public static FirebaseAuth getAutenticacaoFirebase(){
        if(autenticacaoFirebase == null) {
            autenticacaoFirebase = FirebaseAuth.getInstance();
        }
        return autenticacaoFirebase;
    }
}
