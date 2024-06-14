package mx.omarmartinez.criminalintent.fragments.crimedetail

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.doOnLayout
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
import mx.omarmartinez.criminalintent.utils.getScaledBitmap
import java.io.File
import java.text.DateFormat
import java.util.Date


class CrimeDetailFragment : Fragment() {

    private var _binding: FragmentCrimeDetailBinding? = null
    private val args: CrimeDetailFragmentArgs by navArgs()
    private var photoName: String? = null

    private val crimeDetailViewModel: CrimeDetailViewModel by viewModels {
        CrimeDetailViewModelFactory(args.crimeId)
    }

    private val selectSuspect = registerForActivityResult(
        ActivityResultContracts.PickContact()
    ){
        it?.let {
            parseContactSelection(it)
        }
    }

    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicture()){didTakePicture: Boolean ->
        if(didTakePicture && photoName != null){
            crimeDetailViewModel.updateCrime { oldCrime ->
                oldCrime.copy(photoFileName = photoName)
            }
        }
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

            crimeSuspect.setOnClickListener {
                selectSuspect.launch(null)
            }

            val selectSuspectIntent = selectSuspect.contract.createIntent(requireContext(), null)
            crimeSuspect.isEnabled = canResolveIntent(selectSuspectIntent)

            crimeCamera.setOnClickListener {
                photoName = "IMG_${Date()}.JPG"
                val photoFile = File(requireContext().applicationContext.filesDir, photoName)

                val photoUri  = FileProvider.getUriForFile(
                    requireContext(),
                    "mx.omarmartinez.criminalintent.fileprovider",
                    photoFile
                )

                takePhoto.launch(photoUri)
            }

            val captureImageIntent = takePhoto.contract.createIntent(
                requireContext(),
                Uri.parse("")
            )
            crimeCamera.isEnabled = canResolveIntent(captureImageIntent)
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

            crimeSuspect.text = crime.suspect.ifEmpty {
                getString(R.string.crime_suspect_text)
            }

            crimeReport.setOnClickListener {
                val reportIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, getCrimeReport(crime))
                    putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject))
                }

                val chooserIntent = Intent.createChooser(
                    reportIntent,
                    getString(R.string.send_report)
                )

                startActivity(chooserIntent )
            }

            updatePhoto(crime.photoFileName)
        }
    }

    private fun getCrimeReport(crime: Crime): String{
        val solvedString = if(crime.isSolved){
            getString(R.string.crime_report_solved)
        } else{
            getString(R.string.crime_report_unsolved)
        }

        val dateString = DateFormat.getDateInstance().format(crime.date)

        val suspectText = if(crime.suspect.isBlank()){
            getString(R.string.crime_report_no_suspect)
        } else{
            getString(R.string.crime_report_suspect, crime.suspect)
        }

        return getString(R.string.crime_report, crime.title, dateString, solvedString, suspectText)
    }

    private fun parseContactSelection(contactUri: Uri){
        val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)

        val queryCursor = requireActivity().contentResolver
            .query(contactUri, queryFields, null, null, null)

        queryCursor?.use {
            if(it.moveToFirst()){
                val suspect = it.getString(0)
                crimeDetailViewModel.updateCrime { oldCrime ->
                    oldCrime.copy(suspect = suspect)
                }
            }
        }
    }

    private fun canResolveIntent(intent: Intent): Boolean {
        val packageManager: PackageManager = requireActivity().packageManager
        val resolveActivity: ResolveInfo? = packageManager.resolveActivity(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        )

        return resolveActivity != null
    }

    private fun updatePhoto(photoFileName: String?) {
        if(binding.crimePhoto.tag != photoFileName){
            val photoFile = photoFileName?.let {
                File(requireContext().applicationContext.filesDir, it)
            }

            if(photoFile?.exists() == true){
                binding.crimePhoto.doOnLayout { measuredView ->
                    val scaleBitmap = getScaledBitmap(
                        photoFile.path,
                        measuredView.width,
                        measuredView.height
                    )

                    binding.crimePhoto.setImageBitmap(scaleBitmap)
                    binding.crimePhoto.tag = photoFileName
                }
            }
        } else{
            binding.crimePhoto.setImageBitmap(null)
            binding.crimePhoto.tag = null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}