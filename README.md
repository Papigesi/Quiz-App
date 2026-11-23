# QuizApp Demo - Online Quiz Uygulaması

Java Spring Boot ile yazılmış **saf REST API** + **statik HTML/JS frontend**'den oluşan bir online quiz uygulaması.

## Veri Modeli

```
Category (1) ──< (N) Quiz   >──┐
                               │  M:N  (quiz_questions)
Category (1) ──< (N) Question >─┘

QuizResult ──> User
QuizResult ──> Quiz (nullable)
```

- **Category**: Kategori ayrı bir entity. Ad benzersizdir. Admin panelinden eklenir/silinir; silmek için içinde quiz olmaması gerekir.
- **Question**: Bir kategoriye ait (`@ManyToOne Category`) çoktan seçmeli soru. Havuzda **bağımsız** yaşar; herhangi bir quize bağlı olmayabilir veya aynı kategoride birden çok quize ait olabilir.
- **Quiz**: Bir kategoriye ait (`@ManyToOne Category`), başlığı, açıklaması ve süre limiti olan bir sınav. Soruları M:N ilişkiyle (`quiz_questions` join tablosu) tutar. Quize sadece aynı kategorideki sorular eklenebilir.
- **QuizResult**: Bir kullanıcının bir quizi çözmesinden oluşan kayıt. Puan sunucuda hesaplanır.

### Akış
1. Admin önce **kategori** oluşturur.
2. Admin **soru havuzuna** sorular ekler (her soru bir kategoriye düşer).
3. Admin **quiz** oluşturur (başlık + kategori + süre).
4. Admin quiz için "**Soruları Yönet**" ekranını açar, havuzdan istediği soruları o quize ekler / çıkarır. Bir soru aynı kategoride istenildiği kadar quizde kullanılabilir.
5. Kullanıcı bir quiz seçtiğinde sorular sunucu tarafında **her seferinde rastgele sıralanır**.

## Özellikler

### Kullanıcı (USER)
- Kayıt ol / giriş yap
- Kategoriye göre gruplanmış quiz listesinden birini seç ve çöz
- Sorular **her çözüm denemesinde rastgele sıralanır** (sunucu tarafı shuffle)
- Quiz'e özel geri sayım sayacı (süre dolunca otomatik gönderir)
- Skoru ve cevap dağılımını grafik (Chart.js) ile gör
- Geçmiş sonuçlarını ve skor eğilim grafiğini incele

### Admin (ADMIN)
- **Kategori** ekle / sil / güncelle (sadece içinde quiz olmayan kategori silinebilir)
- **Soru havuzu**: kategoriye bağlı bağımsız sorular ekle / sil / güncelle
- **Quiz** ekle / sil / güncelle (kategoriyi sadece var olan kategorilerden seçer; quiz silinirse sorular havuzda kalır)
- **Quiz İçerik Yönetimi**: bir quize havuzdan soru ekle / çıkar (sadece aynı kategoriden)
- Tüm kullanıcıları görüntüle ve sil
- Tüm kullanıcı sonuçlarını listele, quiz bazlı ortalama puan grafiği

### Süper Admin (sadece ana admin)
Uygulama ilk açılışında oluşturulan **tek bir admin** süper admin yetkisine sahip olarak işaretlenir (`User.superAdmin = true`).
Sadece bu kullanıcı:
- Diğer kullanıcıları **admin yapabilir** (`POST /api/users/{id}/promote`)
- Diğer adminlerin **admin yetkisini geri alabilir** (`POST /api/users/{id}/demote`)
- Kendi rolünü değiştiremez ve **silinemez**

Diğer adminler kategori/quiz/soru/kullanıcı CRUD'unu yapabilir ama promote/demote işlemlerine erişemez.
Süper admin yetkisi DB'de kalıcı bir bayrak (`users.super_admin`) olarak tutulur ve UI üzerinde devredilemez.

### Ortak
- **Kategori bazlı liderlik tablosu**: her kategori için ayrı sıralama, her kullanıcının o kategorideki en yüksek puanı dikkate alınır. Üstte "her kategorinin lideri" özet bar grafiği

## Mimari

