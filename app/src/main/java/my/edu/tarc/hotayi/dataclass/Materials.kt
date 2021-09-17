package my.edu.tarc.hotayi.dataclass

import com.google.firebase.database.DataSnapshot

data class Materials (
    val LastRecord:Int,
    val Name:String,
    val parts:DataSnapshot,
    val Quantity:Int,
    val RackNo:String,
    val SerialNo:Int,
    )