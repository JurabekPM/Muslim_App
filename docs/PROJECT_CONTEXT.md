# Muslim App davom ettirish qaydi

Oxirgi yangilanish 2026-11-21. Ushbu qayd foydalanuvchining suhbatdagi qarorlari va tekshirilgan kod holati asosida tuzildi. Keyingi ish tugagach qaydni yangilash uchun ildizdagi `AGENTS.md` ko‘rsatmalarini o‘qing.

## Qanday davom ettirish kerak

Yangi Codex chatida ushbu loyiha papkasini oching va quyidagini yuboring.

> AGENTS.md va docs/PROJECT_CONTEXT.md fayllarini o‘qi. Git holatini tekshir, oxirgi saqlangan holatdan davom et. Navbatdagi vazifam — …

Oddiy ChatGPT chatida mahalliy papka avtomatik o‘qilmasligi mumkin. Bunday holatda ushbu qaydni biriktirish va loyiha fayllariga tegishli kirishni ta’minlash kerak.

Bu qayd butun suhbatning so‘zma-so‘z nusxasi emas. Kod, Git tarixi, muhim talablar va dizayn rasmlari birgalikda davom ettirish uchun saqlanadi. Chatdagi har qanday qayd etilmagan detal avtomatik tiklanishiga tayanmang.

## Papkalar va repozitoriy

- Ishchi nusxa `C:/Users/turgu/Documents/ChatGPT/Muslim App`.
- Android Studio nusxasi `C:/Users/turgu/StudioProjects/Muslim_App`. Oldingi implementatsiya ishlaridan keyin shu nusxa fast-forward orqali yangilangan. Yangi sessiyada yo‘lni va lokal o‘zgarishlarni tekshiring.
- Asosiy remote `origin` — `https://github.com/JurabekPM/Muslim_App.git`.
- Eski loyiha `upstream` — `https://github.com/JurabekDevelopment/Muslim_App.git`.
- Asosiy branch `master`. Har bir vazifa alohida `codex/…` branchida bajariladi. Git holatini ish boshida tekshiring.
- Ushbu qayd yaratilishidan oldingi oxirgi kod commiti `a1e40d77a6d29fc28cb7301a666549c8f14e10e8`, hudud tarjimalari va xatolarga chidamli qidiruv.
- Foydalanuvchi 2026-11-21 kuni GitHub uchun branch va pull request tartibini tanladi. Yakunlangan o‘zgarish branchga push qilinadi va `master`ga pull request ochiladi. Pull requestda o‘zgarish va haqiqiy tekshiruvlar qayd etiladi. Android Studio nusxasi o‘zgarish `master`ga qo‘shilgach xavfsiz fast-forward qilinadi.
- `.gradle/`, build natijalari va `$CODEX_HOME/generated_images` davom ettirish uchun yagona saqlash manbasi bo‘lmasin. Muhim maketlar `docs/designs/` ichiga ko‘chirildi.

## Mahsulot va uslub

Muslim App O‘zbekiston uchun namoz vaqtlari, tasbeh va Qibla funksiyalarini beradi. Kotlin va Jetpack Compose ishlatiladi. Foydalanuvchi ilovaning tez, yengil, aniq va qotishsiz ishlashini birinchi o‘ringa qo‘yadi.

- Ranglar navy/slate, iliq oltin/saffron, oq temada ivory fon. Amaldagi rang qiymatlarini `feature/presentation/theme` dan oling.
- AppBar glass, sahifaga singishgan. Pastki burchak radiusi olib tashlangan va fon pastga qarab yumshoq so‘nadi. Qora temada oynaning xiraligi 10 dp va ustidagi to‘q ko‘k qatlam kulrang tusni pasaytiradi.
- BottomNav ajralib turadigan oval glass shaklda. Kontent uning orqasidan ko‘rinadi. Tanlov silliq siljiydi. Qora temada to‘q ko‘k qoplama va mayinroq yaltirash och kulrang tusni pasaytiradi.
- Tugmalarda mayin ichkariga bosilish hissi bor. Indikator ustida ham bosilish sezilishi kerak.
- BottomNav istalgan bo‘limidan bosib sudraganda indikator barmoqqa ergashadi. Belgilar vaqtincha tanlangan joyga mos ajraladi. Ekran barmoq qo‘yib yuborilganda almashadi.
- Tanlangan ikonlar filled, tanlanmaganlari outlined. Almashish animatsiyali. Mavjud vector drawable tizimidan foydalaning.
- Matnlar qisqa, tushunarli, iliq va o‘qilishi oson. Holatlar va ekran sarlavhalari mayin almashadi.
- Murakkab yangi dizaynlarni avval maketda ko‘rsatish odati bor. Maket tanlanmagan bo‘lsa kodga qo‘llamang.

