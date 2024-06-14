package mx.omarmartinez.criminalintent.repositories

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import mx.omarmartinez.criminalintent.database.CrimeDatabase
import mx.omarmartinez.criminalintent.database.migration_1_2
import mx.omarmartinez.criminalintent.models.Crime
import java.util.UUID

private const val DATABASE_NAME = "crime-database"

class CrimeRepository private constructor(
    context: Context,
    private val coroutineScope: CoroutineScope = GlobalScope){

    private val database: CrimeDatabase = Room.databaseBuilder(
        context.applicationContext,
        CrimeDatabase::class.java,
        DATABASE_NAME
    )
        .addMigrations(migration_1_2)
        .build()

    fun getCrimes(): Flow<List<Crime>> = database.crimeDAO().getCrimes()
    suspend fun getCrime(id: UUID): Crime = database.crimeDAO().getCrime(id)
    fun updateCrime(crime: Crime){
        coroutineScope.launch {
            database.crimeDAO().updateCrime(crime)
        }
    }
    suspend fun addCrime(crime: Crime){
        database.crimeDAO().addCrime(crime)
    }

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