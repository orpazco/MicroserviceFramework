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
	public static void main(String[] args) throws IOException {
		// init params
		String inputPath = args[0];
		String outputPath = args[1];
		Diary diary = new Diary();
		ExecutorService executor = Executors.newFixedThreadPool(5);

		CountDownLatch latch = new CountDownLatch(4);
		FlowData flowData = null;
		try {
			flowData = JsonHandler.deserialize(inputPath);
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
		Ewoks ewoks = new Ewoks(flowData.getEwoks());

		// Microservice initiation
		LinkedList<MicroService> microservices = new LinkedList<>();
		microservices.add(new LeiaMicroservice(flowData.getAttacks(), diary, latch));
		microservices.add(new LandoMicroservice(flowData.getLando(), diary, latch));
		microservices.add(new C3POMicroservice( diary, ewoks, latch));
		microservices.add(new HanSoloMicroservice(diary, ewoks, latch));
		microservices.add(new R2D2Microservice(flowData.getR2D2(), diary, latch));

		// Threadpool initiation
		for(MicroService mics : microservices)
			executor.execute(mics);
		executor.shutdown();
		try {
			executor.awaitTermination(5, TimeUnit.MINUTES);
			JsonHandler.serialize(diary,outputPath);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
