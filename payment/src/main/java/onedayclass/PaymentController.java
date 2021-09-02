package onedayclass;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

 @RestController
 public class PaymentController {
    @Autowired PaymentRepository paymentRepository;
    @RequestMapping(value = "/payments/requestPayment",
            method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public Boolean requestPayment(@RequestBody Payment payment) throws Exception {
        //결재요청
        System.out.println("####################결제 저장########################");
        paymentRepository.save(payment);
        System.out.println("####################결제 완료########################");
        return true;
    } 
 }