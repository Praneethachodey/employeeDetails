package com.example.employeeDetails.Service;

import com.example.employeeDetails.Entity.HrPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class HrPolicyService
{
    private final WebClient webClient;

    public HrPolicyService(WebClient.Builder webClientBuilder,  @Value("${legacy.server.url}") String legacyUrl) {
        // Initialize the WebClient with the base URL
        this.webClient = webClientBuilder.baseUrl(legacyUrl).build();
    }


    public List<HrPolicy> getPoliciesByDepartment(String department, String sessionId) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/policies")
                            .queryParam("department", department)
                            .queryParam("sessionId", sessionId)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<HrPolicy>>() {})  // Retrieve body as List<HrPolicy>
                    .block();  // Block to get the result synchronously (blocking the thread until response is received)
        } catch (Exception e) {
            System.out.println("Error fetching policies: " + e.getMessage());
            return Collections.emptyList();  // Return an empty list on error
        }
    }
}
