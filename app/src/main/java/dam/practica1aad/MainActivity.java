package dam.practica1aad;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import dam.practica1aad.datos.Adaptador;
import dam.practica1aad.datos.Contacto;
import dam.practica1aad.datos.ListaTelefonos;
import dam.practica1aad.datos.OrdenarLista;

public class MainActivity extends AppCompatActivity {

    private Adaptador adap;
    private ArrayList<Contacto> a;
    private EditText ed1, ed2, ed3;
    private SharedPreferences shared;
    private SharedPreferences fecha2;
    private String fecha;
    private TextView tvFecha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void init() throws IOException, XmlPullParserException {

        final ListView lv = (ListView) findViewById(R.id.lvLista);
        final ImageButton bt = (ImageButton) findViewById(R.id.imageButton);
        shared = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        fecha2 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String b = shared.getString("primera", "no");
        ListaTelefonos x = new ListaTelefonos(this);

        tvFecha = (TextView) findViewById(R.id.fecha);


        if(b.equals("no")) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            fecha = dateFormat.format(new Date());
            tvFecha.setText(fecha);

            SharedPreferences.Editor ed = fecha2.edit();
            ed.putString("fecha", fecha);
            ed.commit();

            shared.edit();
            ed.putString("primera", "si");
            ed.commit();

            a = x.getGestion();
            for (Contacto aux : a) {
                aux.setTelefonos((ArrayList<String>) x.getListaTelefonos(this, aux.getId()));
            }
            Collections.sort(a);
            escribir(a);

        }else{
            String r = fecha2.getString("fecha", fecha);
            tvFecha.setText(r);
            a = new ArrayList<>();
            a = leer();
        }

        adap = new Adaptador(this, R.layout.item, a);

