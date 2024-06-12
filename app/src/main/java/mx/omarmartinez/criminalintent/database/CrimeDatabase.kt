package mx.omarmartinez.criminalintent.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import mx.omarmartinez.criminalintent.Crime

@Database(entities = [Crime::class], version = 1, exportSchema = false)
@TypeConverters(CrimeTypeConverters::class)
abstract class CrimeDatabase :RoomDatabase() {
    abstract fun crimeDAO(): CrimeDAO
}