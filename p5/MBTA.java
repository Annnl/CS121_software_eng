import java.util.*;
import java.io.*;
import java.util.concurrent.locks.*;
import com.google.gson.Gson;
public class MBTA {
  private Map<String, List<Station>> lines = new HashMap<>();
  private Map<String, List<Station>> journeys = new HashMap<>();
  private Map<Train, Station> trainLocations = new HashMap<>();      
  private Map<Train, Integer> trainPositions = new HashMap<>();      
  private Map<Train, Integer> trainDirections = new HashMap<>();  
  private Map<Passenger, Station> passengerLocations = new HashMap<>(); 
  private Map<Passenger, Integer> passengerProgress = new HashMap<>();  
  private Map<Passenger, Train> passengerTrains = new HashMap<>();

  private Map<Station, ReentrantLock> stationLocks = new HashMap<>();
  private Map<Station, Condition> stationConditions = new HashMap<>();
  // Creates an initially empty simulation
  public MBTA() { }

  // Adds a new transit line with given name and stations
  public void addLine(String name, List<String> stations) {
    Train train = Train.make(name);
    List<Station> stationList = new ArrayList<>();
    for (String stationName : stations) {
      Station station = Station.make(stationName);
      stationList.add(station);
      if (!stationLocks.containsKey(station)) {
        ReentrantLock lock = new ReentrantLock();
        stationLocks.put(station, lock);
        stationConditions.put(station, lock.newCondition());
      }

    }
    
    lines.put(name, stationList);
    trainLocations.put(train, stationList.get(0));
    trainPositions.put(train, 0);
    trainDirections.put(train, 1);
  }

  // Adds a new planned journey to the simulation
  public void addJourney(String name, List<String> stations) {
    Passenger passenger = Passenger.make(name);
    List<Station> stationList = new ArrayList<>();
    for (String stationName : stations) {
      stationList.add(Station.make(stationName));
    }
    
    journeys.put(name, stationList);
    passengerLocations.put(passenger, stationList.get(0));
    passengerProgress.put(passenger, 0);
    passengerTrains.put(passenger, null);
  }

  // Return normally if initial simulation conditions are satisfied, otherwise
  // raises an exception
  public void checkStart() {
    for (Map.Entry<String, List<Station>> entry : lines.entrySet()) {
      Train train = Train.make(entry.getKey());
      Station firstStation = entry.getValue().get(0);
      
      if (!trainLocations.get(train).equals(firstStation)) {
        throw new RuntimeException();
      }
    }
    
    for (Map.Entry<String, List<Station>> entry : journeys.entrySet()) {
      Passenger passenger = Passenger.make(entry.getKey());
      Station firstStation = entry.getValue().get(0);
      
      if (!passengerLocations.get(passenger).equals(firstStation)) {
        throw new RuntimeException();
      }
    }
  }

  // Return normally if final simulation conditions are satisfied, otherwise
  // raises an exception
  public void checkEnd() {
    for (Map.Entry<String, List<Station>> entry : journeys.entrySet()) {
      Passenger passenger = Passenger.make(entry.getKey());
      List<Station> journey = entry.getValue();
      int progress = passengerProgress.get(passenger);

      if (progress != journey.size() - 1) {
        throw new RuntimeException();
      }
      
      if (passengerTrains.get(passenger) != null) {
        throw new RuntimeException();
      }
    }
  }

  // reset to an empty simulation
  public void reset() {
    lines.clear();
    journeys.clear();
    trainLocations.clear();
    trainPositions.clear();
    trainDirections.clear();
    passengerLocations.clear();
    passengerProgress.clear();
    passengerTrains.clear();
  }

  // adds simulation configuration from a file
  public void loadConfig(String filename) {
    Gson gson = new Gson();
    
    try (FileReader reader = new FileReader(filename)) {
      Config config = gson.fromJson(reader, Config.class);
      
      if (config.lines != null) {
        for (Map.Entry<String, List<String>> entry : config.lines.entrySet()) {
          addLine(entry.getKey(), entry.getValue());
        }
      }
      
      if (config.trips != null) {
        for (Map.Entry<String, List<String>> entry : config.trips.entrySet()) {
          addJourney(entry.getKey(), entry.getValue());
        }
      }
    } catch (IOException e) {
      throw new RuntimeException();
    }
  }