## Hozir amalda bor funksiyalar

### Namoz vaqtlari va kesh

- Kunlik, 7 kunlik, oylik jadvallar ishlaydi.
- Oldingi 502 javob beradigan manba almashtirilgan. `ApiService` hozir `https://namoz-vaqti.uz/index.php?format=json&lang=lotin` orqali so‘raydi.
- API’da `lotin`, `kirill`, `ru`, `en`, `kk`, `ky`, `tg`, `tk` tillari mavjud. Oldingi sessiyada har birining haqiqiy JSON javobi tekshirilgan.
- Hisoblash, kesh va rasm tanlash uchun kanonik kalitlar saqlanadi. Interfeys tarjimasi lokal resurslardan olinadi, tilni almashtirish keshni almashtirmaydi.
- DataStore orqali oxirgi yuklangan jadval ilova yopilganda ham saqlanadi. Internet bo‘lmasa saqlangan ma’lumot chiqadi. Birinchi yuklash va pastga tortib yangilash mavjud.
- Kesh va API o‘rtasidagi tanlovni `CachedPrayerScheduleRepository` boshqaradi. ViewModel ekranga jadval holatini uzatadi, xato matnlari interfeysda tarjima qilinadi. DataStore fayli, saqlash formati va hudud kalitlari saqlangan.
- Ekran almashganda keskin qayta yuklashdan qochilgan. Yuklanish uchun shimmer va internet holati uchun AppBar’da jonli qisqa xabarlar bor.
- 7 kunlik kartalar animatsiya bilan ochilib yopiladi.
- 30 kunlik kalendar va vaqt kartalari oval. Yuqori va yon chetlari kesilmasin. Boshqa sana tanlanganda ham bugungi sana bilinib tursin.

### Home va progress

- Namozga mos alohida fon rasmlari silliq almashadi. Matn uchun kontrast qoplamalari mavjud.
- `FeaturedPrayerCard.kt` minimal balandligi 150 dp.
- Progress kartaning eng pastki chetiga yopishgan, balandligi 12 dp. Kartaning radiusiga qirqiladi. To‘lgan qismning old uchi yumaloq, Sunset gradienti to‘qsariqdan och oltinga o‘tadi.
- Qolgan vaqt ixcham yoziladi. Alohida qora progress paneli olib tashlangan.
- Telegram Stars videosi asosida mayda oltin uchqunlar va old uchida yumshoq pulsatsiyali oq nur qo‘shilgan. Qolgan vaqt kamayganda harakat tezlashadi.
- Bugungi vaqtlar timeline’da ko‘rsatiladi. Joriy qatordagi vaqt vertikal markazga moslangan bo‘lishi kerak.

### Qibla

- Oldingi qotib qolishlar, sensor holati va navigatsiya xatolari ustida ishlangan.
- Strelka va Ka’ba belgisi bir yo‘nalishda bo‘lishi kerak. Yo‘nalish hisobini dizayn uchun buzib yubormang.
- Aniqlik, vaqtincha moslik va tasdiqlangan moslik farqlanadi. Kam aniqlikni noto‘g‘ri ishonchli topildi deb ko‘rsatmasin.
- Qibla topilganda nur va soya yonib turadi, yo‘nalishdan chiqilganda so‘nadi. Yoy silliq yopiladi. Kompas qotib qolmasin va strelka yashirinmasin.
- Gradus kichik yoziladi. Status kartasi ixcham va pastda. Tomonlar nomi to‘liq yoziladi.
- Kalibrlash dialogi magnitli g‘ilof, metall va havoda 8 chizish haqida tushuntiradi.
- `QiblaHeadingTest` mavjud sensor va status regressiyalarini tekshiradi.

### Hudud tanlash

