package com.challenge.observepoint;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ObservepointApplicationTests {

	@Autowired
	private DataStructureService dataStructureService;

	@BeforeEach
	public void before() {
		dataStructureService.reset();
	}

	@Test
	void simpleIp() {
		var ip = "123.132.244.256";
		dataStructureService.requestHandled(ip);
		assertEquals(dataStructureService.getIpsMap().get(ip), 1);
	}

	@Test
	void sameIpMoreThanOneTime() {
		var ip = "123.132.52.13";
		dataStructureService.requestHandled(ip);
		dataStructureService.requestHandled(ip);
		assertEquals(dataStructureService.getIpsMap().get(ip), 2);
	}

	@Test
	void moreThanOneIp() {
		var ip = "123.132.52.13";
		var ip2 = "10.10.2.10";
		dataStructureService.requestHandled(ip);
		dataStructureService.requestHandled(ip);
		dataStructureService.requestHandled(ip2);
		assertEquals(dataStructureService.getIpsMap().get(ip), 2);
		assertEquals(dataStructureService.getIpsMap().get(ip2), 1);
	}

	@Test
	void top100() throws InterruptedException {
		var ip1 = "123.123.123.121";
		var ip2 = "123.123.123.122";
		var ip3 = "123.123.123.123";
		var ip4 = "123.123.123.124";
		var ip5 = "123.123.123.125";

		for (int i = 0; i < 10; i++) {
			this.dataStructureService.requestHandled(ip1);
		}
		for (int i = 0; i < 20; i++) {
			this.dataStructureService.requestHandled(ip2);
		}
		for (int i = 0; i < 30; i++) {
			this.dataStructureService.requestHandled(ip3);
		}
		for (int i = 0; i < 21; i++) {
			this.dataStructureService.requestHandled(ip4);
		}
		for (int i = 0; i < 11; i++) {
			this.dataStructureService.requestHandled(ip5);
		}

		Thread.sleep(10000);

		long init = System.currentTimeMillis();

		System.out.println(init);
		Map<String, Integer> map = this.dataStructureService.top100();
		System.out.println(System.currentTimeMillis() - init);


		int i = 0;

	}


}
