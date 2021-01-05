package com.greenmarscompany.mayoristacliente.persistence;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.greenmarscompany.mayoristacliente.persistence.dao.CartDao;
import com.greenmarscompany.mayoristacliente.persistence.entity.Acount;
import com.greenmarscompany.mayoristacliente.persistence.entity.ECart;
import com.greenmarscompany.mayoristacliente.persistence.dao.AcountDao;

@Database(entities = {
        Acount.class,
        ECart.class
}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract CartDao getCartDao();

    public abstract AcountDao getAcountDao();

}