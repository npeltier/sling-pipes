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
            "fullName":"pipe.user.gender === 'female' ? 'Ms ' + pipe.user.fullName : 'Mr ' + pipe.user.fullName",
            "generatedBy":"slingPipes"
        }
    }
  }
}
```