```
src/main/java/com/example/quizapp/quizappdemo/
├── QuizappdemoApplication.java          # Boot girişi
├── config/
│   └── DataInitializer.java             # İlk açılışta demo veri
├── controller/                          # @RestController katmanı (saf REST)
│   ├── AuthController.java              # /api/auth/{login,register}
│   ├── UserController.java              # /api/users
│   ├── CategoryController.java          # /api/categories (CRUD)
│   ├── QuizController.java              # /api/quizzes (CRUD + sorular)
│   ├── QuestionController.java          # /api/questions (admin)
│   └── QuizResultController.java        # /api/results
├── dto/                                 # İstek/yanıt nesneleri
├── exception/                           # ResourceNotFound/BadRequest/Unauthorized
│   └── GlobalExceptionHandler.java      # @RestControllerAdvice (JSON hata yanıtı)
├── model/                               # JPA entity'leri (User, Category, Quiz, Question, QuizResult)
├── repository/                          # Spring Data JPA arayüzleri
└── service/                             # İş mantığı (Lombok'suz)

src/main/resources/
├── application.properties
└── static/                              # Frontend (Spring Boot otomatik servis eder)
    ├── *.html
    ├── css/style.css
    └── js/{api,auth,ui}.js
```

### Önemli kararlar

- **REST**, MVC değil: `@RestController` kullanılır, hiçbir Thymeleaf/JSP şablonu yok. Frontend ayrı statik dosyalar olarak `src/main/resources/static/` altında durur.
- **Lombok yok**: getter/setter ve constructor'lar elle yazıldı.
- **Spring Security (minimal)**:
  - `SecurityConfig` — HTTP oturumu (session cookie) ile `/api/**` koruması
  - `QuizUserDetailsService` — e-posta ile kullanıcı yükleme
  - `BCryptPasswordEncoder` — şifre hash
  - `ROLE_ADMIN` / `ROLE_USER` — admin yazma uçları `hasRole("ADMIN")`
  - Login: `AuthenticationManager` + oturum; frontend `fetch(..., { credentials: "include" })`
  - UI için kullanıcı özeti hâlâ `localStorage`'da (isim, rol); yetki sunucu oturumundan doğrulanır
- **Puan sunucuda hesaplanır**: Frontend yalnızca seçilen cevapları gönderir; backend kendi DB'sindeki doğru cevaplarla karşılaştırır ve puanı (0-100) hesaplar.
- **Doğru cevap gizliliği**: Kullanıcı quiz çözmek için sorulara baktığında `correctAnswer` alanı yanıttan çıkarılır (`@JsonInclude(NON_NULL)` + service katmanında temizleme).
- **Quiz silinince**: İçerdiği sorular cascade ile birlikte silinir. Sonuçlar tablosundaki referans null'a düşer ama denormalize tutulan `quizTitle`/`category` kolonları sayesinde geçmiş sonuçlar yine "Spor Quizi 1 (silinmiş)" gibi gösterilebilir.

## REST Uç Noktaları

