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

### Base pipe
rather dummy pipe, outputs what is in input (so what is configured in path). Handy for doing some test mostly
* `sling:resourceType` is `slingPipes/base`

### Container Pipe
assemble a sequence of pipes, with its binding context pipes write to
* `sling:resourceType` is `slingPipes/container`
* `additionalBinding` is a node you can add to set "global" bindings (property=value) in pipe execution
* `additionalScripts` is a multi value property to declare scripts that can be reused in expressions
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

### MultiPropertyPipe
iterates through values of input multi value property and write them to bindings 
* `sling:resourceType` is `slingPipes/multiProperty`
* `path` should be the path of a mv property

### AuthorizablePipe
retrieve authorizable resource corresponding to the id passed in epression
* `sling:resourceType` is `slingPipes/authorizable`
* `expr` should be an authorizable id

### XPathPipe
retrieve resources resulting of an xpath query
* `sling:resourceType` is `slingPipes/xpath`
* `expr` should be a valid xpath query

### ReferencePipe
execute the pipe referenced in path property
* `sling:resourceType` is `slingPipes/reference`
* `path` path of the referenced pipe

## Making configuration dynamic with bindings
in order to make things interesting, most of the configurations are javascript expressions: when a pipe 
is in a container pipe, a valid js expression reusing other pipes names of the container as bindings can be used.
Following configuration are evaluated:
* `path`
* `expr`
* name/value of each property of a write pipe

you can use name of previous pipes in the pipe container, or the special binding `path`, where `path.previousPipe` 
is the path of the current resource of previous pipe named `previousPipe`

global bindings can be set at pipe execution, external scripts can be added to the execution as well (see container pipe
 configuration)

## How to execute a pipe
for now it's possible to execute Pipes through POST command, you'll need to create a slingPipes/plumber resource,
say `etc/pipes` and then to execute
```
curl -u admin:admin -F "path=/etc/pipes/mySamplePipe" http://localhost:8080/etc/pipes.json
```
which will return you the path of the pipes that have been through the output of the configured pipe.

you can add as `bindings` parameter a json object of global bindings you want to add for the execution of the pipe
 
e.g. 

```
 curl -u admin:admin -F "path=/etc/pipes/test" -F "bindings={testBinding:'foo'}" http://localhost:4502/etc/pipes.json
```

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

### slingQuery | multiProperty | authorizable | write
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
      "sling:resourceType": "slingPipes/multiProperty"
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

### xpath | json | write
this use case is for completing repository profiles with external system's data (that has an json api)
```
{
  "jcr:primaryType": "nt:unstructured",
  "jcr:description": "this pipe retrieves json info from an external system and writes them to the user profile, uses moment.js",
  "sling:resourceType": "slingPipes/container",
  "additionalScripts": "/etc/source/moment.js",
  "conf": {
    "jcr:primaryType": "sling:OrderedFolder",
    "profile": {
      "jcr:primaryType": "sling:OrderedFolder",
      "expr": "'/jcr:root/home/users//element(profile,nt:unstructured)[@uid]'",
      "jcr:description": "query all user profile nodes",
      "sling:resourceType": "slingPipes/xpath"
      },
    "json": {
      "jcr:primaryType": "sling:OrderedFolder",
      "expr": "profile.uid ? 'https://my.external.system.corp.com/profiles/' + profile.uid.substr(0,2) + '/' + profile.uid + '.json' : ''",
      "jcr:description": "retrieves json information relative to the given profile, if the uid is not found, expr is empty: the pipe will do nothing",
      "sling:resourceType": "slingPipes/json"
      },
    "write": {
      "jcr:primaryType": "sling:OrderedFolder",
      "path": "path.profile",
      "jcr:description": "write json information to the profile node",
      "sling:resourceType": "slingPipes/write",
      "conf": {
        "jcr:primaryType": "sling:OrderedFolder",
        "jcr:createdBy": "admin",
        "'background'": "json.opt('background')",
        "'about'": "json.opt('about')",
        "jcr:created": "Fri Jul 03 2015 15:32:22 GMT+0200",
        "'birthday'": "json.opt('birthday') ? moment(json.opt('birthday'), \"MMMM DD\").toDate() : ''",
        "'mobile'": "json.opt('mobile')",
        "'connectRooms'": "json.opt('connectRooms')",
        "'interests'": "json.opt('interests')",
        "'volunteer'": "json.opt('volunteer')",
        "'status'": "json.opt('status')"
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

