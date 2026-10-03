package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AllowanceWeekEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AllowanceDao {

    @Query("SELECT * FROM allowance_weeks ORDER BY year DESC, weekNumber DESC")
    fun getAllWeeks(): Flow<List<AllowanceWeekEntity>>

    @Query("SELECT * FROM allowance_weeks WHERE weekNumber = :weekNumber AND year = :year LIMIT 1")
    suspend fun getWeekByNumber(weekNumber: Int, year: Int): AllowanceWeekEntity?

    @Query("SELECT * FROM allowance_weeks WHERE weekNumber = :weekNumber AND year = :year LIMIT 1")
    fun getWeekFlow(weekNumber: Int, year: Int): Flow<AllowanceWeekEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(week: AllowanceWeekEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(weeks: List<AllowanceWeekEntity>)

    @Update
    suspend fun update(week: AllowanceWeekEntity)

    @Query("DELETE FROM allowance_weeks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM allowance_weeks")
    suspend fun deleteAll()
}
