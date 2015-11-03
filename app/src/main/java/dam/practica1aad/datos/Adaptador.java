package dam.practica1aad.datos;


import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import dam.practica1aad.R;

public class Adaptador extends ArrayAdapter<Contacto> {

    private Context contexto;
    private int recurso;
    private ArrayList<Contacto> objeto;
    private LayoutInflater inflador;

    static class ViewHolder {
        public TextView tv1, tv2;
        public ImageView iv;
    }

    public Adaptador(Context context, int recurso, ArrayList<Contacto> objeto){
        super(context, recurso, objeto);
        this.contexto = context;
        this.recurso = recurso;
        this.objeto = objeto;
        this.inflador = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //1
        ViewHolder gv = new ViewHolder();
        if(convertView==null){
            convertView = inflador.inflate(recurso, null);
            TextView tv = (TextView) convertView.findViewById(R.id.nombre);
            gv.tv1 = tv;

            ImageView iv = (ImageView) convertView.findViewById(R.id.foto);
            gv.iv = iv;
            convertView.setTag(gv);
        } else {
            gv = (ViewHolder) convertView.getTag();
        }
        gv.iv.setTag(position);
        gv.tv1.setText(objeto.get(position).getNombre());

        return convertView;
    }

    public boolean borrar(int position) {
        try {
            objeto.remove(position);
            this.notifyDataSetChanged();
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
        return true;
    }

    public void detalles(int posicion) {

        AlertDialog.Builder alert = new AlertDialog.Builder(contexto);
        alert.setTitle("Detalles");
        LayoutInflater inflater = LayoutInflater.from(contexto);

        final View vista = inflater.inflate(R.layout.detalles, null);
        TextView tv1 = (TextView) vista.findViewById(R.id.tvNombre);
        TextView tv2 = (TextView) vista.findViewById(R.id.tvTelefono);
        TextView tv3 = (TextView) vista.findViewById(R.id.tvTelefono2);

        if (objeto.get(posicion).getTelefonos().size() > 1) {
            tv1.setText(objeto.get(posicion).getNombre());
            tv2.setText(objeto.get(posicion).getTelefono(0));
            tv3.setText(objeto.get(posicion).getTelefono(1));
        } else if (objeto.get(posicion).getTelefonos().size() <= 1) {
            tv1.setText(objeto.get(posicion).getNombre());
            tv2.setText(objeto.get(posicion).getTelefono(0));
            tv3.setText("");
        }

        alert.setView(vista);
        alert.setNegativeButton(R.string.dial_atras, null);
        alert.show();
    }
}
