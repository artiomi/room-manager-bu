# Room Occupancy Manager
The project exposes REST API endpoint which allows calculation of rooms availability for
given rooms type and customer prices.<br>
`main/resources/clients.json` JSON file is a sample of clients proposed prices.
In order to override it, property `app.clients-resource` can be used.<br>
Current min threshold for premium rooms is `100` EUR, this property can be changed via `app.premium.min-threshold`.<p> 
__Note:__ In provided test scenarios, for Test#4 is specified a wrong expected result:<br>
should be `1153.99` for `Premium` instead of `1153` and `45` for `Economy` instead of `45.99`.<p>
### Build
Java version - `17`
```
./gradlew clean build 
```

### Test
```
./gradlew test
```
### Run
```
./gradlew bootRun
```
### Swagger
After app startup, Swagger documentation will be available [here](http://localhost:8080/swagger-ui/index.html).

### Call availability check endpoint
```shell
curl -X 'GET' \
    'http://localhost:8080/rooms/availability?availablePremiumRooms=2&availableEconomyRooms=3' \
  -H 'accept: */*'
```
where:<br>
*availablePremiumRooms* - number of available premium rooms [0..n]<br>
*availableEconomyRooms* - number of available economy rooms [0..n]<p>
