package com.example.explorexpert.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.explorexpert.data.model.Trip
import com.example.explorexpert.databinding.AddLinkBottomSheetBinding
import com.example.explorexpert.ui.viewmodel.AddTripItemViewModel
import com.example.explorexpert.ui.viewmodel.TripViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import me.angrybyte.goose.Article
import me.angrybyte.goose.Configuration
import me.angrybyte.goose.ContentExtractor
import javax.inject.Inject

@AndroidEntryPoint
class AddLinkBottomSheetDialogFragment(
    private val trip: Trip
): BottomSheetDialogFragment() {

    @Inject
    lateinit var addTripItemViewModel: AddTripItemViewModel
    private lateinit var tripViewModel: TripViewModel

    private var _binding : AddLinkBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tripViewModel = (requireParentFragment() as TripDialogFragment).tripViewModel
        addTripItemViewModel.setTrip(trip)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = AddLinkBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configureButtons()
    }

    private fun configureButtons() {
        binding.btnAddALinkSave.setOnClickListener {
            val linkURL = binding.txtInputURL.editText?.text.toString()

            if (linkURL == "") {
                binding.txtAddALink.error = "Link cannot be empty"
                return@setOnClickListener
            }
            else {
                binding.txtAddALink.error = null
            }

            val config = Configuration(requireContext().cacheDir.absolutePath)
            val extractor = ContentExtractor(config)

            addTripItemViewModel.addLink(linkURL, extractor)

            (requireParentFragment() as TripDialogFragment).refreshTrip()
            (requireParentFragment() as TripDialogFragment).scheduleTripRefresh()
            tripViewModel.fetchSavedItems()
            this.dismiss()
        }
    }

    companion object {
        const val TAG: String = "AddLinkBottomSheetDialogFragment"
    }
}