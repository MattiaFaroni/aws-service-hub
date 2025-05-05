<h1 align="center">AWS-Service-Hub</h1>

<p align="center">
<img width="100" src="img/api-logo.png" alt=""/>
</p>

<p align="center">
<img src="https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=openjdk&logoColor=white" alt="">
<img src="https://img.shields.io/badge/gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white" alt="">
<img src="https://img.shields.io/badge/Sentry-black?style=for-the-badge&logo=Sentry&logoColor=#362D59" alt="">
<img src="https://img.shields.io/badge/AWS-FF9900?style=for-the-badge&logo=amazonwebservices&logoColor=white" alt="">
</p>

--------

AWS-Service-Hub is a Java application designed to abstract and simplify access to AWS services through a standardized set of REST APIs.
The project is developed using Java 21, built with Gradle, and deployed on Apache Tomcat 10 as the servlet container.  
**Note: A valid AWS account is required to authenticate and access AWS services through this application.** 


## Features
* Verify the connectivity status of a specific service with the configured AWS Load Balancer.

## Load Balancer Service
This service allows you to validate whether a given application or service is correctly registered and connected to the target AWS Load Balancer.
An example request is shown below:

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
If any input parameters are invalid or misconfigured, the service responds with an HTTP 500 status code, including an alarms section in the response body that outlines the detected issues.