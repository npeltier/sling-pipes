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
current output resource). Note that the node name will be used in case no name is provided.
* `path` property, if configured, will override upstream's pipe output as an input.
* `expr` property, expression through which the pipe will execute (depending on the type) 
* `conf` optional child node that contains addition configuration of the pipe (depending on the type)

### Container Pipe
assemble a sequence of pipes, with its binding context pipes write to
* `sling:resourceType` is `slingPipes/container`
* `conf` node contains child pipes' configurations, that will be configured in the order they are found (note you should use sling:OrderedFolder)

### SlingQuery Pipe
executes $(getInput()).children(expression)
* `sling:resourceType` is `slingPipes/slingQuery`
* `expr` mandatory property, contains slingQuery expression through which getInput()'s children will be computed to getOutput()

### Write Pipe
writes given properties to current input
* `sling:resourceType` is `slingPipes/slingQuery`
* `conf` properties names and value of which will be written to the input resource, same resource will be returned  

### JsonPipe
feeds bindings with remote json
* `sling:resourceType` is `slingPipes/json`
* `expr` mandatory property contains url that will be called, the json be sent to the output bindings, getOutput = getInput.
An empty url or a failing url will cut the pipe at that given place.

### MvPropertyPipe
iterates through values of input MV property and write them to bindings 
* `sling:resourceType` is `slingPipes/mv`
* `path` should be the path of a mv property

### AuthorizablePipe
retrieve authorizable resource corresponding to the id passed in epression
* `sling:resourceType` is `slingPipes/authorizable`
* `expr` should be an authorizable id

### XPathPipe
retrieve resources resulting of an xpath query
* `sling:resourceType` is `slingPipes/xpath`
* `expr` should be a valid xpath query

## Making configuration dynamic with bindings
in order to make things interesting, most of the configurations are javascript expressions: when a pipe 
is in a container pipe, a valid js expression reusing other pipes names of the container as bindings can be used.
Following configuration are evaluated:
* `path`
* `expr`
* name/value of each property of a write pipe

you can use name of previous pipes in the pipe container, or the special binding `path`, where `path.previousPipe` 
is the path of the current resource of previous pipe named `previousPipe 

## sample configurations 

### slingQuery | write
this pipe parse all profile nodes, and 
```
{
  "sling:resourceType":"slingPipes/container",
  "name":"Dummy User prefix Sample",
  "jcr:description":"prefix all full names of profile with "Mr" or "Ms" depending on gender",
  "conf":{
    "profile": {
        "sling:resourceType":"slingPipes/slingQuery",
        "expr":"'nt:unstructured#profile'",
        "path":"'/home/users'"
    },
    "writeFullName": {       
        "sling:resourceType":"slingPipes/write",
        "conf": {
            "fullName":"profile.gender === 'female' ? 'Ms ' + profile.fullName : 'Mr ' + profile.fullName",
            "'generatedBy'":"'slingPipes'"
        }
    }
  }
}
```

### slingQuery | mv | authorizable | write
```
{
  "jcr:primaryType": "sling:Folder",
  "jcr:description": "move badge<->user relation ship from badge MV property to a user MV property"
  "name": "badges",
  "sling:resourceType": "slingPipes/container",
  "conf": {
    "jcr:primaryType": "sling:OrderedFolder",
    "badge": {
      "jcr:primaryType": "sling:Folder",
      "jcr:description": "outputs all badge component resources",
      "expr": "'[sling:resourceType=myApp/components/badge]'",
      "path": "'/etc/badges/badges-admin/jcr:content'",
      "sling:resourceType": "slingPipes/slingQuery"
      },
    "profile": {
      "jcr:primaryType": "sling:Folder",
      "jcr:description": "retrieve all user ids from a mv property",
      "path": "path.badge + '/profiles'",
      "sling:resourceType": "slingPipes/mv"
      },
    "user": {
      "jcr:primaryType": "sling:OrderedFolder",
      "jcr:description": "outputs user resource",
      "expr": "profile",
      "sling:resourceType": "slingPipes/authorizable"
      },
    "write": {
      "jcr:primaryType": "sling:OrderedFolder",
      "jcr:descritption": "patches the badge path to the badges property of the user profile"
      "path": "path.user + '/profile'",
      "sling:resourceType": "slingPipes/write",
      "conf": {
        "jcr:primaryType": "nt:unstructured",
        "'badges'": "'+[' + path.badge + ']'"
        }
      }
    }
  }
```
some other samples are in https://github.com/npeltier/sling-pipes/tree/master/src/test/

# Compatibility
For running this tool on a sling instance you need:
- java 8 (Nashorn is used for expression)
- slingQuery (3.0.0) (used in SlingQueryPipe)
- oak (1.2.2) (used in AuthorizablePipe)

