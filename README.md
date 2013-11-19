Requires JVM version 7

Run `./sbt assembly` to build the jar.

```
cp target/scala-2.10/dummy-system-assembly-0.1-SNAPSHOT.jar dummy.jar
```

To start a server on a VM:

```
java -jar dummy.jar server
```

For the client, you'll need to specify some config, so create a copy
of the default config file called `dummy.conf` in the directory from
which you'll be launching the client.

```
cp src/main/resources/reference.conf dummy.conf
```

At minimum, you'll need to modify the URLs under `dummy.client.url`.


