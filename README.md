# Instant Payment and Digital Euro Simulator

A Java service that mimics the Eurosystem's TIPS (Trans-European Instant Gross Settlement system) instant-payment system with support for digital-euro holding limits.

## Features

- **PACS.008 Message Processing**: Reads and validates ISO 20022 credit transfer initiation messages
- **Instant Payment Confirmation**: Confirms payments within the 10-second limit for euro instant payments
- **Digital-Euro Holding Limits**: Enforces per-person holding limits on digital-euro accounts
- **Automatic Waterfall Transfers**: Automatically moves amounts exceeding the holding limit to the user's linked bank account
- **Thread-Safe Operations**: Concurrent payment processing with synchronized account operations
- **Comprehensive Testing**: Unit tests covering all major components

## Architecture

### Core Components

#### Models
- **PaymentMessage**: Represents a PACS.008 credit transfer initiation
- **PaymentResult**: Contains payment processing result and status
- **DigitalEuroAccount**: Represents a digital-euro account with holding limit enforcement
- **WaterfallTransaction**: Tracks automatic transfers to linked bank accounts

#### Processing
- **Pacs008Parser**: Parses XML-based PACS.008 messages and validates payment data
- **PaymentProcessor**: Core engine handling payment execution, validation, and waterfall mechanism
- **DigitalEuroSimulator**: Main application orchestrating the payment flow

## Building

Prerequisites:
- Java 11 or higher
- Maven 3.6.0 or higher

### Compile
```bash
mvn clean compile
```

### Run Tests
```bash
mvn test
```

### Build JAR
```bash
mvn clean package
```

## Usage

### Basic Example

```java
DigitalEuroSimulator simulator = new DigitalEuroSimulator();
simulator.setupDemoAccounts();

String pacs008Xml = """
    <?xml version="1.0" encoding="UTF-8"?>
    <Document>
        <MsgId>MSG001</MsgId>
        <Initiator>
            <Id>ACC001</Id>
        </Initiator>
        <Beneficiary>
            <Id>ACC002</Id>
        </Beneficiary>
        <Amount>500.00</Amount>
        <Ccy>EUR</Ccy>
        <Purp>
            <Cd>OTHR</Cd>
        </Purp>
    </Document>
    """;

PaymentResult result = simulator.processPaymentFromXml(pacs008Xml);
System.out.println("Payment Result: " + result);
```

### Running the Simulator

```bash
java -jar target/digital-euro-simulator-1.0.0.jar
```

## Digital Euro Rules

### Holding Limit Enforcement
Each digital-euro account has a maximum holding limit. When a payment would cause the balance to exceed this limit, the payment is rejected:

```
if (payeeBalance + paymentAmount > holdingLimit)
    reject payment with reason "Payee holding limit would be exceeded"
```

### Waterfall Mechanism
When a digital-euro account balance exceeds its holding limit (after receiving a payment), excess funds are automatically transferred to the account holder's linked bank account:

```
excessAmount = max(0, accountBalance - holdingLimit)
if (excessAmount > 0)
    transfer excessAmount to linkedBankAccount
    deduct excessAmount from digitalEuroAccount
```

## Payment Confirmation Timeline

The system ensures TIPS-compliant instant payment confirmation:

1. **Validation** (< 1ms): Message structure and format validation
2. **Account Verification** (< 1ms): Check payer and payee accounts exist
3. **Balance Check** (< 1ms): Verify payer has sufficient funds
4. **Holding Limit Check** (< 1ms): Verify payee can receive the amount
5. **Execution** (< 1ms): Execute the transfer with synchronization
6. **Waterfall Check** (< 1ms): Trigger waterfall if limit exceeded
7. **Confirmation** (< 10 seconds total): Payment confirmed

## Test Suite

The project includes comprehensive tests:

### PaymentProcessorTest
- Successful payment execution
- Insufficient funds handling
- Holding limit enforcement
- Waterfall transfer triggering
- Invalid message handling
- Multiple concurrent payments

### Pacs008ParserTest
- Valid XML parsing
- Currency validation
- Amount validation
- Missing field handling
- Complex nested message structures

### DigitalEuroAccountTest
- Account creation and management
- Holding limit validation
- Excess amount calculation
- Balance updates

### DigitalEuroSimulatorTest
- Full integration testing
- Multi-payment scenarios
- Waterfall triggering
- 10-second confirmation window

Run tests with:
```bash
mvn test
```

## Data Flow

```
PACS.008 Message (XML)
        ↓
  Pacs008Parser
        ↓
PaymentMessage (Java object)
        ↓
PaymentProcessor.processPayment()
        ├─ Validation
        ├─ Account lookup
        ├─ Balance check
        ├─ Holding limit check
        ├─ Execute transfer
        ├─ Waterfall check
        └─ Return PaymentResult
        ↓
PaymentResult (Success/Rejected)
```

## Configuration

### Demo Accounts

Three demo accounts are created by default:

| Account | Person | Balance | Holding Limit | Linked Bank |
|---------|--------|---------|---------------|-------------|
| ACC001 | PERSON001 | €5000.00 | €2000.00 | BANK001-ACC001 |
| ACC002 | PERSON002 | €3000.00 | €3000.00 | BANK002-ACC002 |
| ACC003 | PERSON003 | €1000.00 | €1500.00 | BANK003-ACC003 |

## Project Structure

```
src/
├── main/java/com/eurosystem/simulator/
│   ├── DigitalEuroSimulator.java
│   ├── model/
│   │   ├── PaymentMessage.java
│   │   ├── PaymentResult.java
│   │   ├── DigitalEuroAccount.java
│   │   └── WaterfallTransaction.java
│   ├── payment/
│   │   └── PaymentProcessor.java
│   └── pacs/
│       └── Pacs008Parser.java
└── test/java/com/eurosystem/simulator/
    ├── payment/
    │   └── PaymentProcessorTest.java
    ├── pacs/
    │   └── Pacs008ParserTest.java
    ├── model/
    │   └── DigitalEuroAccountTest.java
    └── DigitalEuroSimulatorTest.java
```

## Logging

The application uses SLF4J with SimpleLogger. Logs are output to console by default. Configure logging in `src/main/resources/simplelogger.properties` (create if needed).

## Dependencies

- **Gson 2.10.1**: JSON processing (optional, for future enhancements)
- **SLF4J 2.0.5**: Logging
- **JUnit Jupiter 5.9.2**: Testing framework
- **JAXB 2.4.0**: XML binding (Java 11 compatibility)

## Compliance

- **ISO 20022**: PACS.008.003.02 message format support
- **TIPS Standard**: 10-second confirmation window
- **Eurosystem Digital Euro Rules**: Holding limit and waterfall mechanism

## Development Notes

### Thread Safety
- Payment processor uses `ConcurrentHashMap` for account storage
- Account operations are synchronized to prevent race conditions
- Waterfall transactions are stored in synchronized list

### Error Handling
- Parser exceptions provide clear error messages
- Payment processor validates all inputs before execution
- Comprehensive logging at INFO and ERROR levels

### Extensibility
- Easy to add new payment processors
- Pluggable parser implementations
- Account types can be extended with additional fields

## License

Developed by Engr Dr Ammad for the Eurosystem instant-payment framework.

## Contact

Developed by: Engr Ammad Aslam
Date: 2026-10-29
