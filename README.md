# sling-pipes
tool for doing extract - transform - load operations through JCR configuration

often one-shot data transformations need sample code to be written & executed. This tiny framework intends to provide ability
to do such transformations with proven & reusable blocks called pipes, configured through a jcr tree.

## What is a pipe

```
        getOutputBinding       
              ^                
              |                
getInput  +---+---+   getOutput
          |       |            
     +----> Pipe  +---->       
          |       |            
          +-------+            
```
A sling pipe is essentially a sling resource stream:
* it provides an output as a sling resource iterator
* it gets its input either from a configured path, either, if its chained (see container pipes below), from another pipe's output
* if the pipe is contained, it can also provide an output binding that can be reused in the way further pipes will computes their
 inputs or outputs.
 
At the moment, there are 3 types of pipe to consider:
* "read" pipes, that will just output a set of resource depending on the input
* "write" pipes (atm there is just WritePipe existing), that write to the repository, depending on configuration and output
* "container" pipes, that contains pipes, and whose job is to chain their execution : input is the input of their first pipe,
 output is the output of the last pipe it contains.
 
A `Plumber` osgi service is provided to help getting & executing pipes.

## Registered Pipes
a pipe configuration is a jcr node, with:
* `sling:resourceType` property, which must be a pipe type registered by the plumber 
* `name` property, that will be used by a container pipe as an id, and will be the key for the output bindings (default value being a value map of the 
current output resource)
* `path` property, if configured, will override upstream's pipe output as an input.
* `expr` property, expression through which the pipe will execute (depending on the type) 
* `conf` optional child node that contains addition configuration of the pipe (depending on the type)

### Container Pipe
* `sling:resourceType` is `slingPipes/container`
* `conf` node contains child pipes' configurations, that will be configured in the order they are found

### SlingQuery Pipe
* `sling:resourceType` is `slingPipes/slingQuery`
* `expr` mandatory property, contains slingQuery expression through which getInput()'s children will be computed to getOutput()

### Write Pipe
* `sling:resourceType` is `slingPipes/slingQuery`
* `conf` properties names and value of which will be written to the input resource, same resource will be returned  

### JsonPipe
* `sling:resourceType` is `slingPipes/json`
* `expr` mandatory property contains url that will be called, the json be sent to the output bindings, getOutput = getInput

## Making configuration dynamic with bindings
in order to make things interesting, most of the configurations can be either constant string or javascript expressions: when a pipe 
is in a container pipe, a valid js expression reusing other pipes names of the container as bindings can be used.
Following configuration are evaluated:
* `path`
* `expr`
* name/value of each property of a write pipe


## sample configurations 

### Sling Query & write
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
