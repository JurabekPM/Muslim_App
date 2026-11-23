package uz.turgunboyevjurabek.muslimapp.feature.data.local

import android.net.Uri
import androidx.annotation.DrawableRes
import uz.turgunboyevjurabek.muslimapp.R

internal enum class MosquePhotoLicense(val title: String, val url: String) {
    Cc0("CC0 1.0", "https://creativecommons.org/publicdomain/zero/1.0/"),
    By20("CC BY 2.0", "https://creativecommons.org/licenses/by/2.0/"),
    By30("CC BY 3.0", "https://creativecommons.org/licenses/by/3.0/"),
    By40("CC BY 4.0", "https://creativecommons.org/licenses/by/4.0/"),
    BySa40("CC BY-SA 4.0", "https://creativecommons.org/licenses/by-sa/4.0/"),
}

internal data class MosquePhoto(
    @DrawableRes val thumbnail: Int,
    val author: String,
    val fileName: String,
    val license: MosquePhotoLicense = MosquePhotoLicense.BySa40,
) {
    val sourceUrl: String
        get() = Uri.parse("https://commons.wikimedia.org/wiki")
            .buildUpon()
            .appendPath("File:${fileName.replace(' ', '_')}")
            .build()
            .toString()
}

internal object MosquePhotos {
    // The catalog key is the stable OpenStreetMap feature ID, not the display name.
    private val photosByMosqueId = mapOf(
        "node/12029940233" to MosquePhoto(
            R.drawable.mosque_photo_node_12029940233,
            author = "David Stanley from Nanaimo, Canada",
            fileName = "Bibi-Khanym Mosque (8145400614).jpg",
            license = MosquePhotoLicense.By20,
        ),
        "node/4745883987" to MosquePhoto(
            R.drawable.mosque_photo_node_4745883987,
            author = "Ymblanter",
            fileName = "Bukhara Baland Mosque 2.jpg",
        ),
        "node/6466250605" to MosquePhoto(
            R.drawable.mosque_photo_node_6466250605,
            author = "SabirovaNigora",
            fileName = "Shayx Muhammad Sodiq Muhammad Yusuf nomidagi jome' masjidi.jpg",
            license = MosquePhotoLicense.By40,
        ),
        "relation/13911901" to MosquePhoto(
            R.drawable.mosque_photo_relation_13911901,
            author = "Gregor Rom",
            fileName = "Komplex Said Scheliker Bei.jpg",
        ),
        "relation/19934455" to MosquePhoto(
            R.drawable.mosque_photo_relation_19934455,
            author = "Muxriddin Azimov",
            fileName = "Hazrati Imom masjidi 20230421.jpg",
            license = MosquePhotoLicense.Cc0,
        ),
        "relation/4827007" to MosquePhoto(
            R.drawable.mosque_photo_relation_4827007,
            author = "Yuriy Pavlovich",
            fileName = "Minor Mosque Tashkent.jpg",
            license = MosquePhotoLicense.Cc0,
        ),
        "relation/5335620" to MosquePhoto(
            R.drawable.mosque_photo_relation_5335620,
            author = "Humoyun Mehridinov",
            fileName = "Masjidi Kalon 1.jpg",
        ),
        "way/1265442637" to MosquePhoto(
            R.drawable.mosque_photo_way_1265442637,
            author = "Wikisurgery94",
            fileName = "Old tomoni ko'rinishi.jpg",
            license = MosquePhotoLicense.Cc0,
        ),
        "way/143446111" to MosquePhoto(
            R.drawable.mosque_photo_way_143446111,
            author = "Azizxon Muzaffarov",
            fileName = "Kox Ota 2023.jpg",
        ),
        "way/144057369" to MosquePhoto(
            R.drawable.mosque_photo_way_144057369,
            author = "Jamshid Nurkulov",
            fileName = "Ukkosha Mosque 03.jpg",
        ),
        "way/148845610" to MosquePhoto(
            R.drawable.mosque_photo_way_148845610,
            author = "Umid1108",
            fileName = "Xalfa Khudoidad complex. Bukhara.jpg",
        ),
        "way/149766451" to MosquePhoto(
            R.drawable.mosque_photo_way_149766451,
            author = "Muzaffar Murodovich",
            fileName = "Kamolon masjidi.jpg",
        ),
        "way/150076607" to MosquePhoto(
            R.drawable.mosque_photo_way_150076607,
            author = "Bgag",
            fileName = "Khoja Kalon mosque.jpg",
            license = MosquePhotoLicense.Cc0,
        ),
        "way/163272972" to MosquePhoto(
            R.drawable.mosque_photo_way_163272972,
            author = "Jean-Pierre Dalbéra from Paris, France",
            fileName = "La mosquée Bolo-Khaouz (Boukhara, Ouzbékistan) (5720017982).jpg",
            license = MosquePhotoLicense.By20,
        ),
        "way/171374621" to MosquePhoto(
            R.drawable.mosque_photo_way_171374621,
            author = "Mrjavokh1r",
            fileName = "Kuk Gumbaz mosque Shakhrisabz.jpg",
        ),
        "way/172613228" to MosquePhoto(
            R.drawable.mosque_photo_way_172613228,
            author = "Ymblanter",
            fileName = "Bukhara Namazgoh Mosque 3.jpg",
        ),
        "way/174718685" to MosquePhoto(
            R.drawable.mosque_photo_way_174718685,
            author = "Kraftabbas",
            fileName = "Коканд.Мечеть и медресе Джами.jpg",
        ),
        "way/185051424" to MosquePhoto(
            R.drawable.mosque_photo_way_185051424,
            author = "Bgag",
            fileName = "Juma Mosque (Khiva) 01.jpg",
            license = MosquePhotoLicense.Cc0,
        ),
        "way/207933191" to MosquePhoto(
            R.drawable.mosque_photo_way_207933191,
            author = "Kraftabbas",
            fileName = "Навои. Мечеть Касим-Шейх.jpg",
        ),
        "way/299867380" to MosquePhoto(
            R.drawable.mosque_photo_way_299867380,
            author = "Kraftabbas",
            fileName = "Навоийская обл. Мечеть Деггароний.jpg",
        ),
        "way/33427229" to MosquePhoto(
            R.drawable.mosque_photo_way_33427229,
            author = "Yunus M-D",
            fileName = "Фото мечети \"Новза\".jpg",
        ),
        "way/457119463" to MosquePhoto(
            R.drawable.mosque_photo_way_457119463,
            author = "Bok",
            fileName = "Бухара, Магоки-курпа, 20230429.jpg",
        ),
        "way/457994223" to MosquePhoto(
            R.drawable.mosque_photo_way_457994223,
            author = "Ymblanter",
            fileName = "Bukhara Mavlana Sharif Mosque.jpg",
        ),
        "way/466841435" to MosquePhoto(
            R.drawable.mosque_photo_way_466841435,
            author = "Khasan azim1",
            fileName = "Islomobod masjid 01.jpg",
            license = MosquePhotoLicense.Cc0,
        ),
        "way/535736318" to MosquePhoto(
            R.drawable.mosque_photo_way_535736318,
            author = "Muzaffar Murodovich",
            fileName = "Mirza Yusuf masjidi, hovlisidan koʻrinishi.jpg",
        ),
        "way/790949485" to MosquePhoto(
            R.drawable.mosque_photo_way_790949485,
            author = "Alisa Trebuchina",
            fileName = "Orifjonboy masjidi 1.jpg",
        ),
        "way/95639944" to MosquePhoto(
            R.drawable.mosque_photo_way_95639944,
            author = "F.Higer",
            fileName = "Dzhuma-Moschee Taschkent 2009.JPG",
            license = MosquePhotoLicense.By30,
        ),
        "way/982206779" to MosquePhoto(
            R.drawable.mosque_photo_way_982206779,
            author = "Thezillat",
            fileName = "Rakat masjidi.png",
        ),
    )

    operator fun get(mosqueId: String): MosquePhoto? = photosByMosqueId[mosqueId]
}