| Metot  | URL                                              | Açıklama                                                       |
|--------|--------------------------------------------------|----------------------------------------------------------------|
| POST   | `/api/auth/register`                             | Yeni kullanıcı (otomatik USER rolü, şifre BCrypt)              |
| POST   | `/api/auth/login`                                | Giriş (Spring Security oturumu açılır)                         |
| POST   | `/api/auth/logout`                               | Oturumu kapat                                                  |
| GET    | `/api/categories`                                | Tüm kategoriler                                                |
| GET    | `/api/categories/{id}`                           | Tek kategori                                                   |
| POST   | `/api/categories`                                | Yeni kategori (admin)                                          |
| PUT    | `/api/categories/{id}`                           | Kategori güncelle                                              |
| DELETE | `/api/categories/{id}`                           | Kategori sil (içinde quiz varsa reddeder)                      |
| GET    | `/api/quizzes`                                   | Tüm quizler                                                    |
| GET    | `/api/quizzes/{id}`                              | Tek quiz detayı                                                |
| GET    | `/api/quizzes/{id}/questions`                    | Quizin soruları, **rastgele sıralı**, kullanıcı için (cevap gizli) |
| GET    | `/api/quizzes/{id}/questions/admin`              | Quizin soruları (admin için, doğru cevap dahil)                |
| GET    | `/api/quizzes/{id}/available-questions`          | Bu quize eklenebilecek havuz (aynı kategori, henüz eklenmemiş) |
| POST   | `/api/quizzes/{quizId}/questions/{questionId}`   | Havuzdan bir soruyu quize ekle                                 |
| DELETE | `/api/quizzes/{quizId}/questions/{questionId}`   | Soruyu quizden çıkar (havuzda kalır)                           |
| POST   | `/api/quizzes`                                   | Yeni quiz oluştur (admin)                                      |
| PUT    | `/api/quizzes/{id}`                              | Quiz güncelle                                                  |
| DELETE | `/api/quizzes/{id}`                              | Quiz sil (sorular havuzda kalır)                               |
| GET    | `/api/questions/admin`                           | Soru havuzunun tamamı (admin); `?categoryId=` ile filtre       |
| POST   | `/api/questions`                                 | Yeni soru havuza ekle (body'de categoryId zorunlu)             |
| PUT    | `/api/questions/{id}`                            | Soruyu güncelle                                                |
| DELETE | `/api/questions/{id}`                            | Soruyu havuzdan sil (bağlı tüm quizlerden de düşer)            |
| POST   | `/api/results/submit`                            | Quiz cevaplarını gönder, sunucu skoru hesaplar                 |
| GET    | `/api/results/user/{userId}`                     | Bir kullanıcının geçmişi                                       |
| GET    | `/api/results/leaderboard`                       | Kategori → liderlik listesi map'i, her kategoride kullanıcı başı en iyi |
| GET    | `/api/results`                                   | Tüm sonuçlar (admin)                                           |
| GET    | `/api/users`                                     | Tüm kullanıcılar                                               |
| DELETE | `/api/users/{id}`                                | Kullanıcı sil (süper admin silinemez)                          |
| POST   | `/api/users/{id}/promote`                        | Hedef kullanıcıyı admin yap (sadece süper admin, oturum gerekli) |
| POST   | `/api/users/{id}/demote`                         | Hedef admin'in yetkisini geri al (sadece süper admin)          |

## Çalıştırma

### MySQL ile (varsayılan)
1. MySQL'i başlat ve veritabanı/şifreyi ayarla. `application.properties`:
   ```
   spring.datasource.url=jdbc:mysql://localhost:3306/quiz_project?createDatabaseIfNotExist=true
   spring.datasource.username=besdproject
   spring.datasource.password=besdproject
   ```
2. Çalıştır:
   ```powershell
   ./mvnw spring-boot:run
   ```
3. Tarayıcıdan `http://localhost:5000` aç.

### H2 ile (hızlı test, MySQL gerek yok)
`application.properties` içinde MySQL satırlarını yorum yapıp H2 bloğunu aktif et. Veriler bellekte tutulur (uygulama her kapandığında silinir).

### Şema göçü notu
Önceki sürümlerden geliyorsan, `Question` artık tek bir quize bağlı değil (M:N `quiz_questions` join tablosu kullanılır), bu yüzden eski `questions.quiz_id` kolonu artık tanımlı değil. MySQL'de mevcut DB varsa en temizi tabloları silmek:

```sql
DELETE FROM quiz_results;
DROP TABLE IF EXISTS quiz_questions;
DROP TABLE IF EXISTS questions;
DROP TABLE IF EXISTS quizzes;
DROP TABLE IF EXISTS categories;
```

Sonra `./mvnw spring-boot:run` ile başlatınca Hibernate hepsini yeniden oluşturur.
H2 kullanıyorsan uygulamayı yeniden başlatman yeterli (her açılışta sıfırdan oluşur).

Süper admin desteği için `users` tablosuna yeni bir `super_admin` kolonu eklendi. Hibernate `ddl-auto=update` ile bunu otomatik ekler (nullable). Eski veritabanında zaten `admin@quiz.com` kullanıcısı varsa, uygulama açılışında `DataInitializer.ensureSuperAdmin()` bu kullanıcıyı otomatik olarak süper admin olarak işaretler — manuel müdahale gerekmez.

## Demo Hesaplar

Veritabanı boş açıldığında `DataInitializer` otomatik olarak şu kullanıcıları oluşturur:

- **Admin (Süper Admin)**: `admin@quiz.com` / `admin123` — bu hesap diğer kullanıcıları admin yapabilir ve silinemez
- **Kullanıcı**: `user@quiz.com` / `user123`

Ayrıca 6 kategori (Genel Kültür, Popüler Kültür & Eğlence, Spor, Bilim & Teknoloji, Sanat & Edebiyat, Coğrafya & Seyahat) ve her kategoride 4 soru (toplam 24 soru) havuza eklenir. Her kategoriye bir başlangıç quizi oluşturulur ve o kategorinin 4 sorusu o quize bağlanır. Admin "Soruları Yönet" ekranından sorularla istediği gibi farklı quizler kurabilir; bir soru aynı kategoride birden çok quizde yer alabilir.

## Lombok hakkında not

Bu projede **Lombok kullanılmıyor**. Sadece getter/setter ve constructor üretmek için Lombok eklemek ödevin gereksinim listesinde değil ve kütüphane projeyi ekstra IDE plugin'i / annotation processor zincirine bağımlı yapar. Bunun yerine:
- Constructor injection elle yazıldı (`new UserController(UserService userService)`).
- POJO'ların getter/setter'ları (model, dto sınıfları) IDE tarafından üretilebilir; burada açıkça yer alıyor.
