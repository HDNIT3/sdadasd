package softtech.server.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.io.IOException;
import java.net.URLEncoder;

import softtech.server.services.VnPayService;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "http://localhost:3000")
public class VnPayController {

    @Autowired
    private VnPayService vnPayService;

    @PostMapping("/createPay")
    public ResponseEntity<?> createPayment(
            @RequestParam("amount") int amount,
            @RequestParam("orderInfo") String orderInfo,
            HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        String vnpayUrl = vnPayService.createOrder(amount, orderInfo, baseUrl);

        return ResponseEntity.ok(Map.of("success", true, "paymentUrl", vnpayUrl));
    }

    @GetMapping("/callback")
    public void paymentCallback(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int paymentStatus = vnPayService.orderReturn(request);
            String frontendUrl = "http://localhost:3000";

            String vnpResponseCode = request.getParameter("vnp_ResponseCode");
            String vnpTxnRef = request.getParameter("vnp_TxnRef");
            String vnpAmount = request.getParameter("vnp_Amount");
            String vnpBankCode = request.getParameter("vnp_BankCode");
            String vnpTransactionNo = request.getParameter("vnp_TransactionNo");
            String vnpMessage = request.getParameter("vnp_Message");

            System.out.println("✅ Payment callback - Status: " + paymentStatus);
            System.out.println("✅ VNP Response Code: " + vnpResponseCode);
            System.out.println("✅ VNP Transaction Ref: " + vnpTxnRef);

            StringBuilder redirectUrl = new StringBuilder(frontendUrl + "/payment-result?");

            if (paymentStatus == 1) {
                redirectUrl.append("success=true");
                redirectUrl.append("&vnp_ResponseCode=").append(URLEncoder.encode(vnpResponseCode != null ? vnpResponseCode : "00", "UTF-8"));
                redirectUrl.append("&message=").append(URLEncoder.encode("Thanh toán thành công", "UTF-8"));
            } else if (paymentStatus == 0) {
                redirectUrl.append("success=false");
                redirectUrl.append("&vnp_ResponseCode=").append(URLEncoder.encode(vnpResponseCode != null ? vnpResponseCode : "24", "UTF-8"));
                redirectUrl.append("&message=").append(URLEncoder.encode("Thanh toán bị hủy", "UTF-8"));
            } else {
                redirectUrl.append("success=false");
                redirectUrl.append("&vnp_ResponseCode=").append(URLEncoder.encode(vnpResponseCode != null ? vnpResponseCode : "-1", "UTF-8"));
                redirectUrl.append("&message=").append(URLEncoder.encode("Thanh toán thất bại", "UTF-8"));
            }

            if (vnpTxnRef != null) {
                redirectUrl.append("&vnp_TxnRef=").append(URLEncoder.encode(vnpTxnRef, "UTF-8"));
            }
            if (vnpAmount != null) {
                redirectUrl.append("&vnp_Amount=").append(URLEncoder.encode(vnpAmount, "UTF-8"));
            }
            if (vnpBankCode != null) {
                redirectUrl.append("&vnp_BankCode=").append(URLEncoder.encode(vnpBankCode, "UTF-8"));
            }
            if (vnpTransactionNo != null) {
                redirectUrl.append("&vnp_TransactionNo=").append(URLEncoder.encode(vnpTransactionNo, "UTF-8"));
            }
            if (vnpMessage != null) {
                redirectUrl.append("&vnp_Message=").append(URLEncoder.encode(vnpMessage, "UTF-8"));
            }

            System.out.println("🔗 Redirecting to: " + redirectUrl.toString());
            response.sendRedirect(redirectUrl.toString());

        } catch (Exception e) {
            response.sendRedirect("http://localhost:3000/payment-result?success=false&message=" +
                    URLEncoder.encode("Lỗi hệ thống: " + e.getMessage(), "UTF-8"));
        }
    }
}