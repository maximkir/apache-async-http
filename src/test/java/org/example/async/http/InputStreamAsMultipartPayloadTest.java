package org.example.async.http;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.junit.Rule;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.aMultipart;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class InputStreamAsMultipartPayloadTest {


    @Rule
    public WireMockClassRule instanceRule = new WireMockClassRule(9090);


    @Test
    public void inputStreamPartTest() throws Exception {

        String partBody = "hello";

        instanceRule.stubFor(any(urlPathEqualTo("/hello"))
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("part-name")
                                .withHeader("Content-Type", equalTo("application/octet-stream"))
                                .withHeader("Content-Transfer-Encoding", equalTo("binary"))
                                .withBody(equalTo(partBody)))
                .willReturn(aResponse()));

        byte[] body = partBody.getBytes(UTF_8);

        try (CloseableHttpAsyncClient httpAsyncClient = HttpAsyncClients.createDefault()) {
            httpAsyncClient.start();
            HttpPost postRequest = new HttpPost(URI.create("/hello"));

            MultipartEntityBuilder multipartBuilder = MultipartEntityBuilder.create();
            multipartBuilder.addBinaryBody("part-name", new ByteArrayInputStream(body));
            postRequest.setEntity(multipartBuilder.build());

            Future<HttpResponse> fHttpResponse = httpAsyncClient.execute(HttpHost.create("http://localhost:9090"), postRequest, null);

            assertThat(fHttpResponse.get(3, TimeUnit.SECONDS).getStatusLine().getStatusCode()).isEqualTo(HttpURLConnection.HTTP_OK);
        }

    }


}
