package com.bastiangperez.bgp_conversor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

//Creamos adaptador personalizado que recibirá el array de banderas, los datos almacenados en el array y nos inflará la vista de la listview basándose
// en el layout customizado que hemos hecho (en la carpeta layout).
public class CustomlistViewAdapter extends BaseAdapter {
    Context context;
    int flags[];
    String[] dataNames;
    LayoutInflater inflter;

    public CustomlistViewAdapter(Context applicationContext, int[] flags, String[] dataNames) {
        this.context = applicationContext;
        this.flags = flags;
        this.dataNames = dataNames;
        inflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return flags.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.custom_listview_items, null);
        ImageView icon = (ImageView) view.findViewById(R.id.imageView1);
        TextView names = (TextView) view.findViewById(R.id.textView1);
        icon.setImageResource(flags[i]);
        names.setText(dataNames[i]);
        return view;
    }
}