- Oldingi sheet alohida `RegionSelectionScreen` ga ko‘chirilgan. AppBar’dagi location tugmasidan ochiladi, shu ekranda asosiy BottomNav yashiriladi.
- 2C maketi qo‘llangan. Tanlangan hudud slate kartada, GPS uchun oltin keng tugma bor. [2C maket](designs/location-2c.png).
- Tablar olib tashlangan. Manbadagi 14 ta asosiy hududdan boshlanadi. Viloyat ichida shahar va tumanlar, Toshkent shahri ichida uning tumanlari chiqadi.
- `RegionHierarchy.kt` barcha 223 hududni provayder tartibida qamrab oladi. Ro‘yxat internetsiz ochiladi.
- Viloyatning o‘z jadvalini tanlash uchun ichki ro‘yxatda viloyat bo‘yicha qator ham mavjud.
- GPS mos hududni tanlaydi va `Hozirgi joylashuv` belgisi chiqadi. Qo‘lda tanlanganda belgi mayin so‘nadi.
- Hudud turini chapda qo‘shimcha badge qilib ko‘rsatish foydalanuvchi tomonidan bekor qilingan. Tur nom bilan bir matnda yoziladi.
- Til tanlash BottomNav’dagi Menyu ekraniga ko‘chirilgan.

### Sakkiz til va hudud nomlari

- `AppLanguage.kt` da o‘zbek lotin, o‘zbek kirill, rus, ingliz, qozoq, qirg‘iz, tojik, turkman tillari bor.
- Tanlov `SharedPreferences` dagi `app_language` / `tag` orqali saqlanadi. `MainActivity.attachBaseContext` locale kontekstini qo‘llaydi, til almashtirishda Activity qayta yaratiladi.
- String va kalendar resurslari `values`, `values-en`, `values-ru`, `values-b+uz+Cyrl`, `values-kk`, `values-ky`, `values-tg`, `values-tk` ichida.
- Home, 7/30 kunlik, hududlar, Qibla, Tasbeh va xabarlar lokalizatsiya qilingan. Tarjimalarning barcha tillardagi tabiiyligi professional muharrir tomonidan tasdiqlanmagan.
- `RegionNames.kt` keng ishlatiladigan tarjima nomlarini saqlaydi. Qo‘qon ruscha `Коканд`, o‘zbek kirillda `Қўқон`, inglizcha `Kokand`.
- Lug‘atda alohida nom yo‘q bo‘lsa transliteratsiya ishlaydi. O‘zbek kirill imlosi provayderning 223 ta hudud ro‘yxati bilan solishtirilgan, istisnolar yozilgan.
- `RegionSearchIndex.kt` oldindan tayyorlanadigan lokal indeksda lotin/kirill, muqobil nomlar va til variantlarini qidiradi. Avval aniq, prefiks va qisman mosliklar, ular yo‘q bo‘lsa xatoga chidamli mosliklar chiqadi.
- 4–6 harfli so‘zlarda 1, uzunroq so‘zlarda 2 ta tahrirga ruxsat beriladi. Yonma-yon harf almashishi bitta xato. Juda qisqa so‘rovlarda fuzzy qidiruv cheklangan.
- Viloyat ichidagi qidiruv o‘sha hudud bilan cheklanadi. Asosiy ro‘yxatdagi qidiruv barcha hududlarni qamrab oladi.
- Saqlangan slug va namoz jadvali identifikatorlarini tarjima qilmang.

## Menyu va mavzu

BottomNav’da beshinchi `Menyu` bo‘limi bor. `MenuScreen.kt` da ixcham mavzu tanlovi, sakkiz til uchun dialog va `Ilova haqida` oynasi joylashgan. `AppThemeMode.kt` tanlovni `appearance/theme` prefida saqlaydi. Standart `Tizim` Android mavzusiga ergashadi, `Oq` va `Qora` esa ilova mavzusini darhol qo‘llaydi. Kunduz va tun manzaralari Compose Canvas’da chiziladi.

- [7 — Ixcham namunalar](designs/menu-7-compact.png). Tizim/Oq/Qora uchun uchta kichik keng namuna bir qatorda. Shu joylashuv tanlangan.
- [8 — Bitta katta namuna](designs/menu-8-preview.png). Mavzu preview paneli va pastida segmented tanlov.
- [9 — Qatorli tanlov](designs/menu-9-rows.png). Har rejim alohida keng qatorda kichik preview bilan.
- [5 — Oldingi tayanch maket](designs/menu-5-theme-previews.png). Uchta balandroq mavzu preview’si, til va About qatorlari.

