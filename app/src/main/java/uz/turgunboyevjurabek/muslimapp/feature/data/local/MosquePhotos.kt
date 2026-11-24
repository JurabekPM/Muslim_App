package uz.turgunboyevjurabek.muslimapp.feature.data.local

import android.net.Uri
import androidx.annotation.DrawableRes
import uz.turgunboyevjurabek.muslimapp.R

internal enum class MosquePhotoLicense(val title: String, val url: String) {
    Cc0("CC0 1.0", "https://creativecommons.org/publicdomain/zero/1.0/"),
    By20("CC BY 2.0", "https://creativecommons.org/licenses/by/2.0/"),
    By30("CC BY 3.0", "https://creativecommons.org/licenses/by/3.0/"),
    By40("CC BY 4.0", "https://creativecommons.org/licenses/by/4.0/"),
    BySa30("CC BY-SA 3.0", "https://creativecommons.org/licenses/by-sa/3.0/"),
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
    private val extraPhotosByMosqueId = mapOf(
        "relation/21151698" to listOf(
            MosquePhoto(
                R.drawable.mosque_photo_relation_21151698_2,
                author = "MarjonaKhidirova",
                fileName = "Xonaqoh masjidi — Oʻzbekiston, Fargʻona viloyati, Margʻilon 1.jpg",
            ),
            MosquePhoto(
                R.drawable.mosque_photo_relation_21151698_3,
                author = "MarjonaKhidirova",
                fileName = "Xonaqoh masjidi — Oʻzbekiston, Fargʻona viloyati, Margʻilon 4.jpg",
            ),
        ),
        "relation/4827007" to listOf(
            MosquePhoto(
                R.drawable.mosque_photo_relation_4827007_2,
                author = "Humoyun Mehridinov",
                fileName = "Minor masjidi 10.jpg",
            ),
            MosquePhoto(
                R.drawable.mosque_photo_relation_4827007_3,
                author = "Humoyun Mehridinov",
                fileName = "Minor masjidi 14.jpg",
            ),
            MosquePhoto(
                R.drawable.mosque_photo_relation_4827007_4,
                author = "Umarxon III",
                fileName = "Anhor water canal and Minor Mosque night view.jpg",
            ),
        ),
        "way/1093072077" to listOf(
            MosquePhoto(
                R.drawable.mosque_photo_way_1093072077_2,
                author = "Iaa2023",
                fileName = "Kokildor ota xonaqosi 02.jpg",
            ),
        ),
        "way/144057369" to listOf(
            MosquePhoto(
                R.drawable.mosque_photo_way_144057369_2,
                author = "Jamshid Nurkulov",
                fileName = "Ukkosha Mosque 01.jpg",
            ),
            MosquePhoto(
                R.drawable.mosque_photo_way_144057369_3,
                author = "Jamshid Nurkulov",
                fileName = "Ukkosha Mosque 02.jpg",
            ),
        ),
        "way/174718685" to listOf(
            MosquePhoto(
                R.drawable.mosque_photo_way_174718685_2,
                author = "Dmitriy Tashkent",
                fileName = "Qoʻqon jome masjidi.jpg",
            ),
            MosquePhoto(
                R.drawable.mosque_photo_way_174718685_3,
                author = "Nikolai Bulykin",
                fileName = "Кокандская городская мечеть (1).jpg",
            ),
            MosquePhoto(
                R.drawable.mosque_photo_way_174718685_4,
                author = "Kraftabbas",
                fileName = "Коканд.Мечеть Джами 1.jpg",
            ),
        ),
        "way/185230691" to listOf(
            MosquePhoto(
                R.drawable.mosque_photo_way_185230691_2,
                author = "Panpanchik",
                fileName = "Oq masjid (Xorazm).jpg",
            ),
            MosquePhoto(
                R.drawable.mosque_photo_way_185230691_3,
                author = "Panpanchik",
                fileName = "Oq masjid, gumbazi.jpg",
            ),
        ),
        "way/449151719" to listOf(
            MosquePhoto(
                R.drawable.mosque_photo_way_449151719_2,
                author = "Arina Pan",
                fileName = "Медресе Саид-Ахмад ходжа.jpg",
            ),
            MosquePhoto(
                R.drawable.mosque_photo_way_449151719_3,
                author = "Arina Pan",
                fileName = "Медресе Саид-Ахмад ходжа 2.jpg",
            ),
        ),
        "way/451808369" to listOf(
            MosquePhoto(
                R.drawable.mosque_photo_way_451808369_2,
                author = "Mitti quyoshcha",
                fileName = "Gʻishtli masjid binosi.jpg",
            ),
            MosquePhoto(
                R.drawable.mosque_photo_way_451808369_3,
                author = "Nikolai Bulykin",
                fileName = "Коканд, мечеть Гиштлик, потолок (1).jpg",
            ),
        ),
        "way/452168942" to listOf(
            MosquePhoto(
                R.drawable.mosque_photo_way_452168942_2,
                author = "Petar Milošević",
                fileName = "Minarets and Dome of Devonaboy Jome Mosque, Andijan, Uzbekistan (2025).jpg",
            ),
            MosquePhoto(
                R.drawable.mosque_photo_way_452168942_3,
                author = "Petar Milošević",
                fileName = "Iwan of Devonaboy Jome Mosque, Andijan, Uzbekistan (2025).jpg",
            ),
        ),
        "way/614044909" to listOf(
            MosquePhoto(
                R.drawable.mosque_photo_way_614044909_2,
                author = "BeshevI",
                fileName = "Hazrat Khizr Mosque 909.jpg",
            ),
            MosquePhoto(
                R.drawable.mosque_photo_way_614044909_3,
                author = "BeshevI",
                fileName = "Hazrat Khizr Mosque 913.jpg",
            ),
        ),
    )

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
        "node/7139516193" to MosquePhoto(
            R.drawable.mosque_photo_node_7139516193,
            author = "Bobyrr",
            fileName = "Mosque Khoja Ahrar 4740.JPG",
        ),
        "relation/13911901" to MosquePhoto(
            R.drawable.mosque_photo_relation_13911901,
            author = "Gregor Rom",
            fileName = "Komplex Said Scheliker Bei.jpg",
        ),
        "relation/1553869" to MosquePhoto(
            R.drawable.mosque_photo_relation_1553869,
            author = "Petar Milošević",
            fileName = "Медресе Баракхан,Ташкент (Barakhan Madrasah, Baroqxon madrasasi).jpg",
        ),
        "relation/19934455" to MosquePhoto(
            R.drawable.mosque_photo_relation_19934455,
            author = "Muxriddin Azimov",
            fileName = "Hazrati Imom masjidi 20230421.jpg",
            license = MosquePhotoLicense.Cc0,
        ),
        "relation/21151698" to MosquePhoto(
            R.drawable.mosque_photo_relation_21151698,
            author = "MarjonaKhidirova",
            fileName = "Xonaqoh masjidi — Oʻzbekiston, Fargʻona viloyati, Margʻilon 2.jpg",
        ),
        "relation/2324986" to MosquePhoto(
            R.drawable.mosque_photo_relation_2324986,
            author = "Ymblanter",
            fileName = "Kukeldash Madrassah side view.jpg",
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
        "way/1093072077" to MosquePhoto(
            R.drawable.mosque_photo_way_1093072077,
            author = "Iaa2023",
            fileName = "Kokildor ota xonaqosi 01.jpg",
        ),
        "way/1158817582" to MosquePhoto(
            R.drawable.mosque_photo_way_1158817582,
            author = "Mitti quyoshcha",
            fileName = "Qoʻqon hadya hoji jome masjidi.jpg",
            license = MosquePhotoLicense.Cc0,
        ),
        "way/1265442637" to MosquePhoto(
            R.drawable.mosque_photo_way_1265442637,
            author = "Wikisurgery94",
            fileName = "Old tomoni ko'rinishi.jpg",
            license = MosquePhotoLicense.Cc0,
        ),
        "way/141617154" to MosquePhoto(
            R.drawable.mosque_photo_way_141617154,
            author = "St. Christofor",
            fileName = "Muyi Muborak Madrasah.jpg",
        ),
        "way/141617158" to MosquePhoto(
            R.drawable.mosque_photo_way_141617158,
            author = "Jamshid Nurkulov",
            fileName = "Tillashayx masjidining old tomondan koʻrinishi.jpg",
        ),
        "way/141624570" to MosquePhoto(
            R.drawable.mosque_photo_way_141624570,
            author = "Ymblanter",
            fileName = "Kaffal Shashi Mausoleum rear view.jpg",
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
        "way/185037012" to MosquePhoto(
            R.drawable.mosque_photo_way_185037012,
            author = "Carpodacus",
            fileName = "Bogbonli mosque.jpg",
        ),
        "way/185051424" to MosquePhoto(
            R.drawable.mosque_photo_way_185051424,
            author = "Bgag",
            fileName = "Juma Mosque (Khiva) 01.jpg",
            license = MosquePhotoLicense.Cc0,
        ),
        "way/185230691" to MosquePhoto(
            R.drawable.mosque_photo_way_185230691,
            author = "Iaa2023",
            fileName = "Oq masjid 01.jpg",
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
        "way/351273147" to MosquePhoto(
            R.drawable.mosque_photo_way_351273147,
            author = "4pf3154f7",
            fileName = "Mausoleum Termez.jpg",
        ),
        "way/448234922" to MosquePhoto(
            R.drawable.mosque_photo_way_448234922,
            author = "Lechhansl",
            fileName = "Sultan Saodat Komplex Seit.JPG",
            license = MosquePhotoLicense.BySa30,
        ),
        "way/449151719" to MosquePhoto(
            R.drawable.mosque_photo_way_449151719,
            author = "Arina Pan",
            fileName = "Двор Медресе Саид-Ахмад ходжа.jpg",
        ),
        "way/451808369" to MosquePhoto(
            R.drawable.mosque_photo_way_451808369,
            author = "Nikolai Bulykin",
            fileName = "Коканд, мечеть Гиштлик.jpg",
        ),
        "way/452168942" to MosquePhoto(
            R.drawable.mosque_photo_way_452168942,
            author = "Petar Milošević",
            fileName = "Devonaboy Jome Mosque, Andijan, Uzbekistan.jpg",
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
        "way/614044909" to MosquePhoto(
            R.drawable.mosque_photo_way_614044909,
            author = "Akhemen",
            fileName = "Hazrati Hizr Mosque in Samarkand (August 2018).jpg",
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
    ).mapValues { (mosqueId, cover) -> listOf(cover) + extraPhotosByMosqueId[mosqueId].orEmpty() }

    operator fun get(mosqueId: String): List<MosquePhoto> = photosByMosqueId[mosqueId].orEmpty()
}
