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

- [ ] Compose BOM, Kotlin/Compose compiler va Lifecycle uchun mos barqaror versiyalarni o‘sha paytda qayta tekshirish. Build vositalarining mosligini tekshirib, kichik PR’larda yangilash.
- [ ] Oq va qora mavzuda glass, navigatsiya va Tasbix animatsiyalarini Samsung’da qayta tekshirish.
- [ ] Retrofit, Hilt va Haze’ni aniq ehtiyoj yoki moslik talabi tug‘ilganda yangilash. Tasbix tarixiga uzoq muddatli saqlash yoki sana bo‘yicha qidiruv qo‘shilsa, Room’ga migratsiyani alohida baholash.

## 6. Play Store uchun tayyorgarlik

- [ ] Nashr rejalashtirilsa, targetSdk’ni Google Play talabi va amaldagi Android versiyasiga mos ko‘tarish. Ish boshlanishida talabni qayta tekshirish.
- [ ] Release build, AppBar/BottomNav insets, ruxsatlar va asosiy ekranlarni sinash. Nashrga tayyorlash va nashr qilish qarorini alohida olish.
