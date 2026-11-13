package com.thomascook.hotelbooking.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thomascook.hotelbooking.exception.InvalidRequestException;
import com.thomascook.hotelbooking.dto.HotelContentDto;
import com.thomascook.hotelbooking.dto.RoomOptionDto;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/** Read-only Nuitee Connect adapter. It intentionally does not call prebook or book. */
@Service
public class NuiteeHotelProvider {
  private final RestClient client;
  private final ObjectMapper objectMapper;
  private final String apiKey;
  private final boolean enabled;
  private final String currency;
  private final String nationality;

  public NuiteeHotelProvider(
      RestClient.Builder builder,
      ObjectMapper objectMapper,
      @Value("${nuitee.base-url:https://api.liteapi.travel}") String baseUrl,
      @Value("${nuitee.api-key:}") String apiKey,
      @Value("${nuitee.enabled:false}") boolean enabled,
      @Value("${app.currency:INR}") String currency,
      @Value("${nuitee.guest-nationality:IN}") String nationality) {
    this.client = builder.baseUrl(baseUrl).build();
    this.objectMapper = objectMapper;
    this.apiKey = apiKey;
    this.enabled = enabled;
    this.currency = currency;
    this.nationality = nationality;
  }

  public boolean isEnabled() {
    return enabled && !apiKey.isBlank();
  }

  public HotelContentDto content(String hotelId) {
    try {
      String raw = client.get().uri(uri -> uri.path("/v3.0/data/hotel").queryParam("hotelId", hotelId).build())
          .header("X-API-Key", apiKey).accept(MediaType.APPLICATION_JSON).retrieve().body(String.class);
      JsonNode d = objectMapper.readTree(raw).path("data");
      List<String> images = new ArrayList<>();
      for (JsonNode image : d.path("hotelImages")) { String url = text(image, "url", null); if (url != null) images.add(url); }
      List<String> amenities = strings(d.path("hotelFacilities"));
      List<HotelContentDto.RoomContentDto> rooms = new ArrayList<>();
      for (JsonNode room : d.path("rooms")) {
        List<String> roomImages = new ArrayList<>();
        for (JsonNode image : room.path("photos")) { String url=text(image,"url",null); if(url!=null) roomImages.add(url); }
        rooms.add(new HotelContentDto.RoomContentDto(text(room,"roomName",text(room,"name","Room")), text(room,"roomSize",null), integer(room,"maxOccupancy"), strings(room.path("hotelFacilities")), roomImages));
      }
      JsonNode location = d.path("location");
      return new HotelContentDto(d.path("id").asText(hotelId), text(d,"name",null), text(d,"hotelDescription",null), text(d,"address",null), text(d,"city",null), text(d,"country",null), number(location,"latitude"), number(location,"longitude"), text(d,"main_photo",null), text(d,"thumbnail",null), amenities, images, rooms);
    } catch (RestClientResponseException ex) { throw new InvalidRequestException("Nuitee hotel details failed (HTTP " + ex.getStatusCode().value() + ")"); }
    catch (Exception ex) { throw new InvalidRequestException("Nuitee hotel details failed: " + ex.getMessage()); }
  }

