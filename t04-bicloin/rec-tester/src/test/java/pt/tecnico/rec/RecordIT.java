package pt.tecnico.rec;

import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.*;
import pt.tecnico.rec.grpc.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

import static io.grpc.Status.INVALID_ARGUMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RecordIT extends BaseIT{

	@Test
	public void pingOKTest() {
		PingRequest request = PingRequest.newBuilder().build();
		PingResponse response = frontend.ping(request);
		assertEquals("Pong.", response.getOutput());
	}

	// Write and Read Test for User and Station and Exception if empty id is sent.
	@Test
	public void testStation() {
		frontend.write(WriteRequest.newBuilder().setId("u/bot/balance").setValue(15).build());
		assertEquals(15, frontend.read(ReadRequest.newBuilder().setId(("u/bot/balance")).build()).getValue());
		frontend.write(WriteRequest.newBuilder().setId("s/mapo/bikes").setValue(5).build());
		frontend.write(WriteRequest.newBuilder().setId("s/mapo/levantamentos").setValue(3).build());
		frontend.write(WriteRequest.newBuilder().setId("s/mapo/devolucoes").setValue(2).build());
		assertEquals(5, frontend.read(ReadRequest.newBuilder().setId(("s/mapo/bikes")).build()).getValue());
		assertEquals(3, frontend.read(ReadRequest.newBuilder().setId(("s/mapo/levantamentos")).build()).getValue());
		assertEquals(2, frontend.read(ReadRequest.newBuilder().setId(("s/mapo/devolucoes")).build()).getValue());
	}
}
