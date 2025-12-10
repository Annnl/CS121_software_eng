import java.io.*;
import java.util.*;
import java.util.concurrent.locks.*;

public class Sim {

  public static void run_sim(MBTA mbta, Log log) {
    List<Thread> threads = new ArrayList<>();
    List<TrainRunnable> trainRunnables = new ArrayList<>();
    
    for (String lineName : mbta.getLineNames()) {
      Train train = Train.make(lineName);
      TrainRunnable trainRunnable = new TrainRunnable(train, mbta, log);
      trainRunnables.add(trainRunnable);
      Thread trainThread = new Thread(trainRunnable);
      threads.add(trainThread);
      trainThread.start();
    }
    

    List<Thread> passengerThreads = new ArrayList<>();
    for (String passengerName : mbta.getPassengerNames()) {
      Passenger passenger = Passenger.make(passengerName);
      Thread passengerThread = new Thread(new PassengerRunnable(passenger, mbta, log));
      passengerThreads.add(passengerThread);
      passengerThread.start();
    }

    for (Thread thread : passengerThreads) {
      try {
        thread.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    

    for (TrainRunnable trainRunnable : trainRunnables) {
      trainRunnable.stop();
    }

    for (String lineName : mbta.getLineNames()) {
      Train train = Train.make(lineName);
      Station location = mbta.getTrainLocation(train);
      ReentrantLock lock = mbta.getStationLock(location);
      lock.lock();
      try {
        mbta.getStationCondition(location).signalAll();
      } finally {
        lock.unlock();
      }
    }
    

    for (Thread thread : threads) {
      if (passengerThreads.contains(thread)) continue;
      try {
        thread.join(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.out.println("usage: ./sim <config file>");
      System.exit(1);
    }

    MBTA mbta = new MBTA();
    mbta.loadConfig(args[0]);

    Log log = new Log();

    run_sim(mbta, log);

    String s = new LogJson(log).toJson();
    PrintWriter out = new PrintWriter("log.json");
    out.print(s);
    out.close();

    mbta.reset();
    mbta.loadConfig(args[0]);
    Verify.verify(mbta, log);
  }
}




class TrainRunnable implements Runnable {
  private Train train;
  private MBTA mbta;
  private Log log;
  private volatile boolean shouldStop = false;
  
  public TrainRunnable(Train train, MBTA mbta, Log log) {
    this.train = train;
    this.mbta = mbta;
    this.log = log;
  }
  
  public void stop() {
    shouldStop = true;
  }
  
  @Override
  public void run() {
      while (!shouldStop) {
          Station currentStation = mbta.getTrainLocation(train);
          Station nextStation = mbta.getNextStation(train);

          ReentrantLock currentLock = mbta.getStationLock(currentStation);
          ReentrantLock nextLock = mbta.getStationLock(nextStation);

          currentLock.lock();
          nextLock.lock();
          try {
              while (mbta.isStationOccupied(nextStation, train) && !shouldStop) {
                  try {
                    mbta.getStationCondition(nextStation).await();
                  } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                  }
              }
              if (shouldStop) break;

              mbta.moveTrain(train, currentStation, nextStation);
              log.train_moves(train, currentStation, nextStation);

              mbta.getStationCondition(currentStation).signalAll();
              mbta.getStationCondition(nextStation).signalAll();
          } finally {
              nextLock.unlock();
              currentLock.unlock();
          }

          try {
            Thread.sleep(10);
          } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
      }
  }
}


class PassengerRunnable implements Runnable {
  private Passenger passenger;
  private MBTA mbta;
  private Log log;
  
  public PassengerRunnable(Passenger passenger, MBTA mbta, Log log) {
    this.passenger = passenger;
    this.mbta = mbta;
    this.log = log;
  }
  
  @Override
  public void run() {
    while (!mbta.isPassengerDone(passenger)) {
      Station currentLocation = mbta.getPassengerLocation(passenger);
      Train currentTrain = mbta.getPassengerTrain(passenger);
      Station targetStation = mbta.getPassengerNextStation(passenger);
      
      if (targetStation == null) {
        break; 
      }
      
      if (currentTrain == null) {

        ReentrantLock stationLock = mbta.getStationLock(currentLocation);
        Condition stationCondition = mbta.getStationCondition(currentLocation);
        
        stationLock.lock();
        try {
          Train trainToBoard = findTrainToBoard(currentLocation, targetStation);
          
          if (trainToBoard != null) {
            mbta.boardPassenger(passenger, trainToBoard);
            log.passenger_boards(passenger, trainToBoard, currentLocation);
          } else {
            stationCondition.await();
          }
        } catch (InterruptedException e) {
          return;
        } finally {
          stationLock.unlock();
        }
        
      } else {
        Station trainLocation = mbta.getTrainLocation(currentTrain);
        
        if (trainLocation.equals(targetStation)) {
          ReentrantLock stationLock = mbta.getStationLock(trainLocation);
          stationLock.lock();
          try {
            mbta.deboardPassenger(passenger, trainLocation);
            log.passenger_deboards(passenger, currentTrain, trainLocation);
          } finally {
            stationLock.unlock();
          }
        } else {
          try {
            Thread.sleep(5);
          } catch (InterruptedException e) {
            return;
          }
        }
      }
    }
  }

  
  private Train findTrainToBoard(Station currentStation, Station targetStation) {
    for (String lineName : mbta.getLineNames()) {
      Train train = Train.make(lineName);
      Station trainLocation = mbta.getTrainLocation(train);
      
      if (trainLocation.equals(currentStation)) {
        List<Station> line = mbta.getTrainLine(train);
        if (line.contains(targetStation)) {
          return train;
        }
      }
    }
    return null;
  }
}
