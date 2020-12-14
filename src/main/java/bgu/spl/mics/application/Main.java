package bgu.spl.mics.application;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.passiveObjects.Diary;
import bgu.spl.mics.application.passiveObjects.Ewoks;
import bgu.spl.mics.application.passiveObjects.FlowData;
import bgu.spl.mics.application.passiveObjects.JsonHandler;
import bgu.spl.mics.application.services.*;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** This is the Main class of the application. You should parse the input file,
 * create the different components of the application, and run the system.
 * In the end, you should output a JSON.
 */
public class Main {
	private static final String PATH = "input.json" ;

	public static void main(String[] args) {
		long begin = System.currentTimeMillis();
		System.out.println("start: " + begin);
		CountDownLatch latch = new CountDownLatch(4);
		FlowData flowData = null;
		try {
			flowData = JsonHandler.deserialize(PATH);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Diary diary = new Diary();
		Ewoks ewoks = new Ewoks(flowData.getEwoks());
		// Microservice initiation
		LinkedList<MicroService> microservices = new LinkedList<>();
		microservices.add(new LeiaMicroservice(flowData.getAttacks(), diary, latch));
		microservices.add(new LandoMicroservice(flowData.getLando(), diary, latch));
		microservices.add(new C3POMicroservice( diary, ewoks, latch));
		microservices.add(new HanSoloMicroservice(diary, ewoks, latch));
		microservices.add(new R2D2Microservice(flowData.getR2D2(), diary, latch));
		// Threadpool initiation
		ExecutorService executor = Executors.newFixedThreadPool(5);
		for(MicroService mics : microservices)
			executor.execute(mics);
		executor.shutdown();
		try {
			executor.awaitTermination(70, TimeUnit.SECONDS);
			JsonHandler.serialize(diary,"out.json");
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		System.out.print("end: " + end);
		long total = end-begin;
		System.out.print("\ntotal: " + total);

	}
}
