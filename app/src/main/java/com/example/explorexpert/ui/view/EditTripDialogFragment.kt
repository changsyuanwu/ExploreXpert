package com.example.explorexpert.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.explorexpert.R
import com.example.explorexpert.data.model.Trip
import com.example.explorexpert.databinding.DialogEditTripBinding
import com.example.explorexpert.ui.viewmodel.TripViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EditTripDialogFragment (
    private var trip: Trip
): DialogFragment() {

    @Inject
    lateinit var tripViewModel: TripViewModel

    private var _binding: DialogEditTripBinding? = null

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.FullScreenDialogSlideLeftStyle)

        tripViewModel.setTrip(trip)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogEditTripBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showProgressIndicator()

        configureInitialUI()
        configureButtons()

        hideProgressIndicator()
    }

    private fun configureInitialUI() {
        binding.txtInputTripName.editText?.setText(trip.name)
        binding.switchPrivacy.isChecked = !trip.private
    }

    private fun configureButtons() {
        binding.btnBackIcon.setOnClickListener {
            refreshParentFragment()
            this.dismiss()
        }

        binding.btnDelete.setOnClickListener {
            val confirmDialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete this trip?")
                .setMessage("This will permanently delete all saved items, notes, and links in this trip.")
                .setNeutralButton("Cancel") { dialog, which ->
                    dialog.dismiss()
                }
                .setPositiveButton("Delete") { dialog, which ->
                    tripViewModel.deleteTrip(trip.id)

                    // Dismiss this dialog and the parent trip dialog
                    dialog.dismiss()
                    (requireParentFragment() as TripDialogFragment).dismiss()

                    refreshGrandparentFragment()
                    this.dismiss()
                }
                .show()
        }

        binding.btnSave.setOnClickListener {
            val newTripName = binding.txtInputTripName.editText?.text.toString()
            val isPublic = binding.switchPrivacy.isChecked

            // If the name is (different and non empty) or privacy setting was toggled, update the trip
            if ((newTripName != trip.name && newTripName != "") || !isPublic != trip.private ) {
                tripViewModel.updateTrip(newTripName, !isPublic)
            }

            refreshParentFragment()
            refreshGrandparentFragment()

            this.dismiss()
        }
    }

    private fun refreshParentFragment() {
        (requireParentFragment() as TripDialogFragment).refreshTrip()
        (requireParentFragment() as TripDialogFragment).scheduleTripRefresh()
    }

    private fun refreshGrandparentFragment() {
        ((requireParentFragment() as TripDialogFragment).requireParentFragment() as PlanFragment).refreshRecyclerViews()
        ((requireParentFragment() as TripDialogFragment).requireParentFragment() as PlanFragment).scheduleRecyclerViewsRefresh()
    }

    private fun showProgressIndicator() {
        binding.progressIndicator.visibility = View.VISIBLE
    }

    private fun hideProgressIndicator() {
        binding.progressIndicator.visibility = View.INVISIBLE
    }

    companion object {
        const val TAG: String = "EditTripDialogFragment"
    }
}