# sling-pipes
tool for doing extract - transform - load operations through JCR configuration

sample configurations (json notation, but it's jcr, e.g. container's conf are ordered) :
```
{
  "sling:resourceType":"slingPipes/container",
  "name":"Dummy User prefix Sample",
  "jcr:description":"prefix all full names of profile with "Mr" or "Ms" depending on gender",
  "conf":{
    "profile": {
        "sling:resourceType":"slingPipes/slingQuery",
        "expr":"nt:unstructured#profile",
        "path":"/home/users"
    },
    "writeFullName": {       
        "sling:resourceType":"slingPipes/write",
        "conf": {
            "fullName":"profile.gender === 'female' ? 'Ms ' + profile.fullName : 'Mr ' + profile.fullName",
            "generatedBy":"slingPipes"
        }
    }
  }
}
```

some other samples are in https://github.com/npeltier/sling-pipes/tree/master/src/test/
