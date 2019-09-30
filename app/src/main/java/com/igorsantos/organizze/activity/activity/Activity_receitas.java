package com.igorsantos.organizze.activity.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.igorsantos.organizze.R;
import com.igorsantos.organizze.activity.config.ConfiguracaoFirebase;
import com.igorsantos.organizze.activity.helper.Base64Custom;
import com.igorsantos.organizze.activity.helper.DateUtil;
import com.igorsantos.organizze.activity.model.Movimentacao;
import com.igorsantos.organizze.activity.model.Usuario;

public class Activity_receitas extends AppCompatActivity {

    private TextInputEditText campoData, campoCategoria, campoDescricao;
    private EditText campoValor;

    private Movimentacao movimentacao;

    private DatabaseReference referenciaDatabase = ConfiguracaoFirebase.getReferenciaDatabaseFirebase();
    private FirebaseAuth autenticacaoFirebase = ConfiguracaoFirebase.getAutenticacaoFirebase();

    private Double receitaTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receitas);

        campoValor = findViewById(R.id.editValor);
        campoData = findViewById(R.id.editData);
        campoCategoria = findViewById(R.id.editCategoria);
        campoDescricao = findViewById(R.id.editDescricao);

        //preenche o campo data com a data atual
        campoData.setText(DateUtil.dataAtual());

        recuperarReceitaTotal();
    }

    public void salvarReceita(View view){

        if(validarCamposReceita()){
            String data = campoData.getText().toString();

            Double valorRecuperado = Double.parseDouble(campoValor.getText().toString());

            movimentacao = new Movimentacao();
            movimentacao.setValor(Double.parseDouble(campoValor.getText().toString()));
            movimentacao.setCategoria(campoCategoria.getText().toString());
            movimentacao.setDescricao(campoDescricao.getText().toString());
            movimentacao.setData(data);
            movimentacao.setTipo("r");

            Double receitaAtualizada = receitaTotal + valorRecuperado;
            atualizarReceita(receitaAtualizada);

            movimentacao.salvar(data);

            Toast.makeText(getApplicationContext(), "Receita salva com sucesso!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public Boolean validarCamposReceita(){
        String textoValor = campoValor.getText().toString();
        String textoData = campoData.getText().toString();
        String textoCategoria = campoCategoria.getText().toString();
        String textoDescricao = campoDescricao.getText().toString();

        if(!textoValor.isEmpty()){
            if(!textoData.isEmpty()){
                if(!textoCategoria.isEmpty()){
                    if(!textoDescricao.isEmpty()){
                        return true;

                    }else{
                        Toast.makeText(Activity_receitas.this, "Preencha a descrição!", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }else{
                    Toast.makeText(Activity_receitas.this, "Preencha a categoria!", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }else{
                Toast.makeText(Activity_receitas.this, "Preencha a data!", Toast.LENGTH_SHORT).show();
                return false;
            }
        }else{
            Toast.makeText(Activity_receitas.this, "Preencha o valor!", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public void recuperarReceitaTotal(){
        String emailUsuario = autenticacaoFirebase.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);
        DatabaseReference referenciaUsuario = referenciaDatabase.child("usuario").child(idUsuario);

        referenciaUsuario.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue(Usuario.class);

                receitaTotal = usuario.getReceitaTotal();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void atualizarReceita(Double receita){
        String emailUsuario = autenticacaoFirebase.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);
        DatabaseReference referenciaUsuario = referenciaDatabase.child("usuario").child(idUsuario);

        referenciaUsuario.child("receitaTotal").setValue(receita);
    }
}