2026-11-21 kuni oldingi dizayn tahlili asosida uchta maket tayyorlandi. Foydalanuvchi mavjud ko‘k-oltin palitra va glass navigatsiya bilan Home’dagi Sunset progressni hamda Menyu uchun kunduz va tun namunalarini tanladi.

- [Asosiy ekran](designs/home-day-night.png). Oq va qora tema, Sunset progress, oltin bilan ajratilgan joriy namoz va eslatma belgilarining taklif etilgan joylashuvi.
- [Menyu va mavzu](designs/menu-day-night.png). «Tizim / Oq / Qora» uchun kunduz va tun manzaralari, til, eslatmalar va ilova haqida qatorlari.
- [Qibla va Ramazon](designs/qibla-ramadan-concept.png). Ka’ba belgili kompas va saharlik, iftor hamda qolgan vaqt uchun Ramazon konsepti.

Maketdagi namoz vaqtlari, progress va sanoq vizual namunalardir. Eslatmalar, Hijriy sana va Ramazon funksiyalari hozircha konsept.

Maketlar konsept rasmlaridir. Imagegen’dagi matn, tanlov holati yoki ayrim masofalar implementatsiyadagi qat’iy talab sifatida qabul qilinmasin. Besh bo‘limli nav uchun barcha sakkiz tilda uzun matn va katta font scale holatini hisobga olish kerak. Mavjud press/drag/filled-outline animatsiyalar saqlanishi talab etilgan.

## Tasbix maketlari

Tasbix uchun oq va qora temada uchta maket tayyorlandi. Ularda keng «Zikr +1» tugmasi, «33 / 99 / Erkin» tanlovi, «−1» va «Qayta boshlash» ko‘rsatilgan. «Subhanalloh» va 18/33 sanoq namuna sifatida ishlatilgan.

- [1 — Oltin halqa](designs/tasbix-gold-ring.png). Katta sanoq atrofida uzluksiz progress halqasi.
- [2 — Raqamli hisoblagich](designs/tasbix-digital-counter.png). Raqamlar yumaloq kartada, progress gorizontal chiziqda.
- [3 — Tasbeh donalari](designs/tasbix-bead-counter.png). Sanoq atrofidagi donalar ketma-ket oltin rangga o‘tadi. Maketdagi donalar soni tasviriy namuna.

Foydalanuvchi uchala maketdagi alohida katta «Zikr +1» tugmasi va bosish joyini noqulay deb topdi. [Bir qo‘lda sanash maketi](designs/tasbix-one-handed.png) oq va qora temada tayyorlandi. Katta sanoq va progress pastdagi keng kartada. Karta bo‘ylab bosish sanoqni oshiradi. «−1» va «Qayta boshlash» kartadan yuqorida. Keyin foydalanuvchi amaldagi joylashuvni saqlashni tanladi.

Foydalanuvchi amaldagi Tasbix joylashuvini afzal ko‘rdi. [Yangilangan maket](designs/tasbix-current-refinement.png) katta sanoqni tepada, bosiladigan ko‘pburchakni pastda saqlaydi. Uning tepasida bir xil uslubdagi «Tarix», «−1» va «Nollash» ikonkalari bor. Shu joylashuv ilovaga qo‘llandi. Qisqa bosishda ko‘pburchak doiraga yetib, silliq qaytadi. Uzoq bosishda doira barmoq qo‘yib yuborilguncha qoladi. Har yakunlangan bosish sanoqni bir marta oshiradi va qurilma sozlamalari ruxsat bersa yengil vibratsiya beradi. «−1» sanoqni bittaga kamaytiradi. Nollash tasdiqlanganda yakunlangan sanoq va vaqt DataStore tarixiga yoziladi. Oxirgi 20 ta yakunlangan sanoq saqlanadi. Eski sanoq uchun avvalgi DataStore kaliti ishlatiladi.

Debug APK va 23 ta unit test muvaffaqiyatli yakunlandi. Samsung SM_S916N’da alohida test DataStore bilan avvalgi sanoqni o‘qish, qisqa va uzoq bosishda bitta qo‘shish, −1, nollashni bekor qilish va tasdiqlash, nolga tushganda −1 bloklanishi hamda tarixda to‘g‘ri qiymat chiqishi sinovdan o‘tdi. Morf animatsiyasi kadrma-kadr o‘lchanmagan.

