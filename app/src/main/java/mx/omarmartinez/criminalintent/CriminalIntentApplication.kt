package mx.omarmartinez.criminalintent

import android.app.Application
import mx.omarmartinez.criminalintent.repositories.CrimeRepository

class CriminalIntentApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CrimeRepository.initialize(this)
    }
}