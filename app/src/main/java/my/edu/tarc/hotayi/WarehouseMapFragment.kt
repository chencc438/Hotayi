package my.edu.tarc.hotayi

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.database.*
import android.view.Gravity

import android.widget.AdapterView
import androidx.core.view.children
import androidx.core.widget.doAfterTextChanged
import com.google.zxing.integration.android.IntentIntegrator


class WarehouseMapFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?


    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_warehouse_map, container, false)
        val ll: LinearLayout = view.findViewById(R.id.linearLayout_Warehouse)

        val searchBar= view.findViewById<EditText>(R.id.textInput_Serial)
        val scanIntegrator = IntentIntegrator.forSupportFragment(this)

        getRacks(ll)

        searchBar.doAfterTextChanged {
            searchMaterial(searchBar.text.toString())
        }

        //spinner
        val spn = view.findViewById<Spinner>(R.id.spn_Admin)
        spn.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long,
            ) {
                if (position != 0) {

                    scanIntegrator.setPrompt("Scan")
                    scanIntegrator.setBeepEnabled(true)
                    scanIntegrator.setBarcodeImageEnabled(true)
                    scanIntegrator.initiateScan()
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }
        return view
    }

    private fun searchMaterial(text:String){
        val myRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Materials")
        val fl: FrameLayout = requireView().findViewById(R.id.frame_layout)
        myRef.get().addOnSuccessListener { datasnapshot ->
            if (datasnapshot.exists()){
                datasnapshot.children.forEach {
                    if(it.child("serialNo").value.toString() == text){
                        val rackid= it.child("rackNo").value.toString().substring(1).toInt()
                        val container = fl.findViewById<TextView>(rackid)
                        val gd = GradientDrawable()
                        gd.setColor(Color.RED) // Changes this drawbale to use a single color instead of a gradient
                        gd.cornerRadius = 5f
                        gd.setStroke(1, -0x1000000)

                        container.background = gd
                    }else{
                        val rackid= it.child("rackNo").value.toString().substring(1).toInt()
                        val container = fl.findViewById<TextView>(rackid)
                        val gd = GradientDrawable()
                        gd.setColor(Color.GREEN) // Changes this drawbale to use a single color instead of a gradient
                        gd.cornerRadius = 5f
                        gd.setStroke(1, -0x1000000)

                        container.background = gd
                    }
                }
            }
        }
    }

    private fun onSpinnerSelected(popupview: View, fl: FrameLayout, position: Int, spn: Spinner) {
        val myRef = FirebaseDatabase.getInstance().getReference("Racks")
        val myRef2 =FirebaseDatabase.getInstance().getReference("Materials")
        val btncancel = popupview.findViewById<Button>(R.id.btn_Cancel)
        val btnconfirm = popupview.findViewById<Button>(R.id.btn_Confirm)
        val textviewtitle = popupview.findViewById<TextView>(R.id.textView_Title)
        val textviewpop2 = popupview.findViewById<TextView>(R.id.textView_Pop2)
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
        )
        params.gravity = Gravity.CENTER
        updateView(fl, spn, true)

        btncancel.setOnClickListener {
            updateView(fl, spn, false)
            (popupview.parent as ViewGroup).removeView(popupview)
        }
        if (position == 1) {
            textviewtitle.text = getString(R.string.addrack)
            btnconfirm.setOnClickListener {
                val rackid = textviewpop2.text.toString()

                myRef.child(rackid).get().addOnSuccessListener {
                    if (it.exists()) {
                        Toast.makeText(
                            activity,
                            "Rack ID ${rackid} is already Exist!",
                            Toast.LENGTH_LONG
                        ).show()

                    } else {
                        myRef.child(rackid).child("rackNo").setValue(rackid)
                        Toast.makeText(
                            activity,
                            "Successfully Added Rack ${rackid}!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                updateView(fl, spn, false)
                (popupview.parent as ViewGroup).removeView(popupview)
            }
        }
        if (position == 2) {

            textviewtitle.text = getString(R.string.deleterack)

            btnconfirm.setOnClickListener {

                val rackid = textviewpop2.text.toString()

                myRef.child(rackid).get().addOnSuccessListener {
                    if (it.exists()) {
                        myRef.child(rackid).removeValue().addOnSuccessListener {
                            Toast.makeText(
                                activity,
                                "Successfully deleted Rack ${rackid}!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        myRef2.get().addOnSuccessListener { datasnapshot ->
                            if (datasnapshot.exists()) {
                                datasnapshot.children.forEach { snap ->
                                    if(snap.child("rackNo").value.toString() == rackid){
                                        myRef2.child(snap.key.toString()).child("rackNo").setValue("")
                                        snap.child("Parts").children.forEach { snapshot ->
                                            if(snapshot.child("status").value.toString() == "2"){
                                                myRef2.child(snap.key.toString()).child("Parts").child(snapshot.key.toString()).child("status").setValue(1)
                                            }
                                        }
                                    }
                                }
                            }
                        }


                    } else {
                        Toast.makeText(
                            activity,
                            "Rack ID ${rackid} Not found in Database!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                updateView(fl, spn, false)
                (popupview.parent as ViewGroup).removeView(popupview)
            }

        }
        activity?.addContentView(popupview, params)
    }

    private fun updateView(fl: FrameLayout, spn: Spinner, bool: Boolean) {
        if (!bool) {
            for (x in fl.children) {
                x.isEnabled = true
            }
        } else {

            for (x in fl.children) {
                x.isEnabled = false
            }
        }
        spn.setSelection(0)
    }

    private fun view(): TableLayout {
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )

        val table = TableLayout(activity)

        table.layoutParams = params

        val myRef = FirebaseDatabase.getInstance().getReference("Racks")
        myRef.get().addOnSuccessListener {
            it.children.forEach { it ->
                val row = TableRow(activity)
                val tv = TextView(activity)
                val rackno = it.child("rackNo").value.toString()
                tv.text = rackno
                tv.setPadding(0, 25, 400, 25)

                row.addView(tv)
                table.addView(row)

                val container = TableRow(activity)
                val tv2 = TextView(activity)
                val gd = GradientDrawable()
                gd.setColor(Color.GREEN) // Changes this drawbale to use a single color instead of a gradient
                gd.cornerRadius = 5f
                gd.setStroke(1, -0x1000000)
                tv2.background = gd
                tv2.setPadding(0, 10, 400, 10)
                tv2.id = rackno.substring(1).toInt()
                buttonEffect(tv2, rackno)
                container.addView(tv2)
                table.addView(container)

            }
        }
        return table
    }

    @SuppressLint("ClickableViewAccessibility")
    fun buttonEffect(button: View, rackno: String) {
        button.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.background.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP)
                    v.invalidate()
                }
                MotionEvent.ACTION_UP -> {
                    v.background.clearColorFilter()
                    v.invalidate()
                    showMaterialInfo(rackno,v)
                }
                MotionEvent.ACTION_CANCEL ->{
                    v.background.clearColorFilter()
                    v.invalidate()
                }

            }
            true
        }
    }


    private fun getRacks(sv: LinearLayout) {
        val myRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Racks")

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                sv.removeAllViews()
                sv.addView(view())
                //Log.w(ContentValues.TAG, materialList[1].)
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
            }
        })
    }


    @SuppressLint("SetTextI18n", "ResourceAsColor")
    private fun showMaterialInfo(id: String, v: View) {
        val spn = requireActivity().findViewById<Spinner>(R.id.spn_Admin)
        val fl: FrameLayout = requireView().findViewById(R.id.frame_layout)
        val myRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Materials")
        val inflater = LayoutInflater.from(activity)
        val popupview = inflater.inflate(
            R.layout.warehouse_rackinfo_popup_window,
            requireView().parent as ViewGroup,
            false
        )
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
        )
        params.gravity = Gravity.CENTER

        updateView(fl, spn, true)
        v.isEnabled = false
        val ll: LinearLayout = popupview.findViewById(R.id.linear_layout)
        myRef.get().addOnSuccessListener {
            it.children.forEach { material ->
                if(material.child("rackNo").value.toString()  == id){
                    var name = material.child("name").value.toString()
                    var serial = material.child("serialNo").value.toString()
                    var quantity = material.child("quantity").value.toString()
                    val tv = TextView(activity)
                    tv.text = "$serial: $name (Qty:$quantity)"
                    tv.setTypeface(null, Typeface.BOLD)
                    tv.textSize = 16F
                    ll.addView(tv)
                    material.child("Parts").children.forEach {
                        if(it.child("status").value.toString() =="2") {
                            val partNo = it.child("partNo").value.toString()
                            val rackInDate = it.child("rackInDate").value.toString()

                            val receivedBy = it.child("receivedBy").value.toString()
                            val receivedDate = it.child("receivedDate").value.toString()


                            val tv = TextView(activity)
                            tv.text = "Part: $partNo"
                            tv.setTypeface(null, Typeface.BOLD)
                            ll.addView(tv)
                            val tv2 = TextView(activity)
                            tv2.text = "RackInDate: $rackInDate"
                            ll.addView(tv2)
                            val tv3 = TextView(activity)
                            tv3.text = "ReceivedBy: $receivedBy"
                            ll.addView(tv3)
                            val tv5 = TextView(activity)
                            tv5.text = "ReceivedDate: $receivedDate"
                            ll.addView(tv5)
                            val tv6 = TextView(activity)
                            tv6.text = " "
                            ll.addView(tv6)
                        }
                    }
                }
            }
            val btn = Button(activity)
            btn.setOnClickListener {
                updateView(fl, spn, false)
                v.isEnabled = true
                (popupview.parent as ViewGroup).removeView(popupview)
            }
            ll.addView(btn)
            btn.text = "BACK"
            val gd = GradientDrawable()
            gd.setColor(R.color.purple_500) // Changes this drawbale to use a single color instead of a gradient
            gd.cornerRadius = 5f
            btn.background = gd

            activity?.addContentView(popupview, params)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val inflater = LayoutInflater.from(activity)
        val spn = requireActivity().findViewById<Spinner>(R.id.spn_Admin)
        val position = spn.selectedItemPosition
        val fl: FrameLayout = requireView().findViewById(R.id.frame_layout)
        val popupview = inflater.inflate(
            R.layout.warehouse_popup_window,
            requireView().parent as ViewGroup,
            false
        )
        val textviewpop2 = popupview.findViewById<TextView>(R.id.textView_Pop2)
        val btnConfirm = popupview.findViewById<Button>(R.id.btn_Confirm)
        var result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {
            if(result.contents != null) {
                textviewpop2.text = result.contents.toString()
            }else {
                textviewpop2.text =""
                btnConfirm.isEnabled = false
            }
                onSpinnerSelected(popupview, fl, position, spn)

        }
    }

}

