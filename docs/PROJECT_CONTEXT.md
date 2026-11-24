# Muslim App davom ettirish qaydi

Oxirgi yangilanish 2026-11-24. Ushbu qayd foydalanuvchining suhbatdagi qarorlari va tekshirilgan kod holati asosida tuzildi. Keyingi ish tugagach qaydni yangilash uchun ildizdagi `AGENTS.md` ko‘rsatmalarini o‘qing.

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
- Umumiy AppBar uchun [C variant](designs/appbar-c-trailing-chip.png) tanlandi. Sarlavha chapda, tanlangan hududning qisqa va mahalliylashtirilgan nomi o‘ngdagi oval tugmada. Ovalning minimal ko‘rinadigan balandligi 30 dp, ikonlar 14 dp, hudud nomi `labelMedium` uslubida. Material 3 tugmaning 48 dp minimal bosish maydonini ta’minlaydi. Hudud tugmasi asosiy beshta bo‘limda, jumladan Menyuda ham bor. Qibla ekrani orqaga qaytish tugmasini saqlaydi.
- PR #24 sinovidan keyin foydalanuvchi C variantdagi sarlavha va hudud tugmasi kontent surilganda ham doim ko‘rinib turishini tanladi. Glass fon, yumshoq soya, aloqa holati va bo‘lim almashgandagi sarlavha animatsiyasi saqlanadi. Debug APK, 45 ta unit test va `lintRelease` o‘tdi.
- Yakuniy APK Samsung SM-S916N’ga o‘rnatildi. Asosiy va 7 kunlik oynalar oq va qora mavzularda surilganda sarlavha hamda hudud tugmasi o‘z joyida qoldi. Surilgandan keyin ham tugma hudud tanlash oynasini ochdi. Avvalgi «Tizim» mavzusi tiklandi.
- Ixcham hudud tugmasi Samsung SM-S916N’da oq va qora mavzularda tekshirildi. «Qo‘qon» ovali 299 × 101 px dan 259 × 84 px ga kichraydi. Tugma markazidan 20 dp yuqorida, ovalning tashqarisida bosilganda ham ikkala mavzuda hudud tanlash ochildi. Avvalgi «Tizim» mavzusi tiklandi.
- BottomNav ajralib turadigan oval glass shaklda. Kontent uning orqasidan ko‘rinadi. Tanlov silliq siljiydi. Qora temada to‘q ko‘k qoplama va mayinroq yaltirash och kulrang tusni pasaytiradi.
- Tugmalarda mayin ichkariga bosilish hissi bor. Indikator ustida ham bosilish sezilishi kerak.
- BottomNav istalgan bo‘limidan bosib sudraganda indikator barmoqqa ergashadi. Belgilar vaqtincha tanlangan joyga mos ajraladi. Ekran barmoq qo‘yib yuborilganda almashadi.
- Tanlangan ikonlar filled, tanlanmaganlari outlined. Almashish animatsiyali. Mavjud vector drawable tizimidan foydalaning.
- Matnlar qisqa, tushunarli, iliq va o‘qilishi oson. Holatlar va ekran sarlavhalari mayin almashadi.
- Ilova ichida ekran chetidan orqaga surilganda sahifalar asl o‘lchamida mayin so‘nib almashadi. Menyu, Tasbex va 7 kunlikdan Asosiyga qaytishda videoda ko‘ringan kichrayish Navigation Compose’ning standart animatsiyasidan kelgan. NavHost’da orqaga surish uchun o‘tishlar aniq berildi. Debug APK va 43 ta unit test o‘tdi. Samsung SM-S916N’da PR #22 va #23 birga yig‘ilgan APK o‘rnatildi. Telefon sozlamalari o‘zgartirilmagan holda 7 kunlik va Menyudan Asosiyga 1,1 soniyalik chet imo-ishorasi bilan qaytish kadrma-kadr ko‘rildi. Chiqayotgan sahifa kichraymadi.
- Murakkab yangi dizaynlarni avval maketda ko‘rsatish odati bor. Maket tanlanmagan bo‘lsa kodga qo‘llamang.

## Hozir amalda bor funksiyalar

### Yaqin masjidlar

