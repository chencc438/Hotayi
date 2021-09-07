package my.edu.tarc.hotayi

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.zxing.integration.android.IntentIntegrator
import org.json.JSONObject

class RetrieveProductFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view  = inflater.inflate(R.layout.fragment_retrieve_product, container, false)

        val scanIntegrator = IntentIntegrator.forSupportFragment(this)
        scanIntegrator.setPrompt("Scan")
        scanIntegrator.setBeepEnabled(true)
        scanIntegrator.setBarcodeImageEnabled(true)
        scanIntegrator.initiateScan()


        return view
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        var result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if(result != null){
            val obj: JSONObject = JSONObject(result.contents)
            val strCode = obj.getString("name")
            view?.findViewById<TextView>(R.id.textView_result)?.text = strCode
        }
    }
}