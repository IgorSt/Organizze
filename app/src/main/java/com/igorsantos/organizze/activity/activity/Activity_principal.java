package com.igorsantos.organizze.activity.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.igorsantos.organizze.R;
import com.igorsantos.organizze.activity.adapter.AdapterMovimentacao;
import com.igorsantos.organizze.activity.config.ConfiguracaoFirebase;
import com.igorsantos.organizze.activity.helper.Base64Custom;
import com.igorsantos.organizze.activity.model.Movimentacao;
import com.igorsantos.organizze.activity.model.Usuario;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Activity_principal extends AppCompatActivity {

    private MaterialCalendarView calendarView;
    private TextView textViewSaudacao;
    private TextView textViewSaldo;

    private FirebaseAuth autenticacaoFirebase = ConfiguracaoFirebase.getAutenticacaoFirebase();
    private DatabaseReference referenciaDatabase = ConfiguracaoFirebase.getReferenciaDatabaseFirebase();
    private DatabaseReference referenciaUsuario;
    private DatabaseReference referenciaDatabaseMovimentacao;

    private ValueEventListener valueEventListenerUsuario;
    private ValueEventListener valueEventListenerMovimentacoes;

    private Double despesaTotal = 0.0;
    private Double receitaTotal = 0.0;
    private Double resumoUsuario = 0.0;

    private RecyclerView recyclerView;
    private AdapterMovimentacao adapterMovimentacao;

    private List<Movimentacao> listaMovimentacoes = new ArrayList<>();

    private String mesAnoSelecionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Organizze");
        setSupportActionBar(toolbar);

        textViewSaudacao = findViewById(R.id.textViewSaudacao);
        textViewSaldo = findViewById(R.id.textViewSaldo);

        calendarView = findViewById(R.id.calendarView);
        configuraCalendarView();

        recyclerView = findViewById(R.id.recyclerMovimentos);

        //CONFIGURA O ADAPTER
        adapterMovimentacao = new AdapterMovimentacao(listaMovimentacoes, this);

        //CONFIGURA O RECYCLERVIEW
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapterMovimentacao);

    }

    private void recuperarMovimentacoes(){
        String emailUsuario = autenticacaoFirebase.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);

        referenciaDatabaseMovimentacao = referenciaDatabase.child("movimentacao")
                                                            .child(idUsuario)
                                                            .child(mesAnoSelecionado);

        valueEventListenerMovimentacoes = referenciaDatabaseMovimentacao.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listaMovimentacoes.clear();

                for(DataSnapshot dados: dataSnapshot.getChildren()){
                    Movimentacao movimentacao = dados.getValue(Movimentacao.class);
                    listaMovimentacoes.add(movimentacao);
                }

                adapterMovimentacao.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void recuperarResumo(){
        String emailUsuario = autenticacaoFirebase.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);
        referenciaUsuario = referenciaDatabase.child("usuario").child(idUsuario);

        valueEventListenerUsuario = referenciaUsuario.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue(Usuario.class);

                despesaTotal = usuario.getDespesaTotal();
                receitaTotal = usuario.getReceitaTotal();
                resumoUsuario = receitaTotal - despesaTotal;

                DecimalFormat decimalFormat = new DecimalFormat("0.##");
                String resultadoFormatado = decimalFormat.format(resumoUsuario);

                textViewSaudacao.setText("Olá, " + usuario.getNome());
                textViewSaldo.setText("R$ " + resultadoFormatado);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);

        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.menuSair:
                autenticacaoFirebase.signOut();
                startActivity(new Intent(this, MainActivity.class));
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void adicionarReceita(View view){
        Intent intent = new Intent(getApplicationContext(), Activity_receitas.class);
        startActivity(intent);
    }
    public void adicionarDespesa(View view){
        Intent intent = new Intent(getApplicationContext(), Activity_despesas.class);
        startActivity(intent);
    }

    public void configuraCalendarView(){
        CharSequence meses[] = {"Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",  "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};
        calendarView.setTitleMonths(meses);

        CalendarDay dataAtual = calendarView.getCurrentDate();
        String mesSelecionado = String.format("%02d", dataAtual.getMonth());
        mesAnoSelecionado = mesSelecionado + "" + dataAtual.getYear();

        calendarView.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                mesAnoSelecionado = date.getMonth() + "" + date.getYear();

                referenciaDatabaseMovimentacao.removeEventListener(valueEventListenerMovimentacoes);
                recuperarMovimentacoes();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        recuperarResumo();
        recuperarMovimentacoes();
    }

    @Override
    protected void onStop() {
        super.onStop();

        referenciaDatabase.removeEventListener(valueEventListenerUsuario);

        referenciaDatabaseMovimentacao.removeEventListener(valueEventListenerMovimentacoes);
    }
}
