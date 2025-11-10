package de.oglimmer.picz.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class GoogleMapGenerator {

    @Value("${google.maps.apiKey}")
    private String apiKey;

    private final RestTemplate template = new RestTemplate();

    @SneakyThrows
    public byte[] generateMap(double mapCenterLatitude, double mapCenterLongitude, double markerLatitude, double markerLongitude, int zoomLevel) {
        String url = url(mapCenterLatitude, mapCenterLongitude, markerLatitude, markerLongitude, zoomLevel);
        log.debug("Map url: {}", url);
        RequestEntity<Void> request = RequestEntity.get(new URI(url)).build();
        ResponseEntity<byte[]> response = template.exchange(request, byte[].class);
        return response.getBody();
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    @SneakyThrows
    private String url(double centerLatitude, double centerLongitude, double markerLatitude, double markerLongitude, int zoom) {

        // Build the Google Static Maps API URL
        StringBuilder urlBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/staticmap?");

        // Add center coordinates using the provided center values (not the image location)
        urlBuilder.append("center=").append(centerLatitude).append(",").append(centerLongitude);

        // Add zoom level
        urlBuilder.append("&zoom=").append(zoom);

        // Add size parameter
        urlBuilder.append("&size=640x640");

        // Add scale parameter
        urlBuilder.append("&scale=2");

        // Add marker for image location (pin location may be different from map center)
        String markerParam = "markers=color:red%7Clabel:X%7C" + markerLatitude + "," + markerLongitude;
        urlBuilder.append("&").append(markerParam);

        // Set map type (roadmap, satellite, hybrid, terrain)
        urlBuilder.append("&maptype=roadmap");

        // Add language
        urlBuilder.append("&language=de-DE");

        // Add API key
        urlBuilder.append("&key=").append(apiKey);

        return urlBuilder.toString();
    }


}