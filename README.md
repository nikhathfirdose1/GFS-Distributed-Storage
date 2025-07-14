# GFS-Inspired Distributed Storage System 🚀

A distributed storage system inspired by the Google File System (GFS), designed for scalable, fault-tolerant file storage and retrieval across multiple nodes.

---

## ⚙️ Key Features

- 🧠 **Chunk Master**: Manages metadata, client requests, and chunk distribution  
- 💾 **Chunk Servers**: Store data chunks, handle read/write requests  
- 🤝 **Client**: Interfaces with master and servers for upload/download  
- 🔁 **Fault Tolerance**: Data replication across multiple servers  
- 📈 **Scalable Architecture**: Load-balanced chunk distribution

---

## 🛠️ Technologies Used

- **Language:** Java  
- **Framework:** Spring Boot  
- **Architecture:** Microservices  
- **Security:** SSL/TLS, RSA, OpenSSL  
- **Build Tool:** Maven  
- **Testing:** JUnit, Postman

---

## 🔍 System Components

<details>
<summary><strong>1. Chunk Master</strong></summary>

- Manages metadata of stored files  
- Allocates chunks to chunk servers based on load balancing  
- Receives heartbeat signals from chunk servers for health monitoring  

</details>

<details>
<summary><strong>2. Chunk Server</strong></summary>

- Stores and manages file chunks  
- Responds to read and write requests from the client  
- Sends periodic heartbeat signals to the chunk master  

</details>

<details>
<summary><strong>3. Client</strong></summary>

- CLI-based interface for file upload/download  
- Splits files into chunks before uploading  
- Merges retrieved chunks back into the original file during downloads  

</details>

---

## 🔄 Data Flow

### 📥 Write Operation

1. Client sends a write request to the chunk master  
2. Chunk master returns a list of chunk servers  
3. Client splits the file and sends chunks to those servers  

### 📤 Read Operation

1. Client requests file metadata from the chunk master  
2. Master returns chunk locations  
3. Client downloads chunks and reconstructs the file  

---

## 🧪 Testing and Validation

- ✅ Unit tests for chunk storage, retrieval, and heartbeat logic  
- ✅ Integration testing across client, master, and chunk servers  
- ✅ API validation using Postman

---

## 🚀 Getting Started

### ✅ Prerequisites

- Java 11+  
- Maven  

### 🛠️ Setup & Run

```bash
# Clone the repository
git clone https://github.com/nikhathfirdose1/chunkfs.git
cd chunkfs

# Build the project
mvn clean install

# Start the Chunk Master
java -jar chunkmaster/target/chunkmaster.jar

# Start a Chunk Server (run this in separate terminals for multiple servers)
java -jar chunkserver/target/chunkserver.jar

# Run the Client
java -jar client/target/client.jar
