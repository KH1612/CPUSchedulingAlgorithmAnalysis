//M. M. Kuttel 2024 mkuttel@gmail.com
package barScheduling;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

/*
 This is the basicclass, representing the patrons at the bar
 */

public class Patron extends Thread {

	private Random random = new Random();// for variation in Patron behaviour

	private CountDownLatch startSignal; // all start at once, actually shared
	private Barman theBarman; // the Barman is actually shared though

	private int ID; // thread ID
	private int lengthOfOrder;
	private long startTime, endTime, endResponse; // for all the metrics

	public static FileWriter fileW;

	private DrinkOrder[] drinksOrder;

	Patron(int ID, CountDownLatch startSignal, Barman aBarman) {
		this.ID = ID;
		this.startSignal = startSignal;
		this.theBarman = aBarman;
		this.lengthOfOrder = random.nextInt(5) + 1;// between 1 and 5 drinks
		drinksOrder = new DrinkOrder[lengthOfOrder];
	}

	public void writeToFile(String data) throws IOException {
		synchronized (fileW) {
			fileW.write(data);
		}
	}

	public void sortDrinkOrders(DrinkOrder[] orders) { // Sort order by execution time
		Arrays.sort(orders, (o1, o2) -> o1.compareTo(o2));
	}

	public void run() {
		try {
			// Do NOT change the block of code below - this is the arrival times
			startSignal.countDown(); // this patron is ready
			startSignal.await(); // wait till everyone is ready
			int arrivalTime = random.nextInt(300) + ID * 100; // patrons arrive gradually later
			sleep(arrivalTime);// Patrons arrive at staggered times depending on ID
			System.out.println("thirsty Patron " + this.ID + " arrived");
			// END do not change

			// create drinks order
			for (int i = 0; i < lengthOfOrder; i++) {
				drinksOrder[i] = new DrinkOrder(this.ID);

			}
			System.out.println("Patron " + this.ID + " submitting order of " + lengthOfOrder + " drinks"); // output in
																											// standard
																											// format -
																											// do not
																											// change
																											// this
			startTime = System.currentTimeMillis();// started placing orders
			for (int i = 0; i < lengthOfOrder; i++) {
				System.out.println("Order placed by " + drinksOrder[i].toString());
				theBarman.placeDrinkOrder(drinksOrder[i]);
			}
			sortDrinkOrders(drinksOrder); // Sort drinks order so that shortest drink is acknowledged first
			long runningTime = 0;
			for (int i = 0; i < lengthOfOrder; i++) {
				drinksOrder[i].waitForOrder();
				runningTime += drinksOrder[i].getExecutionTime();
				if (i == 0) { // Time of first drink recieved
					endResponse = System.currentTimeMillis();
				}
			}

			endTime = System.currentTimeMillis();
			long responseTime = endResponse - startTime; // Response Time
			long totalTime = endTime - startTime; // Turnaround Time
			long waitingTime = totalTime - runningTime; // Waiting Time
			float thoughput = (float) lengthOfOrder / ((float) totalTime / 1000); // Throughput
			writeToFile(String.format("%d,%d,%d,%d,%d,%d,%.2f\n", ID, lengthOfOrder, arrivalTime, totalTime,
					responseTime, waitingTime, thoughput)); // Write result to CSV file
			System.out.println("Patron " + this.ID + " got order in " + totalTime);

		} catch (InterruptedException e1) { // do nothing
		} catch (IOException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
	}
}
	

