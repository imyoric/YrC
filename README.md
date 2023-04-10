# YrC
# How to add in Maven projects?
* Import repo:
```
    <repositories>
        <repository>
            <id>yoricRepo</id>
            <name>Yoric Repo</name>
            <url>https://github.com/imyoric/YrC/raw/repository</url>
        </repository>
    </repositories>
```
* Add dependency:
* Check actual version from Releases!
```
<dependencies>
  <dependency>
        <groupId>ru.yoricya.mvnd.yrc</groupId>
        <artifactId>YrC</artifactId>
        <version>VERSION</version>
  </dependency>
</dependencies>
```
# For YrC Developers:
* Check: [All Methods and Functions](Funcs.md)
