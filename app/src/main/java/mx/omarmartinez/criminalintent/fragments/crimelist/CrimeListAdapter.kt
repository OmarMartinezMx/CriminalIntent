package mx.omarmartinez.criminalintent.fragments.crimelist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import mx.omarmartinez.criminalintent.models.Crime
import mx.omarmartinez.criminalintent.databinding.ListItemCrimeBinding
import java.text.DateFormat
import java.util.UUID

class CrimeHolder(
    private val binding: ListItemCrimeBinding
) : RecyclerView.ViewHolder(binding.root){
    fun bind(crime: Crime, onCrimeClicked:(crimeId: UUID) -> Unit){
        binding.crimeTitle.text = crime.title
        binding.crimeDate.text = DateFormat.getDateInstance().format(crime.date)

        binding.crimeSolved.visibility = if (crime.isSolved){
            View.VISIBLE
        } else{
            View.GONE
        }

        binding.root.setOnClickListener {
            onCrimeClicked(crime.id)
        }
    }
}

class CrimeListAdapter(
    private val crimes: List<Crime>,
    private val onCrimeClicked: (crimeId: UUID) -> Unit
) : RecyclerView.Adapter<CrimeHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemCrimeBinding.inflate(inflater, parent, false)
        return CrimeHolder(binding)
    }

    override fun getItemCount(): Int {
        return crimes.size
    }

    override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
        val crime = crimes[position]
        holder.bind(crime, onCrimeClicked)
    }

}