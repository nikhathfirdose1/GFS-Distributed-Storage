# GFS-Inspired Distributed Storage System

This project implements a distributed storage system inspired by the Google File System (GFS). It is designed to handle large-scale file storage and retrieval using a distributed architecture.

## Key Features

- **Chunk Master:** Manages metadata, chunk distribution, and client interactions.
- **Chunk Server:** Stores data chunks and handles read/write requests.
- **Client:** Interfaces with the chunk master and chunk servers for file storage and retrieval.
- **Fault Tolerance:** Replicates data across multiple servers for durability.
- **Scalable Architecture:** Designed for large-scale data handling with load balancing.

## Technologies Used
- **Language:** Java
- **Framework:** Spring Boot
- **Architecture:** Microservices
- **Security:** SSL/TLS, RSA, OpenSSL
- **Tools:** Maven, Postman

## System Components

### 1. Chunk Master
- Manages metadata of stored files.
- Allocates chunks to chunk servers based on load balancing.
- Receives heartbeat signals from chunk servers for health monitoring.

### 2. Chunk Server
- Stores and manages file chunks.
- Responds to read and write requests from the client.
- Sends periodic heartbeat signals to the chunk master.

### 3. Client
- Provides a command-line interface for users to upload and retrieve files.
- Splits files into chunks before uploading.
- Merges retrieved chunks back into the original file during downloads.

## Data Flow

### Write Operation:
1. The client sends a write request to the chunk master.
2. The chunk master returns the list of chunk servers for data storage.
3. The client splits the file into chunks and sends them to the specified chunk servers.

### Read Operation:
1. The client requests a file from the chunk master.
2. The chunk master provides the chunk server locations.
3. The client retrieves chunks from the chunk servers and reconstructs the file.

## Chunk Master Implementation
### Core Components:
- **ChunkMasterApplication:** Entry point for the Spring Boot application.
- **ChunkMasterController:** Manages client requests for chunk allocation and retrieval.
- **MetadataService:** Maintains file metadata and chunk mappings.
- **LoadBalancer:** Assigns chunks to the least-loaded chunk servers for optimized distribution.

## Chunk Server Implementation
### Core Components:
- **ChunkServerApplication:** Entry point for the Spring Boot application.
- **ChunkServerController:** Handles HTTP requests for chunk operations.
- **ChunkServerService:** Manages storage and retrieval of data chunks.
- **HeartBeatService:** Sends periodic heartbeat messages to the master server.

## Client Implementation
### Core Components:
- **ClientApplication:** Entry point for the client-side operations.
- **ClientService:** Handles file splitting, merging, and interaction with the master and chunk servers.


## Testing and Validation
- **Postman** used for endpoint validation.
- **Unit Tests** for chunk storage, retrieval, and heartbeat mechanisms.
- **Integration Testing** with client, master, and chunk server components.

## Setup and Deployment

### Prerequisites:
- Java 11+
- Maven
- Apache Kafka & Zookeeper

### Steps:
1. Clone the repository:
   ```bash
   git clone <repo-url>
   ```
2. Build the project:
   ```bash
   mvn clean install
   ```
3. Start Chunk Master and Servers:
   ```bash
   java -jar target/chunkmaster.jar
   java -jar target/chunkserver.jar
   ```
4. Run the Client:
   ```bash
   java -jar target/client.jar
   ```
