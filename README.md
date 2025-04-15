<h1 align="center">AWS-Service-Hub</h1>

<p align="center">
<img width="100" src="img/api-logo.png" alt=""/>
</p>

<p align="center">
<img src="https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=openjdk&logoColor=white" alt="">
<img src="https://img.shields.io/badge/gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white" alt="">
<img src="https://img.shields.io/badge/AWS-FF9900?style=for-the-badge&logo=amazonwebservices&logoColor=white" alt="">
</p>

--------

AWS-Service-Hub is a Java application that exposes a set of REST APIs to simplify interaction with AWS services.
The project is developed using Java 21 with Gradle and Tomcat 10.  
**Note: An AWS account is required to use this project.**

## Features
* Check whether a specific service is connected to the AWS load balancer

## Load Balancer Service
This service enables you to verify if a service is properly connected to the AWS load balancer.  
Below is an example of a request:

```http request
POST /aws/load-balancer/service/status
```

```body
{
    "loadBalancerName": "ExampleLoadBalancer",
    "instanceId": "i-0wae3ec5d1628g626",
    "region": "eu-west-1",
    "port": "1299"
}
```
If the service is correctly connected to the load balancer, the result will resemble the example below:

```body
{
    "status": "ATTACHED",
    "timestamp": "2025-04-15T10:25:20.823045692Z"
}
```
If any input information is incorrect, the response will return a 500 status code along with an 'alarms' section detailing the errors found.