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
⚠️ **Note:** To use the APIs, you must have a valid AWS account. AWS credentials must be properly configured on your local machine using the standard `.aws` directory. 


## Features
* Check the connectivity status of a specific service or application with the configured AWS Load Balancer.
* Retrieve a filtered list of EC2 instances based on region, status, instance type, and more.

## Load Balancer Service
This service verifies whether a specific instance is correctly registered and connected to an AWS Load Balancer.  
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
If input parameters are missing or invalid, the service will respond with HTTP status codes 400 or 503, including an alarms section in the response body to highlight detected issues.

## Ec2 List Service
This service retrieves all EC2 instances that match the specified filter criteria such as instance type, status, and region.
An example request is shown below:

```http request
POST /aws/ec2/instances
```

```body
{
    "region": "eu-west-1",
    "type": "m7g.xlarge",
    "status": "RUNNING"
}
```
If the instances match the specified filters, the response will resemble the example shown below:

```body
{
    "instances": [
        {
            "id": "i-0r832dw4e58723d56",
            "name": "example-1",
            "region": "eu-west-1",
            "status": "RUNNING",
            "type": "m7g.xlarge",
            "privateIp": "172.32.2.452",
            "publicIp": "73.27.24.36",
            "subnetId": "subnet-4c0a3f3a",
            "platform": "Linux/UNIX",
            "launchTime": "2024-05-09T10:21:34Z"
        },
        {
            "id": "i-029de5wf2d2fr485w",
            "name": "example-2",
            "region": "eu-west-1",
            "status": "RUNNING",
            "type": "m7g.xlarge",
            "privateIp": "172.32.6.268",
            "publicIp": "34.272.413.51",
            "subnetId": "subnet-5de9d839",
            "platform": "Linux/UNIX",
            "launchTime": "2024-04-23T14:04:12Z"
        }
    ]
}
```

If the request contains invalid parameters, the service responds with HTTP status 400, including an alarms section detailing the validation issues.