Tasbixning keyingi sayqalida Tarix, −1 va Nollash qatori biroz yuqoriga surildi. Shakl markazidagi yozuvga tasbeh belgisi qo‘yildi. Ekran o‘quvchisi uchun mahalliy «Zikr» nomi saqlandi. Yon va pastdagi bo‘sh maydonlar sanoqni oshiradi, amallar qatori mustaqil ishlaydi. Sanoq bir xil enli raqamlar bilan terildi, yordamchi matnlar ilova tipografiyasiga moslandi. Oshganda raqamlar yuqoriga, kamayganda pastga aylanadi. Tasdiqlangan nollashda razryadlar orqaga aylanib nolga keladi.

Debug build, 26 ta unit test va Samsung SM_S916N’da ikkita Tasbix instrumentatsiya testi o‘tdi. Shakl yonidagi bosish, tasdiqlash va tarix, shuningdek 124 dan 0 ga qaytishdagi oraliq kadr tekshirildi.

Tasbix raqamlari uchun animatsiya maydoni ekran eniga kengaytirildi va yuqori-pastki chetlarda yumshoq so‘nish qo‘shildi. Tasdiqlangan nollash 1050 ms davom etadi. Oddiy plus va minus 200 ms. Debug build va 26 ta unit test o‘tdi. Yangilangan APK Samsung SM_S916N’ga o‘rnatildi va ikkita Tasbix testi o‘tdi. 124 dan 0 ga qaytishda o‘rta va kechgi kadrlarning chetlari tekshirildi.

Nollashda chapdagi raqamlar pastga sirg‘alib so‘nadi, birlik razryadi aylanib markazda 0 bo‘lib to‘xtaydi. Davomiylik 1050 ms. Debug build, 26 ta unit test va Samsung SM_S916N’da ikkita Tasbix testi o‘tdi. 124 → 0 kadrlarida chapdagi raqamlarning pastga siljishi va so‘nishi hamda yakuniy nolning markazda turishi tekshirildi.

Nollash juda tez nolga o‘tgandek ko‘ringani uchun animatsiya 1550 ms ga uzaytirildi. Avval hamma razryad orqaga aylanadi. Chap raqamlar animatsiyaning 58 foizidan keyin pastga sirg‘alib so‘na boshlaydi va birlik noli markazga siljiydi. Debug build, 26 ta unit test va Samsung SM_S916N’da ikkita Tasbix testi o‘tdi. 124 → 0 da dastlabki, o‘rta, so‘nish va yakuniy kadrlar solishtirildi.

Nollanish animatsiyasini ko‘rish uchun debug versiyada saqlangan sanoq bo‘lmasa vaqtincha 1234 ko‘rsatiladi. Avvaldan saqlangan qiymat ustun. Tasdiqlangan nollash DataStore’ga 0 yozadi. Release uchun boshlang‘ich qiymat 0. Debug build va unit testlar o‘tdi. Samsung SM_S916N’da bo‘sh test DataStore uchun 1234, saqlangan qiymatning ustunligi va nollashdan keyin 0 yozilishi tasdiqlandi.

Debug versiyada katta Tasbix tugmasini uzoq bosish sinov uchun joriy sanoqni 1234 ga qo‘yadi. Bu amal tarixga yozuv qo‘shmaydi. Qisqa bosish sanoqni bittaga oshiradi. Release versiyada sinov amali faol emas. Debug build va unit testlar o‘tdi. Samsung SM_S916N’da uzoq bosish bilan 1234 ga o‘tish, keyingi qisqa bosishda 1235 chiqishi va tarix saqlanishi tekshirildi.

1234 → 0 nollanish videosida bir nechta yakuniy nol yonma-yon ko‘ringani aniqlandi. Endi chapdagi yakuniy nollar razryadga kirayotganda pastga tushib so‘nadi. Birlik noli markazda qoladi. Debug build va unit testlar o‘tdi. Samsung SM_S916N’da ikkita Tasbix testi o‘tdi, jumladan 1234 → 0 animatsiyasining yakuniga yaqin kadrda chap nollar qolmasligi tekshirildi.

