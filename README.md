# sling-pipes
tool for doing extract - transform - load operations through JCR configuration

sample configurations:
```
{
  "sling:resourceType":"/apps/sling-pipes/slingQuery",
  "get":"some SlingQuery Retrieving User Profiles From Current Repository",
  "name":"users",
  "remoteProps":{
    "sling:resourceType":"/apps/sling-pipes/httpQuery",
    "get":"some Http request retrieving information, using ${users} data",
    "name":"remoteProps",
    "remoteLogin":"blah",
    "remotePwd":"pwd",
    "write": {
      "sling:resourceType":"/apps/sling-pipes/write",
      "write":"prop=value chain, value using expression using ${users} and ${remoteProps}"
    }
  }
}
```

or 

```
{
  "sling:resourceType":"/apps/sling-pipes/slingQuery",
  "get":"some SlingQuery Retrieving Nodes from repository",
  "name":"nodes",
  "userReader":{
    "sling:resourceType":"/apps/sling-pipes/slingQuery",
    "get":"reading user names from an mv property",
    "write": {
      "sling:resourceType":"/apps/sling-pipes/write",
      "write":"prop=value chain, value using writing ${nodes.path} in the ${getUser(userReader)}'s profile"
    }
  }
}
```
