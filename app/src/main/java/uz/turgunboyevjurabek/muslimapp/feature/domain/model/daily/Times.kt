package uz.turgunboyevjurabek.muslimapp.feature.domain.model.daily


import com.google.gson.annotations.SerializedName

data class Times(
    @SerializedName("asr")
    var asr: String?,
    @SerializedName("hufton")
    var hufton: String?,
    @SerializedName("peshin")
    var peshin: String?,
    @SerializedName("quyosh")
    var quyosh: String?,
    @SerializedName("shom_iftor")
    var shomIftor: String?,
    @SerializedName("tong_saharlik")
    var tongSaharlik: String?
)