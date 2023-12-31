package com.beckachu.androidfeed.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.beckachu.androidfeed.data.entities.NotiEntity;

import java.util.List;

/**
 * This interface handles all operations with data in the database
 * <p>
 * Common naming conventions for DAO methods:
 * insert, get, update, and delete
 */
@Dao
public interface NotiDao {

    @Query("SELECT * FROM notientity ORDER BY nid ASC")
    List<NotiEntity> getAllByIdAsc();

    @Query("SELECT * FROM notientity WHERE nid < :id AND packageName LIKE :packageName ORDER BY nid DESC LIMIT :pageSize")
    List<NotiEntity> getAllOlderThanId(long id, int pageSize, String packageName);

    @Query("SELECT * FROM notientity WHERE packageName LIKE :packageName ORDER BY nid DESC LIMIT :pageSize")
    List<NotiEntity> getNewest(int pageSize, String packageName);

    @Query("SELECT * FROM notientity WHERE nid = :id")
    NotiEntity getById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(NotiEntity notiEntity);

    @Query("DELETE FROM notientity WHERE nid = :id")
    int delete(int id);

}