Sinov uchun 1234 boshlang‘ich qiymati va uzoq bosish orqali 1234 qo‘yish amali olib tashlandi. Yangi sanoq 0 dan boshlanadi. Mavjud sanoq va tarix saqlanadi. 1234 → 0 faqat animatsiya regressiya testidagi namuna. Nollanish animatsiyasi saqlandi. Debug build va unit testlar o‘tdi. Samsung SM_S916N’ga APK o‘rnatildi. Telefonda boshqa ilova faol bo‘lgani uchun yakuniy instrumentatsiya testi o‘tkazilmadi.

Tasbix tugmasida soya ko‘pburchak va doira konturiga ergashadi. 16 dp yoyilish va 8 dp pastga siljish bor. Soya sanoqqa bog‘liq emas. Bosish animatsiyasi tugma chetida mayin oltin yaltirash beradi va qo‘yib yuborilganda so‘nadi. Debug build va unit testlar o‘tdi. Samsung SM_S916N’dagi birinchi sinov tugma foni oltin yaltirashni yopayotganini ko‘rsatdi. Chizish tartibi tuzatildi. Yangilangan APK o‘rnatildi va oq hamda qora mavzuda soyaning pastga yo‘nalishi, bosilgandagi oltin yaltirash, qo‘yib yuborilgandagi so‘nish va sanoq tekshirildi. Ikkita Tasbix instrumentatsiya testi o‘tdi.

Oq mavzuda bosilgandagi oltin yaltirashning eng yuqori opacity qiymati 0.48 dan 0.62 ga ko‘tarildi. Qora mavzuda 0.48 saqlandi. Debug build va unit testlar o‘tdi. Samsung SM_S916N’da oq va qora mavzuda soya, bosilgandagi yaltirash va qo‘yib yuborgandagi so‘nish qayta tekshirildi. Soyani yaqin ingichka va uzoq yumshoq qatlamlardan qilish hamda bosishda ularni ixchamlashtirish taklif qilindi.

Tasbix soyasining ikki qatlami qo‘llandi. Yaqin soya 6 dp, keng soya 18 dp yoyiladi. Bosilganda ikkalasi konturga yaqinlashadi, keng qatlam yengilroq bo‘ladi. Oltin yaltirashning oq va qora mavzudagi sozlamalari saqlandi. Sanoq maydoni balandligi 24 dp kengaytirildi. Raqamlar tepa va pastda yumshoq so‘nadi hamda chizish maydonidan to‘liq chiqib ketadi. Debug build, unit testlar va Samsung SM_S916N’da uchta Tasbix testi o‘tdi. Oq va qora mavzuda soya ixchamlashishi, oltin yaltirash va raqamlarning ikki yo‘nalishdagi oraliq kadrlari tekshirildi.

Tasbixda +1 va −1 raqam aylanishi oxirgi ikki bosish oralig‘iga mos ravishda 64–340 ms davom etadi. Birinchi bosish va uzoq pauzadan keyingi bosish 340 ms. Nollash avvalgi animatsiyani ishlatadi. Sanoqning tepa va pastki so‘nish maydoniga yana 24 dp qo‘shildi, gradient chuqurligi 30 dp dan 42 dp ga oshirildi. Debug build, 29 ta unit test va Samsung SM_S916N’da uchta Tasbix testi o‘tdi. Ketma-ket oltita +1 va oltita −1 saqlanishi hamda ikkala mavzuda yuqoriga va pastga harakat kadrlari tekshirildi.

Tasbixning tez aylanishida joriy va keyingi raqam bitta razryadda birga yorqin turmasligi uchun ularning shaffofligi navbat bilan pasayadi. Tepa-pastki gradient saqlandi. +1 va −1 animatsiyalari 80–460 ms oralig‘ida, yakka bosish 460 ms. Sanoq ostida doimiy markazlangan «Zikrni boshlang» yoki «Zikr davom etmoqda» yozuvi chiqadi. Sakkiz til resursi yangilandi. Debug build, unit testlar va Samsung SM_S916N’da uchta Tasbix testi o‘tdi. Tez +1/−1, oq va qora mavzudagi raqam kadrlari hamda ikkala yozuv holati tekshirildi.

### Tasbix dialoglari maketlari

«Zikr tarixi» va «Hisobni nollash» dialoglari uchun ImageGen orqali oq va qora mavzuda uchta maket tayyorlandi.