        registerForContextMenu(lv);
        lv.setAdapter(adap);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adap.detalles(position);
            }
        });

        bt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                añadir();
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_contextual, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo vistaInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int posicion = vistaInfo.position;

        switch (item.getItemId()) {
            case R.id.menu_editar:
                editar(posicion);
                return true;
            case R.id.menu_borrar:
                try {
                    borrar(posicion);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void borrar(final int posicion) throws IOException {
        a.remove(posicion);
        escribir(a);
        adap.notifyDataSetChanged();
    }

    public void editar(final int posicion){

        AlertDialog.Builder alert= new AlertDialog.Builder(this);
        alert.setTitle("Editar");
        LayoutInflater inflater= LayoutInflater.from(this);

        final View vista = inflater.inflate(R.layout.editar, null);

        final EditText et1, et2, et3;
        et1 = (EditText) vista.findViewById(R.id.ededit);
        et2 = (EditText) vista.findViewById(R.id.ededit2);
        et3 = (EditText) vista.findViewById(R.id.ededit3);

        et1.setHint(a.get(posicion).getNombre());
        if(a.get(posicion).getTelefonos().size() == 2) {
            et2.setHint(a.get(posicion).getTelefono(0));
            et3.setHint(a.get(posicion).getTelefono(1));
        }else if (a.get(posicion).getTelefonos().size() == 1){
            et2.setHint(a.get(posicion).getTelefono(0));
        }

        alert.setPositiveButton(R.string.editar,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        long id = a.size() - 1;

                        ArrayList<String> listaT;

                        String nombre = et1.getText().toString();
                        listaT = new ArrayList<>();
                        listaT.add(et2.getText().toString());
                        listaT.add(et3.getText().toString());

                        a.get(posicion).setNombre(nombre);
                        a.get(posicion).setTelefonos(listaT);

                        Collections.sort(a, new OrdenarLista());

                        try {
                            escribir(a);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        adap = new Adaptador(MainActivity.this, R.layout.item, a);
                        ListView lv = (ListView) findViewById(R.id.lvLista);
                        lv.setAdapter(adap);
                    }
                });
        alert.setView(vista);
        alert.setNegativeButton(R.string.dial_atras, null);
        alert.show();
    }

    public void añadir(){

        AlertDialog.Builder alert= new AlertDialog.Builder(this);
        alert.setTitle("Añadir");
        LayoutInflater inflater= LayoutInflater.from(this);

        final View vista = inflater.inflate(R.layout.anadir, null);

        alert.setView(vista);

        alert.setPositiveButton(R.string.aceptar,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        ArrayList<String> listaT;
                        long id = a.size() - 1;

                        ed1 = (EditText) vista.findViewById(R.id.nombreAn);
                        ed2 = (EditText) vista.findViewById(R.id.telefonoAn);
                        ed3 = (EditText) vista.findViewById(R.id.telefono2An);

                        String nombre = ed1.getText().toString();
                        listaT = new ArrayList<>();
                        listaT.add(ed2.getText().toString());
                        listaT.add(ed3.getText().toString());

                        Contacto nuevo = new Contacto(id, nombre, listaT);

                        a.add(nuevo);
                        Collections.sort(a, new OrdenarLista());

                        try {
                            escribir(a);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        adap = new Adaptador(MainActivity.this, R.layout.item, a);
                        ListView lv = (ListView) findViewById(R.id.lvLista);
                        lv.setAdapter(adap);
                        adap.notifyDataSetChanged();
                    }
                });

        alert.setNegativeButton(R.string.dial_atras, null);
        alert.show();
    }

    public void sincronizar(View v) throws IOException, XmlPullParserException {

        tvFecha = (TextView) findViewById(R.id.fecha);
        fecha2 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        fecha = dateFormat.format(new Date());
        tvFecha.setText(fecha);

        SharedPreferences.Editor ed = fecha2.edit();
        ed.putString("fecha", fecha);
        ed.commit();

        ArrayList<Contacto> p = new ArrayList<>();
        ListaTelefonos x = new ListaTelefonos(this);
        p = x.getGestion();

        for(Contacto aux:p){
            aux.setTelefonos((ArrayList<String>) x.getListaTelefonos(this,aux.getId()));
        }

        for (Contacto c : p) {
            if (!repetido(c)){
                a.add(c);
            }
        }
        escribir(a);

        Collections.sort(p);
        Collections.sort(a);
        adap = new Adaptador(MainActivity.this, R.layout.item, a);
        ListView lv = (ListView) findViewById(R.id.lvLista);
        lv.setAdapter(adap);
        adap.notifyDataSetChanged();
    }

    public void escribir(List<Contacto> x) throws IOException {

        Random r = new Random();
        FileOutputStream fosxml = new FileOutputStream(new File(getExternalFilesDir(null),"contactos.xml"));

        XmlSerializer docxml = Xml.newSerializer();
        docxml.setOutput(fosxml, "UTF-8");
        docxml.startDocument(null, Boolean.valueOf(true));
        docxml.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

        ArrayList<String> l= new ArrayList<>();

        docxml.startTag(null, "contactos");
        for(int i = 0; i<x.size();i++){
            docxml.startTag(null, "contacto");
            docxml.startTag(null, "nombre");
            docxml.attribute(null, "id", String.valueOf(x.get(i).getId()));
            docxml.text(x.get(i).getNombre().toString());
            docxml.endTag(null, "nombre");

            for(int j=0; j<x.get(i).getTelefonos().size(); j++) {
                docxml.startTag(null, "telefono");
                docxml.text(x.get(i).getTelefono(j).toString());
                docxml.endTag(null, "telefono");
            }
            docxml.endTag(null, "contacto");
        }
        docxml.endDocument();
        docxml.flush();
        fosxml.close();
    }

    public ArrayList<Contacto> leer() throws IOException, XmlPullParserException {

        ArrayList<Contacto> copia = new ArrayList<Contacto>();
        Contacto c = null;
        int id = 0;
        ArrayList <String> telf= new ArrayList<>();
        String nom="";

        XmlPullParser lectorxml = Xml.newPullParser();
        lectorxml.setInput(new FileInputStream(new File(getExternalFilesDir(null), "contactos.xml")), "utf-8");
        int evento = lectorxml.getEventType();
        int atrib=0;

        while (evento != XmlPullParser.END_DOCUMENT){
            if(evento == XmlPullParser.START_TAG){
                String etiqueta = lectorxml.getName();
                Log.v("etiqueta", etiqueta);
                if(etiqueta.compareTo("contacto")==0){
                    telf=new ArrayList<>();
                    c = null;
                    atrib=0;
                    nom="";
                }

                if(etiqueta.compareTo("nombre")==0){
                    Log.v("etiqueta","entra");
                    atrib = Integer.parseInt(lectorxml.getAttributeValue(null, "id"));
                    Log.v("etiqueta", String.valueOf(atrib));
                    nom=lectorxml.nextText();
                    Log.v("etiqueta",nom);

                } else if(etiqueta.compareTo("telefono")==0){
                    String texto = lectorxml.nextText();
                    telf.add(texto);
                }
            }
            if(evento==XmlPullParser.END_TAG){
                String etiqueta = lectorxml.getName();
                if(etiqueta.compareTo("contacto")==0){
                    c = new Contacto(atrib,nom,telf);
                    Log.v("Contacto",c.getNombre()+c.getId());
                    copia.add(c);
                }
            }
            evento = lectorxml.next();
        }
        Log.v("Contacto", copia.toString());
        return copia;
    }

    public boolean repetido(Contacto c) throws IOException, XmlPullParserException {

        XmlPullParser lectorxml = Xml.newPullParser();
        lectorxml.setInput(new FileInputStream(new File(getExternalFilesDir(null), "contactos.xml")), "utf-8");
        int evento = lectorxml.getEventType();
        long atrib = 0;

        while (evento != XmlPullParser.END_DOCUMENT){
            if(evento == XmlPullParser.START_TAG){
                String etiqueta = lectorxml.getName();

                if(etiqueta.compareTo("nombre")==0){
                    atrib = Long.parseLong(lectorxml.getAttributeValue(null, "id"));
                    if (atrib == c.getId()){
                        return true;
                    }
                }
            }
            evento = lectorxml.next();
        }
        return false;
    }
}