- «Asosiy → Yaqin masjidlar» bo‘limi ochilganda «Yaqin atrof» rejimi telefon joylashuvini oladi va 20 km ichidagi eng yaqin 50 ta natijani chiqara oladi. Masofa to‘g‘ri chiziq bo‘yicha hisoblanadi. Hudud tanlagichida «Butun O‘zbekiston», 12 viloyat, Qoraqalpog‘iston Respublikasi va Toshkent shahri bor. Filtr ro‘yxat va xaritaga birdek ta’sir qiladi hamda namoz jadvali hududidan mustaqil.
- «Ro‘yxat/Xarita» tanlovi DataStore’dan tiklanadi. «Yaqin atrof»da shahar qidiruvi faqat hudud menyusidagi «Shahar tanlash» amali orqali ochiladi. Qo‘lda tanlangan shahar eslab qolinadi va keyingi safar shahar oynasida belgilanadi. Xarita tanlangan holda internet uzilsa, mavjud xabar orqali ro‘yxatga o‘tish mumkin.
- [PR #29](https://github.com/JurabekPM/Muslim_App/pull/29) Android’ning taxminiy va aniq joylashuv ruxsatlarini qabul qiladi. Joylashuv olinmasa, qisqa holat va qayta urinish tugmasi chiqadi. Koordinatalar diskka yozilmaydi. Samsung SM-S916N’da ruxsat berilgach ro‘yxat telefon joylashuvi bo‘yicha ochilishi va «Yaqin atrof» qayta tanlanganda shahar tanlash oynasi chiqmasligi tekshirildi. 8 ta masjidlar Compose testi, 45 ta unit test va `lintRelease` o‘tdi. Debug va AndroidTest APK’lari yig‘ildi.
- Butun mamlakat xaritasiga katalogdagi barcha nuqtalar beriladi. Hudud tanlanganda kamera shu hududdagi natijalarni qamrab oladi. Yaqin belgilar MapLibre’da soni yozilgan guruhga birlashadi. Guruh bosilganda xarita yaqinlashadi. Hudud kodi katalogga OpenStreetMap ma’muriy chegaralari bo‘yicha oflayn biriktiriladi. `scripts/update-mosque-catalog.ps1` va Node.js’dagi `scripts/assign-mosque-regions.mjs` geometriyani tekshiradi. Hozirgi 2 280 yozuvning hammasida 14 hududdan birining kodi bor.
- Yuqori o‘ngdagi qidiruv orqali butun O‘zbekiston katalogidan masjid nomi, muqobil nom, manzil yoki aholi punkti bo‘yicha internetsiz izlash mumkin. Qidiruvga GPS ruxsati kerak emas. Shahar nomi to‘liq mos kelsa uning 20 km atrofidagi natijalar ham qo‘shiladi. Natija kartasidagi «Xaritada ko‘rish» tanlangan masjidni MapLibre xaritasida markazlaydi.
- Internetsiz katalogda 2 280 ta masjid yozuvi va 347 ta shahar yoki shaharcha bor. O‘zbekistonning 14 ta ma’muriy markazi atrofida natija borligi tekshirildi. Asosiy OpenStreetMap ma’lumoti 2026-11-23 kungi Overpass nusxasi, joy nomlari va bino chegaralari esa Geofabrik’ning 2026-11-22 kungi O‘zbekiston eksportidan olindi. Ma’lumot ODbL 1.0 bo‘yicha tarqatiladi. [Geofabrik O‘zbekiston nusxasi](https://download.geofabrik.de/asia/uzbekistan.html).
- Yangilangan `scripts/update-mosque-catalog.ps1` bir xil joyga tegishli OpenStreetMap nomlarini ishlatadi. Manzili nashr qilingan manba bilan solishtirilgan yozuvlar `scripts/mosque-name-sources.json` da manbasi bilan saqlanadi. 21 ta nomsiz yoki umumiy nomli yozuvga alohida nom topildi. Hali alohida nomi tasdiqlanmagan 589 ta yozuvdan 586 tasida manzil yoki yaqin aholi punkti mavjud. Taxminiy aholi punkti «Yaqin hudud» deb belgilanadi.
- MapLibre Compose 0.20.0 va OpenGL faqat xarita ko‘rinishi ochilganda ishga tushadi. OpenFreeMap’ning Liberty va Dark stillari ishlatiladi. Masjid belgisi tafsilotlarni ochadi. «Yo‘lni ochish» tanlangan masjid koordinatalari bilan Google Maps havolasini ochadi. Interfeys matnlari sakkiz tilda.
- Samsung SM_S916N’da joriy joylashuv orqali ro‘yxat, qora mavzuda joylashuv belgisi, Toshkentni qo‘lda izlab tanlash, oq mavzuda xarita hamda masjid belgisi orqali tafsilotlar tekshirildi. Telefonning qora mavzusi tiklandi. Qurilmadagi ikkita avtomatik test internetsiz xarita holati, katalogdan ro‘yxatni ochish va maxfiylik dialogini qamradi. Google Maps’dagi marshrut alohida tekshirilmadi.
- Debug APK, release APK, release AAB va 40 ta unit test o‘tdi. `lintRelease` 0 ta xato va mavjud 6 ta ogohlantirish bilan o‘tdi. Debug APK va 64-bit MapLibre ELF segmentlari 16 KB alignment tekshiruvidan o‘tdi. Universal debug APK 19,7 MB dan 63,4 MB gacha oshdi. Release AAB ichidagi ARM64 MapLibre kutubxonalarining siqilgan hajmi 6 MB.
- Butun O‘zbekiston qidiruvi yangilanishida 42 ta unit test o‘tdi. Samsung SM_S916N’da ikkita Android testi o‘tdi. Ular tanlangan shahardagi ro‘yxatni va GPS hamda internet holati bo‘lmasdan Faqih Abulays masjidini topishni tekshirdi. Xaritada ochish amali internet talab qilishi ham tekshirildi.
- Hudud filtri yangilanishida 43 ta unit test va Samsung SM_S916N’dagi uchta Android testi o‘tdi. Telefonning jonli xaritasida «Butun O‘zbekiston» 2 280 ta va «Andijon viloyati» 190 ta masjidni ko‘rsatdi. Ikkala ko‘rinishda ham guruh belgilari va sonlar chizildi.
- Ro‘yxat, qidiruv va xaritadagi masjid kartalarida OpenStreetMap ID’si bo‘yicha 45 ta joyning 66 ta tekshirilgan Wikimedia Commons surati ko‘rsatiladi. JPEG fayllar ilovaga biriktirilgan, jami 2,42 MB va internetsiz ochiladi. Suratga bosganda ilova ichidagi galereya ochiladi. 10 ta yozuvda boshqa kadrlarga surish yoki o‘q tugmalari orqali o‘tish mumkin. Muallif, litsenziya va asl fayl ma’lumotlari «Menyu → Ilova haqida → Surat litsenziyalari»da ko‘rsatiladi. Surati topilmagan masjidda standart belgi ko‘rinadi. [Surat manbalari](MOSQUE_PHOTOS.md).
- Xaritadagi alohida nuqtalar masjid ikonkasiga almashtirildi. Tanlangan masjid kattaroq oltin belgi bilan ajraladi, uzoqdan yaqin masjidlar soni ko‘rsatilgan guruhlarga birlashadi. Debug APK va 43 ta unit test o‘tdi. Samsung SM_S916N’da to‘rtta Android testi o‘tdi, oq mavzuda Minor surati, standart rasm va tanlangan oltin xarita belgisi jonli ko‘rildi.
- Masjid belgisi 33 dp, tanlangani 40 dp. Kengroq bosish zonasiga bir nechta belgi tushsa, bosilgan joyga eng yaqini tanlanadi. Guruhlar zich joylarda belgilar ustma-ust chiqishini kamaytiradi. Tafsilot kartasi xarita ustida ochiladi va bo‘sh joyga yoki guruhga bosganda yopiladi. Samsung SM_S916N’da oq mavzuda mamlakat va Andijon xaritasi, ikonka chetidan tanlash, xarita siljimasligi, kartani yopish va guruhni yaqinlashtirish tekshirildi. Debug APK, 43 ta unit test va 4 ta Android testi o‘tdi.
- «Yaqin masjidlar»ga Asosiy oynadan kirish uchun uchta qora mavzudagi maket tayyorlandi. [1. Manzil ostidagi ixcham karta](designs/home-mosques-1-compact-card.png), [2. Manzil yonidagi tugma](designs/home-mosques-2-location-action.png), [3. Jadval ostidagi suratli karta](designs/home-mosques-3-preview-card.png). Har bir maketda yonma-yon ochiladigan masjidlar ekrani ham bor. Surat, xarita va jadval ma’lumotlari tasviriy namuna. Variant tanlangach dizayn ilovaga qo‘llanadi.
- Foydalanuvchi [jadval ostidagi keng kartalar](designs/home-mosques-qibla-banners.png) variantini tanladi. «Yaqin masjidlar» kirishi Menyudan Asosiy oynaga ko‘chdi, «Qibla» uchun ikkinchi karta qo‘shildi. Ikkala karta butun yuzasi bilan bosiladi, joriy sakkiz tilda ishlaydi va jadval yuklanishidan mustaqil. Tasvirlar ilovada saqlanadi. AppBar’dagi Qibla tugmasi, namoz kartasi, jadval va BottomNav dizayni saqlandi. Debug APK va 43 ta unit test o‘tdi. Samsung SM-S916N’da ikkala mavzudagi ko‘rinish va oq mavzuda ikkala kartadan ekran ochilishi tekshirildi. Telefonning avvalgi mavzusi tiklandi.
- PR #21 umumiy AppBar’dan Qibla tugmasini olib tashlaydi. Kompas Asosiy oynadagi Qibla kartasidan ochiladi. Hudud tanlash tugmasi Menyuda ham chap tomonda ko‘rinadi. PR #21, #22 va #23 birga yig‘ilgan debug APK Samsung SM-S916N’ga o‘rnatildi. Beshta asosiy bo‘lim AppBar’i, Menyudan hudud tanlashning ochilishi va Qibla kartasidan kompas ochilishi tekshirildi. Birgalikdagi yig‘ilishda 45 ta unit test o‘tdi.
- «Yaqin masjidlar» ekranini yangilash uchun ImageGen bilan dastlab uchta maket tayyorlandi. [1. Ixcham ro‘yxat](designs/nearby-mosques-1-compact-list.png) qidiruv, hudud va natija sonini ko‘rinadigan joyga qo‘yadi. [2. Keng xarita](designs/nearby-mosques-2-map-first.png) tanlangan masjidning yorqin belgisini pastdagi ixcham karta bilan bog‘laydi. [3. Suratli kartalar](designs/nearby-mosques-3-photo-cards.png) ro‘yxatdan tanlangan masjidning xaritadagi tafsilotiga o‘tishni ko‘rsatadi.
- [PR #25 qoralamasiga](https://github.com/JurabekPM/Muslim_App/pull/25) ro‘yxat uchun uchta yangi maket qo‘shildi. [4. Bosilganda ochiladigan qator](designs/nearby-mosques-4-expandable-rows.png) xarita va yo‘l tugmalarini tanlangan masjid ostida ko‘rsatadi. [5. Eng yaqini tepada](designs/nearby-mosques-5-nearest-first.png) «Yaqin atrof» rejimida birinchi natijani masofa va yo‘l tugmasi bilan ajratadi. [6. Pastdan ochiladigan tafsilot](designs/nearby-mosques-6-detail-sheet.png) tumanlar bo‘yicha ro‘yxatni va tanlangan masjid uchun pastdagi amallarni ko‘rsatadi. Surat, manzil va masofalar maket uchun namuna.
- Foydalanuvchi 5-variantning «Eng yaqin» kartasi va ixcham ro‘yxatini tanladi. Joylashuv va xarita boshqaruvi uchun ImageGen bilan uchta yangilangan maket tayyorlandi. [5A. Yonma-yon](designs/nearby-mosques-5a-location-map-row.png) GPS holati va alohida «Xarita» tugmasini bir qatorga qo‘yadi. [5B. Pastdagi almashtirgich](designs/nearby-mosques-5b-bottom-view-switch.png) «Ro‘yxat / Xarita» o‘tishini bosh barmoq yetadigan joyda ko‘rsatadi. [5C. Mini xarita](designs/nearby-mosques-5c-map-preview-action.png) tanlangan shahar yonida tasvirli «Xarita» tugmasini ko‘rsatadi. Mini xarita tasviriy namuna, jonli xarita alohida bosishda ochiladi.
- Keyingi maketlar uchun 5B tanlandi. Joylashuv va saralash uch xil ko‘rinishda chizildi. [5B-1. Alohida tugmalar](designs/nearby-mosques-5b-1-quick-controls.png) hudud va saralashni yonma-yon ko‘rsatadi, GPS va qo‘lda tanlangan shahar holatlarini ajratadi. [5B-2. Filtr va saralash oynasi](designs/nearby-mosques-5b-2-filter-sheet.png) hudud va tartibni birga tanlashga imkon beradi. [5B-3. Bir bosishda saralash](designs/nearby-mosques-5b-3-sort-tabs.png) «Masofa / Nomi» tanlovini ro‘yxat ustida ko‘rsatadi. «Eng yaqin» kartasi ro‘yxat boshida turadi. Maketlarning joylashuv va masofalari namuna.
- Foydalanuvchi biriktirgan rasm bilan 5B-1 ro‘yxat maketini tasdiqladi. [Ilk xarita maketi](designs/nearby-mosques-5b-1-map.png) oq mavzuda GPS va ko‘k nuqtani, qora mavzuda qo‘lda tanlangan shaharni ko‘rsatadi. Xarita tasviri va masofalar maket uchun namuna.
- Masjidlar ro‘yxatida hudud tugmasi yonidagi «Saralash» boshqaruvi «Yaqin atrof», viloyat va butun mamlakat holatlarida ham ko‘rinishi kerak. [Uchta oq va qora maket](designs/nearby-mosques-filter-options.png) tayyorlandi. «Ikki kapsula» saralash nomini qisqa ko‘rsatadi, «Bitta panel» hudud va saralashni bitta fon ichida ajratadi, «Oltin tugma» saralash amaliga mayin urg‘u beradi. Maketlarda joylashuv olinmaganda nom bo‘yicha saralash mavjud, masofa uchun joylashuv kerak. 320 px enida tugmalar sig‘ishi va maket menyulari tekshirildi. Variant tanlangach ilovaga qo‘llanadi.
- «Bitta panel» asosida [yozuvsiz saralash belgisining ikki holati](designs/nearby-mosques-sort-icon.png) tayyorlandi. Belgi o‘ngda yoki chapda turadi, qolgan joy hudud nomiga ajratiladi. Saralash usullari menyuda matn bilan ko‘rsatiladi. Oq va qora maketlar hamda tugmalarning 320 px enida sig‘ishi tekshirildi. Dizayn tanlovi [PR #30](https://github.com/JurabekPM/Muslim_App/pull/30) qoralamasida.
- Chapdagi yozuvsiz saralash belgisi uchun [uchta yangi uslub](designs/nearby-mosques-sort-left-styles.png) chizildi. «Yumshoq kapsula» yumaloq tugma fonini, «Ingichka kontur» yengil chiziq va ajratgichni, «Mayin oltin» oltin tusdagi ixcham tugmani ko‘rsatadi. Uchalasida hudud o‘ngda. Oq va qora ko‘rinishlar, 320 px enida joylashuv hamda maket menyulari tekshirildi.
- Foydalanuvchi «Yumshoq kapsula»ning chapdagi dumaloq saralash tugmasini ma’qulladi va [hudud tanlovi maketlaridan «Ixcham maydon»ni](designs/nearby-mosques-region-select-styles.png) tanladi. PR #30 da bitta yumshoq panel, chapda yozuvsiz tugma, o‘ngda yupqa konturli hudud maydoni qo‘llandi. Hudud nomi bitta qatorda, uzun nomlar uch nuqta bilan qisqaradi. Saralash barcha hududlar ro‘yxatida bor. Joriy koordinata mavjud bo‘lsa masofa, har qanday hududda nom bo‘yicha saralash mumkin. Masofa uchun «Yaqin atrof»da olingan koordinata faqat ochiq ekran davomida ishlatiladi. Saralash hisobi fon oqimida bajariladi. PR #30 joriy joylashuv uchun PR #29 commitlariga tayangan. Debug va test APK’lari yig‘ildi, 45 ta unit test va `lintRelease` o‘tdi.
- [Qayta chizilgan 5B-1 xarita maketi](designs/nearby-mosques-5b-1-map-v2.png) xaritani pastki boshqaruv orqasigacha yoyadi. Oq mavzuda GPS nuqtasi, joylashuvga qaytish tugmasi va masjid tanlanmagan holat ko‘rsatilgan. Qora mavzuda tanlangan shahar, oltin masjid belgisi va xarita ustidagi yopiladigan tafsilot kartasi bor. Pastdagi «Ro‘yxat / Xarita» almashtirgichi tanlangan ro‘yxat maketidagi shakl va mavzu ranglariga mos.
- 5B-1 ro‘yxati va qayta chizilgan xarita PR #25 da ilovaga qo‘llandi. Foydalanuvchi xaritadagi alohida «Yaqin atrof» tugmasini chizib tashladi. Hudud tanlash joylashuv qatoriga birlashtirildi. «Yaqin atrof»da qo‘lda shahar tanlash ham shu menyudan ochiladi. O‘ng tugma GPS’ni yangilaydi. Ro‘yxatda eng yaqin masjid tepada, qolganlari masofa yoki nom bo‘yicha saralanadi. Saralash xarita belgilariga tatbiq etilmaydi. Xarita pastki almashtirgich orqasigacha yoyiladi. Suratlar tasdiqlangan katalogdan yoki mavzuga mos standart ikonadan olinadi.
- Debug APK, 45 ta unit test va `lintRelease` o‘tdi. Samsung SM-S916N’da 5 ta Android testi o‘tdi. Jonli qora mavzuda GPS holati, ro‘yxatdan xaritaga o‘tish, joylashuv qatoridan hudud tanlash, masjid belgisidan karta ochish va yopish hamda GPS markazlash tugmasi sinaldi.
- Ro‘yxat tanlangan 5B-1 maketiga yaqinlashtirildi. Hudud va saralash tabletkalarida alohida ikonlar, masofa yonida pin, yo‘l amallarida burilish strelkalari ishlatiladi. «Eng yaqin» kartasida kengroq surat va to‘liq enli oltin tugma, keyingi qatorlarda ixcham gorizontal suratlar bor. Surat bo‘lmagan joyda sokin rangli standart ikonka chiqadi. Oltin tanlov kapsulasi pastdagi «Ro‘yxat / Xarita» ichida silliq siljiydi, ro‘yxat uchun boshqaruv ostida alohida joy ajratildi. Samsung’da oq va qora ro‘yxatlar vizual solishtirildi, almashtirgich sinaldi va avvalgi «Tizim» mavzusi tiklandi. Debug APK, 45 ta unit test, 5 ta Android testi va `lintRelease` o‘tdi.
- Oddiy masjid qatorlaridagi yo‘l tugmasi uchun oq va qora mavzuda uchta maket tayyorlandi. [1. Dumaloq strelka](designs/nearby-mosques-directions-round.png) ixcham rangli fonda yo‘nalish belgisini, [2. «Yo‘l» tugmasi](designs/nearby-mosques-directions-labeled.png) ikonka va qisqa matnni, [3. Chetga ajratilgan tugma](designs/nearby-mosques-directions-edge.png) ingichka chiziq va yumaloq burchakli tugmani ko‘rsatadi. Foydalanuvchi birinchi variantni tanladi. Oddiy qatorlarda 40 dp dumaloq fon, 48 dp bosish maydoni, mavzuga mos rang va mayin bosilish animatsiyasi ishlatiladi. Amaldagi yo‘l ochish va ekran o‘quvchisi yorlig‘i saqlangan.
- [PR #26](https://github.com/JurabekPM/Muslim_App/pull/26) da Qo‘qondagi Hadya Hoji, Marg‘ilondagi Said Ahmadxo‘ja majmuasi va Andijondagi Devonaboy suratlari oflayn miniatyuralarga qo‘shildi. Uch suratning katalog yozuvlari, litsenziyalari va JPEG o‘lchamlari tekshirildi. Debug APK yig‘ildi, uchala surat APK tarkibida borligi tasdiqlandi va 45 ta unit test o‘tdi. [Qo‘qondagi boshqa masjidlar bo‘yicha qidiruv](MOSQUE_PHOTOS.md#qoqondagi-qoshimcha-surat-manbalari) davomida bino mosligi yoki suratdan foydalanish ruxsati tasdiqlanmagan yozuvlar qayd etildi.
- PR #26 dagi APK Samsung SM-S916N’ga o‘rnatildi. Dumaloq yo‘l tugmasi oq va qora mavzularda vizual tekshirildi. Hadya Hoji qatoridagi tugma Google Maps’ni ochdi. Uchala yangi miniatyura va ularning muallif hamda litsenziya oynalari ochildi. Sinovdan so‘ng «Tizim» mavzusi tiklandi. `lintRelease` 0 xato va mavjud 8 ogohlantirish bilan o‘tdi.
- Foydalanuvchi masjid suratlari qamrovini imkon qadar butun O‘zbekiston bo‘yicha oshirishni so‘radi. Katalogdagi 2 280 ta OpenStreetMap yozuvida foto manbalari qidirildi. Mavjud Wikimedia bog‘lanishlari va qo‘shimcha qidiruvdan 13 ta ochiq litsenziyali surat qo‘shildi. Ular orasida Xonagoh (Marg‘ilon), Tillashayx (Toshkent), Hazrati Xizr va Xoja Ahror (Samarqand), Oq masjid va Bog‘bonli (Xiva) bor. Qo‘qondagi uchta galereya katalog joyiga solishtirildi. Samsung’da uchta galereya belgisi ko‘rindi, G‘ishtlik belgisi mos Yandex galereyasini ochdi. Yakuniy APK telefonga o‘rnatildi, Hazrati Xizr surati, muallifi va litsenziyasi ochildi. APK tarkibida 44 ta JPEG tasdiqlandi, 45 ta unit test va `lintRelease` o‘tdi. Qo‘qondagi Shayxon uchun tasdiqlangan surat hali topilmagan.
- Foydalanuvchi barcha topilgan suratlar ilova ichida ochilishini va bir nechta kadr bo‘lsa ularni shu yerda ko‘rishni so‘radi. PR #26 ga 22 ta ochiq litsenziyali surat, jumladan G‘ishtlikning uchta kadri, hamda suratlarni surib ko‘rish oynasi qo‘shildi. Galereyaga olib boruvchi tashqi havolalar olib tashlandi. Avg‘onbog‘, Zinbardor va Shayxon uchun foydalanish huquqi va masjidga mosligi tasdiqlangan surat hali topilmagan. APK ichida 66 ta JPEG tekshirildi. 45 ta unit test, `lintRelease` (0 xato, 8 ogohlantirish) va Samsung SM-S916N’dagi 5 ta masjid ekrani testi o‘tdi. Minor suratlari orasida surish, muallif yozuvining yangilanishi va asl manzilning ilovada matn sifatida ochilishi tekshirildi. Samsung’dagi odatiy qidiruvdan G‘ishtlik topildi va uchala kadri o‘q tugmasi orqali ochildi.

- PR #26 masterga qo‘shildi. Ishchi va Android Studio nusxalari fast-forward qilindi, Studio’ning mahalliy sozlamalari saqlandi. Keyingi taklif — ro‘yxat, qidiruv va xaritadan ochiladigan bitta [masjid tafsiloti sheeti](designs/nearby-mosques-sheet-detail.png). Unda mavjud manzil, masofa, suratlar va yo‘l amali birga ko‘rinadi. «Xaritada ko‘rish» amali ro‘yxat va qidiruvdan kirilganda kerak.
- «Yaqin masjidlar» uchun beshta oq va qora maket tasdiqlandi va [PR #27](https://github.com/JurabekPM/Muslim_App/pull/27) da qo‘llandi. Masjid tafsiloti ro‘yxat, qidiruv va xarita belgisidan ochiladi. «Xaritada ko‘rish» ro‘yxat va qidiruvdan tanlangan masjidni markazlaydi. Orqaga qaytish avvalgi ro‘yxat yoki qidiruvni ochadi. [Hudud](designs/nearby-mosques-sheet-area.png) va [shahar](designs/nearby-mosques-sheet-city.png) tanlovi pastdan ochiladi. [Suratlar](designs/nearby-mosques-photo-viewer.png) ilovaning ichidagi katta oynada, muallif va litsenziya ochiladigan manba qatorida ko‘rinadi. [Saralash](designs/nearby-mosques-sort-and-error.png) menyusida joriy tanlov belgilanadi, tashqi yo‘lni ochib bo‘lmasa ixcham dialog yordam beradi. Yangi matnlar sakkiz tilda.
- PR #27 ning debug APK’si Samsung SM-S916N’ga o‘rnatildi. Oq va qora mavzuda ro‘yxat va qidiruv natijasidan tafsilot, Minor xarita belgisidan shu sheetning ochilishi, shahar va hudud tanlovi, saralash hamda suratlar oynasi tekshirildi. Surat manbasi ilova ichida ochildi. Avvalgi «Tizim» mavzusi tiklandi. Samsung’da 6 ta Compose testi, jumladan yo‘l ochilmagandagi dialog va oflayn qidiruv, mahalliy 45 ta unit test hamda `lintRelease` o‘tdi. Lintda 0 xato va mavjud 8 ta ogohlantirish bor.

- PR #27 da foydalanuvchi belgilagan joylar ixchamlandi. Ro‘yxat va xaritadagi joylashuv kapsulasida bitta satr, hudud yoki shahar tanlovi va GPS amali bor. Galereyadagi «Surat manbasi» paneli olib tashlandi. Xaritaning pastidagi havolalar o‘rniga yuqori burchakdagi kredit ilova ichidagi litsenziya oynasini ochadi. Ro‘yxat oxiridagi OpenStreetMap yozuvi oddiy matn.
- Ixchamlashtirilgan debug APK va test APK yig‘ildi, 45 ta unit test o‘tdi. Debug APK Samsung SM-S916N’ga o‘rnatildi. Telefonda boshqa ilova ochiq bo‘lgani uchun ushbu yig‘ilishdagi interfeys va Compose testlari telefonda ishga tushirilmadi.

- PR #27 ning keyingi yangilanishida «Yaqin masjidlar»dagi GPS qatori va xaritadagi qurilma joylashuvi olib tashlandi. «Yaqin atrof» birinchi marta tanlansa shahar tanlagich ochiladi. Saqlangan shahar hudud tugmasidan almashtiriladi. Yaqin masjidlar faqat shu rejim ochilganda hisoblanadi. Masjid qidiruv indeksi so‘rovdan keyin quriladi va ekran ichida qayta ishlatiladi. OpenStreetMap yozuvi xarita tayyor bo‘lgach besh soniya ko‘rinadi, barcha xarita litsenziyalari burchakdagi ma’lumot tugmasidan ochiladi.
- Yangilangan APK Samsung SM-S916N’ga o‘rnatildi. Jonli sinovda qurilma joylashuvi qatori ko‘rinmasligi, OpenStreetMap yozuvining yig‘ilishi va litsenziya oynasining ilova ichida ochilishi tasdiqlandi. Samsung’da 7 ta Compose testi, jumladan GPSsiz shahar tanlash va oflayn ro‘yxat, hamda mahalliy 45 ta unit test o‘tdi.

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

Android instrumentatsiya testi namunaviy jadvallarni amaldagi repository orqali DataStore’ga yozadi. Skript emulyatordagi ilova jarayonini to‘xtatib, internet yo‘qligini tekshiradi. Test qayta ochilgan ilovada kunlik, 7 kunlik va oylik namoz vaqtlarini solishtiradi. Test uchun Muslim App o‘rnatilmagan emulyatordan foydalaning.

```powershell
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest
.\scripts\test-offline-prayer-schedules.ps1 -Serial emulator-5554
```

Android 16 emulatorida ilova jarayoni to‘xtatildi va faol tarmoq yo‘qligi tasdiqlandi. Ilova qayta ochilganda kunlik jadval, 7 kunlik jadvalning birinchi va oxirgi kuni, hamda oylik jadvaldagi tanlangan kunning oltita namoz vaqti keshdagi namunalar bilan mos keldi. Shu sinovdan keyin emulyator yopildi. Keyingi qurilma sinovlari ulangan Samsung’da bajariladi.

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

[Tasdiqlangan oylar bo‘yicha tarix maketi](designs/tasbix-history-by-month.png) oq va qora mavzuda qo‘llandi. Eng yangi yozuvli oy ochiq turadi. Boshqa oy tanlanganda ro‘yxat va oy belgisi animatsiya bilan o‘zgaradi. Oy qatorida yozuvlar soni, ochiq bo‘limda kun, vaqt va zikr soni chiqadi. «Sana tanlash» orqali faqat yozuv bor kun tanlanadi va ro‘yxat o‘sha kunga o‘tadi. «Yopish» tugmasi pastda turadi. Tarixning 20 yozuvlik chegarasi olib tashlandi. Mavjud DataStore fayli, kaliti va yozuvlar formati saqlandi.

Debug APK, test APK va 36 ta unit test o‘tdi. `lintRelease` 0 xato va avvalgi 4 ta ogohlantirish bilan yakunlandi. Samsung SM-S916N’da 5 ta Tasbix testi, jumladan oq va qora mavzuda boshqa oyni ochish va sana tanlash, hamda 100 yozuvda «Yopish» tugmasining ko‘rinib turishi tekshirildi. Qurilmaning Tasbix saqlov fayli xeshi yakuniy APK o‘rnatilishidan oldin, keyin va sinovdan so‘ng bir xil bo‘ldi. Eski formatdagi 20 ta namuna yozuvi yangi nollashdan keyin ham saqlanib, testda qayta ochildi.

Tasbixdagi «Zikrni boshlang» va «Zikr davom etmoqda» yozuvlari sanoq bilan bitta vertikal tartibda joylashadi. Amallar qatoridan oldin alohida oraliq bor. Katta sanash tugmasi qolgan balandlikka mos o‘lchanadi. Raqamlarning yuqori va pastda so‘nish maydoni saqlandi. Debug APK, test APK va 36 ta unit test o‘tdi. `lintRelease` 0 xato va avvalgi 4 ta ogohlantirish bilan yakunlandi. Samsung SM-S916N’da 5 ta Tasbix testi, jumladan oq va qora mavzuda yozuvning sanoq ostida va amallardan ajralib turishi tekshirildi. Qurilmadagi mavjud sanoq va tarix fayli sinovdan oldin va keyin bir xil bo‘ldi.

Tasbix uchun [Sanoq rejimi](designs/tasbix-count-modes.png) va [Sunset konturining 3-varianti](designs/tasbix-sunset-button.png) tanlandi. Sanoq tepasidagi ixcham tugma «33 marta», «99 marta», «Erkin sanoq» yoki «Boshqa son» tanlovini pastdan ochadi. Tanlov saqlanadi, joriy sanoq va tarix qoladi. Standart rejim Erkin. Maqsadli rejimda sanoq maqsaddan keyin ham davom etadi va kontur to‘liq to‘lgan holatda qoladi. Maqsadga birinchi yetish bitta alohida haptik signal beradi. Nollash keyingi sanoq uchun signalni tiklaydi, tanlangan sonni saqlaydi.

Sunset progressini katta tugma konturi chizadigan `MorphPolygonShape` bilan bir xil shakl belgilaydi. Bosib turganda ikkalasi birga dumaloqlashadi. Oltin press yaltirashi maqsadli rejimda kontur chizig‘ining ichkarisida, pastki qismda. Home va Tasbix Sunset ranglari va yaltirash fazasi yordamchisidan birga foydalanadi. «Zikrni boshlang» va «Zikrni davom ettiring» yozuvlari kirishda va nollashdan keyin 5 soniya ko‘rinib so‘nadi, joyi saqlanadi.

Debug APK, test APK va 37 ta unit test o‘tdi. `lintRelease` 0 xato va avvalgi 4 ta ogohlantirish bilan yakunlandi. Samsung SM-S916N’da yakuniy APK bilan 6 ta Tasbix testi o‘tdi. Oq va qora mavzuda Sunset chizig‘ining bo‘sh va bosilgan kadrlari, 33, 99, Erkin va 54 lik boshqa son, nolni rad etish va yordamchi yozuvning so‘nishi tekshirildi. Test oldi va keyingi haqiqiy sanoq va tarix fayllari xeshi mos keldi. Maqsad chegarasidagi bitta signal mantiqi va rejimning qayta ochilishda saqlanishi unit testda tekshirildi.

PR #17 dagi vizual sayqalda Sunset chizig‘i 7 dp dan 9 dp ga qalinlashdi. Tashqi chiziq tugma chetiga tutashadi. Bosilgandagi oltin nur pastki ichki qirra bo‘ylab tarqaladi, tashqi soyaning yoyilishi bosilganda qisqaradi. Samsung SM-S916N’da 6 ta Tasbix testi o‘tdi. Oq va qora mavzudagi bosilgan kadrlar ko‘rildi, mavjud sanoq va tarix fayli saqlandi. Build, 37 ta unit test va 0 xatoli `lintRelease` o‘tdi.

Sanoq maqsadi tanlanganda bosishdagi oltin nur pastki ichki qirrada kuchaytirildi. Nur gradienti yuqoriga qarab so‘nadi. Samsung SM-S916N’dagi oldingi va yangi oq hamda qora kadrlar solishtirildi. Ikkala mavzuda ham qirradagi nur kuchaygani ko‘rindi. 6 ta qurilma testi, 37 ta unit test va `lintRelease` o‘tdi.

Bosish animatsiyasi uchun [6-variant](designs/tasbix-bead-glow.png) tanlandi. Katta tugmadagi tasbeh donalari ketma-ket yaltiraydi. Tez bosishda nur donalar bo‘ylab yurishni yakunlab so‘nadi. Sanoqning Sunset progressi qirrada turadi. Qo‘shimcha Sunset effekti uchun joylashuv maketlari ko‘rib chiqilmoqda.

Samsung SM-S916N’da 6 ta Tasbix testi o‘tdi. Oq va qora mavzuda maqsadli hamda Erkin rejimdagi donalar yaltirashi, Sunset konturi va bosishdan keyingi tiklanish tekshirildi. Mavjud sanoq va tarix fayli saqlandi. Debug build, 37 ta unit test va `lintRelease` o‘tdi.

Sunset uchun [progress uchi varianti](designs/tasbix-sunset-tip.png) tanlandi. Tugma bosilganda progress uchidagi qisqa oq-oltin chaqnash chiziq ichida ko‘rinib, taxminan 300 ms da so‘nadi. Tez bosishlarda nur joriy yorqinlikdan yangilanadi. [Qo‘shimcha nur maketlari](designs/tasbix-sunset-press-options.png) yuqori chapdan boshlab progress konturi, tugma tagi, barcha tomon va barmoq tekkan tomondagi qisqa yoyni ko‘rsatadi.

Samsung’da `TasbexScreenTest` sinfidagi 6 ta sinov o‘tdi. Oq va qora mavzuda progress uchi bosish boshida yorishishi va bosib turilganda so‘nishi tekshirildi. Telefonda sanoq va tarix fayli saqlandi. Debug APK, 37 ta unit test va `lintRelease` muvaffaqiyatli yakunlandi (0 xato, 4 ogohlantirish).

Tashqi nur uchun avval 4-variant tanlandi. Barmoq tekkan tomonga eng yaqin qirrada qisqa Sunset yoyi paydo bo‘ladi, tugmaning shakliga ergashadi va mayin so‘nadi. Markazga bosilganda nur pastki qirrada chiqadi. Erkin rejimda ham qirra yaltiraydi. Progress uchidagi chaqnash shu bosish animatsiyasidan foydalanadi. Keyin 1-variantni, ya’ni to‘lgan progress konturi bo‘ylab nurni, taqqoslash uchun sinash rejalashtirilgan.

Samsung SM-S916N’da 6 ta Tasbix sinovi o‘tdi. Oq va qora mavzuda chap hamda o‘ng bosishlar mos qirrani yoritishi, ushlab turilganda nur so‘nishi va progress uchidagi chaqnash ishlashi kadrlar orqali tekshirildi. Haqiqiy sanoq va tarix fayli saqlandi. Debug va test APK’lari yig‘ildi, 37 ta unit test hamda `lintRelease` o‘tdi (0 xato, 4 ogohlantirish).

4-variant takomillashtirildi. Bosilgan tomondagi Sunset yoyi kengaydi, qirra ortidagi iliq soya 38 dp gacha yoyilib yumshoq so‘nadi. Nur bosib turilganda yonadi, qo‘yib yuborilganda yoki bosish bekor qilinganda so‘nadi. Tasbeh donalaridagi nur to‘lqini 650 ms davom etadi va tez bosishlarda boshlanib bo‘lgan to‘lqin oxirigacha yuradi. Progress uchiga oq yadro, oltin atrof va qisqa yorug‘ iz qo‘shildi. Barmoq qo‘yib yuborilganda yorug‘ uch yangi sanoqqa siljiydi.

Samsung SM-S916N’da takroriy sinovda 6 ta Tasbix testi o‘tdi. Oq va qora mavzuda chap va o‘ng qirra 800 ms bosib turilganda yoniq qolgani, bosish tugagach so‘ngani, donalar yaltirashi 448 ms da ko‘ringani va progress uchidagi oq nuqta ajralishi kadrlar bo‘yicha tasdiqlandi. Haqiqiy sanoq va tarix fayli xeshi mos keldi. Debug va test APK’lari, 37 ta unit test va `lintRelease` o‘tdi (0 xato, avvalgi 4 ogohlantirish).

1- va 4-variantlar Samsung’da bir xil 24/33 sanoqda oq va qora mavzuda solishtirilgach, foydalanuvchi 4-variantni tanladi. Oq mavzuda qirra nuri va progress uchida to‘qroq Sunset aksenti ishlatiladi. Katta tugma bosib sudralganda nur barmoq yo‘nalishiga qarab qirra bo‘ylab eng qisqa yo‘ldan silliq siljiydi. Barmoq markazdan o‘tganda oxirgi yo‘nalish saqlanadi. Oddiy bosish bir marta sanaydi. Sudrash tugaganda nur so‘nadi va sanoq o‘zgarmaydi. [Taqqoslash sinovi PR #18](https://github.com/JurabekPM/Muslim_App/pull/18).

Samsung SM-S916N’da 6 ta Tasbix testi o‘tdi. Oq va qora mavzuda bitta ushlab turilgan barmoq bilan chapdan past orqali o‘ngga sudrash, chap va o‘ng qirraning navbat bilan yorishishi hamda barmoq qo‘yib yuborilgandan keyingi so‘nish kadrlar bo‘yicha tekshirildi. Sudrashdan keyingi sanoq va oddiy bosishdagi bitta qo‘shish tasdiqlandi. Qurilmadagi asl sanoq va tarix fayli xeshi mos keldi. Debug va test APK’lari, 37 ta unit test va `lintRelease` o‘tdi (0 xato, avvalgi 4 ogohlantirish).

Tasbixda «Ovoz va tebranish» sozlamasi sanoq rejimining o‘ng tomonida. Bosish va yakun uchun ovoz hamda tebranish alohida yoqiladi yoki o‘chiriladi. Ovozlar dastlab o‘chiq va telefon ovozsiz yoki faqat tebranish rejimida chalinmaydi. Tebranishlar dastlab yoqilgan va tizim sozlamasiga amal qiladi. Yakunda bir martalik 165 ms tebranish, qisqa ikki notali ovoz hamda sanoq ostida «33 ta zikr bajarildi» kabi 2,3 soniyalik xabar bor. Sozlamalar avvalgi DataStore faylida alohida kalitlarda saqlanadi. Matnlar sakkiz tilda.

Samsung SM-S916N’da 7 ta Tasbix sinovi o‘tdi. Oq va qora mavzuda to‘rtta sozlama, 33 ga yetishdagi so‘nuvchi xabar va keyingi bosishda qayta chiqmasligi tekshirildi. Samsung jurnali ikkita qisqa ovozning boshlanganini va yakundagi 165 ms tebranish effektini qayd etdi. Asl sanoq va tarix fayli xeshi mos keldi.

Debug va test APK’lari yig‘ildi, 38 ta unit test hamda `lintRelease` o‘tdi (0 xato, avvalgi 4 ogohlantirish). Unit test alohida sozlamalar parallel yozilganda, sanoq nollanganda va DataStore qayta ochilganda saqlanishini tekshirdi.

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

2-bosqichda kunlik, haftalik va oylik jadvallar DataStore qayta ochilgach yangi repository orqali internetsiz o‘qilishi sinovdan o‘tdi. Mos davr bo‘lmasa so‘nggi jadval ko‘rsatiladi. Qo‘lda yangilashdagi HTTP 502 va tarmoq xatolarida saqlangan jadval qoladi. Mahalliy debug build va 30 ta unit test o‘tdi. Samsung’da shu bosqich uchun amaliy sinov o‘tkazilmagan. PR #3 masterga qo‘shildi va Android Studio nusxasi yangilandi.

Namoz jadvali uchun API DTO’lari `feature/data/network` ichida, v1 kesh DTO’lari hamda ularning xaritalashi `feature/data/local/ScheduleCacheFormat.kt` ichida. Repository Gson’dan mustaqil Kotlin modellarini qaytaradi. Kunlik, haftalik va oylik yozuvlar bir xil DataStore fayli, JSON maydonlari va hudud kalitlaridan foydalanadi. Debug build va 31 ta unit test o‘tdi. Testlar API xaritalashini, eski JSON’dan o‘qishni va shu formatda qayta yozishni qamrab oldi. Samsung SM_S916N’da yangilashdan oldin olingan haqiqiy kesh nusxasidan kunlik, 7 kunlik va oylik jadvallarning barcha namoz vaqtlari tarmoq so‘rovisiz o‘qildi. PR #4 masterga qo‘shildi va Android Studio nusxasi yangilandi.

Qibla uchun sensorlar va magnit og‘ish hisobini `QiblaSensors` boshqaradi. Ekran yo‘nalish, aniqlik va tasdiq uchun ixcham ViewModel holatini oladi. Route sensorlarni ekran faol bo‘lganda ishga tushiradi va chiqilganda to‘xtatadi. Hudud tanlashdagi Android ruxsati va GPS so‘rovi `RegionSelectionRoute` ichida. Ekran izlash, natija yoki xato holatini ko‘rsatadi. Qo‘lda tanlash GPS so‘rovini bekor qiladi.

Qibla/GPS o‘zgarishida debug build va 32 ta unit test o‘tdi. Samsung SM_S916N’da Qibla animatsiyasi instrumentatsiya testi o‘tdi. Jonli sinovda Qibla yo‘nalishi yuklandi, 7 kunlik jadvalga o‘tilganda kompas yopildi va qaytilganda yana ishga tushdi. Android sensor jurnali magnetometr hamda aylanish sensori har safar ro‘yxatga olingani va chiqishda bo‘shatilganini ko‘rsatdi. «Hozirgi joylashuvim» hududni tanladi va qayta ochilganda GPS belgisi chiqdi. Aniq yo‘nalishga qo‘lda burib, telefonning haqiqiy azimutini alohida o‘lchash o‘tkazilmadi.

PR #5 masterga qo‘shildi va Android Studio nusxasi yangilandi. 4-bosqichda Tasbix sanoqi, kamaytirish, nollash va tarix `TasbihRepository` orqali boshqariladi. DataStore fayli `counter`, `counter` va `dhikr_history` kalitlari hamda tarixning `timestamp:count` formati saqlangan. Nollash ikkala qiymatni bitta saqlash operatsiyasida yangilaydi. Amaldagi tarix chegarasi 20 yozuv. Hudud sozlamalarini ajratish roadmap’da keyingi vazifa.

Tasbix repository o‘zgarishida debug build va 34 ta unit test o‘tdi. Unit testlar eski formatni qayta ochish, parallel nollashda bitta yozuv qo‘shilishi, 20 yozuv chegarasi va tez bosishlarni qamrab oldi. Samsung SM_S916N’da 4 ta instrumentatsiya testi o‘tdi. Ularda nollash dialogi, oq/qora mavzudagi animatsiyalar, tarix va 100 ta namunaviy yozuvni varaqlash tekshirildi. Haqiqiy sanoq va tarix yangilashdan oldin va keyin ekranda solishtirildi, asl DataStore fayli o‘zgarmadi.

PR #6 masterga qo‘shildi va Android Studio nusxasi yangilandi. Hudud tanlovi va GPS belgisi uchun saqlash `RegionSelectionRepository` orqali boshqariladi. DataStore fayli `region`, `select_region` va `select_region_from_gps` kalitlari saqlangan. Eski nomlar kanonik hududga avvalgidek moslanadi. Mahalliy debug build va 36 ta unit test o‘tdi. Testlar eski tanlov va GPS belgisini qayta ochish, qo‘lda tanlanganda belgini o‘chirish va yangi GPS tanlovini saqlashni qamrab oldi.

Samsung SM_S916N’da haqiqiy hudud tanlovi va «Hozirgi joylashuv» belgisi yangilanishdan oldin hamda APK o‘rnatilib ilova qayta ochilgandan keyin ekranda solishtirildi. Ikkalasi mos keldi. Asl DataStore fayli o‘zgarmadi. Shu o‘zgarish bilan 4-bosqich yakunlandi.

PR #7 masterga qo‘shildi va Android Studio nusxasi yangilandi. 5-bosqichda Compose BOM 2026.11.01, Kotlin va Compose compiler 2.4.21 hamda Lifecycle 2.11.1 tanlandi. Compose 1.13.0 talab qilgan Android SDK 37.1 build va CI’da ishlatiladi. Rangli soya Compose’ning Android `nativePaint` API’siga o‘tdi.

Yangilangan Gradle build Kotlin Gradle plugin va Compose compiler 2.4.21 ni tanlagani tasdiqlandi. Debug APK, AndroidTest APK va 36 ta unit test o‘tdi. Samsung SM_S916N’da oq va qora mavzuda Asosiy va 7 kunlik ekranlar, glass AppBar/BottomNav, ro‘yxatni varaqlash va karta soyasi ko‘rildi. Tasbix hamda Qiblaning 5 ta instrumentatsiya testi o‘tdi. Telefonning oldingi mavzusi tiklandi.

PR #8 masterga qo‘shildi va Android Studio nusxasi yangilandi. 2026-11-21 kuni Retrofit va converter-gson 3.0.0, Hilt plagini, Android kutubxonasi va protsessori 2.60.1, Haze va haze-glass 2.1.0 Maven Central’dagi eng so‘nggi barqaror nashrlar ekani tasdiqlandi. Shu uch kutubxonaning amaldagi versiyalari PR #8 dagi Samsung sinovida ishlatilgan. Birlashtirilgan master’da debug build va 36 ta unit test o‘tdi. Kutubxona sozlamalariga o‘zgartirish talab qilinmadi. 5-bosqich yakunlandi.

6-bosqichda Google Play telefon ilovalari uchun talab qilgan Android 16 (API 36) `targetSdk` sifatida tanlandi. `compileSdk` 37.1, `minSdk` 24. Release AAPT tekshiruvida yettita JPEG rasmining kengaytmasi .png ekani aniqlandi. Fayllar o‘sha resurs nomlari bilan .jpg qilindi. GitHub Actions APK’lar, release AAB va unit testlarni tekshiradi.

Release APK, release AAB, release lint va 36 ta unit test o‘tdi. AAB bundletool orqali tekshirildi, 16 KB zip alignment va to‘rtta 64-bit ELF kutubxonasida 16 KB LOAD alignment tasdiqlandi. Samsung SM_S916N Android 17 (API 37) va 4 KB sahifada mahalliy sinov imzosi qo‘yilgan non-debuggable release APK ishga tushdi. Asosiy, 7 va 30 kunlik ekranlar, AppBar/BottomNav insets, Qibla yo‘nalishi, Android orqaga ishorasi, mavjud joylashuv ruxsatlari, GPS orqali hudud tanlash va Tasbix tarixi tekshirildi. 16 KB qurilmada ishga tushirish alohida tekshiriladi.

PR #9 masterga qo‘shildi va Android Studio nusxasi fast-forward qilindi. 6-bosqich uchun [maxfiylik siyosati](PRIVACY_POLICY.md) va [Data safety javoblari](PLAY_DATA_SAFETY.md) loyihalari tayyorlandi. Maxfiylik matni sakkiz tilda Android resurslariga qo‘shildi. Tashqi jadval xizmatiga hudud va davr yuboriladi. Android geokodlash, Location SDK uzatmalari va jadval xizmatining log siyosati tasdiqlangach Data safety javoblari yakunlanadi.

Android 16 x86_64 emulyatorda haqiqiy sahifa o‘lchami `16384` bayt edi. Sahifa moslik rejimi o‘chirildi va mahalliy sinov imzosi qo‘yilgan release APK o‘rnatilib, asosiy Activity sovuq ishga tushdi. APK’ning `zipalign -P 16` sinovi va AAB ichidagi ARM64 hamda x86_64 kutubxonalarining 16 KB ELF tekshiruvi o‘tdi. Debug va release build, release AAB hamda 36 ta unit test o‘tdi. Alohida `lintRelease` oldingi kodda oltita xatoni ko‘rsatdi. Ular API 26 chaqiruvlari va `app_name` tarjimasi bilan bog‘liq.

PR #10 masterga qo‘shildi va Android Studio nusxasi fast-forward qilindi. Kalendar loyihadagi Java vaqt API desugaring’idan foydalanadi va ortiqcha API 26 cheklovlari olib tashlandi. Qibla tebranishi uchun Android 7.0 va 7.1 ga mos chaqiruv qo‘shildi. «Muslim App» tarjima qilinmaydigan brend nomi sifatida belgilandi. `lintRelease` 0 ta xato va avvalgi 49 ta ogohlantirish bilan o‘tdi. Debug APK, release APK, AAB va 36 ta unit test muvaffaqiyatli yakunlandi.

PR #11 da «Menyu → Maxfiylik siyosati» bandi dialogga ulandi. Android 16 emulyatorda standart tarmoq o‘chiq holda `PrivacyPolicyTest` o‘tdi. Test dialog sarlavhasi, to‘liq matni va yopilishini tekshiradi.

PR #11 masterga qo‘shildi va Android Studio nusxasi fast-forward qilindi. AAB til bo‘yicha ajratilmaydi va ilova ichidagi sakkiz til birga paketlanadi. Ishlatilmayotgan resurslar va bir xil launcher ikonka nusxalari olib tashlandi. Launcher’ning mavjud shaffof logosi mavzuli ikonka qatlami sifatida ishlatiladi. Til va mavzu sozlamalari AndroidX KTX orqali avvalgi saqlash kalitlariga yoziladi.

`lintRelease` 0 ta xato ko‘rsatdi, ogohlantirishlar 49 tadan 4 taga tushdi. `OldTargetApi` ogohlantirishi bilan `targetSdk 36` joriy Google Play talabiga mos holda saqlandi. Qolgan uchta `PluralsCandidate` qisqa vaqt birliklariga tegishli. Ularning sakkiz tildagi yozilishi son bilan o‘zgarmaydi. Debug APK, release APK, AAB va 36 ta unit test o‘tdi. AAB ichidagi sakkiz til va 16 KB joylashuvi hamda APK ichidagi mavzuli ikonka qatlami tekshirildi.

PR #12 masterga qo‘shildi va Android Studio nusxasi yangilandi. GitHub Actions `master`ga ochilgan PR’larda va `master`ga push qilinganda APK/AAB buildlari hamda unit testlar bilan birga `:app:lintRelease`ni ham ishga tushiradi.

Play Store’ga hozircha nashr qilinmaydi. Yuborishdan oldin tasdiqlangan maxfiylik siyosatining ommaviy URL’i, Data safety javoblari, upload kaliti bilan imzolangan AAB, Play App Signing va hisob turiga tegishli sinov talablari qoladi. [Target API](https://developer.android.com/google/play/requirements/target-sdk) · [16 KB](https://developer.android.com/guide/practices/page-sizes) · [App signing](https://developer.android.com/studio/publish/app-signing) · [Maxfiylik va Data safety](https://support.google.com/googleplay/android-developer/answer/10144311).

## Build va qurilmada tekshirish

AppBar soyasining pastki o‘tishi 68 dp gacha kengaytirildi. Debug build va unit testlar o‘tdi. Yangilangan APK Samsung SM_S916N’da o‘rnatilib, oq temada 7 kunlik ro‘yxat AppBar ostidan surilganda o‘tish tekshirildi.

Qora AppBar va BottomNav ranglari yangilangach debug build va unit testlar o‘tdi. 2026-11-21 kuni yangilangan APK Samsung SM_S916N’ga o‘rnatildi va Asosiy ekran qora temada oldingi skrinshot bilan solishtirildi. Fon nuqtalarida AppBar `#383F47` dan `#182028` ga, BottomNav `#646E77` dan `#293640` ga o‘zgargani tasdiqlandi.

Menyu va Sunset yangilanishida debug build va unit testlar o‘tdi. Samsung’da oq va qora Menyu ko‘rinishlari, qora mavzuning ilova qayta ochilganda saqlanishi, Tizim tanlovining belgilanishi va saqlanishi, til dialogi hamda «Ilova haqida» oynasi tekshirildi. Foydalanuvchi qolgan telefon tekshiruvlarini o‘zi bajarishini aytdi.

Joriy versiyalarni Gradle fayllaridan tekshiring. Oxirgi holatda compileSdk 37.1, minSdk 24, targetSdk 36. Compose BOM 2026.11.01, Kotlin va Compose compiler 2.4.21, Lifecycle 2.11.1. Haze va haze-glass 2.1.0. Android Studio JBR bilan build ishlatilgan.

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
