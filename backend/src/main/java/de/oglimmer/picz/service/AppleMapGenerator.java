package de.oglimmer.picz.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AppleMapGenerator {

    public AppleMapGenerator() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Value("${apple.maps.teamId}")
    private String teamId;

    @Value("${apple.maps.keyId}")
    private String keyId;

    @Value("${apple.maps.privateKey}")
    private String privateKey;

    private final RestTemplate template = new RestTemplate();

    @SneakyThrows
    public byte[] generateMap(double imageLatitude, double imageLongitude, double centerLatitude, double centerLongitude, double spanLatitude, double spanLongitude) {
        String url = url(imageLatitude, imageLongitude, centerLatitude, centerLongitude, spanLatitude, spanLongitude);
        log.debug("Map url: " + url);
        RequestEntity<Void> request = RequestEntity.get(new URI(url)).build();
        ResponseEntity<byte[]> response = template.exchange(request, byte[].class);
        return response.getBody();
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    @SneakyThrows
    private String url(double imageLatitude, double imageLongitude, double centerLatitude, double centerLongitude, double spanLatitude, double spanLongitude) {
        // PrivateKey privateKey = createPrivateKey(new InputStreamReader(getClass().getResourceAsStream(privateKeyPath)));
        privateKey = privateKey.replaceAll("\\\\n", "\n"); // support multiline private key stored where multiline is not supported
        PrivateKey privateKey = createPrivateKey(new StringReader(this.privateKey));
        Map<String, String> params = getStringStringMap(imageLatitude, imageLongitude, centerLatitude, centerLongitude, spanLatitude, spanLongitude);
        String snapshotPath = "/api/v1/snapshot?" + params.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + urlEncode(entry.getValue()))
                .collect(Collectors.joining("&"));
        String completePath = snapshotPath + "&teamId=" + teamId + "&keyId=" + keyId;
        String signature = sign(completePath, privateKey);

        return "https://snapshot.apple-mapkit.com" + completePath + "&signature=" + signature;
    }

    private static Map<String, String> getStringStringMap(double imageLatitude, double imageLongitude, double centerLatitude, double centerLongitude, double spanLatitude, double spanLongitude) {
        // https://developer.apple.com/documentation/snapshots/annotation
        Map<String, String> params = new HashMap<>();
        params.put("center", centerLatitude + "," + centerLongitude);
        params.put("spn", spanLatitude + "," + spanLongitude);
        params.put("size", "640x640");
        params.put("scale", "2");
        params.put("annotations", "[{\"point\":\"" + imageLatitude + "," + imageLongitude + "\",\"markerStyle\":\"large\",\"color\":\"ff2600\",\"glyphText\":\"X\"}]"); // markerStyle = dot, balloon, large, img
        params.put("t", "standard"); // standard, mutedstandard, hybrid, satellite;
        params.put("lang", "de-DE");
        params.put("poi", "1");
        return params;
    }

    private PrivateKey createPrivateKey(Reader reader) throws IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
        PemReader pemReader = new PemReader(reader);
        PemObject pemObject = pemReader.readPemObject();
        byte[] privateKeyBytes = pemObject.getContent();

        KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", "BC");
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        return keyFactory.generatePrivate(privateKeySpec);
    }

    private String sign(String data, PrivateKey privateKey) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance("SHA256withECDSA", "BC");
        signature.initSign(privateKey, new SecureRandom());
        signature.update(data.getBytes());

        byte[] signatureBytes = signature.sign();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);
    }

}
