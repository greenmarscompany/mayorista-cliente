package com.greenmarscompany.mayoristacliente.persistence.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.greenmarscompany.mayoristacliente.persistence.entity.ECart;

@Dao
public interface CartDao {

    @Query("SELECT * FROM cart")
    java.util.List<ECart> getCarts();

    @Query("SELECT * FROM cart WHERE uid = :uid  ")
    ECart getCart(String uid);

    @Insert
    void addCart(ECart... cart);

    @Delete
    void deleteCart(ECart... cart);

    @Update
    void updateCart(ECart... cart);

    @Query("DELETE FROM cart")
    void deleteAllCart();

    @Query("SELECT count(uid) FROM cart")
    int getCountCart();
}