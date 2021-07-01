package com.greenmarscompany.cliente.persistence;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.greenmarscompany.cliente.persistence.dao.CartDao;
import com.greenmarscompany.cliente.persistence.entity.Acount;
import com.greenmarscompany.cliente.persistence.entity.ECart;
import com.greenmarscompany.cliente.persistence.dao.AcountDao;

@Database(entities = {
        Acount.class,
        ECart.class
}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract CartDao getCartDao();

    public abstract AcountDao getAcountDao();

}