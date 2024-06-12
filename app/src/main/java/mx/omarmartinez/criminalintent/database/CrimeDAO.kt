package mx.omarmartinez.criminalintent.database

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import mx.omarmartinez.criminalintent.Crime
import java.util.UUID

@Dao
interface CrimeDAO{
    @Query("SELECT * FROM crime")
    fun getCrimes(): Flow<List<Crime>>

    @Query("SELECT * FROM crime WHERE id = (:id)")
    suspend fun getCrime(id: UUID): Crime
}