# moneyTransferTestTask
**API**:
* Method: POST
* URL: localhost:8080/moneyTransfer/
* Request Body:
```
{
	"accountFromId" : 2,
	"accountToId": 1,
	"amount": 100
}
```
* Entry class: MoneyTransferServlet

## To run it locally run:
```
gradle appRun
```
or
```
gradle buildProduct and go to build/output/moneyTrasfer/ and run start.sh or start.bat.
```
