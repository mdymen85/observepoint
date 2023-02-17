package com.challenge.observepoint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ObservepointApplicationTests {

	@Autowired
	private DataStructureService dataStructureService;

	@BeforeEach
	public void before() {
		dataStructureService.clear();
	}

	@Test
	void simpleIp() {
		var ip = "123.132.244.252";
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

	@Test
	public void top100Test() {
		var ip1 = "123.123.123.221";
		var ip2 = "123.123.123.222";
		var ip3 = "123.123.123.223";
		var ip4 = "123.123.123.224";
		var ip5 = "123.123.123.225";
		var ip6 = "123.123.123.226";
		Map<String, Integer> ips = new HashMap<>();
		ips.put(ip1, 501);
		ips.put(ip2, 502);
		ips.put(ip3, 503);
		ips.put(ip4, 504);
		ips.put(ip5, 505);
		ips.put(ip6, 506);

		for (int i = 0; i < 1000000; i++) {
			int random = new Random().nextInt(150 - 1) + 1;
			ips.put("123.123.0." + random, random);
		}
		ips.forEach((ip, count) -> {
			for (int i = 0; i < count; i++) {
				this.dataStructureService.requestHandled(ip);
			}
		});
		final Map<String, Integer> top100Map = this.dataStructureService.top100();
		System.out.println(top100Map);
		assertEquals(501, top100Map.get(ip1));
		assertEquals(502, top100Map.get(ip2));
		assertEquals(503, top100Map.get(ip3));
		assertEquals(504, top100Map.get(ip4));
		assertEquals(505, top100Map.get(ip5));
		assertEquals(506, top100Map.get(ip6));
		assertEquals(100, top100Map.size());
	}

	@Test
	public void top100With6IpsTest() {
		var ip1 = "123.123.123.121";
		var ip2 = "123.123.123.122";
		var ip3 = "123.123.123.123";
		var ip4 = "123.123.123.124";
		var ip5 = "123.123.123.125";
		var ip6 = "123.123.123.126";
		List<String> ips = new ArrayList<>();
		ips.addAll(Arrays.asList(ip1, ip1, ip1, ip1, ip1));
		ips.addAll(Arrays.asList(ip2, ip2, ip2, ip2));
		ips.addAll(Arrays.asList(ip3, ip3, ip3));
		ips.addAll(Arrays.asList(ip4, ip4, ip4));
		ips.addAll(Arrays.asList(ip5, ip5, ip5, ip5));
		ips.addAll(Arrays.asList(ip6, ip6));

		ips.forEach(ip -> this.dataStructureService.requestHandled(ip));
		final Map<String, Integer> top100Map = this.dataStructureService.top100();
		System.out.println(top100Map);
		assertEquals(5, top100Map.get(ip1));
		assertEquals(4, top100Map.get(ip2));
		assertEquals(4, top100Map.get(ip5));
		assertEquals(3, top100Map.get(ip3));
		assertEquals(6, top100Map.size());
	}

	@Test
	public void top100ConcurrentTest() throws InterruptedException {
		Map<String, Integer> ips = new HashMap<>();

		for (int i = 0; i < 1000000000; i++) {
			int random = new Random().nextInt(150 - 1) + 1;
			ips.put("123.123.0." + random, random);
		}
		int numberOfThreads = 100;
		ExecutorService service = Executors.newFixedThreadPool(1000);
		CountDownLatch latch = new CountDownLatch(numberOfThreads);

		Random newRandom = new Random();

		for (int i = 0; i < numberOfThreads; i++) {
			service.execute(() -> {

				try {
					Thread.sleep(newRandom.nextInt(1000));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				ips.forEach((ip, count) -> {
					for (int j = 0; j < count; j++) {
						this.dataStructureService.requestHandled(ip);
					}
				});

				latch.countDown();
			});
		}
		latch.await();


		final Map<String, Integer> top100Map = this.dataStructureService.top100();
		assertEquals(100, top100Map.size());
	}

}