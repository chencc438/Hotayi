package my.edu.tarc.hotayi

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import my.edu.tarc.hotayi.databinding.FragmentMenuBinding


class MenuFragment : Fragment() {

    private lateinit var binding: FragmentMenuBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_menu, container, false)

        binding.product.setOnClickListener{
            Navigation.findNavController(it).navigate(R.id.action_menuFragment_to_productFragment)
        }
        binding.retrieveProduct.setOnClickListener{
            Navigation.findNavController(it).navigate(R.id.action_menuFragment_to_retrieveProductFragment)
        }
        binding.warehouseMap.setOnClickListener{
            Navigation.findNavController(it).navigate(R.id.action_menuFragment_to_warehouseMapFragment)
        }
        binding.report.setOnClickListener{
            Navigation.findNavController(it).navigate(R.id.action_menuFragment_to_reportFragment)
        }

        return binding.root
    }
}