package com.example.explorexpert.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.explorexpert.databinding.CreateTripBottomSheetBinding
import com.example.explorexpert.ui.viewmodel.AuthViewModel
import com.example.explorexpert.ui.viewmodel.CreateTripViewModel
import com.example.explorexpert.ui.viewmodel.PlanViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CreateTripBottomSheetDialogFragment: BottomSheetDialogFragment() {

    @Inject
    lateinit var createTripViewModel: CreateTripViewModel

    private var _binding: CreateTripBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = CreateTripBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configureButtons()
    }

    private fun configureButtons() {
        binding.btnBottomSheetCreateTrip.setOnClickListener {
            val tripName = binding.txtInputTripName.editText?.text.toString()

            if (tripName == "") {
                binding.txtInputTripName.error = "Trip name cannot be empty"
                return@setOnClickListener
            }
            else {
                binding.txtInputTripName.error = null
            }

            createTripViewModel.createTrip(tripName)

            val parentFrag = requireParentFragment()
            Snackbar.make(
                parentFrag.view as View,
                "Successfully created $tripName",
                Snackbar.LENGTH_SHORT
            ).show()

            refreshParent()

            this.dismiss()
        }
    }

    private fun refreshParent() {
        (requireParentFragment() as PlanFragment).refreshRecyclerViews()
        (requireParentFragment() as PlanFragment).scheduleRecyclerViewsRefresh()
    }

    companion object {
        const val TAG: String = "CreateTripBottomSheetDialogFragment"
    }

}