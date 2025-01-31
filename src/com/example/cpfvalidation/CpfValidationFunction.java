package com.example.cpfvalidation;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.TelemetryConfiguration;
import com.microsoft.azure.functions.*;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Azure Functions with HTTP Trigger for CPF Validation
 */
public class CpfValidationFunction {

    // Regex for CPF validation
    private static final Pattern CPF_PATTERN = Pattern.compile("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}");

    // Telemetry client for monitoring
    private final TelemetryClient telemetryClient;

    public CpfValidationFunction() {
        String instrumentationKey = System.getenv("APPINSIGHTS_INSTRUMENTATIONKEY");
        this.telemetryClient = new TelemetryClient(TelemetryConfiguration.createDefault());
        if (instrumentationKey != null) {
            TelemetryConfiguration.getActive().setInstrumentationKey(instrumentationKey);
        }
    }

    /**
     * This function listens at the endpoint "/api/validateCpf".
     * Example request: { "cpf": "123.456.789-09" }
     */
    @FunctionName("validateCpf")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, route = "validateCpf")
            HttpRequestMessage<Optional<Map<String, String>>> request,
            final ExecutionContext context) {
        context.getLogger().info("Processing CPF validation request.");

        // Parse input JSON
        Map<String, String> body = request.getBody().orElse(Collections.emptyMap());
        String cpf = body.get("cpf");

        telemetryClient.trackEvent("CPF Validation Request Received");

        if (cpf == null || !CPF_PATTERN.matcher(cpf).matches() || !isValidCpf(cpf)) {
            telemetryClient.trackEvent("Invalid CPF Request");
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("message", "Invalid CPF format or checksum."))
                    .build();
        }

        telemetryClient.trackEvent("Valid CPF Request");
        return request.createResponseBuilder(HttpStatus.OK)
                .body(Collections.singletonMap("message", "Valid CPF."))
                .build();
    }

    // CPF validation logic (checksum)
    private boolean isValidCpf(String cpf) {
        cpf = cpf.replace(".", "").replace("-", "");

        if (cpf.matches("^(\\d)\\1{10}$")) return false; // Reject repeating numbers

        int sum1 = 0, sum2 = 0;
        for (int i = 0; i < 9; i++) {
            int digit = Character.getNumericValue(cpf.charAt(i));
            sum1 += digit * (10 - i);
            sum2 += digit * (11 - i);
        }

        int checkDigit1 = (sum1 * 10) % 11;
        checkDigit1 = (checkDigit1 == 10) ? 0 : checkDigit1;

        sum2 += checkDigit1 * 2;
        int checkDigit2 = (sum2 * 10) % 11;
        checkDigit2 = (checkDigit2 == 10) ? 0 : checkDigit2;

        return checkDigit1 == Character.getNumericValue(cpf.charAt(9)) &&
                checkDigit2 == Character.getNumericValue(cpf.charAt(10));
    }
}