  public List<Station> getTrainLine(Train train) {
    return lines.get(train.toString());
  }
  
  public List<Station> getPassengerJourney(Passenger passenger) {
    return journeys.get(passenger.toString());
  }

  public ReentrantLock getStationLock(Station s) {
    return stationLocks.get(s);
  }
  
  public Condition getStationCondition(Station s) {
    return stationConditions.get(s);
  }
  public Set<String> getLineNames() {
    return lines.keySet();
  }
  
  public Set<String> getPassengerNames() {
    return journeys.keySet();
  }
  public boolean isStationOccupied(Station s, Train excludeTrain) {
    ReentrantLock lock = stationLocks.get(s);
    lock.lock();
    try {
      for (Map.Entry<Train, Station> entry : trainLocations.entrySet()) {
        if (!entry.getKey().equals(excludeTrain) &&
          entry.getValue().equals(s)) {
          return true;
        }
      }
      return false;
    } finally {
      lock.unlock();
    }
  }
  
  public void moveTrain(Train t, Station from, Station to) {
    ReentrantLock lockFrom = stationLocks.get(from);
    ReentrantLock lockTo = stationLocks.get(to);

    if (from.toString().compareTo(to.toString()) < 0) {
      lockFrom.lock();
      lockTo.lock();
    } else {
      lockTo.lock();
      lockFrom.lock();
    }

    try {
      trainLocations.put(t, to);
      int fromIndex = getTrainLine(t).indexOf(from);
      int toIndex = getTrainLine(t).indexOf(to);
      trainPositions.put(t, toIndex);

      if (toIndex > fromIndex) trainDirections.put(t, 1);
      else trainDirections.put(t, -1);

      if (toIndex == getTrainLine(t).size() - 1) trainDirections.put(t, -1);
      else if (toIndex == 0) trainDirections.put(t, 1);

    } finally {
      lockTo.unlock();
      lockFrom.unlock();
    }
  }
  

  public Station getNextStation(Train t) {
    List<Station> line = getTrainLine(t);
    int currentPos = trainPositions.get(t);
    int direction = trainDirections.get(t);
    int nextPos = currentPos + direction;

    if (nextPos >= line.size()) {
        nextPos = currentPos - 1;
    } else if (nextPos < 0) {
        nextPos = currentPos + 1;
    }
    
    return line.get(nextPos);
  }
  

  public void boardPassenger(Passenger p, Train t) {
    passengerTrains.put(p, t);
  }
  
  public void deboardPassenger(Passenger p, Station s) {
    passengerTrains.put(p, null);
    passengerLocations.put(p, s);
    int progress = passengerProgress.get(p);
    passengerProgress.put(p, progress + 1);
  }
  
  public boolean isPassengerDone(Passenger p) {
    List<Station> journey = getPassengerJourney(p);
    int progress = passengerProgress.get(p);
    return progress >= journey.size() - 1;
  }

  public Station getPassengerLocation(Passenger p) {
    return passengerLocations.get(p);
  }
  
  public Train getPassengerTrain(Passenger p) {
    return passengerTrains.get(p);
  }
  
  public Station getTrainLocation(Train t) {
    return trainLocations.get(t);
  }
  
  public Station getPassengerNextStation(Passenger p) {
    List<Station> journey = getPassengerJourney(p);
    int progress = passengerProgress.get(p);
    if (progress + 1 < journey.size()) {
      return journey.get(progress + 1);
    }
    return null;
  }

  public int getPassengerProgress(Passenger p) {
    return passengerProgress.get(p);
  }
  
  public int getTrainPosition(Train t) {
    return trainPositions.get(t);
  }
  
  public int getTrainDirection(Train t) {
    return trainDirections.get(t);
  }
}
