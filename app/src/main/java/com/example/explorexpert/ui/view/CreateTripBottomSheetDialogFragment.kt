package com.example.explorexpert.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.explorexpert.databinding.CreateTripBottomSheetBinding
import com.example.explorexpert.ui.viewmodel.AuthViewModel
import com.example.explorexpert.ui.viewmodel.CreateTripViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CreateTripBottomSheetDialogFragment: BottomSheetDialogFragment() {

    @Inject
    lateinit var createTripViewModel: CreateTripViewModel

    private var _binding: CreateTripBottomSheetBinding? = null
    private val binding get() = _binding!!

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


    }

    private fun configureButtons() {
        binding.btnBottomSheetCreateTrip.setOnClickListener {

        }
    }

    companion object {
        const val TAG: String = "CreateTripBottomSheetDialogFragment"
    }

}