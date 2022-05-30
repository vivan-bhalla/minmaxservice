# minmaxservice

## Introduction 

This is a project for Baraka where we open a connection with a Baraka web socket which constantly sends messages which contain the time and the stock pricee. We have to calculate the stock's minimum and maximum price for particular intervals of 1 min from when the service starts.

#### Prerequisites: 
1. Please Make sure you jave Java Version 1.8 installed
2. Make sure you have mvn installed
3. There should be nothing running on the port 8080 as this application runs on that port

## How To Start?

1. Clone the Project:
    ` git clone https://github.com/vivan-bhalla/minmaxservice.git `

2. Run `mvn clean install` in the folder

3. Run `java -jar minmaxservice-0.0.1-SNAPSHOT.jar`. 
Note: The jar file will be create in the `target` folder.
When the websocket is opened, you will receive a `Webwocket opened` message. the service is running.

4. Go to `localhost:8080` and you should see the welcome message

5. To get price of any stock go to `localhost:8080/getprice/{stock}` for example to get the prices of TSLA stock, type `localhost:8080/getprice/TSLA`.

