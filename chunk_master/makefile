# Define variables
APP_NAME=chunk-master
JAR_FILE=target/$(APP_NAME)-0.0.1-SNAPSHOT.jar

# Default target
.PHONY: all
all: clean build run

# Clean the project
.PHONY: clean
clean:
	mvn clean

# Build the project
.PHONY: build
build:
	mvn package

# Run the application
.PHONY: run
run:
	java -jar $(JAR_FILE)

# Run the application in debug mode
.PHONY: debug
debug:
	java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar $(JAR_FILE)

# Run tests
.PHONY: test
test:
	mvn test

# Package the application into a JAR file
.PHONY: package
package:
	mvn package

# Clean, build, and run the application
.PHONY: restart
restart: clean build run
