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
- Joriy ishlab kelinayotgan branch `master`. Boshqa branchga o‘tilgan bo‘lsa Git holatini asos oling.
- Ushbu qayd yaratilishidan oldingi oxirgi kod commiti `a1e40d77a6d29fc28cb7301a666549c8f14e10e8`, hudud tarjimalari va xatolarga chidamli qidiruv.
- Foydalanuvchi GitHub’ga ish yakunida push qilib borishni, repozitoriyni public qilishni so‘ragan. Yangi sessiyada mavjud ruxsatlar va remote holatini tekshiring.
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

Foydalanuvchi uchala maketdagi alohida katta «Zikr +1» tugmasi va bosish joyini noqulay deb topdi. [Bir qo‘lda sanash maketi](designs/tasbix-one-handed.png) oq va qora temada tayyorlandi. Katta sanoq va progress pastdagi keng kartada. Karta bo‘ylab bosish sanoqni oshiradi. «−1» va «Qayta boshlash» kartadan yuqorida. Maket bo‘yicha fikr kutilmoqda.

Foydalanuvchi amaldagi Tasbix joylashuvini afzal ko‘rdi. [Yangilangan maket](designs/tasbix-current-refinement.png) katta sanoqni tepada, bosiladigan ko‘pburchakni pastda saqlaydi. Uning tepasida bir xil uslubdagi «Tarix», «−1» va «Nollash» ikonkalari bor. Bu maket muhokama uchun tayyorlandi.

## Muhim kod yo‘llari

Asosiy prefiks `app/src/main/java/uz/turgunboyevjurabek/muslimapp/`.

- `MainActivity.kt` va `feature/presentation/navigation/MuslimApp.kt` kirish va umumiy scaffold.
- `feature/presentation/navigation/AppRoute.kt` barqaror navigatsiya yo‘llari.
- `feature/presentation/ui/components/MyTopBar.kt`, `MyBottomNavigationBar.kt` umumiy glass elementlar.
- `feature/presentation/ui/components/FeaturedPrayerCard.kt`, `PrayerTimeline.kt` Home kartasi va vaqt chizig‘i.
- `feature/presentation/ui/screens/RegionSelectionScreen.kt` hudud tanlash. `MenuScreen.kt` mavzu, til dialogi va ilova haqida oynasi.
- `feature/domain/model/Regions.kt`, `RegionHierarchy.kt`, `RegionNames.kt`, `RegionSearchIndex.kt`, `RegionAddressMatcher.kt` hudud katalogi va mosliklar.
- `feature/presentation/ui/components/LocalizedScheduleText.kt`, `ScheduleRegion.kt` tarjima qilingan jadval matnlari.
- `feature/presentation/state/PrayerScheduleViewModel.kt` jadval va kesh holatlari.
- `feature/data/repository` remote va saqlangan ma’lumotlar.
- `feature/data/location` sensorlar va GPS.
- `app/src/test` mavjud JUnit testlar.

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
