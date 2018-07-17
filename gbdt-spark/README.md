# Gradient Boosting Regression Tree Parallelized Implementation #

**Please create pull requests for bug fix or new feature.**

## Quick Start ##
* local test: **sbt test**
* test on cluster: please refer to **demo/demo.sh**

## Usage of Model File ##
_Java_

dependency:

```xml
<groupId>com.meituan.mobile.recommend</groupId>
<artifactId>gbdt</artifactId>
<packaging>jar</packaging>
<version>1.0-SNAPSHOT</version>
```

example:

```java
GBDT gbdt = new GBDT();
// or you can use GBDT::loadFromStream
gbdt.loadFromFile("/path-to-model-file");
Tuple t = new Tuple();
t.feature = Arrays.asList(-4.0, 2.0, 3.0);
System.out.println(gbdt.predict(t));
```
