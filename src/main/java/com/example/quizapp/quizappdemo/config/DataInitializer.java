package com.example.quizapp.quizappdemo.config;

import com.example.quizapp.quizappdemo.model.Category;
import com.example.quizapp.quizappdemo.model.Question;
import com.example.quizapp.quizappdemo.model.Quiz;
import com.example.quizapp.quizappdemo.model.User;
import com.example.quizapp.quizappdemo.repository.CategoryRepository;
import com.example.quizapp.quizappdemo.repository.QuestionRepository;
import com.example.quizapp.quizappdemo.repository.QuizRepository;
import com.example.quizapp.quizappdemo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Uygulama ilk kez çalıştığında veritabanı boşsa demo amaçlı:
 *  - bir admin + bir kullanıcı
 *  - 6 kategori
 *  - her kategoride 4 soru (toplam 24 soru, havuzda)
 *  - her kategoriye bir başlangıç quizi (sorular o quize eklenmiş)
 * ekler.
 */
@Configuration
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           QuizRepository quizRepository,
                           QuestionRepository questionRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedUsers();
        migratePlainTextPasswords();
        ensureSuperAdmin();
        seedCategoriesQuestionsQuizzes();
    }

    private void seedUsers() {
        if (userRepository.count() > 0) return;
        userRepository.save(new User("Admin", "admin@quiz.com",
                passwordEncoder.encode("admin123"), "ADMIN", true));
        userRepository.save(new User("Demo Kullanıcı", "user@quiz.com",
                passwordEncoder.encode("user123"), "USER"));
    }

    /** Eski düz metin şifreleri BCrypt'e çevirir (Spring Security geçişi). */
    private void migratePlainTextPasswords() {
        for (User user : userRepository.findAll()) {
            String pw = user.getPassword();
            if (pw != null && !pw.startsWith("$2a$") && !pw.startsWith("$2b$")) {
                user.setPassword(passwordEncoder.encode(pw));
                userRepository.save(user);
            }
        }
    }

 
    private void ensureSuperAdmin() {
        boolean alreadyExists = userRepository.findAll().stream().anyMatch(User::isSuperAdmin);
        if (alreadyExists) return;

        userRepository.findByEmail("admin@quiz.com").ifPresent(seedAdmin -> {
            seedAdmin.setSuperAdmin(true);
            if (seedAdmin.getRole() == null || !"ADMIN".equals(seedAdmin.getRole())) {
                seedAdmin.setRole("ADMIN");
            }
            userRepository.save(seedAdmin);
        });
    }

    private void seedCategoriesQuestionsQuizzes() {
        if (categoryRepository.count() > 0) return;

        // 1) Kategoriler
        Category genelKulturCat = categoryRepository.save(new Category(
                "Genel Kültür", "Türkiye ve dünyaya dair temel bilgiler"));
        Category popKulturCat = categoryRepository.save(new Category(
                "Popüler Kültür & Eğlence", "Film, dizi, kitap ve eğlence dünyası"));
        Category sporCat = categoryRepository.save(new Category(
                "Spor", "Futbol, basketbol, olimpiyatlar"));
        Category bilimCat = categoryRepository.save(new Category(
                "Bilim & Teknoloji", "Temel bilimler ve teknoloji"));
        Category sanatCat = categoryRepository.save(new Category(
                "Sanat & Edebiyat", "Resim, edebiyat ve klasik eserler"));
        Category cografyaCat = categoryRepository.save(new Category(
                "Coğrafya & Seyahat", "Dünya ve Türkiye coğrafyası"));

        // 2) Soru havuzu (her kategoride 4 soru)
        List<Question> genelKulturQs = questionRepository.saveAll(List.of(
                new Question("Türkiye'nin başkenti neresidir?",
                        "İstanbul", "İzmir", "Ankara", "Bursa",
                        "C", genelKulturCat),
                new Question("Türkiye'de kaç il vardır?",
                        "67", "75", "81", "85",
                        "C", genelKulturCat),
                new Question("Bir yıl kaç gündür (artık olmayan yıl)?",
                        "364", "365", "366", "360",
                        "B", genelKulturCat),
                new Question("Cumhuriyetin ilanından önceki Osmanlı yönetim biçimi neydi?",
                        "Krallık", "Monarşi", "Saltanat", "Cumhuriyet",
                        "C", genelKulturCat)));

        List<Question> popKulturQs = questionRepository.saveAll(List.of(
                new Question("Harry Potter kitap serisinin yazarı kimdir?",
                        "Stephen King", "J.K. Rowling", "George R.R. Martin", "Suzanne Collins",
                        "B", popKulturCat),
                new Question("\"Titanic\" filmindeki başrol erkek oyuncu kimdir?",
                        "Brad Pitt", "Leonardo DiCaprio", "Tom Cruise", "Johnny Depp",
                        "B", popKulturCat),
                new Question("Game of Thrones dizisi hangi kitap serisinden uyarlanmıştır?",
                        "Yüzüklerin Efendisi", "A Song of Ice and Fire", "Witcher", "Dune",
                        "B", popKulturCat),
                new Question("\"Avatar\" (2009) filminin yönetmeni kimdir?",
                        "Steven Spielberg", "Christopher Nolan", "James Cameron", "Peter Jackson",
                        "C", popKulturCat)));

        List<Question> sporQs = questionRepository.saveAll(List.of(
                new Question("Bir futbol maçının normal süresi kaç dakikadır?",
                        "60", "80", "90", "120",
                        "C", sporCat),
                new Question("Bir basketbol takımı sahada kaç oyuncuyla oynar?",
                        "4", "5", "6", "7",
                        "B", sporCat),
                new Question("Yaz Olimpiyat Oyunları kaç yılda bir düzenlenir?",
                        "2", "3", "4", "5",
                        "C", sporCat),
                new Question("Futbolda VAR ne anlama gelir?",
                        "Video Assistant Referee", "Very Active Referee",
                        "Virtual Assistant Replay", "Video Action Review",
                        "A", sporCat)));

        List<Question> bilimQs = questionRepository.saveAll(List.of(
                new Question("Suyun kimyasal formülü nedir?",
                        "CO2", "O2", "H2O", "NaCl",
                        "C", bilimCat),
                new Question("Bilgisayarın \"beyni\" olarak adlandırılan parça hangisidir?",
                        "RAM", "CPU", "GPU", "SSD",
                        "B", bilimCat),
                new Question("Albert Einstein'ın ünlü denklemi nedir?",
                        "F = ma", "E = mc²", "a² + b² = c²", "PV = nRT",
                        "B", bilimCat),
                new Question("Güneş sistemindeki en büyük gezegen hangisidir?",
                        "Mars", "Jüpiter", "Dünya", "Satürn",
                        "B", bilimCat)));

        List<Question> sanatQs = questionRepository.saveAll(List.of(
                new Question("\"Suç ve Ceza\" romanı kimin eseridir?",
                        "Tolstoy", "Dostoyevski", "Çehov", "Puşkin",
                        "B", sanatCat),
                new Question("Mona Lisa tablosunu kim yapmıştır?",
                        "Picasso", "Leonardo da Vinci", "Van Gogh", "Michelangelo",
                        "B", sanatCat),
                new Question("İstiklal Marşı'nın yazarı kimdir?",
                        "Yahya Kemal Beyatlı", "Nazım Hikmet",
                        "Mehmet Akif Ersoy", "Necip Fazıl Kısakürek",
                        "C", sanatCat),
                new Question("\"Sefiller\" (Les Misérables) romanı kimin eseridir?",
                        "Honoré de Balzac", "Victor Hugo", "Alexandre Dumas", "Émile Zola",
                        "B", sanatCat)));

        List<Question> cografyaQs = questionRepository.saveAll(List.of(
                new Question("Dünyanın en yüksek dağı hangisidir?",
                        "K2", "Everest", "Kilimanjaro", "Mont Blanc",
                        "B", cografyaCat),
                new Question("Eyfel Kulesi hangi şehirdedir?",
                        "Roma", "Londra", "Paris", "Berlin",
                        "C", cografyaCat),
                new Question("Türkiye'nin en uzun nehri hangisidir?",
                        "Sakarya", "Fırat", "Kızılırmak", "Yeşilırmak",
                        "C", cografyaCat),
                new Question("Yüz ölçümü bakımından dünyanın en büyük kıtası hangisidir?",
                        "Afrika", "Asya", "Kuzey Amerika", "Avrupa",
                        "B", cografyaCat)));

        // 3) Quizler — her kategoriye bir başlangıç quizi.
        //    Aynı kategoride başka quizler oluşturup farklı soru altkümeleri seçilebilir.
        seedQuiz("Genel Kültür Quizi", genelKulturCat,
                "Türkiye ve dünyaya dair temel sorular", genelKulturQs);
        seedQuiz("Popüler Kültür & Eğlence Quizi", popKulturCat,
                "Film, dizi ve kitap dünyasından sorular", popKulturQs);
        seedQuiz("Spor Quizi", sporCat,
                "Futbol, basketbol ve olimpiyatlara dair sorular", sporQs);
        seedQuiz("Bilim & Teknoloji Quizi", bilimCat,
                "Temel bilim ve teknoloji bilgileri", bilimQs);
        seedQuiz("Sanat & Edebiyat Quizi", sanatCat,
                "Resim, edebiyat ve klasik eserler", sanatQs);
        seedQuiz("Coğrafya & Seyahat Quizi", cografyaCat,
                "Dünya ve Türkiye coğrafyası", cografyaQs);
    }

    private void seedQuiz(String title, Category category, String description, List<Question> questions) {
        Quiz quiz = new Quiz(title, category, description, 60);
        quiz.getQuestions().addAll(questions);
        quizRepository.save(quiz);
    }
}
