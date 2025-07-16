package software.amazon.opentelemetry;

import io.opentelemetry.exporter.internal.http.HttpSender;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import okhttp3.*;
import okio.BufferedSink;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.signer.Aws4Signer;
import software.amazon.awssdk.auth.signer.params.Aws4SignerParams;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.regions.Region;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class AwsAuthSender implements HttpSender {
    private final OkHttpClient client;
    private final HttpUrl url;
    private final AwsCredentialsProvider credentialsProvider;
    private final Aws4Signer signer;
    private final String serviceName;
    private final Region region;

    public AwsAuthSender(String endpoint, String serviceName, Region region) {
        this.url = HttpUrl.get(endpoint);
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .callTimeout(30, TimeUnit.SECONDS)
                .build();
        this.credentialsProvider = DefaultCredentialsProvider.create();
        this.signer = Aws4Signer.create();
        this.serviceName = serviceName;
        this.region = region;
    }

    @Override
    public void send(Marshaler marshaler, int contentLength, Consumer<Response> onResponse, Consumer<Throwable> onError) {
        try {
            // 1. Marshal the body
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            marshaler.writeBinaryTo(baos);
            byte[] bodyBytes = baos.toByteArray();

            // 2. Build OkHttp request (without auth headers)
            RequestBody requestBody = RequestBody.create(bodyBytes, MediaType.parse("application/x-protobuf"));
            Request.Builder requestBuilder = new Request.Builder().url(url).post(requestBody);

            // 3. Build SdkHttpFullRequest for signing
            SdkHttpFullRequest.Builder sdkReqBuilder = SdkHttpFullRequest.builder()
                    .method(SdkHttpMethod.POST)
                    .uri(URI.create(url.toString()))
                    .putHeader("Content-Type", "application/x-protobuf")
                    .contentStreamProvider(() -> new java.io.ByteArrayInputStream(bodyBytes));

            // 4. Sign the request
            Aws4SignerParams signerParams = Aws4SignerParams.builder()
                    .signingName(serviceName)
                    .signingRegion(region)
                    .awsCredentials(credentialsProvider.resolveCredentials())
                    .build();
            SdkHttpFullRequest signedRequest = signer.sign(sdkReqBuilder.build(), signerParams);

            // 5. Add signed headers to OkHttp request
            for (Map.Entry<String, List<String>> entry : signedRequest.headers().entrySet()) {
                for (String value : entry.getValue()) {
                    requestBuilder.header(entry.getKey(), value);
                }
            }

            // 6. Send the request
            client.newCall(requestBuilder.build()).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    onError.accept(e);
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) {
                    try (ResponseBody respBody = response.body()) {
                        byte[] respBytes = respBody != null ? respBody.bytes() : new byte[0];
                        onResponse.accept(new HttpSender.Response() {
                            @Override
                            public int statusCode() {
                                return response.code();
                            }

                            @Override
                            public String statusMessage() {
                                return response.message();
                            }

                            @Override
                            public byte[] responseBody() {
                                return respBytes;
                            }
                        });
                    } catch (IOException ex) {
                        onError.accept(ex);
                    }
                }
            });
        } catch (Exception e) {
            onError.accept(e);
        }
    }

    @Override
    public CompletableResultCode shutdown() {
        client.dispatcher().cancelAll();
        client.connectionPool().evictAll();
        return CompletableResultCode.ofSuccess();
    }
}
