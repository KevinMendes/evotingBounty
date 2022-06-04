# voting-server
The Microservices of the voting-server are responsible for receiving, processing and storing the votes in the ballot box.

| Name     | Description    | 
| --------|---------|
| **API Gateway**  | The API Gateway is the main entry point for all communication and integration of the services. This component serves as a proxy and as a filter. All requests reach the rest of the components through this microservice. It does not contain any business logic.    |
| **Authentication** | The component that generates and validates the authentication token as part of the login process and during any other request that requires authentication |
| **Certificate Registry** | This service contains the different certificates (Platform Root, Tenant and Admin Board), checks their validity when uploaded, and returns them when requested.  |
| **Election Information** | This service provides the ballot information i.e. the options for the different contests of an Election Event the voter is eligible to participate in. It performs vote validations, stores the vote and the confirmation proof of the vote. |
| **Extended Authentication** | This service authenticates a voter based on additional information, for example, the year of birth. It works in conjunction with the authentication service. The Extended Authentication is optional, and the type of additional information is configurable.  |
| **Orchestrator** | Receives requests for the Control Components and oversees the workload and communication amongst the Control Component nodes at several stages of the election. |
| **Vote Verification** | Verifies that a vote is «cast as intended» by computing the Choice Return Codes corresponding to the encrypted vote with the help of the Control Components. It also performs the second level of vote correctness verification by checking the content of the vote against the rules applicable to the voter. |
| **Voter Material** | Provides information about the voter (e.g. credentials, eligible election events and reference to verification codes). |
| **Voting Workflow** | Responsible for receiving and managing requests from the client. It controls the workflow for the Election Event as well as the status of the voter. |


## Usage
The voting-server microservices are packaged as .war files and deployed in separate TomEE instances.

## Development

```
mvn clean install
```
