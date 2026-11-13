package uz.turgunboyevjurabek.muslimapp.feature.domain.model.monthly


import com.google.gson.annotations.SerializedName

data class BirOylikItem(
    @SerializedName("date")
    var date: String?=null,
    @SerializedName("day")
    var day: Int?=null,
    @SerializedName("hijri_date")
    var hijriDate: HijriDate?=null,
    @SerializedName("month")
    var month: Int?=null,
    @SerializedName("region")
    var region: String?=null,
    @SerializedName("regionNumber")
    var regionNumber: Int?=null,
    @SerializedName("times")
    var times: Times?=null,
    @SerializedName("weekday")
    var weekday: String?=null
)