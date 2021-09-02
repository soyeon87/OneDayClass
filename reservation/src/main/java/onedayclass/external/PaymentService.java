package onedayclass.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@FeignClient(name="payment", url="${prop.pay.url}")
public interface PaymentService {
    @RequestMapping(method= RequestMethod.POST, path="/payments/requestPayment")
    public boolean requestPayment(@RequestBody Payment payment);

}

