sbt-jasmine-plugin
=================

An SBT plugin for running jasmine tests in your build.

Getting the plugin
------------------

The most convenient way of using this plugin is to add a source dependency in a scala file under project/project:

```
lazy val plugins = Project("plugins", file("."))
    .dependsOn(uri("git://github.com/guardian/sbt-jasmine-plugin.git#1.0"))
```

you will also need to import the plugin's settings in the usual way:

```
seq(jasmineSettings : _*)
```


Running the plugin
------------------

Override the following settings setting in your build:

 * appJsDir - the root directory where your application javascript lives
 * appJsLibDir - the root directory where you put javascript library files thast your application uses (e.g jquery)
 * jasmineTestDir - the directory that contains your jasmine tests, jasmine will look for /specs and /mocks sub directories
 * jasmineConfFile - the test.dependencies.js configuration file that loads the required application js and lib js files into the test context.


For a project laid out as follows:

```
src/
|-- main
|   `-- webapp
|       `-- static
|           `-- js
|               `-- samples
|                   |-- <app js files here>
|                   `-- lib
|                       `-- <js library files here>
`-- test
    `-- webapp
        `-- static
            `-- js
                |-- mocks
                |   `-- <jasmine mock js files here>
                |-- specs
                |   |-- <jasmine spec js files here>
                `-- test.dependencies.js

```

The project configuration would be:

```
appJsDir <+= sourceDirectory { src => src / "main" / "webapp" / "static" / "js" / "samples"}

appJsLibDir <+= sourceDirectory { src => src / "main" / "webapp" / "static" / "js" / "samples" / "lib" }

jasmineTestDir <+= sourceDirectory { src => src / "test" / "webapp" / "static" / "js" }

jasmineConfFile <+= sourceDirectory { src => src / "test" / "webapp" / "static" / "js" / "test.dependencies.js" }
```

You can now run the jasmine task to run the tests.

See [sbt-jasmine-example](https://github.com/guardian/sbt-jasmine-example) for a full working example project.


Paths exposed to your tests
---------------------------

The following path variables are available to your javascript (in test.dependencies.js and the tests):

* EnvJasmine.testDir = the jasmineTestDir (note no trailing slash on this path)
* EnvJasmine.mocksDir = EnvJasmine.testDir / mocks
* EnvJasmine.specsDir = EnvJasmine.testDir / specs
* EnvJasmine.rootDir = the appJsDir
* EnvJasmine.libDir = the appJsLibRoot

N.B. all path variables have a trailing slash so you don't need to add them yourself when building paths. Thus to load
the query library as in your test.dependencies.js file you would add the following line:

```
EnvJasmine.loadGlobal(EnvJasmine.libDir + "jquery-1.4.4.js");
```


Running as part of test
-----------------------

To automatically run the jasmine plugin as part of your project's test phase you can add the following to you build.sbt:

```
(test in Test) <<= (test in Test) dependsOn (jasmine)
```

Generating an html runner page
------------------------------

If you need to run your jasmine tests in a browser (for example if, heaven forbid, you have failing tests and want to dubug them)
you can run the ```jasmine-gen-runner``` task, this will output a runner html file that you can load in a browser to run your jasmine tests.
A link to the output runner file is output in the sbt console.

Release
-------
To release a new version, tag it and push the tag to github.

```
git tag -a 1.XXX
git push --tags
```



