package uz.turgunboyevjurabek.muslimapp.feature.domain.model.weekly


import com.google.gson.annotations.SerializedName

data class BirhaftalikItem(
    @SerializedName("date")
    var date: String?=null,
    @SerializedName("hijri_date")
    var hijriDate: HijriDate?=null,
    @SerializedName("region")
    var region: String?=null,
    @SerializedName("times")
    var times: Times?=null,
    @SerializedName("weekday")
    var weekday: String?= null
)