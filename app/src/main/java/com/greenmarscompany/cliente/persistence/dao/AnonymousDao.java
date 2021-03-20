package com.greenmarscompany.cliente.persistence.dao;

import androidx.room.Dao;
import androidx.room.Query;

import com.greenmarscompany.cliente.persistence.entity.Anonymous;

import java.util.List;

@Dao
public interface AnonymousDao {

    @Query("SELECT * FROM anonymous")
    List<Anonymous> getUsers();

    @Query("DELETE FROM  anonymous")
    void deleteAllUsers();

}