- [1 — Ixcham](designs/tasbix-dialogs-1-compact.png). Tarix bitta ixcham ro‘yxatda, joriy sanoq bir qatorda.
- [2 — Kartali](designs/tasbix-dialogs-2-cards.png). Tarix kartalarda, joriy sanoq kattaroq panelda.
- [3 — Keng tugmalar](designs/tasbix-dialogs-3-stacked.png). Tarix vaqt chizig‘ida, nollash tugmalari vertikal joylashgan.

Yana uchta maket oq va qora mavzuda tayyorlandi.

- [4 — 143 → 0](designs/tasbix-dialogs-4-reset-preview.png). Tarix sana, vaqt va sanoq bilan tartiblangan. Nollashda natija oldindan ko‘rsatiladi.
- [5 — Bosh barmoqqa yaqin](designs/tasbix-dialogs-5-bottom-sheet.png). Tarix pastdan ochiladi, nollash tasdig‘i pastda keng tugmalar bilan chiqadi.
- [6 — Sokina](designs/tasbix-dialogs-6-quiet.png). Yengil ajratkichli tarix va ixcham nollash dialogi.

Foydalanuvchi Tarix uchun 3-maketdagi vaqt chizig‘i bilan pastdan ochiladigan sheetni, Nollash uchun 1-maketdagi ixcham dialogni tanladi. Tarixda uzun ro‘yxat LazyColumn ichida aylanadi, «Yopish» tugmasi pastda ko‘rinib turadi. Mavjud saqlash chegarasi 20 yozuv. Nollash joriy sanoqni ko‘rsatadi, bekor qilish va tasdiqlash tugmalari ajratilgan. Sanoq tarixga faqat tasdiqdan so‘ng qo‘shiladi. «Joriy sanoq» yorlig‘i sakkiz tilda bor. Debug build va unit testlar o‘tdi. Samsung SM_S916N’ga yangilanish o‘rnatildi. Boshqa ilova faol bo‘lgani uchun 100 yozuv bilan varaqlash va dialoglarning telefon sinovi hali bajarilmadi.

## Muhim kod yo‘llari

Asosiy prefiks `app/src/main/java/uz/turgunboyevjurabek/muslimapp/`.

- `MainActivity.kt` va `feature/presentation/navigation/MuslimApp.kt` kirish va umumiy scaffold.
- `feature/presentation/navigation/AppRoute.kt` barqaror navigatsiya yo‘llari.
- `feature/presentation/ui/components/MyTopBar.kt`, `MyBottomNavigationBar.kt` umumiy glass elementlar.
- `feature/presentation/ui/components/FeaturedPrayerCard.kt`, `PrayerTimeline.kt` Home kartasi va vaqt chizig‘i.
- `feature/presentation/ui/screens/RegionSelectionScreen.kt` hudud tanlash. `MenuScreen.kt` mavzu, til dialogi va ilova haqida oynasi.
- `feature/domain/model/Regions.kt`, `RegionHierarchy.kt`, `RegionNames.kt`, `RegionSearchIndex.kt`, `RegionAddressMatcher.kt` hudud katalogi va mosliklar.
- `feature/presentation/ui/components/LocalizedScheduleText.kt`, `ScheduleRegion.kt` tarjima qilingan jadval matnlari.
- `feature/presentation/state/PrayerScheduleViewModel.kt` jadvalni ekran bilan bog‘laydi.
- `feature/data/repository/CachedPrayerScheduleRepository.kt` va `ScheduleCache.kt` kesh, API so‘rovi va saqlash qoidalari.
- `feature/data/location` sensorlar va GPS.
- `app/src/test` mavjud JUnit testlar.

## Rivojlantirish yo‘li

[Arxitektura auditi bo‘yicha vaqtinchalik reja](ROADMAP.md) faqat 2026-11-21 kungi tavsiyalar uchun PR’lar tartibini belgilaydi. Avval Android CI, so‘ng kesh, domen modellari, Tasbix va hudud sozlamalari, kutubxonalar hamda Play Store tayyorgarligi. PR’lar masterga tasdiqdan keyin qo‘shiladi. 1-bosqichda GitHub Actions masterga ochilgan PR va masterga push bo‘lganda debug build hamda unit testlarni ishga tushiradi. Mahalliy debug build va 30 ta unit test o‘tdi. GitHub Actions tekshiruvi o‘tdi va PR #2 masterga qo‘shildi.