  public List<RoomOptionDto> rooms(String hotelId, java.time.LocalDate checkIn, java.time.LocalDate checkOut, int guests) {
    Map<String,Object> body = new HashMap<>(); body.put("hotelIds", List.of(hotelId)); body.put("checkin",checkIn.toString()); body.put("checkout",checkOut.toString()); body.put("currency",currency); body.put("guestNationality",nationality); body.put("occupancies",List.of(Map.of("adults",Math.max(1,guests)))); body.put("roomMapping",true); body.put("maxRatesPerHotel",5); body.put("includeHotelData",true);
    try { String raw=client.post().uri("/v3.0/hotels/rates").header("X-API-Key",apiKey).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).body(body).retrieve().body(String.class); return parseRooms(raw,java.time.temporal.ChronoUnit.DAYS.between(checkIn,checkOut)); }
    catch(RestClientResponseException ex){throw new InvalidRequestException("Nuitee room rates failed (HTTP "+ex.getStatusCode().value()+")");} catch(Exception ex){throw new InvalidRequestException("Nuitee room rates failed: "+ex.getMessage());}
  }

  public List<ExternalHotel> search(
      String destination, java.time.LocalDate checkIn, java.time.LocalDate checkOut, int guests) {
    if (!isEnabled()) return List.of();
    Map<String, Object> body = new HashMap<>();
    body.put("cityName", destination.trim());
    body.put("countryCode", "IN");
    body.put("checkin", checkIn.toString());
    body.put("checkout", checkOut.toString());
    body.put("currency", currency);
    body.put("guestNationality", nationality);
    body.put("occupancies", List.of(Map.of("adults", Math.max(1, guests))));
    body.put("maxRatesPerHotel", 1);
    body.put("includeHotelData", true);

    try {
      String raw = client.post().uri("/v3.0/hotels/rates")
          .header("X-API-Key", apiKey)
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
          .body(body)
          .retrieve().body(String.class);
      return parse(raw, java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut));
    } catch (RestClientResponseException ex) {
      throw new InvalidRequestException("Nuitee search failed (HTTP " + ex.getStatusCode().value() + ")");
    } catch (Exception ex) {
      throw new InvalidRequestException("Nuitee search failed: " + ex.getMessage());
    }
  }

  private List<ExternalHotel> parse(String raw, long nights) throws Exception {
    JsonNode root = objectMapper.readTree(raw);
    if (root.has("error") && !root.path("error").isMissingNode()) {
      JsonNode error = root.path("error");
      throw new InvalidRequestException(error.path("description").asText(error.path("message").asText("No Nuitee availability")));
    }
    Map<String, JsonNode> metadata = new HashMap<>();
    for (JsonNode hotel : root.path("hotels")) {
      String id = hotel.path("id").asText("");
      if (!id.isBlank()) metadata.put(id, hotel);
    }

    List<ExternalHotel> result = new ArrayList<>();
    for (JsonNode item : root.path("data")) {
      String hotelId = item.path("hotelId").asText("");
      JsonNode hotel = metadata.getOrDefault(hotelId, item.path("hotel"));
      JsonNode room = firstRoom(item);
      JsonNode rate = room.path("rates").isArray() && room.path("rates").size() > 0
          ? room.path("rates").get(0) : item.path("rate");
      if (hotelId.isBlank() || rate.isMissingNode()) continue;
      BigDecimal totalPrice = amount(rate);
      BigDecimal price = totalPrice == null || nights < 1
          ? totalPrice : totalPrice.divide(BigDecimal.valueOf(nights), 2, java.math.RoundingMode.HALF_UP);
      if (price == null) continue;
      result.add(new ExternalHotel(
          hotelId,
          text(hotel, "name", "Hotel " + hotelId),
          firstNonBlank(text(hotel, "city_name", ""), text(hotel, "address", ""), destinationFallback(hotel)),
          text(hotel, "story", "Live hotel inventory from Nuitee Connect."),
          text(hotel, "address", null),
          text(hotel, "country_code", "IN"),
          number(hotel, "latitude"), number(hotel, "longitude"), price,
          rate.path("availableRooms").asInt(rate.path("roomsLeft").asInt(1)),
          firstNonBlank(text(room, "offerId", ""), text(rate, "offerId", ""), null),
          firstNonBlank(text(rate, "name", ""), text(room, "name", ""), "Room"),
          text(hotel, "chain", null), text(hotel, "main_photo", null), text(hotel, "thumbnail", null),
          number(hotel, "rating"), number(hotel, "stars"), hotel.path("review_count").isNumber() ? hotel.path("review_count").intValue() : null,
          text(rate, "boardName", null)));
    }
    return result;
  }

  private JsonNode firstRoom(JsonNode item) {
    for (JsonNode room : item.path("roomTypes")) {
      return room;
    }
    return item.path("roomType");
  }

  private BigDecimal amount(JsonNode rate) {
    JsonNode total = rate.path("retailRate").path("total");
    if (total.isArray() && total.size() > 0) {
      JsonNode amount = total.get(0).path("amount");
      if (amount.isNumber() || !amount.asText("").isBlank()) return new BigDecimal(amount.asText());
    }
    JsonNode amount = rate.path("price").path("amount");
    if (amount.isNumber() || !amount.asText("").isBlank()) return new BigDecimal(amount.asText());
    return rate.has("price") && rate.path("price").isNumber()
        ? rate.path("price").decimalValue() : null;
  }

  private String text(JsonNode node, String field, String fallback) {
    String value = node.path(field).asText("").trim();
    return value.isBlank() ? fallback : value;
  }

  private List<RoomOptionDto> parseRooms(String raw, long nights) throws Exception {
    JsonNode root=objectMapper.readTree(raw); List<RoomOptionDto> result=new ArrayList<>();
    for(JsonNode item:root.path("data")) for(JsonNode room:item.path("roomTypes")) for(JsonNode rate:room.path("rates")) {
      BigDecimal total=amount(rate); if(total==null) continue; BigDecimal perNight=nights>0?total.divide(BigDecimal.valueOf(nights),2,java.math.RoundingMode.HALF_UP):total;
      int available=rate.path("availableRooms").asInt(rate.path("roomsLeft").asInt(1));
      result.add(new RoomOptionDto(firstNonBlank(text(room,"offerId",null),text(rate,"offerId",null),""), firstNonBlank(text(rate,"name",null),text(room,"name",null),"Room"), firstNonBlank(text(room,"roomSize",null),text(rate,"roomSize",null),null), integer(room,"maxOccupancy"), text(rate,"boardName",null), perNight,total,text(rate.path("cancellationPolicies"),"refundableTag",null),strings(room.path("amenities")),text(room,"main_photo",null),available));
    }
    return result;
  }
  private Integer integer(JsonNode node,String field){return node.path(field).isNumber()?node.path(field).intValue():null;}
  private List<String> strings(JsonNode node){List<String> out=new ArrayList<>();if(node.isArray())for(JsonNode n:node){String v=n.asText("").trim();if(!v.isBlank())out.add(v);}return out;}

  private String firstNonBlank(String... values) {
    for (String value : values) if (value != null && !value.isBlank()) return value;
    return "India";
  }

  private String destinationFallback(JsonNode hotel) {
    return text(hotel, "country_code", "India");
  }

  private Double number(JsonNode node, String field) {
    return node.path(field).isNumber() ? node.path(field).doubleValue() : null;
  }

  public record ExternalHotel(
      String sourceId, String name, String location, String description, String address,
      String countryCode, Double latitude, Double longitude, BigDecimal pricePerNight,
      int availableRooms, String offerId, String roomName, String brand, String mainPhoto,
      String thumbnail, Double guestRating, Double starRating, Integer reviewCount, String boardName) {}
}
