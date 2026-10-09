package com.ecommerce.app.config;

import com.ecommerce.app.entity.Product;
import com.ecommerce.app.entity.Role;
import com.ecommerce.app.entity.User;
import com.ecommerce.app.repository.ProductRepository;
import com.ecommerce.app.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@Profile("dev")
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            UserRepository userRepository,
            ProductRepository productRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        initializeUsers();
        initializeProducts();

        System.out.println("================================");
        System.out.println("ShopHub Demo Data Loaded");
        System.out.println("Admin: adminDataInit@example.com");
        System.out.println("Password: admin123");
        System.out.println("User: userDataInit@example.com");
        System.out.println("Password: user123");
        System.out.println("================================");
    }

    // =========================================================
    // USERS
    // =========================================================

    private void initializeUsers() {

        if (userRepository.findByEmail("adminDataInit@example.com").isEmpty()) {

            User admin = new User();

            admin.setUsername("adminDataInit");
            admin.setEmail("adminDataInit@example.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            admin.setCreatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());

            userRepository.save(admin);
        }

        if (userRepository.findByEmail("userDataInit@example.com").isEmpty()) {

            User user = new User();

            user.setUsername("userDataInit");
            user.setEmail("userDataInit@example.com");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setRole(Role.USER);
            user.setActive(true);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());

            userRepository.save(user);
        }
    }

    // =========================================================
    // PRODUCTS
    // =========================================================

    private void initializeProducts() {

        // =========================
        // ELECTRONICS
        // =========================

        saveProductIfNotExists(
                "Dell Inspiron Laptop",
                "15.6 inch laptop with Intel Core i5 processor",
                649.99,
                20,
                "Electronics",
                "Dell",
                "https://images.unsplash.com/photo-1496181133206-80ce9b88a853"
        );

        saveProductIfNotExists(
                "Sony Wireless Headphones",
                "Noise cancelling wireless headphones with deep bass",
                129.99,
                35,
                "Electronics",
                "Sony",
                "https://images.unsplash.com/photo-1505740420928-5e560c06d30e"
        );

        saveProductIfNotExists(
                "Samsung Galaxy Smartphone",
                "Modern smartphone with AMOLED display and powerful processor",
                599.99,
                25,
                "Electronics",
                "Samsung",
                "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9"
        );

        saveProductIfNotExists(
                "Mechanical Gaming Keyboard",
                "RGB mechanical keyboard designed for gaming and productivity",
                69.99,
                40,
                "Electronics",
                "Logitech",
                "https://images.unsplash.com/photo-1587829741301-dc798b83add3"
        );

        saveProductIfNotExists(
                "Wireless Gaming Mouse",
                "Ergonomic wireless mouse with precision optical sensor",
                39.99,
                45,
                "Electronics",
                "Logitech",
                "https://images.unsplash.com/photo-1527814050087-3793815479db"
        );

        // =========================
        // CLOTHING
        // =========================

        saveProductIfNotExists(
                "Classic Cotton T-Shirt",
                "Comfortable regular fit cotton t-shirt for everyday wear",
                19.99,
                80,
                "Clothing",
                "UrbanStyle",
                "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab"
        );

        saveProductIfNotExists(
                "Slim Fit Blue Jeans",
                "Classic slim fit denim jeans with stretch fabric",
                39.99,
                60,
                "Clothing",
                "DenimCo",
                "https://images.unsplash.com/photo-1542272604-787c3835535d"
        );

        saveProductIfNotExists(
                "Casual Hoodie",
                "Soft fleece hoodie suitable for casual everyday outfits",
                34.99,
                45,
                "Clothing",
                "UrbanStyle",
                "https://images.unsplash.com/photo-1556821840-3a63f95609a7"
        );

        saveProductIfNotExists(
                "Women's Summer Dress",
                "Lightweight and comfortable summer dress",
                44.99,
                35,
                "Clothing",
                "FashionHub",
                "https://images.unsplash.com/photo-1496747611176-843222e1e57c"
        );

        saveProductIfNotExists(
                "Formal Office Shirt",
                "Premium cotton formal shirt suitable for office and business wear",
                29.99,
                50,
                "Clothing",
                "ClassicWear",
                "https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf"
        );

        // =========================
        // BOOKS
        // =========================

        saveProductIfNotExists(
                "Java Programming Guide",
                "Complete guide to Java programming for beginners and developers",
                29.99,
                50,
                "Books",
                "TechBooks",
                "https://images.unsplash.com/photo-1544947950-fa07a98d237f"
        );

        saveProductIfNotExists(
                "Clean Code",
                "A practical guide to writing clean and maintainable software",
                34.99,
                35,
                "Books",
                "ProgrammingPress",
                "https://images.unsplash.com/photo-1532012197267-da84d127e765"
        );

        saveProductIfNotExists(
                "Database Management Systems",
                "Introduction to relational databases, SQL and database design",
                27.99,
                30,
                "Books",
                "TechBooks",
                "https://images.unsplash.com/photo-1543002588-bfa74002ed7e"
        );

        saveProductIfNotExists(
                "Python Programming",
                "Beginner friendly introduction to Python programming",
                24.99,
                40,
                "Books",
                "CodeWorld",
                "https://images.unsplash.com/photo-1515879218367-8466d910aaa4"
        );

        saveProductIfNotExists(
                "Web Development Fundamentals",
                "Learn HTML, CSS, JavaScript and modern web development",
                31.99,
                30,
                "Books",
                "WebPress",
                "https://images.unsplash.com/photo-1495446815901-a7297e633e8d"
        );

        // =========================
        // HOME & KITCHEN
        // =========================

        saveProductIfNotExists(
                "Coffee Maker",
                "Automatic coffee maker for home and office use",
                89.99,
                25,
                "Home & Kitchen",
                "BrewMaster",
                "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085"
        );

        saveProductIfNotExists(
                "Stainless Steel Cookware Set",
                "Premium stainless steel cookware set for everyday cooking",
                119.99,
                20,
                "Home & Kitchen",
                "HomeChef",
                "https://images.unsplash.com/photo-1556911220-e15b29be8c8f"
        );

        saveProductIfNotExists(
                "Electric Mixer",
                "Powerful electric mixer for baking and cooking",
                54.99,
                30,
                "Home & Kitchen",
                "KitchenPro",
                "https://images.unsplash.com/photo-1570222094114-d054a817e56b"
        );

        saveProductIfNotExists(
                "Ceramic Dinner Set",
                "Elegant ceramic dinner set for everyday dining",
                69.99,
                25,
                "Home & Kitchen",
                "HomeStyle",
                "https://images.unsplash.com/photo-1603199506016-b9a594b593c0"
        );

        saveProductIfNotExists(
                "Insulated Water Bottle",
                "Stainless steel insulated bottle that keeps drinks hot or cold",
                24.99,
                60,
                "Home & Kitchen",
                "HydroMax",
                "https://images.unsplash.com/photo-1602143407151-7111542de6e8"
        );

        // =========================
        // BEAUTY
        // =========================

        saveProductIfNotExists(
                "Vitamin C Face Serum",
                "Lightweight vitamin C serum for a bright and refreshed appearance",
                18.99,
                45,
                "Beauty",
                "GlowCare",
                "https://images.unsplash.com/photo-1556229010-6c3f2c9ca5f8"
        );

        saveProductIfNotExists(
                "Hydrating Face Moisturizer",
                "Daily moisturizer designed to hydrate and soften skin",
                14.99,
                50,
                "Beauty",
                "SkinGlow",
                "https://images.unsplash.com/photo-1611930022073-b7a4ba5fcccd"
        );

        saveProductIfNotExists(
                "Herbal Shampoo",
                "Gentle herbal shampoo for everyday hair care",
                12.99,
                55,
                "Beauty",
                "NatureCare",
                "https://images.unsplash.com/photo-1556228578-8c89e6adf883"
        );

        saveProductIfNotExists(
                "Daily Sunscreen SPF 50",
                "Lightweight daily sunscreen with broad spectrum protection",
                16.99,
                60,
                "Beauty",
                "SunCare",
                "https://images.unsplash.com/photo-1556228720-195a672e8a03"
        );

        saveProductIfNotExists(
                "Body Care Gift Set",
                "Complete body care set suitable for everyday personal care",
                29.99,
                25,
                "Beauty",
                "PureCare",
                "https://images.unsplash.com/photo-1598440947619-2c35fc9aa908"
        );

        // =========================
        // SPORTS
        // =========================

        saveProductIfNotExists(
                "Running Shoes",
                "Comfortable lightweight sports running shoes",
                79.99,
                40,
                "Sports",
                "RunPro",
                "https://images.unsplash.com/photo-1542291026-7eec264c27ff"
        );

        saveProductIfNotExists(
                "Yoga Mat",
                "Non-slip exercise mat suitable for yoga and home workouts",
                24.99,
                50,
                "Sports",
                "FitLife",
                "https://images.unsplash.com/photo-1601925260368-ae2f83cf8b7f"
        );

        saveProductIfNotExists(
                "Football",
                "Durable training football suitable for outdoor games",
                29.99,
                35,
                "Sports",
                "SportMax",
                "https://images.unsplash.com/photo-1579952363873-27f3bade9f55"
        );

        saveProductIfNotExists(
                "Cricket Bat",
                "Professional style cricket bat for practice and matches",
                89.99,
                20,
                "Sports",
                "ProCricket",
                "https://images.unsplash.com/photo-1531415074968-036ba1b575da"
        );

        saveProductIfNotExists(
                "Adjustable Dumbbells",
                "Adjustable dumbbells for strength training at home",
                59.99,
                25,
                "Sports",
                "FitLife",
                "https://images.unsplash.com/photo-1583454110551-21f2fa2afe61"
        );

        // =========================
        // GROCERY
        // =========================

        saveProductIfNotExists(
                "Premium Basmati Rice",
                "Long grain premium basmati rice for everyday meals",
                14.99,
                100,
                "Grocery",
                "DailyHarvest",
                "https://images.unsplash.com/photo-1586201375761-83865001e31c"
        );

        saveProductIfNotExists(
                "Arabica Coffee Beans",
                "Premium roasted Arabica coffee beans",
                18.99,
                70,
                "Grocery",
                "CoffeeHouse",
                "https://images.unsplash.com/photo-1447933601403-0c6688de566e"
        );

        saveProductIfNotExists(
                "Whole Wheat Pasta",
                "Healthy whole wheat pasta suitable for quick meals",
                5.99,
                80,
                "Grocery",
                "DailyHarvest",
                "https://images.unsplash.com/photo-1551183053-bf91a1d81141"
        );

        saveProductIfNotExists(
                "Organic Honey",
                "Natural honey suitable for beverages and breakfast",
                9.99,
                60,
                "Grocery",
                "NatureFarm",
                "https://images.unsplash.com/photo-1587049352846-4a222e784d38"
        );

        saveProductIfNotExists(
                "Mixed Dry Fruits",
                "Premium selection of almonds, cashews, raisins and walnuts",
                15.99,
                45,
                "Grocery",
                "HealthyBite",
                "https://images.unsplash.com/photo-1599599810694-b5ac8c6c7c2f"
        );

        // =========================
        // ACCESSORIES
        // =========================

        saveProductIfNotExists(
                "Classic Wrist Watch",
                "Elegant analog wrist watch suitable for casual and formal wear",
                49.99,
                30,
                "Accessories",
                "TimeCraft",
                "https://images.unsplash.com/photo-1524805444758-089113d48a6d"
        );

        saveProductIfNotExists(
                "Leather Wallet",
                "Compact leather wallet with multiple card slots",
                24.99,
                50,
                "Accessories",
                "UrbanCarry",
                "https://images.unsplash.com/photo-1627123424574-724758594e93"
        );

        saveProductIfNotExists(
                "Travel Backpack",
                "Spacious backpack suitable for travel, college and work",
                44.99,
                35,
                "Accessories",
                "TravelPro",
                "https://images.unsplash.com/photo-1553062407-98eeb64c6a62"
        );

        saveProductIfNotExists(
                "Classic Sunglasses",
                "Stylish sunglasses with a lightweight frame",
                19.99,
                60,
                "Accessories",
                "VisionStyle",
                "https://images.unsplash.com/photo-1511499767150-a48a237f0083"
        );

        saveProductIfNotExists(
                "Casual Leather Belt",
                "Classic adjustable leather belt for everyday wear",
                17.99,
                55,
                "Accessories",
                "UrbanCarry",
                "https://images.unsplash.com/photo-1624222247344-550fb60583dc"
        );
    }

    // =========================================================
    // SAVE PRODUCT ONLY IF IT DOES NOT ALREADY EXIST
    // =========================================================

    private void saveProductIfNotExists(
            String name,
            String description,
            double price,
            int stockQuantity,
            String category,
            String brand,
            String imageUrl) {

        if (productRepository.existsByName(name)) {
            return;
        }

        Product product = new Product();

        product.setName(name);
        product.setDescription(description);
        product.setPrice(BigDecimal.valueOf(price));
        product.setStockQuantity(stockQuantity);
        product.setCategory(category);
        product.setBrand(brand);
        product.setImageUrl(imageUrl);
        product.setActive(true);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        productRepository.save(product);

        System.out.println("Added product: " + name);
    }
}
