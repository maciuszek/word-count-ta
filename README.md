### Description

File word count to stdout implementation with Spring in a reactive programming paradigm. Structured with extensibility for alternative data ingress/egress and non-reactive implementations.

### Dependencies
* jdk17
* gnumake (optional)
* docker and an active daemon (optional)

### Build The App As A Jar
`./mvnw package`
or if you have gnumake
`make jar`

### Run The Jar
`java -jar target/wordcount-*-SNAPSHOT.jar ./src/test/resources/input.txt`

### Build A Container To Run The Jar (requires all dependencies including optional)
`make image`

### Run The Container (requires docker)
```
docker run --rm \
-v ./src/test/resources/input.txt:/app/./src/test/resources/input.txt \
-e ARGS='/app/./src/test/resources/input.txt' \
com.maciuszek/wordcount:0.0.1-SNAPSHOT
```

### More Info
* To enable full debug logging run the jvm with `-Dlogging.level.com.maciuszek.wordcount=DEBUG`
* The Makefile has a help page. Run `make help` or just `make`

### Experimental
An alternative faster and more efficient sorting implementation (based around an O(1) lfu algorithm) is in development and in a beta stage (working but not adhere to all business cases) https://github.com/maciuszek/word-count-ta/tree/active_sort 

### Todo
* For CI setup GitHub Actions to run tests
* For CI setup GitHub Actions to push to Docker Hub