2-bosqichda kunlik, haftalik va oylik jadvallar DataStore qayta ochilgach yangi repository orqali internetsiz o‘qilishi sinovdan o‘tdi. Mos davr bo‘lmasa so‘nggi jadval ko‘rsatiladi. Qo‘lda yangilashdagi HTTP 502 va tarmoq xatolarida saqlangan jadval qoladi. Mahalliy debug build va 30 ta unit test o‘tdi. Samsung’da shu bosqich uchun amaliy sinov o‘tkazilmagan. 2-bosqich PR’i tasdiq kutilmoqda.

## Build va qurilmada tekshirish

AppBar soyasining pastki o‘tishi 68 dp gacha kengaytirildi. Debug build va unit testlar o‘tdi. Yangilangan APK Samsung SM_S916N’da o‘rnatilib, oq temada 7 kunlik ro‘yxat AppBar ostidan surilganda o‘tish tekshirildi.

Qora AppBar va BottomNav ranglari yangilangach debug build va unit testlar o‘tdi. 2026-11-21 kuni yangilangan APK Samsung SM_S916N’ga o‘rnatildi va Asosiy ekran qora temada oldingi skrinshot bilan solishtirildi. Fon nuqtalarida AppBar `#383F47` dan `#182028` ga, BottomNav `#646E77` dan `#293640` ga o‘zgargani tasdiqlandi.

Menyu va Sunset yangilanishida debug build va unit testlar o‘tdi. Samsung’da oq va qora Menyu ko‘rinishlari, qora mavzuning ilova qayta ochilganda saqlanishi, Tizim tanlovining belgilanishi va saqlanishi, til dialogi hamda «Ilova haqida» oynasi tekshirildi. Foydalanuvchi qolgan telefon tekshiruvlarini o‘zi bajarishini aytdi.

Joriy versiyalarni Gradle fayllaridan tekshiring. Oxirgi holatda compileSdk 37, minSdk 24, targetSdk 34. Compose BOM 2026.09.00, Haze va haze-glass 2.1.0. Android Studio JBR bilan build ishlatilgan.

PowerShell namunasi, kompyuterdagi yo‘llar mavjudligini avval tekshiring.

```powershell
$env:JAVA_HOME = 'C:/Program Files/Android/Android Studio/jbr'
$env:ANDROID_HOME = "$env:LOCALAPPDATA/Android/Sdk"
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest --offline --quiet --max-workers=3
```

Gradle va SDK keshi workspace tashqarisida. Sandbox muhiti sabab ularga kirish yoki ADB uchun eskalatsiya kerak bo‘lishi mumkin. `--offline` faqat dependency’lar mavjud bo‘lsa ishlaydi. APK `app/build/outputs/apk/debug/app-debug.apk` ichiga yig‘iladi.

Foydalanuvchi fizik Samsung SM_S916N’da tekshirishni afzal ko‘radi va oldin bunga ruxsat bergan. Wi-Fi ADB seriali o‘zgarishi mumkin. `adb devices -l` bilan tekshiring, boshqa ilovadagi ishiga xalal bermaslikka harakat qiling. Qulf yoki uzilgan qurilma bo‘lsa tekshirilmagan qismni aniq yozing.

Oxirgi kod ishida build va unit testlar o‘tgan. Samsung’da `Koknad` so‘rovi rus tilida `город Коканд`, o‘zbek kirillda `Қўқон шаҳри` topgan. Sinovdan keyin telefon o‘zbek lotin tiliga qaytarilgan. Avvalgi sessiyada sakkiz til menyusi, yangi besh tilda Home va oylik kalendar, ingliz tilining restartdan keyin saqlanishi, ruscha namoz va Qibla tekshirilgan. Bu eski tekshiruvlar keyingi o‘zgarishlarni avtomatik tasdiqlamaydi.

## Qaydni yangilash

Har ish tugagach quyidagilarni dolzarb tuting.

1. Qaysi talab bajarildi va foydalanuvchi qaysi maketni tanladi.
2. Hali kutilayotgan qaror yoki qolgan texnik muammo.
3. Haqiqiy build, test va telefon tekshiruvi natijasi.
4. Muhim rasm yoki faylning repozitoriy ichidagi yo‘li.

Kod va ushbu qaydni GitHub’ga birga yuklash qaydni boshqa kompyuterda ham olish imkonini beradi. Noutbukdagi papkani o‘chirish yoki diskni yo‘qotishdan himoya uchun remote’ga push qilinganini alohida tekshiring.
