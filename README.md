# Invoice AWS

 A Spring Boot application that reads invoice files from **AWS S3**, parses and stores them in **MongoDB**, and sends notifications via **AWS SQS**. It also supports retrieving invoices by `accountId`.


---

## 🧩 Overview

This Spring Boot-based backend service is designed to process invoice data asynchronously:

1. Reads invoice files (e.g., JSON or CSV) from **Amazon S3**
2. Parses the file contents
3. Stores parsed invoice records in **MongoDB**
4. Sends confirmation messages via **AWS SQS**
5. Allows querying of invoices by `accountId`

The system uses **caching**, **asynchronous task execution**, and includes **unit and integration tests** for reliability and performance.

---

## ✅ Features

- 📁 **Invoice File Processing**: Automatically processes invoice files uploaded to an S3 bucket.
- 🗃️ **Data Storage**: Stores invoice metadata in MongoDB.
- 🚀 **Async Processing**: Uses Spring’s `@Async` to handle tasks without blocking.
- 📡 **Message Queue**: Leverages **AWS SQS** for decoupled, scalable communication.
- 💾 **Caching Layer**: Improves read performance with caching (e.g., Redis or Caffeine).
- 🧪 **Testing**: Includes comprehensive **JUnit 5** unit and integration tests.
- 🔍 **Query Support**: Supports querying stored invoices by `accountId`.

---

## 🛠️ Technologies Used

| Technology        | Purpose |
|------------------|---------|
| **Spring Boot**  | Backend framework |
| **Java 17+**     | Programming language |
| **AWS S3**       | Store and retrieve invoice files |
| **AWS SQS**      | Send asynchronous messages after processing |
| **MongoDB**      | NoSQL database for storing invoice metadata |
| **Caching**      | Improve query performance (Redis / Caffeine / etc.) |
| **JUnit 5 + Mockito** | Unit and integration testing |
| **Maven**        | Build tool |

---

## 📁 Project Structure




---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven
- AWS Account (for S3 and SQS access)
- MongoDB instance (local or Atlas)
- AWS credentials configured locally or via IAM role

### Setup

1. Clone the repo:
```bash
git clone https://github.com/ahmedrayf/invoice-aws.git 
cd invoice-aws
```
2. Configure AWS and MongoDB settings in application.yml with your credentials

3. Build and run:
```bash   
mvn clean install
mvn spring-boot:run
```
🧪 Testing
```bash
mvn test
```

## License
This project is licensed under Ahmed Rayef License.

## Author
### Ahmed Rayef

[Website](https://ahmedrayf.github.io/)

[LinkedIn](https://www.linkedin.com/in/ahmedrayf/)

[Email](ahmedrayf@hotmail.com)
