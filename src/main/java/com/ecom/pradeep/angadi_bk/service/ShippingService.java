package com.ecom.pradeep.angadi_bk.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class ShippingService {
    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> getShippingStatus(String trackingNumber) {
        String apiUrl = "https://api.courier.com/track?trackingNumber=" + trackingNumber;
        return restTemplate.getForObject(apiUrl, Map.class);
    }
}
