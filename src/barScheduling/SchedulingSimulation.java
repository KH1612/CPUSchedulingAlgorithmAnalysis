//M. M. Kuttel 2024 mkuttel@gmail.com

package barScheduling;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class SchedulingSimulation {
	static int noPatrons = 100; // number of customers - default value if not provided on command line
	static int sched = 0; // which scheduling algorithm, 0= FCFS, 1= SJF

	static CountDownLatch startSignal;

	static Patron[] patrons; // array for customer threads
	static Barman Andre;
	static FileWriter writer;

	public void writeToFile(String data) throws IOException {
		synchronized (writer) {
			writer.write(data);

		}
	}

	public static void main(String[] args) throws InterruptedException, IOException {

		// deal with command line arguments if provided
		if (args.length == 1) {
			noPatrons = Integer.parseInt(args[0]); // total people to enter room
		} else if (args.length == 2) {
			noPatrons = Integer.parseInt(args[0]); // total people to enter room
			sched = Integer.parseInt(args[1]);
		}

		writer = new FileWriter("data/Results_" + Integer.toString(sched) + ".csv", false);
		writer.write(String.format("%s,%s,%s,%s,%s,%s,%s\n", "ID", "Number_of_Drinks", "Arrival_Time",
				"Turnaround_Time", "Response_Time", "Waiting_Time", "Throughput")); // Field names into CSV file
		Patron.fileW = writer;

		startSignal = new CountDownLatch(noPatrons + 2);// Barman and patrons and main method must be raeady

		// create barman
		Andre = new Barman(startSignal, sched);
		Andre.start();

		// create all the patrons, who all need access to Andre
		patrons = new Patron[noPatrons];
		for (int i = 0; i < noPatrons; i++) {
			patrons[i] = new Patron(i, startSignal, Andre);
			patrons[i].start();
		}

		System.out.println("------Andre the Barman Scheduling Simulation------");
		System.out.println("-------------- with " + Integer.toString(noPatrons) + " patrons---------------");

		startSignal.countDown(); // main method ready
		long startSim = System.currentTimeMillis();

		// wait till all patrons done, otherwise race condition on the file closing!
		for (int i = 0; i < noPatrons; i++) {
			patrons[i].join();
		}

		System.out.println("------Waiting for Andre------");
		Andre.interrupt(); // tell Andre to close up
		Andre.join(); // wait till he has
		long endSim = System.currentTimeMillis();
		writer.append(String.format("\n%s\n", "Total_Time (s)"));
		writer.append(String.format("%.2f", (float) (endSim - startSim) / 1000)); // Total execution time in CSV
		writer.close(); // all done, can close file
		System.out.println("------Bar closed------");
	}

}
