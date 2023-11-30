## RMM Service

A Remote Monitoring and Management (RMM) platform helps IT professionals manage a fleet of Devices with Services associated with them. This Web Service will fulfill the most basic requirements of an RMM by keeping a simple inventory of Devices and Services to calculate their total costs. The service will provide its functionality via a RESTful API.

Next, find a brief description of some system design choices. This does not pretend to be an exhaustive nor a complete system design document.

### Where is the design document?

[Here](https://onthehost-my.sharepoint.com/:b:/g/personal/david_univercenter_com/EU_t1k7E1V9PhTax4Dl12JgBowkVtpQtmHA_aqdHoziXSg?e=T77VcN) it's. You don't need an account, it can be open in a cognito window of your browser. You can also find it in the root of this repository (design_notes.pdf) - I had some issues while downloading it, that's why I put it in OneDrive.

### Building and Running

 - Run **locally** (Java 21 JDK required – not tried with other Java versions):  
`./gradlew bootRun`


 - Run with **Docker** (first execution of bootBuildImage can take over 3 minutes):  
`./gradlew bootBuildImage`  
   - If want to see the logs: `docker run -p 8080:8080 normm`
   - If want to run in detached mode: `docker run -d -p 8080:8080 normm`

 - Open http://localhost:8080/swagger-ui/index.html or use Postman or similar tool.


### Testing

./gradlew bootTestRun
