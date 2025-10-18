package softtech.server.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import softtech.server.dto.AuthDTO.RegisterRequest;
import softtech.server.enums.Role;
import softtech.server.repositories.*;
import softtech.server.services.OtpService;

import java.time.LocalDate;

@Configuration
public class DataInitializer {

    @Autowired
    private AccountRepo accountRepository;

    @Bean
    CommandLineRunner initDatabase(OtpService otpService) {
        return args -> {

            // ADMIN mặc định
            if (accountRepository.findByUsername("admin")==null) {
                RegisterRequest adminReq = new RegisterRequest();
                adminReq.setUsername("admin");
                adminReq.setPassword("User@123");
                adminReq.setRole(Role.ADMIN.name());
                adminReq.setFullName("Admin Nguyen");
                adminReq.setEmail("admin@example.com");
                adminReq.setPhoneNumber("0123456789");
                adminReq.setDateOfBirth(LocalDate.of(1990, 1, 1));
                adminReq.setBusinessCode("CODE_EMPLOYEE");
                otpService.createAccount(adminReq);
                System.out.println("✅ Default admin account created!");
            }

            // STAFF mặc định
            if (accountRepository.findByUsername("staff")==null) {
                RegisterRequest staffReq = new RegisterRequest();
                staffReq.setUsername("staff");
                staffReq.setPassword("User@123");
                staffReq.setRole(Role.STAFF.name());
                staffReq.setFullName("Staff Tran");
                staffReq.setEmail("staff@example.com");
                staffReq.setPhoneNumber("0123456788");
                staffReq.setDateOfBirth(LocalDate.of(1995, 10, 1));
                staffReq.setBusinessCode("CODE_EMPLOYEE");
                otpService.createAccount(staffReq);
                System.out.println("✅ Default staff account created!");
            }

            // MANAGER mặc định
            if (accountRepository.findByUsername("manager")==null) {
                RegisterRequest managerReq = new RegisterRequest();
                managerReq.setUsername("manager");
                managerReq.setPassword("User@123");
                managerReq.setRole(Role.MANAGER.name());
                managerReq.setFullName("Manager Le");
                managerReq.setEmail("manager@example.com");
                managerReq.setPhoneNumber("0123456787");
                managerReq.setDateOfBirth(LocalDate.of(1988, 5, 15));
                managerReq.setBusinessCode("CODE_EMPLOYEE");
                otpService.createAccount(managerReq);
                System.out.println("✅ Default manager account created!");
            }

            // CUSTOMER mặc định
            if (accountRepository.findByUsername("user")==null) {
                RegisterRequest customerReq = new RegisterRequest();
                customerReq.setUsername("user");
                customerReq.setPassword("User@123");
                customerReq.setRole(Role.CUSTOMER.name());
                customerReq.setFullName("Customer Pham");
                customerReq.setEmail("customer@example.com");
                customerReq.setPhoneNumber("0987654321");
                customerReq.setDateOfBirth(LocalDate.of(2000, 5, 10));
                otpService.createAccount(customerReq);
                System.out.println("✅ Default customer account created!");
            }

            // ===== TẠO 20 NHÂN VIÊN =====
            String[] staffNames = {
                    "Nguyen Van", "Tran Thi", "Le Van", "Pham Thi", "Hoang Van",
                    "Vo Thi", "Dang Van", "Bui Thi", "Do Van", "Ngo Thi",
                    "Duong Van", "Ly Thi", "Truong Van", "Vuong Thi", "Thai Van",
                    "Ha Thi", "Trinh Van", "Dinh Thi", "Lam Van", "Phan Thi"
            };

            String[] givenNames = {
                    "An", "Binh", "Cuong", "Dung", "Em",
                    "Giang", "Hieu", "Khanh", "Linh", "Mai",
                    "Nam", "Phong", "Quan", "Son", "Thao",
                    "Uyen", "Van", "Xuan", "Yen", "Zung"
            };

            System.out.println("\n📋 Creating 5 employees...");
            for (int i = 0; i < 5; i++) {
                String username = "staff" + (i + 1);

                if (accountRepository.findByUsername(username) == null) {
                    RegisterRequest empReq = new RegisterRequest();
                    empReq.setUsername(username);
                    empReq.setPassword("User@123");
                    empReq.setRole(Role.STAFF.name());
                    empReq.setFullName(staffNames[i] + " " + givenNames[i]);
                    empReq.setEmail("staff" + (i + 1) + "@example.com");
                    empReq.setPhoneNumber(String.format("09%08d", 10000000 + i));
                    empReq.setDateOfBirth(LocalDate.of(1990 + (i % 10), (i % 12) + 1, (i % 28) + 1));
                    empReq.setBusinessCode("CODE_EMPLOYEE");

                    otpService.createAccount(empReq);
                    System.out.println("  ✓ Created: " + empReq.getFullName() + " (" + username + ")");
                }
            }

// ===== TẠO 20 KHÁCH HÀNG =====
            String[] customerFirstNames = {
                    "Bao", "Chi", "Dat", "Hoa", "Hung",
                    "Khoa", "Lan", "Minh", "Nhi", "Phuong",
                    "Quang", "Thu", "Tuan", "Vy", "Anh",
                    "Hai", "Hoai", "Kim", "Long", "My"
            };

            String[] customerLastNames = {
                    "Nguyen", "Tran", "Le", "Pham", "Hoang",
                    "Huynh", "Vo", "Dang", "Bui", "Do",
                    "Ngo", "Duong", "Ly", "Cao", "Vuong",
                    "Thai", "Ha", "Trinh", "Dinh", "Lam"
            };

            System.out.println("\n👥 Creating 5 customers...");
            for (int i = 0; i < 5; i++) {
                String username = "customer" + (i + 1);

                if (accountRepository.findByUsername(username) == null) {
                    RegisterRequest custReq = new RegisterRequest();
                    custReq.setUsername(username);
                    custReq.setPassword("User@123");
                    custReq.setRole(Role.CUSTOMER.name());
                    custReq.setFullName(customerLastNames[i] + " " + customerFirstNames[i]);
                    custReq.setEmail("customer" + (i + 1) + "@example.com");
                    custReq.setPhoneNumber(String.format("08%08d", 20000000 + i));
                    custReq.setDateOfBirth(LocalDate.of(1985 + (i % 15), (i % 12) + 1, (i % 28) + 1));

                    otpService.createAccount(custReq);
                    System.out.println("  ✓ Created: " + custReq.getFullName() + " (" + username + ")");
                }
            }

            System.out.println("\n✅ Successfully initialized: 4 default + 5 employees + 20 customers = 29 accounts!");
            System.out.println("📝 Login credentials:");
            System.out.println("   - Employees: staff1-staff20 / User@123");
            System.out.println("   - Customers: customer1-customer20 / User@123");
        };
    }
}
