package my.edu.tarc.hotayi

import android.annotation.SuppressLint
import android.content.ContentValues
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.database.*
import my.edu.tarc.hotayi.dataclass.Material
import android.view.Gravity

import android.widget.AdapterView
import androidx.core.view.children
import my.edu.tarc.hotayi.dataclass.Rack


class WarehouseMapFragment : Fragment() {
    val materialList = arrayListOf<Material>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?


    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_warehouse_map, container, false)
        val popupview = inflater.inflate(R.layout.warehouse_popup_window, container, false)
        val fl: FrameLayout = view.findViewById(R.id.frame_layout)

        fl.addView(view())
        //do a looping to set clickable
        for (x in materialList) {
            val tv1 = view.findViewById<TextView>(x.location)
            buttonEffect(tv1)
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
                if(position!=0) {
                    onSpinnerSelected(popupview, fl, position,spn)
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }
        return view
    }

    private fun onSpinnerSelected(popupview:View, fl:FrameLayout, position:Int, spn:Spinner){
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val btncancel = popupview.findViewById<Button>(R.id.btn_Cancel)
        val btnconfirm = popupview.findViewById<Button>(R.id.btn_Confirm)
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
        )
        params.gravity = Gravity.CENTER


        updateView(fl,spn,true)

        btncancel.setOnClickListener {
            updateView(fl,spn,false)
        }
        if(position == 1) {

            btnconfirm.setOnClickListener{
                val inputnum = popupview.findViewById<EditText>(R.id.input_num).text.toString().toInt()
                val spnrow = popupview.findViewById<Spinner>(R.id.spn_Row).selectedItem.toString().toInt()
                val spncol = popupview.findViewById<Spinner>(R.id.spn_Col).selectedItem.toString().toInt()
                val myRef = FirebaseDatabase.getInstance().getReference("Rack")
                myRef.get().addOnSuccessListener {
                    var rackcount = it.childrenCount
                    for(i in 1..inputnum) {
                        var rackId = rackcount.toInt() + i
                        var rack = Rack(rackId,spnrow,spncol)
                        myRef.child(rackId.toString()).setValue(rack)
                    }
                }
                updateView(fl,spn,false)
                (popupview.parent as ViewGroup).removeView(popupview)
            }
        }
        if(position == 2){

        }



        activity?.addContentView(popupview,params)
    }

    private fun updateView(fl: FrameLayout, spn: Spinner, bool: Boolean) {
        if(!bool) {
            for (x in fl.children) {
                x.isEnabled = true
            }
        }else{

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
        params.width = 1000
        params.height = 1200
        params.setMargins(10,300,0,0)


        val table = TableLayout(activity)
        table.layoutParams = params

        for(x in 1 until 5) {
            for (i in 1 until 3) {
                val row = TableRow(activity)
                for (j in 1 until 8) {
                    val tv = TextView(activity)
                    tv.text = j.toString()
                    tv.setPadding(40,25,40,25)
                    tv.id = x * 100 + i * 10 + j
                    val gd = GradientDrawable()
                    gd.setColor(-0xff0100) // Changes this drawbale to use a single color instead of a gradient
                    gd.cornerRadius = 5f
                    gd.setStroke(1, -0x1000000)
                    tv.background = gd

                    row.addView(tv)
                    //no background no id, just used to split between items
                    val tv2 = TextView(activity)
                    tv2.setPadding(10,25,10,25)

                    row.addView(tv2)
                }
                table.addView(row)
            }
            val whiterow = TableRow(activity)
            for (j in 0 until 1) {
                val tv = TextView(activity)
                tv.setBackgroundColor(Color.parseColor("#FFFFFF"))
                whiterow.addView(tv)
            }
            table.addView(whiterow)
        }

        return table
    }

    @SuppressLint("ClickableViewAccessibility")
    fun buttonEffect(button: View) {
        button.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.background.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP)
                    v.invalidate()
                }
                MotionEvent.ACTION_UP -> {
                    v.background.clearColorFilter()
                    v.invalidate()
                }
            }
            true
        }
    }

    private fun getMaterialData(){
        val myRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Material")

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(snapshot in dataSnapshot.children){
                    var material =Material(snapshot.child("location").value.toString().toInt(),snapshot.child("id").value.toString())
                    materialList.add(material)
                }
                //Log.w(ContentValues.TAG, materialList[1].)
            }
            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
            }
        })
    }

}

