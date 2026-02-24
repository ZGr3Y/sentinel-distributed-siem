package com.sentinel.api.config;

import com.lambdaworks.crypto.SCryptUtil;
import com.sentinel.api.repository.UserRepository;
import com.sentinel.common.domain.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Pattern: Authenticator (L5_Auth-hash)
 * Seeds default users on application startup using Scrypt-hashed passwords,
 * following the exact API from Prof. Tramontana's slides:
 * SCryptUtil.scrypt(passwd, 32768, 8, 1)
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;

    public DataSeeder(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Seeding default users...");

            // Hash passwords using Scrypt (L5_Auth-hash slide 11):
            // SCryptUtil.scrypt(passwd, N=32768, r=8, p=1)
            String adminHash = SCryptUtil.scrypt("admin", 32768, 8, 1);
            String analystHash = SCryptUtil.scrypt("analyst", 32768, 8, 1);

            User admin = new User("admin", adminHash, "ADMIN");
            User analyst = new User("analyst", analystHash, "ANALYST");

            userRepository.save(admin);
            userRepository.save(analyst);

            log.info("Seeded 2 users: admin (ADMIN), analyst (ANALYST)");
        } else {
            log.info("Users already exist, skipping seed.");
        }
    }
}
