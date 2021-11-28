package com.bastiangperez.bgp_conversor;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

//Creamos la clase de mi Base ded datos extendiendo de SQLiteHelper
public class myDataBase extends SQLiteOpenHelper {

    //El archivo por defecto se guardará en  /data/data/com.bastiangperez.bgp_conversor/databases/currencies.db


    public myDataBase(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table currencies(id int primary key, currency text, ratio text)");
    }


    //Con esta instrucción lo que hacemos es que cada vez que se acceda a la base de datos se borren los datos y se vuelva a llamar a la creacion
    //de la tabla nueva. De esa manera se actualizan constantemente las divisas.
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("drop table currencies");
        onCreate(sqLiteDatabase);
    }
}
