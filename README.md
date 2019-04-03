# data-marketplace-facade
Data marketplace facade service

# Data Marketplace Facade
This repository contains the Facade for the data marketplace. Most of the calls from the portal are routed through this facade. The project is written in Scala.
To run this component correctly, you should be familiar with the [Data marketplace](https://github.com/lgsvl/data-marketplace) components because there is a particular dependency between the components.



### Download and work with the code

```bash
git clone git@github.com:lgsvl/data-marketplace-facade.git
cd data-marketplace-facade
```

### Docker Build

You can use the dockerfile to build a docker image:
```
docker build -t facade .
docker run -p 9000:9000 facade
```

### Kubernetes deployment

The [deployment](./deployment) folder contains the deployment and service manifests to deploy this component.
We assume that you have a running Hyperledger Fabric network running with the [Data marketplace Chaincode](https://github.com/lgsvl/data-marketplace-chaincode) deployed on top of it and the [Data marketplace Chaincode Rest](https://github.com/lgsvl/data-marketplace-chaincode-rest) API is also up and running.

