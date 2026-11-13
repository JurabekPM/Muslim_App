package uz.turgunboyevjurabek.muslimapp.feature.domain.model.weekly


import com.google.gson.annotations.SerializedName

data class HijriDate(
    @SerializedName("day")
    var day: Int?,
    @SerializedName("month")
    var month: String?
)