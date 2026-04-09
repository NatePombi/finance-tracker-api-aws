package org.example.financetrackerapi.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.financetrackerapi.exception.UserNotFoundException;
import org.example.financetrackerapi.transaction.Transaction;
import org.example.financetrackerapi.transaction.TransactionRepository;
import org.example.financetrackerapi.user.User;
import org.example.financetrackerapi.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiService {
    @Value("${spring.sendgrid.api-key}")
    private String API_KEY;

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;


    public String analyzeSpending(String prompt){
        try{
            HttpClient client = HttpClient.newHttpClient();

            String requestBody = """
                    {
                        "contents": [
                            {
                                "parts": [
                                    {"text": "%s"}
                                   ]
                            }
                        ]
                    }
                    """.formatted(prompt);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="+API_KEY))
                    .header("Content-Type","application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());

            if(root.has("error")){
                return "API Error: " + root.path("error").path("message").asText();
            }

            JsonNode choices = root.path("candidates");

                System.out.println(requestBody);
            if(!choices.isArray() || choices.size() == 0){
                return "No AI Response";
            }

            String aiMessage = choices
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

            return aiMessage;

        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    public String getTransactionsForAiAnalysis(String email){
        User user = userRepository.findByEmail(email).orElseThrow(()-> new UserNotFoundException("User Not Found"));

        List<Transaction> transaction = transactionRepository.findAllByAccountUser(user);

        String transactionText = transaction.stream()
                .map(t-> t.getDescription() + " (" + t.getCategory().getType()+  ") : R" + t.getAmount())
                .collect(Collectors.joining("\n"));

        return """
                You are a financial advisor.
                
                Analyze my spending:
                
                %s
                
                Give:
                1. Spending summary
                2. Problem areas
                3. Practical Saving tips                
                """.formatted(transactionText);
    }
}
