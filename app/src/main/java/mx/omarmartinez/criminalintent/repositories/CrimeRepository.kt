package mx.omarmartinez.criminalintent.repositories

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.flow.Flow
import mx.omarmartinez.criminalintent.database.CrimeDatabase
import mx.omarmartinez.criminalintent.models.Crime
import java.util.UUID

private const val DATABASE_NAME = "crime-database"

class CrimeRepository private constructor(context: Context){

    private val database: CrimeDatabase = Room.databaseBuilder(
        context.applicationContext,
        CrimeDatabase::class.java,
        DATABASE_NAME
    )
        .createFromAsset(DATABASE_NAME)
        .build()

    fun getCrimes(): Flow<List<Crime>> = database.crimeDAO().getCrimes()
    suspend fun getCrime(id: UUID): Crime = database.crimeDAO().getCrime(id)

    companion object{
        private var INSTANCE: CrimeRepository? = null

        fun initialize(context: Context){
            if(INSTANCE == null){
                INSTANCE = CrimeRepository(context)
            }
        }

        fun get(): CrimeRepository {
            return INSTANCE ?: throw IllegalStateException("CrimeRepository must be initialized")
        }
    }
}