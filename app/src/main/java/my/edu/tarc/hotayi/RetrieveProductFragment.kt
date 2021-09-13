package my.edu.tarc.hotayi

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.firebase.database.FirebaseDatabase
import com.google.zxing.integration.android.IntentIntegrator
import my.edu.tarc.hotayi.databinding.FragmentRetrieveProductBinding
import java.text.SimpleDateFormat

class RetrieveProductFragment : Fragment() {
    private lateinit var binding: FragmentRetrieveProductBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_retrieve_product, container, false)
        binding.btnRetrieveBack.setOnClickListener {
            activity?.onBackPressed()
        }
        val scanIntegrator = IntentIntegrator.forSupportFragment(this)
        scanIntegrator.setPrompt("Scan")
        scanIntegrator.setBeepEnabled(true)
        scanIntegrator.setBarcodeImageEnabled(true)
        scanIntegrator.initiateScan()


        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        var result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {
            afterScan(result.contents.toString())
        } else {
            Toast.makeText(requireActivity(), "Failed to get result", Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun afterScan(serialNo: String) {
        val materialsracksRef = FirebaseDatabase.getInstance().getReference("MaterialsRacks")
        val materialsRef = FirebaseDatabase.getInstance().getReference("Materials")
        materialsRef.child(serialNo).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                if (snapshot.child("Status").value.toString() == 2.toString()) {
                    materialsracksRef.child(serialNo).get().addOnSuccessListener { dataSnapshot ->
                        if (dataSnapshot.exists()) {
                            val sdf = SimpleDateFormat("dd/MM/yyyy")
                            var date: String = sdf.format(System.currentTimeMillis())
                            materialsRef.child(serialNo).child("Status").setValue(3)
                                .addOnSuccessListener {
                                    materialsracksRef.child(serialNo).child("RackOutDate")
                                        .setValue(date).addOnSuccessListener {
                                            binding.textViewResult.text = serialNo
                                            binding.textViewMsg.text =
                                                "Has been issued to production!"

                                        }
                                }
                        } else{
                            binding.textViewResult.text = serialNo
                            //material status is at rack but cannot found on rack
                            binding.textViewMsg.text = "Error!"
                        }
                    }
                }else{
                    binding.textViewResult.text = serialNo
                    //material status is not on rack
                    binding.textViewMsg.text = "Is not on the Rack!"
                }
            }else{
                binding.textViewResult.text = serialNo
                binding.textViewMsg.text = "Material Not found!"
            }
        }
    }
}