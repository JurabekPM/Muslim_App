# Muslim App uchun Play Console Data safety loyihasi

Android relizi `uz.turgunboyevjurabek.muslimapp`, targetSdk 36. Quyidagi javoblar kod va e’lon qilingan xizmat hujjatlariga tayangan. Tasdiq talab qiladigan javoblarni yuborishdan oldin yakunlang.

| Play Console maydoni | Loyiha javobi va asosi |
| --- | --- |
| Does your app collect or share any of the required user data types? | **Yes.** Namoz jadvali API so‘rovida joriy hududdan aniqlangan yoki qo‘lda tanlangan hudud identifikatori tashqi xizmatga yuboriladi. |
| Location → Approximate location | **Collected**, maqsadi **App functionality**. GPS orqali joylashuvni aniqlash ixtiyoriy. Jonli jadval uchun hudud parametri majburiy va qo‘lda tanlangan hudud ham yuboriladi. Shu sababli jadval so‘rovi oqimi uchun **Required**. |
| Approximate location → Shared | **Yes** deb belgilash taklifi. Jadval provayderi mustaqil xizmat. Google Play’dagi istisnoga asos bo‘ladigan kelishuv yoki boshqa dalil tasdiqlansa, javobni qayta baholang. |
| Approximate location → Processed ephemerally | **No** deb belgilash taklifi. API so‘rovi va IP jurnallarining mazmuni hamda saqlash muddati tasdiqlanmagan. |
| Location → Precise location | **Tasdiqlash kerak.** Qibla hisobi telefonda, jadval API’siga koordinata uzatilmaydi. Joriy hududni izlash koordinatani Android Geocoder’ga beradi. Qurilmadagi xizmat koordinatani masofada qayta ishlasa va bu Play deklaratsiyasi doirasiga kirsa, **Collected**, **Optional**, **App functionality** sifatida kiriting. Shared va ephemeral javoblarini xizmatning amaldagi ma’lumot oqimiga moslang. |
| Is all of the user data collected by your app encrypted in transit? | Jadval so‘rovi HTTPS orqali. Umumiy **Yes** javobidan oldin deklaratsiyaga kiradigan Android geokodlash va SDK uzatmalari hamda yo‘naltirishlar tasdiqlanishi kerak. |
| Do you provide a way for users to request that their data is deleted? | Ilovadagi mahalliy ma’lumot o‘chiriladi. Tashqi xizmatdagi ma’lumotni o‘chirishni so‘rashning tasdiqlangan yo‘li yo‘q. Mahalliy o‘chirishni serverdagi o‘chirish deb ko‘rsatmang. |
| Account creation and deletion | Ilovada hisob yaratish yoki kirish funksiyasi yo‘q. |

## Audit qaydlari

- `ApiService` xizmatga hudud va davrni yuboradi. `NetworkModule` HTTPS manzilidan foydalanadi. IP, texnik sarlavhalar va so‘rov jurnallari qanday ishlatilishini provayder bilan tasdiqlang. IP turini uning amaldagi ishlatilishiga qarab baholang. Provayderning saytdagi siyosati texnik yozuvlar vaqtincha qayd etilishi mumkinligini aytadi, API logining aniq maydonlari va muddatini bermaydi. [API hujjati](https://namoz-vaqti.uz/?format=docs&lang=en) · [Provayder siyosati](https://namoz-vaqti.uz/policy.php?lang=en).
- `RegionLocationLookup` Android Geocoder orqali manzil izlaydi. `MyLocationManager` hudud tanlash va Qibla uchun `play-services-location:21.4.0` dan foydalanadi. Google’ning base, basement va tasks bo‘yicha bayonoti Location SDK’ni qamrab olmaydi. [Android Geocoder](https://developer.android.com/reference/android/location/Geocoder) · [Google Play services ma’lumot bayonoti](https://developers.google.com/android/guides/play-data-disclosure).
- Ilova kodida reklama, analitika, hisob yoki Tasbix tarixini ilova serveriga yuborish oqimi yo‘q. Tasbix, namoz keshi va sozlamalar mahalliy saqlanadi. Tizim zaxiralashi yoqilgan. Yakuniy javobda Android boshqaradigan zaxiralash va har qanday uchinchi tomon oqimini Google Play ta’riflariga alohida solishtiring.
- Joylashuvdan foydalanish ilovaning tegishli funksiyalari bilan cheklangan. Geocoder tashqi uzatmasi tasdiqlansa, uning foydalanuvchiga ko‘rinadigan izohi va rozilik oqimini Play talabiga solishtiring.
- Menyuda maxfiylik matni sakkiz tilda ochiladi. Play Console’dagi Privacy policy maydoni uchun tasdiqlangan siyosatning ommaviy HTTPS manzili va maxfiylik bo‘yicha aloqa usuli kerak. Siyosat loyihasi [PRIVACY_POLICY.md](PRIVACY_POLICY.md) faylida.

[Google Play Data safety yo‘riqnomasi](https://support.google.com/googleplay/android-developer/answer/10787469) · [Google Play User Data siyosati](https://support.google.com/googleplay/android-developer/answer/10144311)
