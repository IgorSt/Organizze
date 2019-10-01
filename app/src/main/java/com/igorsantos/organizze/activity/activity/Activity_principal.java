package com.igorsantos.organizze.activity.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.card.MaterialCardView;
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
    private Movimentacao movimentacao;

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

    public void swipe(){
        ItemTouchHelper.Callback itemTouch = new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int dragFlags = ItemTouchHelper.ACTION_STATE_IDLE;
                int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;

                return makeMovementFlags(dragFlags, swipeFlags);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                excluirMovimentacao(viewHolder);
            }
        };

        new ItemTouchHelper(itemTouch).attachToRecyclerView(recyclerView);
    }

    public void excluirMovimentacao(final RecyclerView.ViewHolder viewHolder){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        //CONFIGURA O ALERTDIALOG
        alertDialog.setTitle("Excluir Movimentação da Conta");
        alertDialog.setMessage("Tem certeza que deseja excluir a movimentação da sua conta?");
        alertDialog.setCancelable(false);

        alertDialog.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int position = viewHolder.getAdapterPosition();
                movimentacao = listaMovimentacoes.get(position);

                String emailUsuario = autenticacaoFirebase.getCurrentUser().getEmail();
                String idUsuario = Base64Custom.codificarBase64(emailUsuario);
                referenciaDatabaseMovimentacao = referenciaDatabase.child("movimentacao")
                                                                    .child(idUsuario)
                                                                    .child(mesAnoSelecionado);

                referenciaDatabaseMovimentacao.child(movimentacao.getId()).removeValue();
                adapterMovimentacao.notifyItemRemoved(position);

                Toast.makeText(getApplicationContext(), "Item apagado!", Toast.LENGTH_SHORT).show();

                atualizarSaldo();
            }
        });
        alertDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(Activity_principal.this, "Cancelado", Toast.LENGTH_SHORT).show();
                adapterMovimentacao.notifyDataSetChanged();
            }
        });

        AlertDialog alertDialog1 = alertDialog.create();
        alertDialog1.show();
    }

    public void atualizarSaldo(){
        String emailUsuario = autenticacaoFirebase.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);
        referenciaUsuario = referenciaDatabase.child("usuario").child(idUsuario);

        if(movimentacao.getTipo().equals("r")){
            receitaTotal = receitaTotal - movimentacao.getValor();

            referenciaUsuario.child("receitaTotal").setValue(receitaTotal);
        }if(movimentacao.getTipo().equals("d")){
            receitaTotal = receitaTotal - movimentacao.getValor();

            referenciaUsuario.child("despesaTotal").setValue(receitaTotal);
        }
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
                    movimentacao.setId(dados.getKey());
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
                String mesSelecionado = String.format("%02d", date.getMonth());
                mesAnoSelecionado = mesSelecionado + "" + date.getYear();

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

        swipe();
    }

    @Override
    protected void onStop() {
        super.onStop();

        referenciaDatabase.removeEventListener(valueEventListenerUsuario);

        referenciaDatabaseMovimentacao.removeEventListener(valueEventListenerMovimentacoes);
    }
}
