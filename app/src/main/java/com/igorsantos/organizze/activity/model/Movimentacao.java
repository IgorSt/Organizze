package com.igorsantos.organizze.activity.model;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.igorsantos.organizze.activity.config.ConfiguracaoFirebase;
import com.igorsantos.organizze.activity.helper.Base64Custom;
import com.igorsantos.organizze.activity.helper.DateUtil;

public class Movimentacao {

    private String data;
    private String categoria;
    private String descricao;
    private String tipo;
    private Double valor;

    public Movimentacao() {
    }

    public void salvar(String data){
        FirebaseAuth autenticacaoFirebase = ConfiguracaoFirebase.getAutenticacaoFirebase();

        String idUsuario = Base64Custom.codificarBase64(autenticacaoFirebase.getCurrentUser().getEmail());

        String mesAno = DateUtil.mesAnoDataEscolhida(data);

        DatabaseReference referenciaDatabase = ConfiguracaoFirebase.getReferenciaDatabaseFirebase();
        referenciaDatabase.child("movimentacao")
                .child(idUsuario)
                .child(mesAno)
                .push()
                .setValue(this);
    }

    public String getData() {
        return data;
    }
    public void setData(String data) {
        this.data = data;
    }

    public String getCategoria() {
        return categoria;
    }
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getDescricao() {
        return descricao;
    }
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getTipo() {
        return tipo;
    }
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Double getValor() {
        return valor;
    }
    public void setValor(Double valor) {
        this.valor = valor;
    }
}
