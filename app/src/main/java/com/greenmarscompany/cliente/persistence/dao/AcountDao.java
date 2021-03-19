package com.greenmarscompany.cliente.persistence.dao;

import androidx.room.*;

import com.greenmarscompany.cliente.persistence.entity.Acount;

@Dao
public interface AcountDao {

    @Query("SELECT * FROM acount")
    java.util.List<Acount> getUsers();

    @Query("SELECT * FROM acount WHERE id = :id")
    Acount getUser(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addUser(Acount... user);

    @Delete
    void deleteUser(Acount user);

    @Update
    void updateUser(Acount... user);

    @Query("SELECT * FROM acount WHERE email=:username AND password=:password")
    Acount login(String username, String password);

    @Query("DELETE FROM acount WHERE id= :id")
    void deleteById(int id);

}