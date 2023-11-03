package com.example.explorexpert.ui.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.explorexpert.adapters.TripAdapter
import com.example.explorexpert.data.model.Trip
import com.example.explorexpert.databinding.FragmentPlanBinding
import com.example.explorexpert.ui.viewmodel.PlanViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PlanFragment : Fragment() {

    companion object {
        private val TAG = "PlanFragment"
    }

    @Inject
    lateinit var planViewModel: PlanViewModel

    private var _binding: FragmentPlanBinding? = null

    private lateinit var adapter: TripAdapter

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPlanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configureRecyclerView()
        configureButtons()
        configureObservers()
    }

    private fun configureButtons() {
        binding.btnCreateATrip.setOnClickListener {
            val createTripBottomSheetDialogFragment = CreateTripBottomSheetDialogFragment()
            createTripBottomSheetDialogFragment.show(childFragmentManager, CreateTripBottomSheetDialogFragment.TAG)
        }
    }

    private fun configureRecyclerView() {
        adapter = TripAdapter(object : TripAdapter.ItemClickListener {
            override fun onItemClick(trip: Trip) {
                // Summon dialog for viewing trip saved items
            }
        })

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun configureObservers() {
        planViewModel.trips.observe(viewLifecycleOwner) { trips ->
            adapter.submitList(trips)
        }
    }
}