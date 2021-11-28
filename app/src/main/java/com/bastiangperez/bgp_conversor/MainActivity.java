package com.bastiangperez.bgp_conversor;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.URL;
import java.text.DecimalFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class MainActivity extends AppCompatActivity {

    //Creamos las vistas
    Spinner bgp_originSp;
    Spinner bgp_finalSp;
    Button bgp_convertBtn;
    EditText bgp_euroBox;
    TextView bgp_exchangeBox;
    Button bgp_listExchanges;

    //Creamos las variables que almacenarán los datos a nivel global ya que usaremos esto en la otra Activity.
    public static String[] bgp_currencyArray;
    public static String[] bgp_ratioArray;
    public static String[] bgp_currencyRatioArray;
    //Craeamos las banderas que se corresponderán con los índices de los Spinners.
    public static int[] bgp_flags = {R.drawable.eu, R.drawable.us, R.drawable.jp, R.drawable.bg, R.drawable.cz, R.drawable.dk, R.drawable.gb, R.drawable.hu,
            R.drawable.pl, R.drawable.ro, R.drawable.se, R.drawable.ch, R.drawable.ice, R.drawable.no, R.drawable.cro, R.drawable.ru, R.drawable.tr,
            R.drawable.au, R.drawable.br, R.drawable.ca, R.drawable.cn, R.drawable.hk, R.drawable.id, R.drawable.il, R.drawable.in, R.drawable.kr,
            R.drawable.mx, R.drawable.my, R.drawable.nz, R.drawable.ph, R.drawable.sg, R.drawable.th, R.drawable.za};

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Creamos el binding de las vistas
        bgp_originSp = findViewById(R.id.spEuros);
        bgp_finalSp = findViewById(R.id.spDivisas);
        bgp_convertBtn = findViewById(R.id.btnConvertir);
        bgp_euroBox = findViewById(R.id.etCajaInicial);
        bgp_exchangeBox = findViewById(R.id.etCajaResultado);
        bgp_listExchanges = findViewById(R.id.btnListado);

        //Creamos el Thread que recibirá los datos de Internet y creará los arrays de conversión.
        obtainDatafromXML();

        //Llamamos a los componentes que arrancarán la interfaz.
        setupSpinners();

        //Activamos el intent para que al pulsar sel boton se conecten las dos activities.
        listExchanges();

        //Creamos la lógica para hacer la conversión al pulsar el botón
        testConversion();

    }


    private void obtainDatafromXML() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //Creamos la BBDD y hacemos referencia a la clase que hereda de SQLite.
                    myDataBase database = new myDataBase(getApplicationContext(), "Currencies", null, 1);
                    SQLiteDatabase bgp_myDataBase = database.getReadableDatabase();

                    //Comprobamos que hay Internet a través de la funcion isOnline(). En caso de ser correcto descargamos y leemos los datos de internet y añadimos los datos
                    //actualizados en la BBDD y en los arrays directamente desde el XML..
                    if (isOnline(getApplicationContext())) {

                        //Usamos el DOM para consultar el XML devuelto por la URL, lo guardamos en un documento y extraemos la lista almacenada en un NodeList
                        // que recogerá todos los tag "Cube".
                        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                        DocumentBuilder db = dbf.newDocumentBuilder();
                        Document doc = db.parse(new URL("https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml").openStream());
                        NodeList bgp_nameListCurrencies = (NodeList) doc.getElementsByTagName("Cube");


                        //Creamos una variable que recogerá cada uno de los registros que tienen el tag "Cube" asignado y los sumará para obtener la cantidad
                        //total de registros para crear los arrays de datos Para ello usaremos un bucle que irá sumando los registros al total.
                        int bgp_positionOfCurrency = 0;
                        for (int i = 0; i < bgp_nameListCurrencies.getLength(); i++) {
                            Node p = bgp_nameListCurrencies.item(i);
                            Element person = (Element) p;
                            //Recibimos tanto el valor de las etiquetas currency como el valor de los rates asignados a la etiqueta rate y los
                            //sumamos al contador por cada vuelta del bucle. Siempre filtrando si hubiese algún nulo, para que no lo incluya.
                            String varCurrency = (String) person.getAttribute("currency");
                            String varRate = (String) person.getAttribute("rate");
                            if (varCurrency != "") {
                                bgp_positionOfCurrency = bgp_positionOfCurrency + 1;
                            }
                        }
                        //Registramos una traza en la consola para comprobar que el proceso se realiza con éxito.
                        System.out.println("TRAZA: En total hay " + bgp_positionOfCurrency + " registros en la lista.");


                        //Creamos los distintos arrays que vamos a usar para ir cargando la información.
                        // Como en este caso en el XML no se muestra el cambio del EURO a si mismo, lo añadimos en la primera posición del array
                        //con sus distintos valores, creando los arrays del tamaño del numero total de registros obtendos + 1 que vamos a añadir.
                        bgp_ratioArray = new String[bgp_positionOfCurrency + 1];
                        bgp_currencyArray = new String[bgp_positionOfCurrency + 1];
                        bgp_currencyRatioArray = new String[bgp_positionOfCurrency + 1];
                        //Introducimos los datos del EUR en la primera posición de los tres array.
                        bgp_ratioArray[0] = "1";
                        bgp_currencyArray[0] = "EUR";
                        bgp_currencyRatioArray[0] = "Divisa: " + bgp_currencyArray[0] + " || Tasa de cambio: " + bgp_ratioArray[0] + " €";


                        // Volvemos a recorrer la lista de nombres fijando la variable de los registros a 0
                        // y hacemos que por cada vuelta del bucle añada en cada uno de los arrays en la posición positionOfCurrency+1
                        // (que será al inicio 1 ya que hemos llenado automáticamente la primera posición a mano) los valores correspondientes a las divisas.
                        bgp_positionOfCurrency = 0;
                        for (int i = 0; i < bgp_nameListCurrencies.getLength(); i++) {
                            Node p = bgp_nameListCurrencies.item(i);
                            Element person = (Element) p;
                            String bgp_currency = (String) person.getAttribute("currency");
                            String bgp_rate = (String) person.getAttribute("rate");
                            if (bgp_currency != "") {

                                bgp_ratioArray[bgp_positionOfCurrency + 1] = bgp_rate;
                                bgp_currencyArray[bgp_positionOfCurrency + 1] = bgp_currency;
                                bgp_currencyRatioArray[bgp_positionOfCurrency + 1] = "Divisa: " + bgp_currency + " || Tasa de cambio: " + bgp_rate + " €";
                                bgp_positionOfCurrency = bgp_positionOfCurrency + 1; //Sumamos uno a la posición hasta llegar al final.

                                //Al mismo tiempo añadimos los datos a la BBDD para cuando no haya conexión.
                                ContentValues bgp_mapRegisters = new ContentValues();
                                bgp_mapRegisters.put("id",bgp_positionOfCurrency);
                                bgp_mapRegisters.put("currency",bgp_currencyArray[bgp_positionOfCurrency]);
                                bgp_mapRegisters.put("ratio", bgp_ratioArray[bgp_positionOfCurrency]);
                                bgp_myDataBase.insert("currencies",null,bgp_mapRegisters);
                            }
                        }
                    } else {
                        bgp_myDataBase = database.getReadableDatabase();
                        //Volvemos a obtener el número de registros presentes, esta vez desde la propia BBDD.
                        String count = "select count(*) from currencies";
                        int bgp_bdSize = (int) DatabaseUtils.longForQuery(bgp_myDataBase, count, null);

                        //Creamos los arrays de nuevo al igual que antes con el tamaño del numero de registros de la BBDD
                        // sumando uno que será el EUR que añadiremos en la primer posición de cada array.
                        bgp_currencyRatioArray = new String[bgp_bdSize+1];
                        bgp_currencyRatioArray[0] = "Divisa: EUR || Tasa de cambio: 1 €";
                        bgp_currencyArray = new String[bgp_bdSize+1];
                        bgp_currencyArray[0] = "EUR";
                        bgp_ratioArray = new String[bgp_bdSize+1];
                        bgp_ratioArray[0] = "1";
                        //Lanzamos la consulta para recoger los datos de la BBDD.
                        String select ="select * from currencies";
                        Cursor c = bgp_myDataBase.rawQuery(select,null);
                        //Creamos el contador que empezará desde la posición 1 para evitar que se modifique la divisa EUR introducida.
                        int bgp_contadorDb = 1;
                        while (c.moveToNext()) {
                            String bgp_currency = c.getString(c.getColumnIndexOrThrow("currency"));
                            String bgp_ratios = c.getString(c.getColumnIndexOrThrow("ratio"));
                            //Marcamos por consola la traza de que recibe correctamente los parámetros.
                            System.out.println("Moneda: " + bgp_currency );
                            System.out.println("Ratio: " + bgp_ratios );
                            // Añadimos los datos correspondientes de la consulta de la BBDD a los arrays.
                            bgp_currencyArray[bgp_contadorDb] = bgp_currency;
                            bgp_ratioArray[bgp_contadorDb] = bgp_ratios;
                            bgp_currencyRatioArray[bgp_contadorDb] = "Divisa: " + bgp_currency + " || Tasa de cambio: " + bgp_ratios + " €";
                            bgp_contadorDb=bgp_contadorDb+1;
                        }
                        bgp_myDataBase.close();

                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    public boolean isOnline(Context context) {
        //Con esta función comprobamos que hay conexion a internet de cualquier tipo.
        boolean connected = false;

        ConnectivityManager connec = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Recupera todas las redes (tanto móviles como wifi)
        NetworkInfo[] bgp_redes = connec.getAllNetworkInfo();

        for (int i = 0; i < bgp_redes.length; i++) {
            // Si alguna red tiene conexión, se devuelve true, de lo contrario se devolverá false.
            if (bgp_redes[i].getState() == NetworkInfo.State.CONNECTED) {
                connected = true;
            }

        }
        return connected;
    }

    public void setupSpinners() {

        //Creamos el adapter para los Spinner y los configuramos pasando los datos del array creado en el archivo strings.xml.
        //Las currencies vendrán del array de String con las banderas, y las tasas de cambio als recogerá de Internet.
        String[] bgp_data = getResources().getStringArray(R.array.divisas);

        //creamos el adaptador que nos mostrará los Spinners con las banderas.
        CustomAdapter adapter = new CustomAdapter(this, bgp_flags, bgp_data);


        //Iniciamos los Spinners con el listado completo.
        bgp_originSp.setAdapter(adapter);

        //Establecemos que la selección por defecto del Spinner de origen sea EUR
        bgp_originSp.setSelection(0);


        //Hacemos que en el segundo SPinner también se muestren las divisas con las banderas a través del mismo adaptador. La primera opcion
        bgp_originSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                bgp_exchangeBox.setText("");
                bgp_finalSp.setAdapter(adapter);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //No se va a producir esta situación ya que siempre habrá una opción seleccionada.
            }
        });

        bgp_finalSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //No se va a producir esta situación ya que siempre habrá una opción seleccionada.
            }
        });


    }

    private void testConversion() {
        //Hacemos que el segundo edittext no pueda ser focusable
        bgp_exchangeBox.setFocusable(false);

        //Comprobamos antes de nada que la cantidad de la caja a convertir no esté en blanco. De lo contrario muestra un Toast pidiendo rellenar la casilla
        bgp_convertBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bgp_euroBox.getText().toString().isEmpty()) {
                    AlertDialog.Builder AlertMessage = new AlertDialog.Builder(MainActivity.this);
                    AlertMessage.setTitle("ERROR");
                    AlertMessage.setMessage("Debes introducir una cantidad a convertir en la casilla.");
                    AlertMessage.setPositiveButton("Reintentar", null);
                    AlertMessage.create();
                    AlertMessage.show();
                } else {
                    startConversion(); //Si no está llena arrancamos la conversión.
                }
            }
        });
    }

    private void startConversion() {

        //Comprobamos que las divisas de origen y destino no coincidan (No lo hará, salvo en el caso inicial porque en cuanto selecciones una moneda se hará la transformacion en el segundo spinner).
        // Si es así les saldrá un mensaje de error.
        //Para ello empleamos un AlertDialog que construiremos con las siguientes caraterísticas.
        if (bgp_originSp.getSelectedItemId() == (bgp_finalSp.getSelectedItemId())) {
            AlertDialog.Builder AlertMessage = new AlertDialog.Builder(MainActivity.this);
            AlertMessage.setTitle("ERROR");
            AlertMessage.setMessage("No puedes elegir dos divisas iguales.");
            AlertMessage.setPositiveButton("Reintentar", null);
            AlertMessage.create();
            AlertMessage.show();
        } else {
            //Creamos las dos variables que recibirán la posición del spinner seleccionada y extraemos el valor introducido en la caja de origen.
            //A continuación añadimos los ratios al array y generamos el algoritmo de conversión.
            int bgp_originCode = bgp_originSp.getSelectedItemPosition();
            int bgp_finalCode = bgp_finalSp.getSelectedItemPosition();
            double bgp_inputValue = Double.parseDouble(bgp_euroBox.getText().toString());

            //Los ratios están extraídos del XML.
            //double[] bgp_ratios = {1, 0.86449, 1.176700 , 0.007637, 0.041466, 0.156304, 0.00072091427, 0.217969, 0.230750, 0.000231};
            double bgp_ratiosValor1 = Double.parseDouble(bgp_ratioArray[bgp_originCode]);
            double bgp_ratiosValor2 = Double.parseDouble(bgp_ratioArray[bgp_finalCode]);
            double bgp_results = bgp_inputValue * (bgp_ratiosValor2 / bgp_ratiosValor1);

            //Configuramos el resultado para limitar el número de decimales:
            DecimalFormat df = new DecimalFormat("###,###.###");
            String bgp_roundedResult = df.format(bgp_results);
            bgp_exchangeBox.setText(bgp_roundedResult);
            Toast.makeText(this, "La conversión se ha realizado con éxito", Toast.LENGTH_LONG).show();
        }

    }

    private void listExchanges() {
        bgp_listExchanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent listActivity = new Intent(getApplicationContext(), ListActivity.class);
                startActivity(listActivity);
            }
        });
    }

}
