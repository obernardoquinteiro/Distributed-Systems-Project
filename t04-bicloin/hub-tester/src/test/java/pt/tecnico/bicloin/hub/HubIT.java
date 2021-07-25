package pt.tecnico.bicloin.hub;

import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.*;
import pt.tecnico.bicloin.hub.grpc.*;

import static io.grpc.Status.INVALID_ARGUMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HubIT extends  BaseIT{

	// Simple ping test that returns "Pong." from the server.
	@Test
	public void pingOKTest() {
		PingRequest request = PingRequest.newBuilder().build();
		PingResponse response = frontend.ping(request);
		assertEquals("Pong.", response.getOutput());
	}

	// Checks user "alice" balance and compares with 0.
	@Test
	public void getBalanceTest() {
		BalanceRequest request = BalanceRequest.newBuilder().setUsername("alice").build();
		BalanceResponse response = frontend.getBalance(request);
		assertEquals(0, response.getBalance());
	}

	// Adds 20€ to user "bruno" account and checks if returns 200 BIC.
	@Test
	public void addSaldoTest() {
		TopUpRequest request = TopUpRequest.newBuilder().setUsername("bruno").setCellphone("+35193334444").setCash(20).build();
		TopUpResponse response = frontend.addSaldo(request);
		assertEquals(200, response.getBalance());
	}

	// Attempt to add more than 20€, returns exception.
	@Test
	public void addSaldoExceptionTest() {
		TopUpRequest request = TopUpRequest.newBuilder().setUsername("alice").setCellphone("+35191102030").setCash(21).build();
		assertEquals(
				INVALID_ARGUMENT.getCode(),
				assertThrows(
						StatusRuntimeException.class, () -> frontend.addSaldo(request))
						.getStatus()
						.getCode());
	}

	// Attempt to add cash but cellphone number is not the one associated with the user, returns exception.
	@Test
	public void addSaldoExceptionTest2() {
		TopUpRequest request = TopUpRequest.newBuilder().setUsername("alice").setCellphone("+35199992030").setCash(5).build();
		assertEquals(
				INVALID_ARGUMENT.getCode(),
				assertThrows(
						StatusRuntimeException.class, () -> frontend.addSaldo(request))
						.getStatus()
						.getCode());
	}

	// Tests if the info from a station is correct
	@Test
	public void getStationInfoTest() {
		InfoStationRequest request = InfoStationRequest.newBuilder().setAbrev("istt").build();
		InfoStationResponse response = frontend.getStationInfo(request);
		assertEquals("IST Taguspark, lat 38.7372, -9.3023 long, 20 docas, 4 BIC premio, 12 bicicletas, 0 levantamentos, 0 devolucoes, https://www.google.com/maps/place/38.7372,-9.3023",
				response.getNome() + ", lat " + response.getLat() + ", " + response.getLong() + " long, " +
						response.getDocas() + " docas, " + response.getPrize() + " BIC premio, " +
						response.getBikes() + " bicicletas, " + response.getLevantamentos() + " levantamentos, " +
						response.getDevolucoes() + " devolucoes, " + response.getUrl());
	}

	// Attempt to get info from a station that does not exist, returns exception.
	@Test
	public void getStationInfoTestException() {
		InfoStationRequest request = InfoStationRequest.newBuilder().setAbrev("aaaa").build();
		assertEquals(
				INVALID_ARGUMENT.getCode(),
				assertThrows(
						StatusRuntimeException.class, () -> frontend.getStationInfo(request))
						.getStatus()
						.getCode());

	}

	// Checks the nearest two stations to certain coordinates.
	@Test
	public void locateStationTest() {
		LocateStationRequest request = LocateStationRequest.newBuilder().setCoordsLat("38.7372").setCoordsLong("-9.3023").setK(2).build();
		LocateStationResponse response = frontend.locateStation(request);
		assertEquals("istt" + "\n" + "stao", response.getAbrev(0) + "\n" + response.getAbrev(1));
	}

	// Attempt to bike-up at a station that has no bikes available, returns exception.
	@Test
	public void cantBikeUpTest() {
		TopUpRequest requestS = TopUpRequest.newBuilder().setUsername("carlos").setCellphone("+34203040").setCash(10).build();
		frontend.addSaldo(requestS);
		BikeUpRequest request1 = BikeUpRequest.newBuilder().setUsername("carlos").setCoordsLat("37.0203").setCoordsLong("-7.9238").setAbrev("eslu").build();
		assertEquals(
				INVALID_ARGUMENT.getCode(),
				assertThrows(
						StatusRuntimeException.class, () -> frontend.bikeUp(request1))
						.getStatus()
						.getCode());
	}

	//Attempt to pick up 2 bikes, returns exception.
	@Test
	public void trySecondBikeUpTest() {
		TopUpRequest requestS = TopUpRequest.newBuilder().setUsername("diana").setCellphone("+34010203").setCash(10).build();
		frontend.addSaldo(requestS);
		BikeUpRequest request1 = BikeUpRequest.newBuilder().setUsername("diana").setCoordsLat("38.7075").setCoordsLong("-9.1364").setAbrev("prcm").build();
		frontend.bikeUp(request1);
		BikeUpRequest request2 = BikeUpRequest.newBuilder().setUsername("diana").setCoordsLat("38.7075").setCoordsLong("-9.1364").setAbrev("prcm").build();
		assertEquals(
				INVALID_ARGUMENT.getCode(),
				assertThrows(
						StatusRuntimeException.class, () -> frontend.bikeUp(request2))
						.getStatus()
						.getCode());
	}

	// Test to bike-up and bike-down.
	@Test
	public void bikeUpTest() {
		TopUpRequest requestS = TopUpRequest.newBuilder().setUsername("eva").setCellphone("+155509080706").setCash(10).build();
		frontend.addSaldo(requestS);
		BikeUpRequest request1 = BikeUpRequest.newBuilder().setUsername("eva").setCoordsLat("38.7075").setCoordsLong("-9.1364").setAbrev("prcm").build();
		BikeUpResponse response = frontend.bikeUp(request1);
		assertEquals("OK", response.getResponse());
		BikeDownRequest request2 = BikeDownRequest.newBuilder().setUsername("eva").setCoordsLat("38.7075").setCoordsLong("-9.1364").setAbrev("prcm").build();
		BikeDownResponse response2 = frontend.bikeDown(request2);
		assertEquals("OK", response2.getResponse());
	}

	// Test to return a bike without having made a bike-up before, returns exception.
	@Test
	public void cantBikeDownTest() {
		BikeDownRequest request = BikeDownRequest.newBuilder().setUsername("eva").setCoordsLat("38.7376").setCoordsLong("-9.1545").setAbrev("gulb").build();
		assertEquals(
				INVALID_ARGUMENT.getCode(),
				assertThrows(
						StatusRuntimeException.class, () -> frontend.bikeDown(request))
						.getStatus()
						.getCode());
	}

	// Test to return a bike at a distance over than 200 meters from a station, returns exception.
	@Test
	public void cantBikeDownTest2() {
		TopUpRequest requestS = TopUpRequest.newBuilder().setUsername("eva").setCellphone("+155509080706").setCash(10).build();
		frontend.addSaldo(requestS);
		BikeUpRequest request1 = BikeUpRequest.newBuilder().setUsername("eva").setCoordsLat("38.7075").setCoordsLong("-9.1364").setAbrev("prcm").build();
		frontend.bikeUp(request1);
		BikeDownRequest request2 = BikeDownRequest.newBuilder().setUsername("alice").setCoordsLat("39.2015").setCoordsLong("-9.1700").setAbrev("prcm").build();
		assertEquals(
				INVALID_ARGUMENT.getCode(),
				assertThrows(
						StatusRuntimeException.class, () -> frontend.bikeDown(request2))
						.getStatus()
						.getCode());
		BikeDownRequest request3 = BikeDownRequest.newBuilder().setUsername("eva").setCoordsLat("38.7075").setCoordsLong("-9.1364").setAbrev("prcm").build();
		BikeDownResponse response3 = frontend.bikeDown(request3);
		assertEquals("OK", response3.getResponse());
	}

	// Test to return a bike at a station with all the spots taken, returns exception.
	@Test
	public void cantBikeDownTest3() {
		TopUpRequest requestS = TopUpRequest.newBuilder().setUsername("eva").setCellphone("+155509080706").setCash(10).build();
		frontend.addSaldo(requestS);
		BikeUpRequest request1 = BikeUpRequest.newBuilder().setUsername("eva").setCoordsLat("38.7075").setCoordsLong("-9.1364").setAbrev("prcm").build();
		frontend.bikeUp(request1);
		BikeDownRequest request2 = BikeDownRequest.newBuilder().setUsername("eva").setCoordsLat("38.7255").setCoordsLong("-9.1499").setAbrev("mapo").build();
		assertEquals(
				INVALID_ARGUMENT.getCode(),
				assertThrows(
						StatusRuntimeException.class, () -> frontend.bikeDown(request2))
						.getStatus()
						.getCode());
		BikeDownRequest request3 = BikeDownRequest.newBuilder().setUsername("eva").setCoordsLat("38.7075").setCoordsLong("-9.1364").setAbrev("prcm").build();
		BikeDownResponse response3 = frontend.bikeDown(request3);
		assertEquals("OK", response3.getResponse());
	}
}
