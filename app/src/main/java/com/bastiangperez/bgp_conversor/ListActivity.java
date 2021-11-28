package com.bastiangperez.bgp_conversor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class ListActivity extends AppCompatActivity {

    Button bgp_goBack;
    ListView bgp_ExchangesListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        //Creamos los binding de la vista
        bgp_goBack = findViewById(R.id.btnVolver);
        bgp_ExchangesListView = findViewById(R.id.lvExchanges);

        //Definimos el adaptador que nos mostrará el listView, pasándole tanto el array de banderas como el array con la información de la MainActivity.
        CustomlistViewAdapter adapter2 = new CustomlistViewAdapter(this, MainActivity.bgp_flags, MainActivity.bgp_currencyRatioArray);
        bgp_ExchangesListView.setAdapter(adapter2);

        //Llamamos a la función que controla el botón que nos permitirá volver a la pantalla inicial.
        setupGoBack();
    }

    private void setupGoBack() {
        bgp_goBack.setOnClickListener(view -> {
            Intent goBack = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(goBack);
        });
    }
}