package softtech.server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import softtech.server.dto.AuthDTO.OtpDTO;
import softtech.server.dto.AuthDTO.RegisterRequest;
import softtech.server.dto.AuthDTO.VerifyOtpRequest;
import softtech.server.dto.AuthDTO.FogotPassDTO;
import softtech.server.dto.AuthDTO.OtpFogotPassDTO;
import softtech.server.enums.CustomerType;
import softtech.server.enums.Role;
import softtech.server.exceptions.ValidationException;
import softtech.server.models.Account;
import softtech.server.models.Customer;
import softtech.server.models.Employee;
import softtech.server.repositories.AccountRepo;
import softtech.server.repositories.CustomerRepo;
import softtech.server.repositories.EmployeeRepo;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {
	private static final Logger logger = LoggerFactory.getLogger(OtpService.class);
	
	private final JavaMailSender mailSender;
	private final Map<String, OtpDTO> otpStore = new ConcurrentHashMap<>(); // Thread-safe
	private final Map<String, OtpFogotPassDTO> otpStoreFor = new ConcurrentHashMap<>();
	private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	@Value("${spring.mail.username:}")
	private String mailUsername;

	@Autowired
	private AccountRepo accountRepo;

	@Autowired
	private CustomerRepo customerRepo;

	@Autowired
	private EmployeeRepo employeeRepo;

	@Autowired
	public OtpService(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	/*** OTP GENERATION ***/
	public String generateOtp() {
		SecureRandom random = new SecureRandom();
		int otp = 100000 + random.nextInt(900000);
		return String.valueOf(otp);
	}

	/*** SEND OTP FOR FORGOT PASSWORD ***/
	public void sendOtp_ForgetPass(FogotPassDTO request) throws MessagingException {
		String otp = generateOtp();
		LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);
		otpStoreFor.put(request.getEmail(), new OtpFogotPassDTO(otp, expiresAt, request));
		sendOtpEmail(request.getEmail(), otp);
	}

	/*** VERIFY OTP FOR FORGOT PASSWORD AND RESET ***/
	public Boolean verifyOtpAndFogotPass(FogotPassDTO request) {
		String email = request.getEmail();
		String otp = request.getOtp();

		OtpFogotPassDTO otpData = otpStoreFor.get(email);
		if (otpData == null) {
			throw new ValidationException(Map.of("otp", "OTP not found or already used"));
		}

		if (LocalDateTime.now().isAfter(otpData.getExpiresAt())) {
			otpStoreFor.remove(email);
			throw new ValidationException(Map.of("otp", "OTP has expired"));
		}

		if (!otpData.getCode().equals(otp)) {
			throw new ValidationException(Map.of("otp", "Invalid OTP code"));
		}

		Customer customer = customerRepo.findByEmail(email);
		if (customer == null) {
			throw new ValidationException(Map.of("email", "Customer with this email does not exist"));
		}

		Account account = customer.getAccount();
		if (account == null) {
			throw new ValidationException(Map.of("email", "Account with this email does not exist"));
		}

		account.setPassword(passwordEncoder.encode(request.getNewPassword()));
		accountRepo.save(account);

		otpStoreFor.remove(email);
		return true;
	}

	/*** SEND OTP FOR REGISTRATION ***/
	public void sendOtp(RegisterRequest request) throws MessagingException {
		// Validate trước khi gửi OTP
		validateRegistrationData(request);

		// Generate OTP và lưu cùng thông tin đăng ký
		String otp = generateOtp();
		LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);
		otpStore.put(request.getEmail(), new OtpDTO(otp, expiresAt, request));

		sendOtpEmail(request.getEmail(), otp);
	}

	/*** VERIFY OTP AND REGISTER ACCOUNT ***/
	@Transactional
	public Account verifyOtpAndRegister(VerifyOtpRequest request) {
		String email = request.getEmail();
		String otp = request.getOtp();

		OtpDTO otpData = otpStore.get(email);
		if (otpData == null) {
			throw new ValidationException(Map.of("otp", "OTP not found or already used"));
		}

		if (LocalDateTime.now().isAfter(otpData.getExpiresAt())) {
			otpStore.remove(email);
			throw new ValidationException(Map.of("otp", "OTP has expired"));
		}

		if (!otpData.getCode().equals(otp)) {
			throw new ValidationException(Map.of("otp", "Invalid OTP code"));
		}

		RegisterRequest registerRequest = otpData.getRegisterRequest();
		validateRegistrationData(registerRequest);

		// Xử lý trường hợp khách đã là GUEST trước đó
		Customer existingCustomer = customerRepo.findByPhoneNumber(registerRequest.getPhoneNumber());
		Account account;

		if (existingCustomer != null) {
			// Nâng cấp GUEST -> MEMBER
			existingCustomer.setFullName(registerRequest.getFullName());
			existingCustomer.setEmail(registerRequest.getEmail());
			existingCustomer.setDateOfBirth(registerRequest.getDateOfBirth());
			existingCustomer.setType(CustomerType.MEMBER);

			// Tạo Account
			account = new Account();
			account.setUsername(registerRequest.getUsername());
			account.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
			account.setRole(Role.CUSTOMER);
			accountRepo.save(account);

			existingCustomer.setAccount(account);
			customerRepo.save(existingCustomer);
		} else {
			// Khách hoàn toàn mới
			account = createAccount(registerRequest);
		}

		otpStore.remove(email);
		return account;
	}

	/*** VALIDATE REGISTRATION DATA ***/
	private void validateRegistrationData(RegisterRequest request) {
		Map<String, String> errors = new HashMap<>();

		if (accountRepo.existsByUsername(request.getUsername())) {
			errors.put("username", "Username is already taken");
		}

		// Kiểm tra email
		if (customerRepo.existsByEmail(request.getEmail()) ||
				employeeRepo.existsByEmail(request.getEmail())) {
			errors.put("email", "Email is already taken");
		}

		// Kiểm tra phoneNumber
		Customer existingCustomer = customerRepo.findByPhoneNumber(request.getPhoneNumber());
		if (existingCustomer != null && existingCustomer.getType() != CustomerType.GUEST) {
			// Nếu đã là MEMBER hoặc type khác GUEST → báo lỗi
			errors.put("phoneNumber", "Phone number is already taken");
		}

		if (employeeRepo.existsByPhoneNumber(request.getPhoneNumber())) {
			errors.put("phoneNumber", "Phone number is already taken by an employee");
		}

		if (!errors.isEmpty()) {
			throw new ValidationException(errors);
		}
	}

	/*** CREATE ACCOUNT + CUSTOMER/EMPLOYEE ***/
	public Account createAccount(RegisterRequest request) {
		Account account = new Account();
		account.setUsername(request.getUsername());
		account.setPassword(passwordEncoder.encode(request.getPassword()));
		account.setRole(Role.valueOf(request.getRole()));
		Account savedAccount = accountRepo.save(account);

		if (account.getRole() == Role.CUSTOMER) {
			Customer customer = new Customer();
			customer.setFullName(request.getFullName());
			customer.setEmail(request.getEmail());
			customer.setPhoneNumber(request.getPhoneNumber());
			customer.setDateOfBirth(request.getDateOfBirth());
			customer.setAccount(savedAccount);
			customer.setType(CustomerType.MEMBER);
			customerRepo.save(customer);
		} else {
			if (!"CODE_EMPLOYEE".equals(request.getBusinessCode())) {
				throw new ValidationException(Map.of("businessCode", "Invalid business code"));
			}

			Employee employee = new Employee();
			employee.setFullName(request.getFullName());
			employee.setEmail(request.getEmail());
			employee.setPhoneNumber(request.getPhoneNumber());
			employee.setDateOfBirth(request.getDateOfBirth());
			employee.setAccount(savedAccount);
			employee.setPosition(request.getRole());
			employeeRepo.save(employee);
		}

		return savedAccount;
	}

	/*** SEND OTP EMAIL ***/
	private void sendOtpEmail(String email, String otp) throws MessagingException {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setTo(email);
			helper.setSubject("Your OTP Code");
			helper.setText("Your OTP code is <strong>" + otp + "</strong>. It is valid for 5 minutes.", true);
			mailSender.send(message);
			
			logger.info("OTP email sent successfully to: {}", email);
		} catch (Exception e) {
			logger.warn("Failed to send OTP email to: {}. Error: {}", email, e.getMessage());
			// In development mode with mock sender, this is expected
			if (mailUsername.isEmpty()) {
				logger.info("Using mock mail sender - OTP code for {}: {}", email, otp);
			}
		}
	}

	/*** CLEANUP EXPIRED OTPS ***/
	public void cleanupExpiredOtps() {
		LocalDateTime now = LocalDateTime.now();
		otpStore.entrySet().removeIf(entry ->
				now.isAfter(entry.getValue().getExpiresAt())
		);
	}
}