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
        binding.btnBack.setOnClickListener {
            activity?.onBackPressed()
        }

        val scanIntegrator = IntentIntegrator.forSupportFragment(this)
        scanIntegrator.setPrompt("Scan")
        scanIntegrator.setBeepEnabled(true)
        scanIntegrator.setBarcodeImageEnabled(true)
        scanIntegrator.initiateScan()


        return binding.root
    }

    @SuppressLint("SimpleDateFormat")
    private fun sendToProduction(materialCount: Int) {
        val MaterialRef = FirebaseDatabase.getInstance().getReference("Materials")
        val serialNo = binding.textViewMaterial.text.toString()
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val date: String = sdf.format(System.currentTimeMillis())
        var quantity = binding.editTextQty.text.toString().toInt()
        var i = 0
        var listMaterial: ArrayList<String> = ArrayList()

        if(quantity in 1..materialCount) {
            MaterialRef.child(serialNo).child("Parts").get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    snapshot.children.forEach { datasnapshot ->

                        if (datasnapshot.child("status").value.toString() == "2") {
                            listMaterial.add(datasnapshot.key.toString())
                        }
                    }

                } else {
                    Toast.makeText(activity, "Error!", Toast.LENGTH_LONG).show()
                }
                while (quantity != 0) {
                    MaterialRef.child(serialNo).child("Parts").child(listMaterial[i])
                        .child("status")
                        .setValue(3)
                    MaterialRef.child(serialNo).child("Parts").child(listMaterial[i])
                        .child("rackOutDate")
                        .setValue(date)
                    quantity -= 1
                    i++
                }
                Toast.makeText(activity, "Successfully Sent to Production", Toast.LENGTH_LONG)
                    .show()
            }
            binding.btnSend.isEnabled = false
        }else{
            Toast.makeText(activity,"There is only $materialCount Part on the Rack!", Toast.LENGTH_LONG).show()
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        var result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {
            if(result.contents != null) {
                afterScan(result.contents.toString())
            }else{
                binding.editTextQty.isEnabled = false
                binding.btnSend.isEnabled = false
            }
        } else {
            Toast.makeText(requireActivity(), "Failed to get result", Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun afterScan(serialNo: String) {

        val MaterialRef = FirebaseDatabase.getInstance().getReference("Materials")
        var materialCount = 0


        MaterialRef.child(serialNo).get().addOnSuccessListener { datasnapshot ->
            if (datasnapshot.exists()) {
                MaterialRef.child(serialNo).child("Parts").get()
                    .addOnSuccessListener { datasnap ->
                        datasnap.children.forEach {
                            if (it.child("status").value.toString() == "2") {
                                materialCount += 1
                            }
                        }
                        binding.textViewMaterial.text = serialNo
                        binding.textViewResult.text = "Found!"
                        binding.textViewMsg.text = "$materialCount Parts on Rack found."
                        binding.btnSend.setOnClickListener { sendToProduction(materialCount) }
                    }
            } else {
                binding.textViewMaterial.text = serialNo
                binding.textViewResult.text = "Not found!"
                binding.editTextQty.isEnabled = false
                binding.btnSend.isEnabled = false
            }
        }
    }
}