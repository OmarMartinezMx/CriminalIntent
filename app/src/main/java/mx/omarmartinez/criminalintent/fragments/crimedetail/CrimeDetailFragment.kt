package mx.omarmartinez.criminalintent.fragments.crimedetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.launch
import mx.omarmartinez.criminalintent.R
import mx.omarmartinez.criminalintent.models.Crime
import mx.omarmartinez.criminalintent.databinding.FragmentCrimeDetailBinding
import mx.omarmartinez.criminalintent.fragments.datepicker.DatePickerFragment
import java.util.Date


class CrimeDetailFragment : Fragment() {

    private var _binding: FragmentCrimeDetailBinding? = null
    private val args: CrimeDetailFragmentArgs by navArgs()

    private val crimeDetailViewModel: CrimeDetailViewModel by viewModels {
        CrimeDetailViewModelFactory(args.crimeId)
    }

    private val binding
        get() = checkNotNull(_binding){
            "Cannot access binding because is null. Is the view visible?"
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this){
            if(binding.crimeTitle.text.toString().isBlank()){
                Toast.makeText(context, R.string.crime_title_blank, Toast.LENGTH_SHORT).show()
            } else{
                findNavController().popBackStack()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCrimeDetailBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            crimeTitle.doOnTextChanged { text, _, _, _ ->
                crimeDetailViewModel.updateCrime { oldCrime ->
                    oldCrime.copy(title = text.toString())
                }
            }

            crimeSolved.setOnCheckedChangeListener { _, isChecked ->
                crimeDetailViewModel.updateCrime { oldCrime ->
                    oldCrime.copy(isSolved = isChecked)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch{
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                crimeDetailViewModel.crime.collect{ crime ->
                    crime?.let { updateUI(it) }
                }
            }
        }

        setFragmentResultListener(DatePickerFragment.REQUEST_KEY_DATE){ _, bundle ->
            val newDate = bundle.getSerializable(DatePickerFragment.BUNDLE_KEY_DATE) as Date

            crimeDetailViewModel.updateCrime { it.copy(date = newDate) }
        }
    }

    private fun updateUI(crime: Crime){
        binding.apply {
            if(crimeTitle.text.toString() != crime.title){
                crimeTitle.setText(crime.title)
            }

            crimeDate.text = crime.date.toString()
            crimeSolved.isChecked = crime.isSolved

            crimeDate.setOnClickListener {
                findNavController().navigate(
                    CrimeDetailFragmentDirections.selectDate(crime.date)
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}