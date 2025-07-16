package software.amazon.opentelemetry;

import io.opentelemetry.exporter.internal.http.HttpSender;
import io.opentelemetry.exporter.internal.http.HttpSenderConfig;
import io.opentelemetry.exporter.internal.http.HttpSenderProvider;
import software.amazon.awssdk.regions.Region;

public class AwsAuthSenderProvider implements HttpSenderProvider {
    @Override
    public HttpSender createSender(HttpSenderConfig httpSenderConfig) {
        // TODO: Make serviceName and region configurable if needed
        return new AwsAuthSender(
            httpSenderConfig.getEndpoint(),
            "xray", // Example: AWS X-Ray
            software.amazon.awssdk.regions.Region.US_WEST_2 // Example region
        );
    }
}
