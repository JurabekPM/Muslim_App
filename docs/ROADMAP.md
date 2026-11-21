# Muslim App arxitektura auditi bo‘yicha joriy reja

Bu vaqtinchalik reja 2026-11-21 kungi arxitektura va texnologiya auditi tavsiyalarini birma-bir bajarishga mo‘ljallangan. Tavsiyalar yakunlangach reja yopiladi. Keyingi ilova g‘oyalari alohida rejalashtiriladi.

Har bosqich alohida branch va PR’da bajariladi. PR’da o‘zgarish, tekshiruv natijasi va qolgan ish ko‘rsatiladi. Tasdiqlangach masterga qo‘shiladi. Bosqichlarni tartib bilan qilamiz, shunda muammo chiqsa qaysi o‘zgarishdan kelgani ko‘rinib turadi.

## 1. Avtomatik tekshiruv

- [x] Masterga ochilgan PR’larda va master yangilanganda GitHub Actions orqali debug APK yig‘ish va unit testlarni ishga tushirish.
- [x] Birinchi GitHub Actions natijasini tekshirish va ushbu bosqichni masterga qo‘shish.

## 2. Namoz jadvali va kesh

- [x] Keshdan o‘qish, API’dan yuklash va saqlash qoidalarini repository’ga ajratish. ViewModel ekran holati va foydalanuvchi amallarini boshqaradi.
- [x] Kunlik, haftalik va oylik jadvallar uchun mavjud kesh yozuvlari bilan moslikni tekshirish. Kesh kalitlari, hudud sluglari va vaqt mintaqasini saqlash.
- [x] API xatosi, internet uzilishi, ilovani qayta ochish va qo‘lda yangilash holatlarini tekshirish.

## 3. Namoz modellari va ekran chegaralari

- [x] API va Gson’ga oid DTO’larni data qatlamiga joylash. Repository sof Kotlin domen modellarini qaytaradi.
- [x] Model xaritalashini alohida test qilish. Eski telefon keshidan o‘qish yoki mos migratsiyani sinash.
- [x] Qibla va hudud tanlashdagi sensor/GPS natijalarini ekranga ixcham holat sifatida uzatish. Platforma ruxsati va lifecycle boshqaruvi Android chegarasida qoladi.

## 4. Tasbix va hudud sozlamalarini saqlash

- [x] Tasbix sanoqi, nollash va tarix saqlashini repository’ga ko‘chirish. Nollash paytida tarix va sanoq bitta saqlash operatsiyasida yangilanadi.
- [x] Hudud tanlashning DataStore kalitlari va GPS belgisi saqlashini data qatlamiga ajratish. Avvalgi tanlovlar saqlanadi.
- [x] Samsung’da Tasbix dialoglari, raqam animatsiyasi, saqlangan sanoq va tarixni tekshirish. 100 ta namunaviy tarix yozuvi bilan varaqlash sinovi ham bor.

## 5. Barqaror kutubxona yangilanishlari

- [x] Compose BOM, Kotlin/Compose compiler va Lifecycle uchun mos barqaror versiyalarni o‘sha paytda qayta tekshirish. Build vositalarining mosligini tekshirib, kichik PR’larda yangilash.
- [x] Oq va qora mavzuda glass, navigatsiya va Tasbix animatsiyalarini Samsung’da qayta tekshirish.
- [x] Retrofit 3.0.0, Hilt 2.60.1 va Haze 2.1.0 Maven Central’dagi barqaror nashrlar bilan mosligi tasdiqlandi.
- Tasbix tarixiga uzoq muddatli saqlash yoki sana bo‘yicha qidiruv qo‘shilsa, Room’ga migratsiyani alohida baholash.

## 6. Play Store uchun tayyorgarlik

- [x] Google Play talabi tekshirildi va targetSdk 36 ga ko‘tarildi.
- [x] Release APK va AAB yig‘ildi. Samsung’da AppBar/BottomNav insets, mavjud joylashuv ruxsatlari va asosiy ekranlar sinovdan o‘tdi.
- [x] Maxfiylik siyosati va Data safety uchun loyihalar tayyorlandi. Menyuda maxfiylik matni sakkiz tilda ochiladi.
- [x] Android 16 x86_64 emulyatorda 16 KB sahifada release APK ishga tushdi. APK va AAB joylashuvi tekshirildi.
- [ ] Play Store’ga yuborishdan oldin maxfiylik siyosati uchun faol URL, xizmatlarning ma’lumot oqimi, Data safety javoblari, Play App Signing va hisobga tegishli sinov talablarini yakunlash.
